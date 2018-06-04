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

import de.slubDresden.AgreementType;
import de.slubDresden.CorporationType;
import de.slubDresden.InfoDocument;
import de.slubDresden.InfoType;
import de.slubDresden.RightsType;
import static gov.loc.mods.v3.CodeOrText.CODE;
import gov.loc.mods.v3.DateDefinition;
import gov.loc.mods.v3.ExtensionDefinition;
import gov.loc.mods.v3.IdentifierDefinition;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.NameDefinition;
import gov.loc.mods.v3.NamePartDefinition;
import gov.loc.mods.v3.OriginInfoDefinition;
import gov.loc.mods.v3.RoleDefinition;
import gov.loc.mods.v3.RoleTermDefinition;
import noNamespace.Date;
import noNamespace.Document;
import org.apache.xmlbeans.XmlString;

import static gov.loc.mods.v3.DateDefinition.Encoding.ISO_8601;
import static gov.loc.mods.v3.NameDefinition.Type.CORPORATE;
import static org.qucosa.migration.mappings.MappingFunctions.LOC_GOV_VOCABULARY_RELATORS;
import static org.qucosa.migration.mappings.ChangeLog.Type.MODS;
import static org.qucosa.migration.mappings.ChangeLog.Type.SLUB_INFO;
import static org.qucosa.migration.mappings.MappingFunctions.buildTokenFrom;
import static org.qucosa.migration.mappings.MappingFunctions.dateEncoding;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.formatXPath;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.insertNode;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.select;

public class AdministrativeInformationMapping {

    static final String SLUB_GND_IDENTIFIER = "4519974-7";

    public void mapCompletedDate(Date completedDate, ModsDefinition mods, ChangeLog changeLog) {
        final String mappedDateEncoding = dateEncoding(completedDate);

        OriginInfoDefinition oid = getOriginInfoDistribution(mods, changeLog);

        DateDefinition dateIssued = (DateDefinition)
                select(String.format("mods:dateIssued[@encoding='%s' and @keyDate='%s']",
                        "iso8601", "yes"), oid);
        if (dateIssued == null) {
            dateIssued = oid.addNewDateIssued();
            dateIssued.setEncoding(ISO_8601);
            dateIssued.setKeyDate(XmlString.Factory.newValue("yes"));
            changeLog.log(MODS);
        }

        if (!dateIssued.getStringValue().equals(mappedDateEncoding)) {
            dateIssued.setStringValue(mappedDateEncoding);
            changeLog.log(MODS);
        }
    }

    public void mapDefaultPublisherInfo(Document opus, ModsDefinition mods, ChangeLog changeLog) {
        String publisherName = opus.getPublisherName();
        if (publisherName != null && !publisherName.isEmpty()) {
            NameDefinition nd = (NameDefinition)
                    select("mods:name[@type='corporate' and @displayLabel='mapping-hack-university']", mods);

            // ensure mods:name[@type='corporate']
            if (nd == null) {
                nd = mods.addNewName();
                nd.setType2(CORPORATE);
                changeLog.log(MODS);
            }

            // ensure mods:name/@ID if not set
            if (nd.getID() == null || nd.getID().isEmpty()) {
                String ID = buildTokenFrom("CORP_", publisherName);
                nd.setID(ID);
                changeLog.log(MODS);
            }

            // add mods:name/mods:nameIdentifier[@type='gnd'] if publisher is SLUB
            boolean publisherIsSLUB =
                    publisherName.equals("Saechsische Landesbibliothek- Staats- und Universitaetsbibliothek Dresden")
                            || publisherName.equals("Sächsische Landesbibliothek- Staats- und Universitätsbibliothek Dresden");
            if (publisherIsSLUB) {
                IdentifierDefinition nid = (IdentifierDefinition)
                        select("mods:nameIdentifier[@type='gnd']", nd);
                if (nid == null) {
                    nid = nd.addNewNameIdentifier();
                    nid.setType("gnd");
                    nid.setStringValue(SLUB_GND_IDENTIFIER);
                    changeLog.log(MODS);
                }
            }

            // add mods:name/mods:namePart
            NamePartDefinition npd = (NamePartDefinition) select(
                    formatXPath("mods:namePart[text()='%s']", publisherName), nd);
            if (npd == null) {
                npd = nd.addNewNamePart();
                npd.setStringValue(publisherName);
                changeLog.log(MODS);
            }

            // add mods:name/mods:role
            RoleDefinition rd = (RoleDefinition) select("mods:role", nd);
            if (rd == null) {
                rd = nd.addNewRole();
                changeLog.log(MODS);
            }

            // add mods:name/mods:role/mods:roleTerm
            String role = "prv";
            RoleTermDefinition rtd = (RoleTermDefinition) select(formatXPath("mods:roleTerm[text()='%s']", role), rd);
            if (rtd == null) {
                rtd = rd.addNewRoleTerm();
                rtd.setType(CODE);
                rtd.setAuthority("marcrelator");
                rtd.setAuthorityURI(LOC_GOV_VOCABULARY_RELATORS);
                rtd.setStringValue(role);
                changeLog.log(MODS);
            }

            // ensure mods:extension
            ExtensionDefinition extension = (ExtensionDefinition) select("mods:extension", mods);
            if (extension == null) {
                extension = mods.addNewExtension();
                changeLog.log(MODS);
            }

            // ensure mods:extension/slub:info
            boolean embedNewInfoExtensionAfterwards = false;
            // This variable is needed for adding a created slub:info element later
            InfoDocument infoDocument = InfoDocument.Factory.newInstance();
            InfoType info = (InfoType) select("slub:info", extension);
            if (info == null) {
                info = infoDocument.addNewInfo();
                embedNewInfoExtensionAfterwards = true;
                changeLog.log(MODS);
            }

            // ensure mods:extension/slub:info/slub:corporation
            CorporationType corporation = (CorporationType)
                    select(formatXPath("slub:corporation[@ref='%s']", nd.getID()), info);
            if (corporation == null) {
                corporation = info.addNewCorporation();
                corporation.setRef(nd.getID());
                changeLog.log(MODS);
            }

            // set mods:extension/slub:info/slub:corporation/@place
            String publisherPlace = opus.getPublisherPlace();
            if (publisherPlace != null) {
                publisherPlace = publisherPlace.trim();
                if (!publisherPlace.isEmpty()) {
                    if (!publisherPlace.equals(corporation.getPlace())) {
                        corporation.setPlace(publisherPlace);
                        changeLog.log(MODS);
                    }
                }
            }

            // set mods:extension/slub:info/slub:corporation/@address
            String publisherAddress = opus.getPublisherAddress();
            if (publisherAddress != null) {
                publisherAddress = publisherAddress.trim();
                if (!publisherAddress.isEmpty()) {
                    if (!publisherAddress.equals(corporation.getAddress())) {
                        corporation.setAddress(publisherAddress);
                        changeLog.log(MODS);
                    }
                }
            }

            if (embedNewInfoExtensionAfterwards) {
                insertNode(infoDocument, extension);
                changeLog.log(MODS);
            }
        }
    }

    public void mapVgWortopenKey(Document opus, InfoType info, ChangeLog changeLog) {
        String vgwortOpenKey = opus.getVgWortOpenKey();

        if (vgwortOpenKey != null && !vgwortOpenKey.isEmpty()) {
            final String encodedVgWortOpenKey = vgwortEncoding(vgwortOpenKey);

            if (info.getVgwortOpenKey() == null
                    || !info.getVgwortOpenKey().equals(encodedVgWortOpenKey)) {
                info.setVgwortOpenKey(encodedVgWortOpenKey);
                changeLog.log(SLUB_INFO);
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

    private OriginInfoDefinition getOriginInfoDistribution(ModsDefinition mods, ChangeLog changeLog) {
        OriginInfoDefinition oid = (OriginInfoDefinition)
                select("mods:originInfo[@eventType='distribution']", mods);
        if (oid == null) {
            oid = mods.addNewOriginInfo();
            oid.setEventType("distribution");
            changeLog.log(MODS);
        }
        return oid;
    }


    public void ensureRightsAgreement(InfoType info, ChangeLog changeLog) {
        RightsType rt = (RightsType) select("slub:rights", info);
        if (rt == null) {
            rt = info.addNewRights();
            changeLog.log(SLUB_INFO);
        }

        AgreementType at = (AgreementType)
                select("slub:agreement", rt);
        if (at == null) {
            at = rt.addNewAgreement();
            changeLog.log(SLUB_INFO);
        }

        if (!at.isSetGiven() || !at.getGiven().equals("yes")) {
            at.setGiven("yes");
            changeLog.log(SLUB_INFO);
        }
    }


}
