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

import de.slubDresden.InfoDocument;
import de.slubDresden.InfoType;
import de.slubDresden.NoteType;
import de.slubDresden.ScopeType;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.Document;
import noNamespace.Note;
import noNamespace.OpusDocument;
import org.apache.xmlbeans.XmlString;

public class AdministrationProcessor extends MappingProcessor {
    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        final Document opus = opusDocument.getOpus().getOpusDocument();

        mapNotes(infoDocument, opus.getNoteArray());
    }

    private void mapNotes(InfoDocument infoDocument, Note[] noteArray) {
        InfoType it = (InfoType) select("slub:info", infoDocument);

        if (it == null) {
            it = infoDocument.addNewInfo();
            signalChanges(SLUB_INFO_CHANGES);
        }

        for (Note note : noteArray) {
            final String creator = singleline(note.getCreator());
            final String scope = singleline(note.getScope());
            final String message = multiline(note.getMessage());

            NoteType noteElement = (NoteType) select(String.format("slub:note[@from='%s' and @scope='%s' and @message='%s']",
                    creator, scope, message), it);

            if (noteElement == null) {
                noteElement = it.addNewNote();
                noteElement.setFrom(creator);
                noteElement.setScope(ScopeType.Enum.forString(scope));
                noteElement.setStringValue(message);
                signalChanges(SLUB_INFO_CHANGES);
            }
        }
    }
}
