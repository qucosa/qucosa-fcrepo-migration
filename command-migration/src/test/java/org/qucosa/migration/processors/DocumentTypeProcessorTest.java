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

public class DocumentTypeProcessorTest extends ProcessorTestBase {

    private MappingProcessor processor = new DocumentTypeProcessor();

    @Test
    public void extractsDocumentType() throws Exception {
        String type = "diploma_thesis";
        opus.setType(type);

        runProcessor(processor);

        assertXpathExists(
                "//slub:documentType[text()='" + type + "']",
                info.getDomNode().getOwnerDocument());
    }

    @Test
    public void updatesExistingDocumentType() throws Exception {
        opus.setType("new-type");
        info.setDocumentType("old-type");

        runProcessor(processor);

        assertXpathExists(
                "//slub:documentType[text()='new-type']",
                info.getDomNode().getOwnerDocument());
    }

    @Test
    public void properlyEncodesJournalDocumentType() throws Exception {
        String type = "journal";
        opus.setType(type);

        runProcessor(processor);

        assertXpathExists(
                "//slub:documentType[text()='periodical']",
                info.getDomNode().getOwnerDocument());
    }


}
