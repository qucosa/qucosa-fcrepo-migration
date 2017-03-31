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

import noNamespace.Note;
import noNamespace.Person;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

import static org.qucosa.migration.mappings.Namespaces.NS_FOAF;
import static org.qucosa.migration.mappings.Namespaces.NS_MODS_V3;
import static org.qucosa.migration.mappings.Namespaces.NS_RDF;
import static org.qucosa.migration.mappings.Namespaces.NS_SLUB;
import static org.qucosa.migration.mappings.Namespaces.NS_XLINK;

public class ContactInformationMappingTest extends MappingTestBase {

    private ContactInformationMapping contactInformationMapping;

    @Before
    public void setup() {
        contactInformationMapping = new ContactInformationMapping();
    }

    @Test
    public void Maps_note_element() throws Exception {
        Note note = opusDocument.getOpus().getOpusDocument().addNewNote();
        note.setCreator("me");
        note.setScope("private");
        note.setMessage("The Message");

        contactInformationMapping.mapNotes(
                opusDocument.getOpus().getOpusDocument().getNoteArray(),
                infoDocument.getInfo());

        XMLAssert.assertXpathExists(
                "//slub:note[@from='me' and @scope='private' and text()='The Message']",
                infoDocument.getInfo().getDomNode().getOwnerDocument());
    }

    @Test
    public void extractsSubmitter() throws Exception {
        Person submitter = opusDocument.getOpus().getOpusDocument().addNewPersonSubmitter();
        submitter.setPhone("+49 815 4711");
        submitter.setEmail("m.musterfrau@example.com");
        submitter.setFirstName("Maxi");
        submitter.setLastName("Musterfrau");

        contactInformationMapping.mapPersonSubmitter(
                opusDocument.getOpus().getOpusDocument().getPersonSubmitterArray(),
                infoDocument.getInfo());

        XMLAssert.assertXpathExists(
                "//slub:submitter/foaf:Person[" +
                        "foaf:name='Maxi Musterfrau' and " +
                        "foaf:phone='+49 815 4711' and " +
                        "foaf:mbox='m.musterfrau@example.com']",
                infoDocument.getInfo().getDomNode().getOwnerDocument());
    }

    @BeforeClass
    static public void setupXMLUnitNamespaceContext() {
        XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(
                new HashMap<String, String>() {{
                    put("mods", NS_MODS_V3);
                    put("slub", NS_SLUB);
                    put("foaf", NS_FOAF);
                    put("rdf", NS_RDF);
                    put("xlink", NS_XLINK);
                }}));
    }

}
