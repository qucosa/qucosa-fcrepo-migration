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

package org.qucosa.migration.contexts;

import org.qucosa.migration.routes.QucosaStagingRoute;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.commons.configuration.Configuration;
import org.qucosa.camel.component.opus4.Opus4DataSource;
import org.qucosa.camel.component.sword.SwordConnection;

public class StagingContext extends DefaultCamelContext {

    public StagingContext(Configuration configuration) throws Exception {
        super();
        setName("staging");
        setup(configuration);
    }

    protected void setup(Configuration conf) throws Exception {
        Opus4DataSource opus4DataSource = new Opus4DataSource();
        opus4DataSource.configure(conf);

        SwordConnection swordConnection = new SwordConnection();
        swordConnection.configure(conf);

        SimpleRegistry simpleRegistry = new SimpleRegistry();
        simpleRegistry.put(Opus4DataSource.DATA_SOURCE_NAME, opus4DataSource);
        simpleRegistry.put(SwordConnection.DATA_SOURCE_NAME, swordConnection);
        setRegistry(simpleRegistry);

        setStreamCaching(true);
        setAllowUseOriginalMessage(false);

        addRoutes(new QucosaStagingRoute(conf));
    }

}
