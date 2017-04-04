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
import gov.loc.mods.v3.IdentifierDefinition;
import gov.loc.mods.v3.ModsDefinition;
import noNamespace.Document;
import noNamespace.Identifier;
import org.apache.xmlbeans.XmlObject;

import javax.xml.xpath.XPathExpressionException;

import static org.qucosa.migration.mappings.MappingFunctions.singleline;
import static org.qucosa.migration.mappings.XmlFunctions.nodeExists;
import static org.qucosa.migration.mappings.XmlFunctions.selectAll;

public class IdentifierProcessor extends MappingProcessor {

    @Override
    public void process(Document opus, ModsDefinition mods, InfoType info) throws Exception {
        extractOpusId(opus, mods);

        String[] ns = {"Isbn", "Urn", "Doi", "Issn", "Ppn"};
        for (String n : ns) map(n, opus, mods);
    }

    private void extractOpusId(Document opus, ModsDefinition mods) {
        String opusId = opus.getDocumentId();
        ensureIdentifierElement("opus", opusId, mods);
    }

    private void map(String type, Document opusDocument, ModsDefinition mods) throws XPathExpressionException {
        for (XmlObject xmlObject : selectAll("Identifier" + type, opusDocument)) {
            Identifier oid = (Identifier) xmlObject;
            final String oidValue = oid.getValue();
            final String mappedType = determineTypeName(type, oidValue);
            ensureIdentifierElement(mappedType, oidValue, mods);
        }
    }

    private String determineTypeName(String type, String oidValue) {
        if (oidValue.contains("qucosa")) {
            return "qucosa:" + type;
        } else if ("ppn".equalsIgnoreCase(type)) {
            return "swb-ppn";
        } else {
            return type;
        }
    }

    private void ensureIdentifierElement(String type, String id, ModsDefinition mods) {
        final String mid = singleline(id);
        if (mid != null && !nodeExists(
                String.format("mods:identifier[@type='%s' and text()='%s']", type.toLowerCase(), mid),
                mods)) {
            IdentifierDefinition identifierDefinition = mods.addNewIdentifier();
            identifierDefinition.setType(type.toLowerCase());
            identifierDefinition.setStringValue(mid);
            signalChanges(MODS_CHANGES);
        }
    }
}
