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
import gov.loc.mods.v3.RelatedItemDefinition;
import noNamespace.Document;
import noNamespace.Identifier;
import org.apache.xmlbeans.XmlObject;

import java.util.HashMap;
import java.util.List;

import static gov.loc.mods.v3.RelatedItemDefinition.Type.OTHER_VERSION;
import static org.qucosa.migration.mappings.ChangeLog.Type.MODS;
import static org.qucosa.migration.mappings.MappingFunctions.uri;
import static org.qucosa.migration.xml.XmlFunctions.formatXPath;
import static org.qucosa.migration.xml.XmlFunctions.nodeExists;
import static org.qucosa.migration.xml.XmlFunctions.select;
import static org.qucosa.migration.xml.XmlFunctions.selectAll;

public class IdentifierMapping {

    private static final HashMap<String, String> typeNameMap = new HashMap<String, String>() {{
        put("Isbn", "isbn");
        put("Issn", "issn");
        put("Ppn", "swb-ppn");
        put("Urn", "qucosa:urn");
    }};

    public void mapIdentifiers(Document opus, ModsDefinition mods, ChangeLog changeLog) {
        for (String identifierName : typeNameMap.keySet()) {
            map(identifierName, opus, mods, changeLog);
        }
        // special mapping for DOI identifier
        mapDoiToRelatedItem(opus, mods, changeLog);
    }

    private void map(String type, Document opus, ModsDefinition mods, ChangeLog changeLog) {
        if (type.equals("Issn") && "article".equals(opus.getType())) {
            // Don't map ISSN identifier for document type article because there is no ISSN for
            // articles, so they are for sure ment to be source ISSNs
            return;
        }
        for (XmlObject xmlObject : selectAll("Identifier" + type, opus)) {
            Identifier oid = (Identifier) xmlObject;
            String oidValue = oid.getValue();
            String mappedType = typeNameMap.get(type);
            if (ensureIdentifierElement(mappedType, oidValue, mods)) {
                changeLog.log(MODS);
            }
        }
    }

    private boolean ensureIdentifierElement(String type, String id, XmlObject modsElement) {
        final String mid = uri(id);
        if (mid != null && !nodeExists(
                formatXPath("mods:identifier[@type='%s' and text()='%s']", type.toLowerCase(), mid), modsElement)) {
            IdentifierDefinition identifierDefinition;
            if (modsElement instanceof ModsDefinition) {
                identifierDefinition = ((ModsDefinition) modsElement).addNewIdentifier();
            } else if (modsElement instanceof RelatedItemDefinition) {
                identifierDefinition = ((RelatedItemDefinition) modsElement).addNewIdentifier();
            } else {
                throw new IllegalArgumentException("Mods element is not ModsDefinition or RelatedItemDefinition");
            }
            identifierDefinition.setType(type.toLowerCase());
            identifierDefinition.setStringValue(mid);
            return true;
        }
        return false;
    }

    private void mapDoiToRelatedItem(Document opus, ModsDefinition mods, ChangeLog changeLog) {
        List<XmlObject> doiIdentifiers = selectAll("IdentifierDoi", opus);
        if (doiIdentifiers.isEmpty()) return;

        RelatedItemDefinition ri = (RelatedItemDefinition)
                select("mods:relatedItem[@type='otherVersion']", mods);
        if (ri == null) {
            ri = mods.addNewRelatedItem();
            ri.setType(OTHER_VERSION);
            changeLog.log(MODS);
        }
        for (XmlObject xmlObject : doiIdentifiers) {
            Identifier oid = (Identifier) xmlObject;
            String oidValue = oid.getValue();
            if (ensureIdentifierElement("doi", oidValue, ri)) {
                changeLog.log(MODS);
            }
        }
    }

}
