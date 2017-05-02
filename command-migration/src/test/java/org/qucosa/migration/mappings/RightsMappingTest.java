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

import noNamespace.File;
import org.junit.Before;
import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.junit.Assert.assertTrue;

public class RightsMappingTest extends MappingTestBase {

    private RightsMapping rightsMapping;

    @Before
    public void setup() {
        rightsMapping = new RightsMapping();
    }

    @Test
    public void hasSlubAttachmentElementForEachFile() throws Exception {
        addFile("file1.pdf", true, false);
        addFile("file2.pdf", false, true);

        rightsMapping.mapFileAttachments(opus, info, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                "//slub:rights/slub:attachment[@ref='ATT-0' and @hasArchivalValue='yes' and @isDownloadable='no']",
                info.getDomNode().getOwnerDocument());
        assertXpathExists(
                "//slub:rights/slub:attachment[@ref='ATT-1' and @hasArchivalValue='no' and @isDownloadable='yes']",
                info.getDomNode().getOwnerDocument());
    }

    @Test
    public void holdsNoUnnecessaryAttachmentElements() throws Exception {
        addFile("file1.pdf", true, false);
        info.addNewRights().addNewAttachment().setRef("ATT-1");

        rightsMapping.mapFileAttachments(opus, info, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathNotExists(
                "//slub:rights/slub:attachment[@ref='ATT-1']",
                info.getDomNode().getOwnerDocument());
    }

    private void addFile(String path, Boolean oaiExport, Boolean frontdoorVisible) {
        File f = opus.addNewFile();
        f.setPathName(path);
        f.setOaiExport(oaiExport);
        f.setFrontdoorVisible(frontdoorVisible);
    }

}
