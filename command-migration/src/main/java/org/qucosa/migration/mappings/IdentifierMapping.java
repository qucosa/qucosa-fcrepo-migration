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
import static org.qucosa.migration.mappings.MappingFunctions.singleline;
import static org.qucosa.migration.mappings.XmlFunctions.nodeExists;
import static org.qucosa.migration.mappings.XmlFunctions.select;
import static org.qucosa.migration.mappings.XmlFunctions.selectAll;

public class IdentifierMapping {

    private static final HashMap<String, String> typeNameMap = new HashMap<String, String>() {{
        put("Doi", "doi");
        put("Isbn", "isbn");
        put("Issn", "issn");
        put("Ppn", "swb-ppn");
        put("Urn", "qucosa:urn");
    }};

    public boolean mapIdentifiers(Document opus, ModsDefinition mods) {
        boolean change = false;
        for (String identifierName : typeNameMap.keySet()) {
            change |= map(identifierName, opus, mods);
        }

        // special mapping for DOI identifier
        change |= mapDoiToRelatedItem(opus, mods);

        return change;
    }

    private boolean map(String type, Document opus, ModsDefinition mods) {
        boolean change = false;
        for (XmlObject xmlObject : selectAll("Identifier" + type, opus)) {
            Identifier oid = (Identifier) xmlObject;
            String oidValue = oid.getValue();
            String mappedType = typeNameMap.get(type);
            if (ensureIdentifierElement(mappedType, oidValue, mods)) {
                change = true;
            }
        }
        return change;
    }

    private boolean ensureIdentifierElement(String type, String id, XmlObject modsElement) {
        final String mid = singleline(id);
        if (mid != null && !nodeExists(
                String.format("mods:identifier[@type='%s' and text()='%s']", type.toLowerCase(), mid), modsElement)) {
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

    private boolean mapDoiToRelatedItem(Document opus, ModsDefinition mods) {
        boolean change = false;

        List<XmlObject> doiIdentifiers = selectAll("IdentifierDoi", opus);
        if (doiIdentifiers.isEmpty()) return false;

        RelatedItemDefinition ri = (RelatedItemDefinition)
                select("mods:relatedItem[@type='otherVersion']", mods);
        if (ri == null) {
            ri = mods.addNewRelatedItem();
            ri.setType(OTHER_VERSION);
            change = true;
        }
        for (XmlObject xmlObject : doiIdentifiers) {
            Identifier oid = (Identifier) xmlObject;
            String oidValue = oid.getValue();
            if (ensureIdentifierElement("doi", oidValue, ri)) {
                change = true;
            }
        }
        return change;
    }

}
