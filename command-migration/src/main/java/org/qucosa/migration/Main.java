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

package org.qucosa.migration;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;
import org.qucosa.migration.contexts.MigrationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.exit;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        CommandLineOptions options = new CommandLineOptions(args);
        System.setProperty("sword.noop", String.valueOf(options.isNoop()));
        System.setProperty("sword.slugheader", String.valueOf(options.useSlugHeader()));
        System.setProperty("sword.purge", String.valueOf(options.purgeBeforeDeposit()));
        System.setProperty("sword.collection", String.valueOf(options.getCollection()));
        System.setProperty("transformation.discardExisting", String.valueOf(options.discardExistingDatastreams()));

        if (options.getOwnerId() != null) {
            System.setProperty("sword.ownerID", options.getOwnerId());
        }

        MigrationContext ctx = null;
        try {
            Boolean hasStagingResource = (options.getStageResource() != null);
            Boolean hasStagingResourceFile = (!options.getIdFile().isEmpty());
            Boolean hasTransformResource = (options.getTransformResource() != null);
            Boolean hasTransformResourceFile = (!options.getPidFile().isEmpty());
            Boolean isStageTransform = options.isStageTransform();

            Boolean isTransforming = hasTransformResource
                    || hasTransformResourceFile
                    || isStageTransform;

            Boolean isStaging = hasStagingResource || hasStagingResourceFile;

            System.setProperty("transforming", String.valueOf(isTransforming));

            Configuration conf = new SystemConfiguration();
            ctx = new MigrationContext(conf, isStaging, isTransforming);
            ctx.start();

            ProducerTemplate template = ctx.createProducerTemplate();

            if (hasStagingResource) {
                template.sendBody("direct:staging", options.getStageResource());
            } else if (hasStagingResourceFile) {
                template.sendBody("direct:staging:file", options.getIdFile());
            }

            if (hasTransformResource) {
                template.sendBody("direct:transform", options.getTransformResource());
            } else if (hasTransformResourceFile) {
                template.sendBody("direct:transform:file", options.getPidFile());
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            exit(1);
        } finally {
            if (ctx != null) try {
                ctx.stop();
            } catch (Exception e) {
                System.out.println("Error shutting down Camel: " + e.getMessage());
                e.printStackTrace();
                exit(1);
            }
        }
    }

}
