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
import noNamespace.OpusDocument;
import noNamespace.Title;
import org.apache.xmlbeans.XmlString;
import org.junit.Before;
import org.junit.Test;
import org.qucosa.migration.processors.MappingProcessor;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TitleMappingTest extends MappingTestBase {

    private TitleMapping titleMapping;
    private MappingProcessor mappingProcessor;

    @Before
    public void setup() {
        titleMapping = new TitleMapping();
        mappingProcessor = new MappingProcessor();
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
                mods);
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
                mods);
    }

    @Test
    public void extractsCombinationTitleMainAndTitleSub() throws Exception {
        opus.addLanguage("ger");
        final String language = "ger";
        final String subValue = "Kooperation und Vernetzung der wissenschaftlichen Bibliotheken im Freistaat Sachsen";
        final String mainValue = "Gemeinschaft macht stark";

        opus.addLanguage("eng");
        final String language2 = "eng";
        final String subValue2 = "english sub title";
        final String mainValue2 = "english main title";

        addTitleSub(language, subValue);
        addTitleMain(language, mainValue);

        addTitleSub(language2, subValue2);
        addTitleMain(language2, mainValue2);

        titleMapping.mapTitleMainElements(opus, mods, changeLog);
        titleMapping.mapTitleSubElements(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                "//mods:titleInfo[@lang='" + language + "']/mods:subTitle[text()='" + subValue + "']",
                mods);
        assertXpathExists(
                "//mods:titleInfo[@lang='" + language + "']/mods:title[text()='" + mainValue + "']",
                mods);
        assertXpathExists(
                "//mods:titleInfo[@lang='" + language2 + "']/mods:subTitle[text()='" + subValue2 + "']",
                mods);
        assertXpathExists(
                "//mods:titleInfo[@lang='" + language2 + "']/mods:title[text()='" + mainValue2 + "']",
                mods);
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
                mods);
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
        assertXpathExists("//mods:titleInfo[@lang='ger' and not(@type) and mods:title='Deutscher Titel']", mods);
        assertXpathExists("//mods:titleInfo[@lang='eng' and @type='translated' and mods:title='English Title']", mods);
        assertXpathExists("//mods:titleInfo[@lang='ice' and @type='translated' and mods:title='íslenska titill']", mods);
    }

    @Test
    public void Detect_main_title_if_document_has_multiple_languages() throws Exception {
        opus.addLanguage("ger,eng");
        addTitleMain("ger", "Deutscher Titel");
        addTitleMain("eng", "English Title");

        titleMapping.mapTitleMainElements(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:titleInfo[@lang='ger' and @usage='primary' and mods:title='Deutscher Titel']", mods);
        assertXpathExists("//mods:titleInfo[@lang='eng' and @type='translated' and mods:title='English Title']", mods);
    }

    @Test
    public void Choose_first_main_title_if_no_title_matches_document_language() throws Exception {
        opus.addLanguage("chi");
        addTitleMain("ger", "Deutscher Titel");
        addTitleMain("eng", "English Title");

        titleMapping.mapTitleMainElements(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:titleInfo[@lang='ger' and @usage='primary' and mods:title='Deutscher Titel']", mods);
        assertXpathNotExists("//mods:titleInfo[@lang='ger' and @type='translated' and mods:title='Deutscher Titel']", mods);
    }

    /*
    Ensure TitleSub-Value (from Opus-Xml-String) is correctly mapped to mods:subTitle
    https://jira.slub-dresden.de/browse/CMR-163
     */
    @Test
    public void ensureSubTitleMappingFromOpusXMLExample() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        OpusDocument opusDocument = OpusDocument.Factory.parse(new File(classLoader.getResource("opus_394.xml").getFile()));

        opus = opusDocument.getOpus().getOpusDocument();
        mappingProcessor.process(opus, mods, info, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                "//mods:titleInfo[@lang='ger']/mods:subTitle[text()='Kooperation und Vernetzung der wissenschaftlichen Bibliotheken im Freistaat Sachsen']",
                mods);
    }

    /*
    Ensure TitleSub-Value (from Opus-Xml-String) is correctly mapped to mods:subTitle
    https://jira.slub-dresden.de/browse/CMR-163
     */
    @Test
    public void ensureSubTitleMappingFromOpusXMLExample2() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        OpusDocument opusDocument = OpusDocument.Factory.parse(
                new File(classLoader.getResource("opus_13429.xml").getFile()));

        opus = opusDocument.getOpus().getOpusDocument();
        mappingProcessor.process(opus, mods, info, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                "//mods:titleInfo[@lang='eng']/mods:subTitle[text()='a computer tomographic study in sheep and pigs with atelectasis in " +
                        "otherwise normal lungs']",
                mods);
    }

    /*
        Ensure single quotes remain single quotes.
        https://jira.slub-dresden.de/browse/CMR-381
     */
    @Test
    public void Ensure_title_single_quotes_remain() throws Exception {
        opus.addLanguage("eng");
        addTitleMain("eng", "Parzival by Wolfram von Eschenbach's");

        titleMapping.mapTitleMainElements(opus, mods, changeLog);
        titleMapping.mapTitleMainElements(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:titleInfo[@lang='eng' and mods:title=\"Parzival by Wolfram von Eschenbach's\"]", mods);
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

}
