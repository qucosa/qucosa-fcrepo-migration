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

import noNamespace.Note;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;

public class AdministrationProcessorTest extends ProcessorTestBase {

    final private MappingProcessor processor = new AdministrationProcessor();

    @Test
    public void Maps_note_element() throws Exception {
        Note note = opusDocument.getOpus().getOpusDocument().addNewNote();
        note.setCreator("me");
        note.setScope("private");
        note.setMessage("The Message");

        runProcessor(processor);

        XMLAssert.assertXpathExists(
                "//slub:note[@from='me' and @scope='private' and text()='The Message']",
                infoDocument.getInfo().getDomNode().getOwnerDocument());
    }

}
