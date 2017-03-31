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

import de.slubDresden.InfoDocument;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.Document;
import noNamespace.OpusDocument;
import org.qucosa.migration.mappings.ContactInformationMapping;

public class AdministrationProcessor extends MappingProcessor {

    private final ContactInformationMapping contactInformationMapping = new ContactInformationMapping();

    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        final Document opus = opusDocument.getOpus().getOpusDocument();

        if (contactInformationMapping.mapPersonSubmitter(opus.getPersonSubmitterArray(), infoDocument.getInfo())) {
            signalChanges(SLUB_INFO_CHANGES);
        }

        if (contactInformationMapping.mapNotes(opus.getNoteArray(), infoDocument.getInfo())) {
            signalChanges(SLUB_INFO_CHANGES);
        }

    }

}
