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

package org.qucosa.migration.mappings;

import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.NoteDefinition;
import gov.loc.mods.v3.RelatedItemDefinition;
import noNamespace.Document;

import static gov.loc.mods.v3.RelatedItemDefinition.Type.ORIGINAL;
import static org.qucosa.migration.mappings.ChangeLog.Type.MODS;
import static org.qucosa.migration.mappings.MappingFunctions.singleline;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.select;

public class SourceMapping {

    public void mapSource(Document opus, ModsDefinition mods, ChangeLog changeLog) {
        String reference = singleline(opus.getSource());
        if (reference != null && !reference.isEmpty()) {
            RelatedItemDefinition ri = (RelatedItemDefinition)
                    select("mods:relatedItem[@type='original']", mods);
            if (ri == null) {
                ri = mods.addNewRelatedItem();
                ri.setType(ORIGINAL);
                changeLog.log(MODS);
            }

            NoteDefinition note = (NoteDefinition)
                    select(String.format("mods:note[@type='z' and text()='%s']", reference), ri);
            if (note == null) {
                note = ri.addNewNote();
                note.setType("z");
                note.setStringValue(reference);
                changeLog.log(MODS);
            }
        }
    }

}
