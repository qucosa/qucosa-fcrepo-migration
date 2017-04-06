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

import noNamespace.Date;
import org.junit.Test;
import org.w3c.dom.Document;

import java.math.BigInteger;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.junit.Assert.assertTrue;

public class PublicationInfoMappingTest extends MappingTestBase {

    private final PublicationInfoMapping publicationInfoMapping = new PublicationInfoMapping();

    @Test
    public void extractsLanguage() throws Exception {
        final String lang = "ger";
        opus.addLanguage(lang);

        boolean result = publicationInfoMapping.mapLanguageElement(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(
                "//mods:language/mods:languageTerm[@authority='iso639-2b' and @type='code' and text()='" + lang + "']",
                mods.getDomNode().getOwnerDocument());
    }


    @Test
    public void Extracts_comma_separated_language_list() throws Exception {
        final String lang = "ger,eng,chi";
        opus.addLanguage(lang);

        boolean result = publicationInfoMapping.mapLanguageElement(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        final Document ownerDocument = mods.getDomNode().getOwnerDocument();
        final String s = "//mods:language/mods:languageTerm[@authority='iso639-2b' and @type='code'";
        assertXpathExists(s + " and text()='ger']", ownerDocument);
        assertXpathExists(s + " and text()='eng']", ownerDocument);
        assertXpathExists(s + " and text()='chi']", ownerDocument);
    }

    @Test
    public void extractsPublishedDate() throws Exception {
        Date ocd = opus.addNewPublishedDate();
        ocd.setYear(BigInteger.valueOf(2009));
        ocd.setMonth(BigInteger.valueOf(6));
        ocd.setDay(BigInteger.valueOf(4));
        ocd.setHour(BigInteger.valueOf(12));
        ocd.setMinute(BigInteger.valueOf(9));
        ocd.setSecond(BigInteger.valueOf(40));
        ocd.setTimezone("GMT-2");

        boolean result = publicationInfoMapping.mapOriginInfoElements(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(
                "//mods:originInfo[@eventType='publication']" +
                        "/mods:dateOther[@encoding='iso8601' and @type='submission' and text()='2009-06-04']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void handlesEmptyPublishedDate() throws Exception {
        opus.addNewPublishedDate();

        boolean result = publicationInfoMapping.mapOriginInfoElements(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathNotExists(
                "//mods:originInfo[@eventType='publication']/" +
                        "mods:dateOther[@encoding='iso8601' and @type='submission']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsDateAccepted() throws Exception {
        Date ocd = opus.addNewDateAccepted();
        ocd.setYear(BigInteger.valueOf(2009));
        ocd.setMonth(BigInteger.valueOf(6));
        ocd.setDay(BigInteger.valueOf(20));
        ocd.setHour(BigInteger.valueOf(0));
        ocd.setMinute(BigInteger.valueOf(0));
        ocd.setSecond(BigInteger.valueOf(0));
        ocd.setTimezone("GMT-1");

        boolean result = publicationInfoMapping.mapOriginInfoElements(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(
                "//mods:originInfo[@eventType='publication']/" +
                        "mods:dateOther[@encoding='iso8601' and @type='defense' and text()='2009-06-20']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void handlesEmptyDateAccepted() throws Exception {
        opus.addNewDateAccepted();

        boolean result = publicationInfoMapping.mapOriginInfoElements(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathNotExists(
                "//mods:originInfo[@eventType='publication']/" +
                        "mods:dateOther[@encoding='iso8601' and @type='defense']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsPublishedYear() throws Exception {
        opus.setPublishedYear(BigInteger.valueOf(2009));

        boolean result = publicationInfoMapping.mapOriginInfoElements(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(
                "//mods:originInfo[@eventType='publication']/" +
                        "mods:dateIssued[@encoding='iso8601' and text()='2009']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void handlesEmptyPublishedYear() throws Exception {
        opus.setPublishedYear(null);

        boolean result = publicationInfoMapping.mapOriginInfoElements(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathNotExists(
                "//mods:originInfo[@eventType='publication']/mods:dateIssued",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void handlesZeroPublishedYear() throws Exception {
        opus.setPublishedYear(BigInteger.ZERO);

        boolean result = publicationInfoMapping.mapOriginInfoElements(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathNotExists(
                "//mods:originInfo[@eventType='publication']/mods:dateIssued",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void usesServerDatePublishedIfNoOtherDateIsPresent() throws Exception {
        Date ocd = opus.addNewServerDatePublished();
        ocd.setYear(BigInteger.valueOf(2009));
        ocd.setMonth(BigInteger.valueOf(6));
        ocd.setDay(BigInteger.valueOf(20));
        ocd.setHour(BigInteger.valueOf(0));
        ocd.setMinute(BigInteger.valueOf(0));
        ocd.setSecond(BigInteger.valueOf(0));
        ocd.setTimezone("GMT-1");

        boolean result = publicationInfoMapping.mapOriginInfoElements(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(
                "//mods:originInfo[@eventType='publication']/mods:dateIssued[@encoding='iso8601' and text()='2009-06-20']",
                mods.getDomNode().getOwnerDocument());
    }


    @Test
    public void extractsEdition() throws Exception {
        opus.setEdition("2nd. Edition");

        boolean result = publicationInfoMapping.mapOriginInfoElements(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(
                "//mods:originInfo[@eventType='publication']/mods:edition[text()='2nd. Edition']",
                mods.getDomNode().getOwnerDocument());
    }

}
