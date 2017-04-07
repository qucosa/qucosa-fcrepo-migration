/*
 * Copyright (C) 2017 Saxon State and University Library Dresden (SLUB)
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

package org.qucosa.migration.processors;

import de.slubDresden.InfoType;
import gov.loc.mods.v3.ModsDefinition;
import noNamespace.Document;
import org.qucosa.migration.mappings.SourceMapping;

public class SourcesInfoProcessor extends MappingProcessor {

    private final SourceMapping sourceMapping = new SourceMapping();

    @Override
    public void process(Document opus, ModsDefinition mods, InfoType info) throws Exception {
        if (sourceMapping.mapSource(opus, mods)) signalChanges(MODS_CHANGES);
    }

}
