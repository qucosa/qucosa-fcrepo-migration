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

import noNamespace.Subject;
import noNamespace.Title;
import org.junit.Before;
import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.junit.Assert.assertTrue;

public class ContentualMappingTest extends MappingTestBase {

    private ContentualMapping contentualMapping;

    @Before
    public void setup() {
        contentualMapping = new ContentualMapping();
    }

    @Test
    public void extractsTitleAbstract() throws Exception {
        final String value = "Deutsches Abstract";
        Title oa = opus.addNewTitleAbstract();
        oa.setLanguage("ger");
        oa.setValue(value);

        boolean result = contentualMapping.mapTitleAbstract(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(
                "//mods:abstract[@lang='ger' and @type='summary' and text()='" + value + "']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsSubjectDdc() throws Exception {
        Subject os = opus.addNewSubjectDdc();
        os.setType("ddc");
        os.setValue("004");

        boolean result = contentualMapping.mapSubject("ddc", opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(
                "//mods:classification[@authority='ddc' and text()='004']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsSubjectRvk() throws Exception {
        Subject os = opus.addNewSubjectRvk();
        os.setType("rvk");
        os.setValue("ST 270");

        boolean result = contentualMapping.mapSubject("rvk", opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(
                "//mods:classification[@authority='rvk' and text()='ST 270']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsSubjectSwd() throws Exception {
        Subject os = opus.addNewSubjectSwd();
        os.setType("swd");
        os.setValue("XYZ");

        boolean result = contentualMapping.mapSubject("swd", opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(
                "//mods:classification[@authority='swd' and text()='XYZ']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsSubjectUncontrolled() throws Exception {
        Subject os = opus.addNewSubjectUncontrolled();
        os.setType("uncontrolled");
        os.setLanguage("ger");
        os.setValue("A, B, C");

        boolean result = contentualMapping.mapSubject("uncontrolled", opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(
                "//mods:classification[@authority='z' and @lang='ger' and text()='A, B, C']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsTableOfContent() throws Exception {
        final String value = "Inhaltsverzeichnis";
        opus.setTableOfContent(value);

        boolean result = contentualMapping.mapTableOfContent(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(
                "//mods:tableOfContents[text()='" + value + "']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void Maps_issue_to_mods_part() throws Exception {
        String issue = "Jg. 25.2014, H. 11";
        opus.setIssue(issue);

        boolean result = contentualMapping.mapIssue(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(
                "//mods:part[@type='issue']/mods:detail/mods:number[text()='" + issue + "']",
                mods.getDomNode().getOwnerDocument());
    }

}
