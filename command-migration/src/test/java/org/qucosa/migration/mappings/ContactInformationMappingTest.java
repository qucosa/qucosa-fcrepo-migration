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
import org.junit.Before;
import org.junit.Test;

public class ContactInformationMappingTest extends MappingTestBase {

    private ContactInformationMapping contactInformationMapping;

    @Before
    public void setup() {
        contactInformationMapping = new ContactInformationMapping();
    }

    @Test
    public void Maps_note_element() {
        Note note = opus.addNewNote();
        note.setCreator("me");
        note.setScope("private");
        note.setMessage("The Message");

        contactInformationMapping.mapNotes(opus.getNoteArray(), info, changeLog);

        assertXpathExists("//slub:note[@from='me' and @scope='private' and text()='The Message']", info);
    }

    @Test
    public void Extracts_submitter_information() {
        Person submitter = opus.addNewPersonSubmitter();
        submitter.setPhone("+49 815 4711");
        submitter.setEmail("m.musterfrau@example.com");
        submitter.setFirstName("Maxi");
        submitter.setLastName("Musterfrau");

        contactInformationMapping.mapPersonSubmitter(opus.getPersonSubmitterArray(), info, changeLog);

        assertXpathExists(
                "//slub:submitter/foaf:Person[" +
                        "foaf:name='Maxi Musterfrau' and " +
                        "foaf:phone='+49 815 4711' and " +
                        "foaf:mbox='m.musterfrau@example.com']",
                info);
    }

}
