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

import org.junit.Before;
import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.junit.Assert.assertTrue;

public class DocumentTypeMappingTest extends MappingTestBase {

    private DocumentTypeMapping documentTypeMapping;

    @Before
    public void setup() {
        documentTypeMapping = new DocumentTypeMapping();
    }

    @Test
    public void extractsDocumentType() throws Exception {
        String type = "diploma_thesis";
        opus.setType(type);

        documentTypeMapping.mapDocumentType(opus, info, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                "//slub:documentType[text()='" + type + "']",
                info.getDomNode().getOwnerDocument());
    }

    @Test
    public void updatesExistingDocumentType() throws Exception {
        opus.setType("article");
        info.setDocumentType("book");

        documentTypeMapping.mapDocumentType(opus, info, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                "//slub:documentType[text()='article']",
                info.getDomNode().getOwnerDocument());
    }

    @Test
    public void properlyEncodesJournalDocumentType() throws Exception {
        opus.setType("journal");

        documentTypeMapping.mapDocumentType(opus, info, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                "//slub:documentType[text()='periodical']",
                info.getDomNode().getOwnerDocument());
    }

    /*
    Tests specific Document-Type-Mapping.
    Certain documents should be mapped to "series" type, series-type had no corresponding mapping in Opus
    and therefore became Opus-"journals" which usually maps to "periodical".
     */
    @Test
    public void properlyEncodesSpecificDocumentType() throws Exception {
        opus.setDocumentId("22751");
        opus.setType("journal");

        documentTypeMapping.mapDocumentType(opus, info, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathNotExists(
                "//slub:documentType[text()='periodical']",
                info.getDomNode().getOwnerDocument());
        assertXpathExists("//slub:documentType[text()='series']",
                info.getDomNode().getOwnerDocument());
    }

}
