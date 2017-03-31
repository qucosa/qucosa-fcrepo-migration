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
import gov.loc.mods.v3.ModsDocument;
import noNamespace.Date;
import noNamespace.File;
import noNamespace.OpusDocument;
import org.apache.xmlbeans.XmlException;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;

import static org.qucosa.migration.mappings.MappingFunctions.NS_FOAF;
import static org.qucosa.migration.mappings.MappingFunctions.NS_MODS_V3;
import static org.qucosa.migration.mappings.MappingFunctions.NS_RDF;
import static org.qucosa.migration.mappings.MappingFunctions.NS_SLUB;
import static org.qucosa.migration.mappings.MappingFunctions.NS_XLINK;

public class ProcessorTestBase {

    static {
        NamespaceContext ctx = new SimpleNamespaceContext(
                new HashMap() {{
                    put("mods", NS_MODS_V3);
                    put("slub", NS_SLUB);
                    put("foaf", NS_FOAF);
                    put("rdf", NS_RDF);
                    put("xlink", NS_XLINK);
                }});
        XMLUnit.setXpathNamespaceContext(ctx);
    }

    InfoDocument infoDocument;
    protected ModsDocument modsDocument;
    protected OpusDocument opusDocument;

    @Before
    public void setupBasisDatastreams() throws IOException, XmlException {
        modsDocument = ModsDocument.Factory.newInstance();
        modsDocument.addNewMods();

        opusDocument = OpusDocument.Factory.newInstance();
        opusDocument.addNewOpus().addNewOpusDocument();

        infoDocument = InfoDocument.Factory.newInstance();
        infoDocument.addNewInfo();
    }

    protected void runProcessor(MappingProcessor processor) throws Exception {
        processor.process(opusDocument, modsDocument, infoDocument);
    }

    void addServerDatePublished(int year, int month, int day, int hour, int minute, int second, String timezone) {
        Date sdp = opusDocument.getOpus().getOpusDocument().addNewServerDatePublished();
        sdp.setYear(BigInteger.valueOf(year));
        sdp.setMonth(BigInteger.valueOf(month));
        sdp.setDay(BigInteger.valueOf(day));
        sdp.setHour(BigInteger.valueOf(hour));
        sdp.setMinute(BigInteger.valueOf(minute));
        sdp.setSecond(BigInteger.valueOf(second));
        sdp.setTimezone(timezone);
    }

    File addFile(String path, Boolean oaiExport, Boolean frontdoorVisible) {
        File f = opusDocument.getOpus().getOpusDocument().addNewFile();
        f.setPathName(path);
        f.setOaiExport(oaiExport);
        f.setFrontdoorVisible(frontdoorVisible);
        return f;
    }
}
