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

import static org.junit.Assert.assertTrue;

public class ContentualMappingTest extends MappingTestBase {

    private ContentualMapping contentualMapping;

    @Before
    public void setup() {
        contentualMapping = new ContentualMapping();
    }

    @Test
    public void extractsTitleAbstract() {
        final String value = "Deutsches Abstract";
        Title oa = opus.addNewTitleAbstract();
        oa.setLanguage("ger");
        oa.setValue(value);

        contentualMapping.mapTitleAbstract(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                "//mods:abstract[@lang='ger' and @type='summary' and text()='" + value + "']",
                mods);
    }

    @Test
    public void extractsSubjectDdc() {
        Subject os = opus.addNewSubjectDdc();
        os.setType("ddc");
        os.setValue("004");

        contentualMapping.mapSubject("ddc", opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:classification[@authority='ddc' and text()='004']", mods);
    }

    @Test
    public void extractsSubjectRvk() {
        Subject os = opus.addNewSubjectRvk();
        os.setType("rvk");
        os.setValue("ST 270");

        contentualMapping.mapSubject("rvk", opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:classification[@authority='rvk' and text()='ST 270']", mods);
    }

    @Test
    public void extractsSubjectSwd() {
        Subject os = opus.addNewSubjectSwd();
        os.setType("swd");
        os.setValue("XYZ");

        contentualMapping.mapSubject("swd", opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:classification[@authority='sswd' and text()='XYZ']", mods);
    }

    @Test
    public void extractsSubjectUncontrolled() {
        Subject os = opus.addNewSubjectUncontrolled();
        os.setType("uncontrolled");
        os.setLanguage("ger");
        os.setValue("A, B, C");

        contentualMapping.mapSubject("uncontrolled", opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:classification[@authority='z' and @lang='ger' and text()='A, B, C']", mods);
    }

    @Test
    public void Extracts_multiple_equal_subjects_of_multiple_languages() {
        {
            Subject os = opus.addNewSubjectUncontrolled();
            os.setType("uncontrolled");
            os.setLanguage("ger");
            os.setValue("same, same");
        }
        {
            Subject os = opus.addNewSubjectUncontrolled();
            os.setType("uncontrolled");
            os.setLanguage("eng");
            os.setValue("same, same");
        }

        contentualMapping.mapSubject("uncontrolled", opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:classification[@authority='z' and @lang='ger' and text()='same, same']", mods);
        assertXpathExists("//mods:classification[@authority='z' and @lang='eng' and text()='same, same']", mods);
    }

    @Test
    public void extractsTableOfContent() {
        final String value = "Inhaltsverzeichnis";
        opus.setTableOfContent(value);

        contentualMapping.mapTableOfContent(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:tableOfContents[text()='" + value + "']", mods);
    }

    @Test
    public void Maps_issue_to_mods_part() {
        String issue = "Jg. 25.2014, H. 11";
        opus.setIssue(issue);

        contentualMapping.mapIssue(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:part[@type='issue']/mods:detail/mods:number[text()='" + issue + "']", mods);
    }

}
