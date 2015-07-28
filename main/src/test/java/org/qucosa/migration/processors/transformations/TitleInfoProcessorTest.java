/*
 * Copyright (C) 2015 Saxon State and University Library Dresden (SLUB)
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

package org.qucosa.migration.processors.transformations;

import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.ModsDocument;
import gov.loc.mods.v3.StringPlusLanguage;
import noNamespace.OpusDocument;
import noNamespace.Title;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class TitleInfoProcessorTest {

    private ModsDocument inputModsDocument;
    private OpusDocument inputOpusDocument;
    private MappingProcessor processor = new TitleInfoProcessor();

    @Before
    public void setupBasisDatastreams() throws IOException, XmlException {
        inputModsDocument = ModsDocument.Factory.newInstance();
        inputModsDocument.addNewMods();

        inputOpusDocument = OpusDocument.Factory.newInstance();
        inputOpusDocument.addNewOpus().addNewOpusDocument();
    }

    @Test
    public void hasCorrectLabel() {
        assertEquals("titleinfo", processor.getLabel());
    }

    @Test
    public void extractsTitleMain() throws Exception {
        final String language = "ger";
        final String value = "Effiziente Schemamigration in der modellgetriebenen Datenbankanwendungsentwicklung";
        addTitleMain(language, value);

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        assertTrue("Expected a <mods:titleInfo> element", outputMods.getTitleInfoArray().length > 0);
        assertEquals(language, outputMods.getTitleInfoArray(0).getTitleArray(0).getLang());
        assertEquals(value,
                outputMods.getTitleInfoArray(0).getTitleArray(0).getStringValue());
    }

    @Test
    public void extractsTitleSub() throws Exception {
        final String language = "eng";
        final String value = "The Incredibly Strange Creatures Who Stopped Living and Became Mixed-Up Zombies";
        addTitleSub(language, value);

        ModsDefinition outputMods = processor.process(inputOpusDocument, inputModsDocument).getMods();

        assertTrue("Expected a <mods:titleInfo> element", outputMods.getTitleInfoArray().length > 0);
        assertEquals(language, outputMods.getTitleInfoArray(0).getSubTitleArray(0).getLang());
        assertEquals(value,
                outputMods.getTitleInfoArray(0).getSubTitleArray(0).getStringValue());
    }

    @Test
    public void noChangesWhenTitleMainAlreadyPresent() throws Exception {
        final String language = "ger";
        final String value = "Effiziente Schemamigration in der modellgetriebenen Datenbankanwendungsentwicklung";
        addTitleMain(language, value);

        StringPlusLanguage title = inputModsDocument.getMods().addNewTitleInfo().addNewTitle();
        title.setLang(language);
        title.setStringValue(value);

        processor.process(inputOpusDocument, inputModsDocument);

        assertFalse(processor.hasChanges());
    }

    private void addTitleMain(String language, String value) {
        Title ot = inputOpusDocument.getOpus().getOpusDocument().addNewTitleMain();
        ot.setLanguage(language);
        ot.setValue(value);
    }

    private void addTitleSub(String language, String value) {
        Title ot = inputOpusDocument.getOpus().getOpusDocument().addNewTitleSub();
        ot.setLanguage(language);
        ot.setValue(value);
    }

}
