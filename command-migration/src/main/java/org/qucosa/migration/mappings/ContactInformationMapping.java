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

import com.xmlns.foaf.x01.PersonDocument;
import de.slubDresden.InfoType;
import de.slubDresden.NoteType;
import de.slubDresden.ScopeType;
import de.slubDresden.SubmitterType;
import noNamespace.Note;
import noNamespace.Person;

import static org.qucosa.migration.mappings.ChangeLog.Type.SLUB_INFO;
import static org.qucosa.migration.mappings.MappingFunctions.combineName;
import static org.qucosa.migration.mappings.MappingFunctions.multiline;
import static org.qucosa.migration.mappings.MappingFunctions.singleline;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.formatXPath;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.select;

public class ContactInformationMapping {

    public void mapPersonSubmitter(Person[] submitters, InfoType targetSlubInfoElement, ChangeLog changeLog) {
        for (Person submitter : submitters) {
            final String name = singleline(combineName(submitter.getFirstName(), submitter.getLastName()));
            final String phone = submitter.getPhone();
            final String mbox = submitter.getEmail();

            SubmitterType st = (SubmitterType)
                    select(formatXPath("slub:submitter[foaf:Person/foaf:name='%s']", name), targetSlubInfoElement);

            if (st == null) {
                st = targetSlubInfoElement.addNewSubmitter();
                PersonDocument.Person foafPerson = st.addNewPerson();
                foafPerson.setName(name);
                if (phone != null && !phone.isEmpty()) foafPerson.setPhone(phone);
                if (mbox != null && !mbox.isEmpty()) foafPerson.setMbox(mbox);
                changeLog.log(SLUB_INFO);
            }
        }
    }

    public void mapNotes(Note[] notes, InfoType targetSlubInfoElement, ChangeLog changeLog) {
        for (Note note : notes) {
            final String creator = singleline(note.getCreator());
            final String scope = singleline(note.getScope());
            final String message = multiline(note.getMessage());

            NoteType noteElement = (NoteType) select(
                    formatXPath("slub:note[@from='%s' and @scope='%s' and @message='%s']",
                    creator, scope, message), targetSlubInfoElement);

            if (noteElement == null) {
                noteElement = targetSlubInfoElement.addNewNote();
                noteElement.setFrom(creator);
                noteElement.setScope(ScopeType.Enum.forString(scope));
                noteElement.setStringValue(message);
                changeLog.log(SLUB_INFO);
            }
        }
    }

}
