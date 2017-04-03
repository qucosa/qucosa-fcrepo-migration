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

import noNamespace.Document;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.junit.Assert.assertTrue;
import static org.qucosa.migration.mappings.AdministrativeInformationMapping.SLUB_GND_IDENTIFIER;

public class AdministrativeInformationMappingTest extends MappingTestBase {

    private AdministrativeInformationMapping aim;

    @Before
    public void setup() {
        aim = new AdministrativeInformationMapping();
    }

    @Test
    public void Maps_completed_date_to_originInfo() throws Exception {
        noNamespace.Date date = noNamespace.Date.Factory.newInstance();
        date.setYear(BigInteger.valueOf(2013));
        date.setMonth(BigInteger.valueOf(1));
        date.setDay(BigInteger.valueOf(31));

        boolean result = aim.mapCompletedDate(date, modsDocument.getMods());

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(
                "//mods:originInfo[@eventType='distribution']/" +
                        "mods:dateIssued[@encoding='iso8601' and @keyDate='yes' and text()='2013-01-31']",
                modsDocument.getMods().getDomNode().getOwnerDocument());
    }

    @Test
    public void Maps_publisher_infos_to_MODS_name_element() throws Exception {
        Document doc = opusDocument.getOpus().getOpusDocument();
        doc.setPublisherName("Saechsische Landesbibliothek- Staats- und Universitaetsbibliothek Dresden");
        doc.setPublisherPlace("Dresden");
        doc.setPublisherAddress("Zellescher Weg 18, 01069 Dresden, Germany");

        boolean result = aim.mapDefaultPublisherInfo(doc, modsDocument.getMods());

        org.w3c.dom.Document ownerDocument = modsDocument.getMods().getDomNode().getOwnerDocument();

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists("//mods:name[@type='corporate' and @displayLabel='mapping-hack-default-publisher']", ownerDocument);
        assertXpathExists("//mods:name[@type='corporate' and @displayLabel='mapping-hack-default-publisher']/" +
                "mods:nameIdentifier[@type='gnd' and text()='" + SLUB_GND_IDENTIFIER + "']", ownerDocument);
        assertXpathExists("//mods:extension/slub:corporation[@ref=//mods:name/@ID]", ownerDocument);
        assertXpathExists("//mods:extension/slub:corporation[@ref=//mods:name/@ID" +
                " and @place='" + doc.getPublisherPlace() + "'" +
                " and @address='" + doc.getPublisherAddress() + "']", ownerDocument);
        assertXpathExists("//mods:extension/slub:corporation[@ref=//mods:name/@ID]" +
                "/slub:university[text()='" + doc.getPublisherName() + "']", ownerDocument);
    }

    @Test
    public void Extracts_VgWortOpenKey() throws Exception {
        String vgWortOpenKey = "6fd9288e617c4721b6f25624167249f6";
        opusDocument.getOpus().getOpusDocument().setVgWortOpenKey(vgWortOpenKey);

        boolean result = aim.mapVgWortopenKey(opusDocument, infoDocument);

        assertTrue("Mapper should signal successful change", result);
        XMLAssert.assertXpathExists(
                "//slub:vgwortOpenKey[text()='" + vgWortOpenKey + "']",
                infoDocument.getInfo().getDomNode().getOwnerDocument());
    }

    @Test
    public void Filters_Url_Prefixes_from_VgWortOpenKey() throws Exception {
        String prefix = "http://vg04.met.vgwort.de/";
        String vgWortOpenKey = "6fd9288e617c4721b6f25624167249f6";
        opusDocument.getOpus().getOpusDocument().setVgWortOpenKey(prefix + vgWortOpenKey);

        aim.mapVgWortopenKey(opusDocument, infoDocument);

        XMLAssert.assertXpathExists(
                "//slub:vgwortOpenKey[text()='" + vgWortOpenKey + "']",
                infoDocument.getInfo().getDomNode().getOwnerDocument());
    }

}
