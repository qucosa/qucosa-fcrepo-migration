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

import gov.loc.mods.v3.IdentifierDefinition;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.NoteDefinition;
import gov.loc.mods.v3.RelatedItemDefinition;
import noNamespace.Document;
import noNamespace.Identifier;
import org.apache.xmlbeans.XmlObject;

import static gov.loc.mods.v3.RelatedItemDefinition.Type.ORIGINAL;
import static org.qucosa.migration.mappings.ChangeLog.Type.MODS;
import static org.qucosa.migration.mappings.MappingFunctions.singleline;
import static org.qucosa.migration.mappings.MappingFunctions.uri;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.formatXPath;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.nodeExists;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.select;

public class SourceMapping {

    public void mapSource(Document opus, ModsDefinition mods, ChangeLog changeLog) {
        String reference = singleline(opus.getSource());

        if (reference != null && !reference.isEmpty()) {

            RelatedItemDefinition ri = ensureRelatedItemDefinition(mods, changeLog);

            NoteDefinition note = (NoteDefinition)
                    select(formatXPath("mods:note[@type='z' and text()='%s']", reference), ri);
            if (note == null) {
                note = ri.addNewNote();
                note.setType("z");
                note.setStringValue(reference);
                changeLog.log(MODS);
            }
        }
    }

    public void mapISSN(Document opus, ModsDefinition mods, ChangeLog changeLog) {
        if (!"article".equals(opus.getType())) {
            return;
        }

        Identifier[] issnIdentifiers = opus.getIdentifierIssnArray();
        if (issnIdentifiers == null || issnIdentifiers.length == 0) {
            return;
        }

        RelatedItemDefinition ri = ensureRelatedItemDefinition(mods, changeLog);

        for (Identifier identifier : issnIdentifiers) {
            if (ensureIssnIdentifierElement(identifier.getValue(), ri)) {
                changeLog.log(MODS);
            }
        }
    }

    private RelatedItemDefinition ensureRelatedItemDefinition(ModsDefinition mods, ChangeLog changeLog) {
        RelatedItemDefinition ri = (RelatedItemDefinition)
                select("mods:relatedItem[@type='original']", mods);
        if (ri == null) {
            ri = mods.addNewRelatedItem();
            ri.setType(ORIGINAL);
            changeLog.log(MODS);
        }
        return ri;
    }

    private boolean ensureIssnIdentifierElement(String id, XmlObject modsElement) {
        final String mid = uri(id);
        if (mid != null && !nodeExists(
                formatXPath("mods:identifier[@type='%s' and text()='%s']", "issn".toLowerCase(), mid), modsElement)) {
            IdentifierDefinition identifierDefinition;
            if (modsElement instanceof ModsDefinition) {
                identifierDefinition = ((ModsDefinition) modsElement).addNewIdentifier();
            } else if (modsElement instanceof RelatedItemDefinition) {
                identifierDefinition = ((RelatedItemDefinition) modsElement).addNewIdentifier();
            } else {
                throw new IllegalArgumentException("Mods element is not ModsDefinition or RelatedItemDefinition");
            }
            identifierDefinition.setType("issn".toLowerCase());
            identifierDefinition.setStringValue(mid);
            return true;
        }
        return false;
    }
}
