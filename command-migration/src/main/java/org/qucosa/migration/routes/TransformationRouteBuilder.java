/*
 * Copyright (C) 2015 Saxon State and University Library Dresden (SLUB)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.qucosa.migration.routes;

import de.slubDresden.InfoDocument;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.OpusDocument;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.component.http.BasicAuthenticationHttpClientConfigurer;
import org.apache.camel.component.http.HttpEndpoint;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.qucosa.camel.component.sword.SwordDeposit;
import org.qucosa.migration.processors.DepositMetsGenerator;
import org.qucosa.migration.processors.FileReaderProcessor;
import org.qucosa.migration.processors.MappingProcessor;

import java.util.concurrent.TimeUnit;

import static org.qucosa.migration.processors.aggregate.HashMapAggregationStrategy.aggregateHashBy;

public class TransformationRouteBuilder extends RouteBuilder {

    private final Configuration configuration;

    public TransformationRouteBuilder(Configuration conf) {
        this.configuration = conf;
    }

    @Override
    public void configure() throws Exception {
        final String fedoraUri = getConfigValueOrThrowException("fedora.url");

        configureHttpBasicAuth(
                fedoraUri,
                getConfigValueOrThrowException("fedora.user"),
                getConfigValueOrThrowException("fedora.password"));

        configureHttpBasicAuth(
                getConfigValueOrThrowException("sword.url"),
                getConfigValueOrThrowException("sword.user"),
                getConfigValueOrThrowException("sword.password"));

        ValueBuilder discardExistingDatastreams =
                constant(configuration.getBoolean("transformation.discardExisting"));

        from("direct:transform:file")
                .routeId("transform-file")
                .log("Transforming resources listed in ${body}")
                .process(new FileReaderProcessor("#"))
                .log("Found ${body.size} elements")
                .split(body()).parallelProcessing()
                .to("direct:transform");

        from("direct:transform")
                .routeId("transform")
                .multicast(aggregateHashBy(header("DSID")))
                .parallelProcessing()
                .stopOnException()
                .to("direct:ds:qucosaxml", "direct:ds:mods", "direct:ds:slubxml")
                .end()
                .threads()
                .process(new MappingProcessor())
                .to("direct:ds:update");

        final String datastreamPath = "/objects/${header[PID]}/datastreams/${header[DSID]}";

        from("direct:ds:qucosaxml")
                .routeId("get-qucosaxml")
                .setHeader("PID", body())
                .setHeader("DSID", constant("QUCOSA-XML"))
                .doTry()
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader(Exchange.HTTP_PATH, simple(datastreamPath + "/content"))
                .setBody(constant(""))
                .to(fedoraUri)
                .convertBodyTo(String.class)
                .bean(OpusDocument.Factory.class, "parse(${body})")
                .doCatch(HttpOperationFailedException.class)
                .onWhen(simple("${exception.statusCode} == 404"))
                .log("${header.PID} has no ${header.DSID} datastream for migration")
                .stop();

        final ModsDocument modsDocumentTemplate = ModsDocument.Factory.newInstance();
        modsDocumentTemplate.addNewMods();

        final InfoDocument infoDocumentTemplate = InfoDocument.Factory.newInstance();
        infoDocumentTemplate.addNewInfo();

        from("direct:ds:template")
                .choice()
                .when(simple("${header.DSID} == 'MODS'")).setBody(constant(modsDocumentTemplate))
                .when(simple("${header.DSID} == 'SLUB-INFO'")).setBody(constant(infoDocumentTemplate));

        from("direct:ds:mods")
                .routeId("get-mods")
                .setHeader("PID", body())
                .setHeader("DSID", constant("MODS"))
                .choice()
                    .when(discardExistingDatastreams).to("direct:ds:template")
                    .otherwise().to("direct:tryget:datastream")
                .end()
                .convertBodyTo(String.class)
                .bean(ModsDocument.Factory.class, "parse(${body})");

        from("direct:ds:slubxml")
                .routeId("get-slubxml")
                .setHeader("PID", body())
                .setHeader("DSID", constant("SLUB-INFO"))
                .choice()
                    .when(discardExistingDatastreams).to("direct:ds:template")
                    .otherwise().to("direct:tryget:datastream")
                .end()
                .convertBodyTo(String.class)
                .bean(InfoDocument.Factory.class, "parse(${body})");

        from("direct:tryget:datastream")
                .doTry()
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader(Exchange.HTTP_PATH, simple(datastreamPath + "/content"))
                .setBody(constant(""))
                .to(fedoraUri)
                .doCatch(HttpOperationFailedException.class)
                .onWhen(simple("${exception.statusCode} == 404"))
                .to("direct:ds:template");

        from("direct:ds:update")
                .routeId("update")
                .bean(DepositMetsGenerator.class)
                .choice()
                    .when(body().isNotNull())
                        .setHeader("Qucosa-File-Url", constant(configuration.getString("qucosa.file.url")))
                        .setHeader("Collection", constant(configuration.getString("sword.collection")))
                        .setHeader("Content-Type", constant("application/vnd.qucosa.mets+xml"))
                        .convertBodyTo(SwordDeposit.class)
                        .to("direct:sword:update")
                    .otherwise()
                        .stop()
                .end();

        from("direct:sword:update")
                .routeId("sword-update")
                .log("Updating ${header[PID]}")
                .errorHandler(deadLetterChannel("direct:dead")
                        .maximumRedeliveries(5)
                        .redeliveryDelay(TimeUnit.SECONDS.toMillis(3))
                        .maximumRedeliveryDelay(TimeUnit.SECONDS.toMillis(60))
                        .backOffMultiplier(2)
                        .asyncDelayedRedelivery()
                        .retryAttemptedLogLevel(LoggingLevel.WARN))
                .setHeader("X-No-Op", constant(configuration.getBoolean("sword.noop")))
                .setHeader("X-On-Behalf-Of", constant(configuration.getString("sword.ownerID", null)))
                .threads().throttle(5).asyncDelayed()
                .to("sword:update");
    }

    private void configureHttpBasicAuth(String uri, String user, String password) throws ConfigurationException {
        HttpEndpoint httpEndpoint = (HttpEndpoint) getContext().getEndpoint(uri);
        httpEndpoint.setHttpClientConfigurer(
                new BasicAuthenticationHttpClientConfigurer(false, user, password));
    }

    private String getConfigValueOrThrowException(String key) throws ConfigurationException {
        String val = configuration.getString(key, null);
        if (val == null) {
            throw new ConfigurationException("No config value for " + key);
        }
        return val;
    }
}
