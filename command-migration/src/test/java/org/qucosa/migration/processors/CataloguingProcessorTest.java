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

import noNamespace.Subject;
import noNamespace.Title;
import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

public class CataloguingProcessorTest extends ProcessorTestBase {

    final private MappingProcessor processor = new CataloguingProcessor();

    @Test
    public void extractsTitleAbstract() throws Exception {
        final String value = "Deutsches Abstract";
        Title oa = opus.addNewTitleAbstract();
        oa.setLanguage("ger");
        oa.setValue(value);

        runProcessor(processor);

        assertXpathExists(
                "//mods:abstract[@lang='ger' and @type='summary' and text()='" + value + "']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsTableOfContent() throws Exception {
        final String value = "Inhaltsverzeichnis";
        opus.setTableOfContent(value);

        runProcessor(processor);

        assertXpathExists(
                "//mods:tableOfContents[text()='" + value + "']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsSubjectDdc() throws Exception {
        Subject os = opus.addNewSubjectDdc();
        os.setType("ddc");
        os.setValue("004");

        runProcessor(processor);

        assertXpathExists(
                "//mods:classification[@authority='ddc' and text()='004']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsSubjectRvk() throws Exception {
        Subject os = opus.addNewSubjectRvk();
        os.setType("rvk");
        os.setValue("ST 270");

        runProcessor(processor);

        assertXpathExists(
                "//mods:classification[@authority='rvk' and text()='ST 270']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsSubjectSwd() throws Exception {
        Subject os = opus.addNewSubjectSwd();
        os.setType("swd");
        os.setValue("XYZ");

        runProcessor(processor);

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

        runProcessor(processor);

        assertXpathExists(
                "//mods:classification[@authority='z' and @lang='ger' and text()='A, B, C']",
                mods.getDomNode().getOwnerDocument());
    }
}
