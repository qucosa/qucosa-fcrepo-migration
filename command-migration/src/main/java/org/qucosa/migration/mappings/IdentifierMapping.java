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

import java.util.ArrayList;
import java.util.List;

import static gov.loc.mods.v3.RelatedItemDefinition.Type.OTHER_VERSION;
import static org.qucosa.migration.mappings.ChangeLog.Type.MODS;
import static org.qucosa.migration.mappings.MappingFunctions.uri;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.*;

public class IdentifierMapping {
    private class IdentifierType {
        private final String opusType;
        private final String simpleType;
        private final String modsType;

        public String getOpusType() { return opusType; }
        public String getSimpleType() { return simpleType; }
        public String getModsType() { return modsType; }

        public IdentifierType(String opusType, String simpleType, String modsType) {
            this.opusType = opusType;
            this.simpleType = simpleType;
            this.modsType = modsType;
        }
    }

    private ArrayList<IdentifierType> setupTypeMapping() {
        ArrayList<IdentifierType> typeList = new ArrayList<>();
        typeList.add(new IdentifierType("IdentifierIsbn", "isbn", "isbn"));
        typeList.add(new IdentifierType("IdentifierIssn", "issn", "issn"));
        typeList.add(new IdentifierType("IdentifierPpn", "ppn", "swb-ppn"));
        typeList.add(new IdentifierType("IdentifierUrn", "urn", "qucosa:urn"));

        return typeList;
    }

    public void mapIdentifiers(Document opus, ModsDefinition mods, ChangeLog changeLog) {
        ArrayList<IdentifierType> typeList = setupTypeMapping();

        for (IdentifierType type : typeList) {
            map(type, opus, mods, changeLog);
        }
        // special mapping for DOI identifier
        mapDoiToRelatedItem(opus, mods, changeLog);
        // special mapping (source) for VG-Wort-Identifier
        mapVgwortOpenKey(opus, mods, changeLog);
    }

    private void map(IdentifierType type, Document opus, ModsDefinition mods, ChangeLog changeLog) {
        if (type.getSimpleType().equals("issn") && "article".equals(opus.getType())) {
            return;
        }

        for (XmlObject xmlObject : selectAll(type.getOpusType(), opus)) {
            Identifier oid = (Identifier) xmlObject;
            String oidValue = oid.getValue();
            String mappedType = type.getModsType();
            if (ensureIdentifierElement(mappedType, oidValue, mods)) {
                changeLog.log(MODS);
            }
        }
    }

    private boolean ensureIdentifierElement(String type, String id, XmlObject modsElement) {
        final String mid = uri(id);
        if (mid != null && !nodeExists(
                String.format("mods:identifier[@type='%s' and text()='%s']", type, mid), modsElement)) {
            IdentifierDefinition identifierDefinition;
            if (modsElement instanceof ModsDefinition) {
                identifierDefinition = ((ModsDefinition) modsElement).addNewIdentifier();
            } else if (modsElement instanceof RelatedItemDefinition) {
                identifierDefinition = ((RelatedItemDefinition) modsElement).addNewIdentifier();
            } else {
                throw new IllegalArgumentException("Mods element is not ModsDefinition or RelatedItemDefinition");
            }

            identifierDefinition.setType(type);
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

    private void mapVgwortOpenKey(Document opus, ModsDefinition mods, ChangeLog changeLog) {
        String vgwortOpenKey = opus.getVgWortOpenKey();

        if (vgwortOpenKey != null && !vgwortOpenKey.isEmpty()) {
            // filter URL prefixes
            final String encodedVgWortOpenKey = vgwortEncoding(vgwortOpenKey);
            if (ensureIdentifierElement("vgwortOpenKey", encodedVgWortOpenKey, mods)) {
                changeLog.log(MODS);
            }
        }
    }

    private String vgwortEncoding(String vgWortOpenKey) {
        if (vgWortOpenKey.startsWith("http")) {
            return vgWortOpenKey.substring(
                    vgWortOpenKey.lastIndexOf('/') + 1,
                    vgWortOpenKey.length());
        } else {
            return vgWortOpenKey;
        }
    }

}
