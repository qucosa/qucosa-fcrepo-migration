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

import gov.loc.mods.v3.StringPlusLanguage;
import gov.loc.mods.v3.TitleInfoDefinition;
import noNamespace.Title;
import org.apache.xmlbeans.XmlString;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TitleMappingTest extends MappingTestBase {

    private TitleMapping titleMapping;

    @Before
    public void setup() {
        titleMapping = new TitleMapping();
    }

    @Test
    public void extractsTitleMain() throws Exception {
        opus.addLanguage("ger");
        final String language = "ger";
        final String value = "Effiziente Schemamigration in der modellgetriebenen Datenbankanwendungsentwicklung";
        addTitleMain(language, value);

        titleMapping.mapTitleMainElements(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                "//mods:titleInfo[@lang='" + language + "' and @usage='primary']/mods:title[text()='" + value + "']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsTitleSub() throws Exception {
        final String language = "eng";
        final String value = "The Incredibly Strange Creatures Who Stopped Living and Became Mixed-Up Zombies";
        addTitleSub(language, value);

        titleMapping.mapTitleSubElements(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                "//mods:titleInfo[@lang='" + language + "']/mods:subTitle[text()='" + value + "']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsTitleAlternative() throws Exception {
        final String language = "ger";
        final String value = "Schülerecho";
        addTitleAlternative(language, value);

        titleMapping.mapTitleAlternativeElements(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                "//mods:titleInfo[@lang='" + language + "' and @type='alternative']/mods:title[text()='" + value + "']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void noChangesWhenTitleMainAlreadyPresent() throws Exception {
        final String language = "ger";
        final String value = "Effiziente Schemamigration in der modellgetriebenen Datenbankanwendungsentwicklung";
        addTitleMain(language, value);
        opus.addLanguage(language);

        TitleInfoDefinition titleInfoDefinition = mods.addNewTitleInfo();
        titleInfoDefinition.setLang(language);
        titleInfoDefinition.setUsage(XmlString.Factory.newValue("primary"));

        StringPlusLanguage title = titleInfoDefinition.addNewTitle();
        title.setStringValue(value);

        titleMapping.mapTitleMainElements(opus, mods, changeLog);

        assertFalse("Mapper should not signal changes", changeLog.hasChanges());
    }

    @Test
    public void Assumes_main_title_with_other_than_document_language_is_translated_title() throws Exception {
        opus.addLanguage("ger");
        addTitleMain("ger", "Deutscher Titel");
        addTitleMain("eng", "English Title");
        addTitleMain("ice", "íslenska titill");

        titleMapping.mapTitleMainElements(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        Document ownerDocument = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:titleInfo[@lang='ger' and not(@type) and mods:title='Deutscher Titel']", ownerDocument);
        assertXpathExists("//mods:titleInfo[@lang='eng' and @type='translated' and mods:title='English Title']", ownerDocument);
        assertXpathExists("//mods:titleInfo[@lang='ice' and @type='translated' and mods:title='íslenska titill']", ownerDocument);
    }

    @Test
    public void Detect_main_title_if_document_has_multiple_languages() throws Exception {
        opus.addLanguage("ger,eng");
        addTitleMain("ger", "Deutscher Titel");
        addTitleMain("eng", "English Title");

        titleMapping.mapTitleMainElements(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        Document ownerDocument = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:titleInfo[@lang='ger' and @usage='primary' and mods:title='Deutscher Titel']", ownerDocument);
        assertXpathExists("//mods:titleInfo[@lang='eng' and @type='translated' and mods:title='English Title']", ownerDocument);
    }

    @Test
    public void Choose_first_main_title_if_no_title_matches_document_language() throws Exception {
        opus.addLanguage("chi");
        addTitleMain("ger", "Deutscher Titel");
        addTitleMain("eng", "English Title");

        titleMapping.mapTitleMainElements(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        Document ownerDocument = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:titleInfo[@lang='ger' and @usage='primary' and mods:title='Deutscher Titel']", ownerDocument);
        assertXpathNotExists("//mods:titleInfo[@lang='ger' and @type='translated' and mods:title='Deutscher Titel']", ownerDocument);
    }

    private void addTitleMain(String language, String value) {
        Title ot = opus.addNewTitleMain();
        ot.setLanguage(language);
        ot.setValue(value);
    }

    private void addTitleSub(String language, String value) {
        Title ot = opus.addNewTitleSub();
        ot.setLanguage(language);
        ot.setValue(value);
    }

    private void addTitleAlternative(String language, String value) {
        Title ot = opus.addNewTitleAlternative();
        ot.setLanguage(language);
        ot.setValue(value);
    }

    private void addTitleParent(String language, String value) {
        Title ot = opus.addNewTitleParent();
        ot.setLanguage(language);
        ot.setValue(value);
    }

}
