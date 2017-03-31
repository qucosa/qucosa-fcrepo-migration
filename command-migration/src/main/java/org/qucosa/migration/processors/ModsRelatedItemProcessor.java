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

import gov.loc.mods.v3.DetailDefinition;
import gov.loc.mods.v3.IdentifierDefinition;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.PartDefinition;
import gov.loc.mods.v3.RelatedItemDefinition;
import gov.loc.mods.v3.RelatedItemDefinition.Type;
import gov.loc.mods.v3.StringPlusLanguage;

import static org.qucosa.migration.mappings.MappingFunctions.select;
import static org.qucosa.migration.mappings.MappingFunctions.singleline;

abstract class ModsRelatedItemProcessor extends MappingProcessor {

    void setSortOrderIfDefined(String partNumber, RelatedItemDefinition rid) {
        if (partNumber != null) {
            PartDefinition pd = (PartDefinition) select("mods:part", rid);
            if (pd == null) {
                pd = rid.addNewPart();
                signalChanges(SourcesInfoProcessor.MODS_CHANGES);
            }

            DetailDefinition dd = (DetailDefinition) select("mods:detail", pd);
            if (dd == null) {
                dd = pd.addNewDetail();
                signalChanges(SourcesInfoProcessor.MODS_CHANGES);
            }

            StringPlusLanguage spl = (StringPlusLanguage) select("mods:number", dd);
            if (spl == null) {
                spl = dd.addNewNumber();
                spl.setStringValue(partNumber);
                signalChanges(SourcesInfoProcessor.MODS_CHANGES);
            }
        }
    }

    void setIdentifierIfNotFound(String uri, RelatedItemDefinition rid, final String type) {
        IdentifierDefinition id = (IdentifierDefinition)
                select("mods:identifier[@type='" + type + "' and text()='" + uri + "']", rid);
        if (id == null) {
            id = rid.addNewIdentifier();
            id.setType(type);
            id.setStringValue(uri);
            signalChanges(SourcesInfoProcessor.MODS_CHANGES);
        }
    }

    void setLabelIfdefined(String label, RelatedItemDefinition rid) {
        if (label != null) {
            if (!label.equals(rid.getDisplayLabel())) {
                rid.setDisplayLabel(label);
                signalChanges(SourcesInfoProcessor.MODS_CHANGES);
            }
        }
    }

    RelatedItemDefinition getRelatedItemDefinition(ModsDefinition mods, String label, Type.Enum type) {
        final String mappedLabel = singleline(label);
        final String query = (mappedLabel == null || mappedLabel.isEmpty()) ?
                "mods:relatedItem[@type='" + type + "']" :
                "mods:relatedItem[@type='" + type + "' and @displayLabel='" + mappedLabel + "']";

        RelatedItemDefinition rid = (RelatedItemDefinition)
                select(query, mods);
        if (rid == null) {
            rid = mods.addNewRelatedItem();
            rid.setType(type);
            signalChanges(SourcesInfoProcessor.MODS_CHANGES);
        }
        return rid;
    }

}
