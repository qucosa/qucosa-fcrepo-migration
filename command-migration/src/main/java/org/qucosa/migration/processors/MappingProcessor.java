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

import de.slubDresden.InfoDocument;
import de.slubDresden.InfoType;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.Document;
import noNamespace.OpusDocument;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Map;

public abstract class MappingProcessor implements Processor {
    public static final String MODS_CHANGES = "MODS_CHANGES";
    static final String SLUB_INFO_CHANGES = "SLUB-INFO_CHANGES";
    private String label;
    private boolean modsChanges;
    private boolean slubChanges;

    @Override
    public void process(Exchange exchange) throws Exception {
        Map m = (Map) exchange.getIn().getBody();

        modsChanges = (boolean) exchange.getProperty(MODS_CHANGES, false);
        slubChanges = (boolean) exchange.getProperty(SLUB_INFO_CHANGES, false);

        try {
            OpusDocument opusXmlObject = getOpusDocument(m);
            ModsDocument modsXmlObject = getModsDocument(m);
            InfoDocument infoXmlObject = getInfoDocument(m);

            process(opusXmlObject.getOpus().getOpusDocument(),
                    modsXmlObject.getMods(),
                    infoXmlObject.getInfo());
        } catch (RuntimeException rte) {
            throw new Exception("Processor failed with RuntimeException", rte);
        }

        exchange.getIn().setBody(m);
        exchange.setProperty(MODS_CHANGES, modsChanges);
        exchange.setProperty(SLUB_INFO_CHANGES, slubChanges);
    }

    private InfoDocument getInfoDocument(Map m) {
        InfoDocument infoXmlObject = (InfoDocument) m.get("SLUB-INFO");
        if (infoXmlObject != null) {
            if (infoXmlObject.getInfo() == null) {
                throw new IllegalArgumentException("SLUB-INFO XML has no <info> element");
            }
        } else {
            throw new IllegalArgumentException("SLUB-INFO XML is missing");
        }
        return infoXmlObject;
    }

    private ModsDocument getModsDocument(Map m) {
        ModsDocument modsXmlObject = (ModsDocument) m.get("MODS");
        if (modsXmlObject != null) {
            if (modsXmlObject.getMods() == null) {
                throw new IllegalArgumentException("MODS XML has no <mods> element");
            }
        } else {
            throw new IllegalArgumentException("MODS XML is missing");
        }
        return modsXmlObject;
    }

    private OpusDocument getOpusDocument(Map m) {
        OpusDocument opusXmlObject;
        opusXmlObject = (OpusDocument) m.get("QUCOSA-XML");
        if (opusXmlObject != null) {
            if (opusXmlObject.getOpus() != null) {
                if (opusXmlObject.getOpus().getOpusDocument() == null) {
                    throw new IllegalArgumentException("QUCOSA XML has no <Opus>/<Opus_Document> element");
                }
            } else {
                throw new IllegalArgumentException("QUCOSA XML has no <Opus> element");
            }
        } else {
            throw new IllegalArgumentException("QUCOSA XML is missing");
        }
        return opusXmlObject;
    }


    public abstract void process(
            Document inOpusDocument,
            ModsDefinition outModsDocument,
            InfoType outInfoDocument) throws Exception;

    public String getLabel() {
        if (label == null) {
            String classname = this.getClass().getSimpleName();
            if (classname.endsWith("Processor")) {
                label = classname.substring(0, classname.length() - 9).toLowerCase();
            }
        }
        return label;
    }

    public void signalChanges(String dsid) {
        if (dsid.equals(MODS_CHANGES)) {
            this.modsChanges = true;
        } else if (dsid.equals(SLUB_INFO_CHANGES)) {
            this.slubChanges = true;
        }
    }

    Boolean hasChanges() {
        return this.modsChanges;
    }

}
