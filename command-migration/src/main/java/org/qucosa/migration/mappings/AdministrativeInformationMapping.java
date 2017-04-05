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

import de.slubDresden.CorporationType;
import de.slubDresden.InfoType;
import gov.loc.mods.v3.DateDefinition;
import gov.loc.mods.v3.ExtensionDefinition;
import gov.loc.mods.v3.IdentifierDefinition;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.NameDefinition;
import gov.loc.mods.v3.OriginInfoDefinition;
import noNamespace.Date;
import noNamespace.Document;
import org.apache.xmlbeans.XmlString;

import static gov.loc.mods.v3.DateDefinition.Encoding.ISO_8601;
import static gov.loc.mods.v3.NameDefinition.Type.CORPORATE;
import static org.qucosa.migration.mappings.MappingFunctions.buildTokenFrom;
import static org.qucosa.migration.mappings.MappingFunctions.dateEncoding;
import static org.qucosa.migration.mappings.XmlFunctions.select;

public class AdministrativeInformationMapping {

    static final String SLUB_GND_IDENTIFIER = "4519974-7";

    public boolean mapCompletedDate(Date completedDate, ModsDefinition mods) {
        ChangeSignal change = new ChangeSignal();

        final String mappedDateEncoding = dateEncoding(completedDate);

        OriginInfoDefinition oid = getOriginInfoDistribution(mods, change);

        DateDefinition dateIssued = (DateDefinition)
                select(String.format("mods:dateIssued[@encoding='%s' and @keyDate='%s']",
                        "iso8601", "yes"), oid);
        if (dateIssued == null) {
            dateIssued = oid.addNewDateIssued();
            dateIssued.setEncoding(ISO_8601);
            dateIssued.setKeyDate(XmlString.Factory.newValue("yes"));
            change.signal();
        }

        if (!dateIssued.getStringValue().equals(mappedDateEncoding)) {
            dateIssued.setStringValue(mappedDateEncoding);
            change.signal();
        }

        return change.signaled();
    }

    public boolean mapDefaultPublisherInfo(Document opus, ModsDefinition mods) {
        ChangeSignal change = new ChangeSignal();

        String publisherName = opus.getPublisherName();
        if (publisherName != null && !publisherName.isEmpty()) {
            NameDefinition nd = (NameDefinition)
                    select("mods:name[@type='corporate' and @displayLabel='mapping-hack-default-publisher']", mods);

            // ensure mods:name[@type='corporate']
            if (nd == null) {
                nd = mods.addNewName();
                nd.setType("corporate");
                nd.setType2(CORPORATE);
                nd.setDisplayLabel("mapping-hack-default-publisher");
                change.signal();
            }

            // ensure mods:name/@ID if not set
            if (nd.getID() == null || nd.getID().isEmpty()) {
                String ID = buildTokenFrom("CORP_", publisherName);
                nd.setID(ID);
                change.signal();
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
                    change.signal();
                }
            }

            // ensure mods:extension
            ExtensionDefinition extension = (ExtensionDefinition) select("mods:extension", mods);
            if (extension == null) {
                extension = mods.addNewExtension();
                change.signal();
            }

            // ensure mods:extension/slub:info
            boolean embedExtensionAfterwards = false;
            InfoType info = (InfoType) select("slub:info", extension);
            if (info == null) {
                info = InfoType.Factory.newInstance();
                embedExtensionAfterwards = true;
                change.signal();
            }

            // ensure mods:extension/slub:info/slub:corporation
            CorporationType corporation = (CorporationType)
                    select("slub:corporation[@slub:ref='" + nd.getID() + "']", info);
            if (corporation == null) {
                corporation = info.addNewCorporation();
                corporation.setRef(nd.getID());
                change.signal();
            }

            // set mods:extension/slub:info/slub:corporation/@place
            String publisherPlace = opus.getPublisherPlace();
            if (publisherPlace != null) {
                publisherPlace = publisherPlace.trim();
                if (!publisherPlace.isEmpty()) {
                    if (!publisherPlace.equals(corporation.getPlace())) {
                        corporation.setPlace(publisherPlace);
                        change.signal();
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
                        change.signal();
                    }
                }
            }

            // ensure mods:extension/slub:info/slub:corporation/slub:university
            XmlString university = (XmlString) select("slub:university", corporation);
            if (university == null) {
                university = corporation.addNewUniversity();
                university.setStringValue(publisherName);
                change.signal();
            }

            if (embedExtensionAfterwards) extension.set(info);

        }

        return change.signaled();
    }

    public boolean mapVgWortopenKey(Document opus, InfoType info) {
        boolean change = false;
        String vgwortOpenKey = opus.getVgWortOpenKey();

        if (vgwortOpenKey != null && !vgwortOpenKey.isEmpty()) {
            final String encodedVgWortOpenKey = vgwortEncoding(vgwortOpenKey);

            if (info.getVgwortOpenKey() == null
                    || !info.getVgwortOpenKey().equals(encodedVgWortOpenKey)) {
                info.setVgwortOpenKey(encodedVgWortOpenKey);
                change = true;
            }
        }

        return change;
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

    private OriginInfoDefinition getOriginInfoDistribution(ModsDefinition mods, ChangeSignal change) {
        OriginInfoDefinition oid = (OriginInfoDefinition)
                select("mods:originInfo[@eventType='distribution']", mods);
        if (oid == null) {
            oid = mods.addNewOriginInfo();
            oid.setEventType("distribution");
            change.signal();
        }
        return oid;
    }

}
