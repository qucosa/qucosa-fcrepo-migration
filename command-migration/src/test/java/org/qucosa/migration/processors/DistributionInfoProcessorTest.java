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

import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.junit.Assert.fail;

public class DistributionInfoProcessorTest extends ProcessorTestBase {

    private MappingProcessor processor = new DistributionInfoProcessor();

    @Test
    public void extractsPublisherName() throws Exception {
        final String publisher = "Universitätsbibliothek Leipzig";
        opus.setPublisherName(publisher);

        runProcessor(processor);

        assertXpathExists(
                "//mods:originInfo[@eventType='distribution']/" +
                        "mods:publisher[text()='" + publisher + "']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsPublisherPlace() throws Exception {
        final String place = "Leipzig";
        opus.setPublisherPlace(place);

        runProcessor(processor);

        assertXpathExists(
                "//mods:originInfo[@eventType='distribution']/mods:place/" +
                        "mods:placeTerm[@type='text' and text()='" + place + "']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsServerDatePublished() throws Exception {
        addServerDatePublished(2009, 6, 4, 12, 9, 40, "GMT-2");

        runProcessor(processor);

        assertXpathExists(
                "//mods:originInfo[@eventType='distribution']/" +
                        "mods:dateIssued[@encoding='iso8601' and @keyDate='yes' and text()='2009-06-04']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void handlesEmptyFields() {
        opus.setPublisherName(null);
        opus.setPublisherPlace("");
        addServerDatePublished(2009, 6, 4, 12, 9, 40, "GMT-2");

        try {
            runProcessor(processor);
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
