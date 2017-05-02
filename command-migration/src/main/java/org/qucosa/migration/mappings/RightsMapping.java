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

import de.slubDresden.AttachmentType;
import de.slubDresden.InfoType;
import de.slubDresden.RightsType;
import de.slubDresden.YesNo;
import noNamespace.Document;
import org.apache.xmlbeans.XmlObject;

import java.util.ArrayList;

import static de.slubDresden.YesNo.YES;
import static org.qucosa.migration.mappings.ChangeLog.Type.MODS;
import static org.qucosa.migration.mappings.MappingFunctions.yesNoBooleanMapping;
import static org.qucosa.migration.mappings.XmlFunctions.select;
import static org.qucosa.migration.mappings.XmlFunctions.selectAll;

public class RightsMapping {

    public void mapFileAttachments(Document opus, InfoType info, ChangeLog changeLog) {
        RightsType rights = info.getRights();
        if (rights == null) {
            rights = info.addNewRights();
            changeLog.log(MODS);
        }

        final ArrayList<String> existingAttachmentRefs = new ArrayList<>();
        for (XmlObject o : selectAll("slub:attachment", rights)) {
            final AttachmentType eat = (AttachmentType) o;
            existingAttachmentRefs.add(eat.getRef());
        }

        final ArrayList<String> processedAttachmentRefs = new ArrayList<>();

        int i = 0;
        for (noNamespace.File opusFile : opus.getFileArray()) {
            final String ref = "ATT-" + i;
            final YesNo.Enum hasArchivalValue = yesNoBooleanMapping(opusFile.getOaiExport());
            final YesNo.Enum isDownloadable = yesNoBooleanMapping(opusFile.getFrontdoorVisible());
            final YesNo.Enum isRedistributable = YES;

            final String query = String.format("slub:attachment[@ref='%s']", ref);
            AttachmentType at = (AttachmentType) select(query, rights);
            if (at == null) {
                at = rights.addNewAttachment();
                changeLog.log(MODS);
            }

            processedAttachmentRefs.add(ref);

            if (at.getRef() == null || !at.getRef().equals(ref)) {
                at.setRef(ref);
                changeLog.log(MODS);
            }
            if (at.getHasArchivalValue() == null || !at.getHasArchivalValue().equals(hasArchivalValue)) {
                at.setHasArchivalValue(hasArchivalValue);
                changeLog.log(MODS);
            }
            if (at.getIsDownloadable() == null || !at.getIsDownloadable().equals(isDownloadable)) {
                at.setIsDownloadable(isDownloadable);
                changeLog.log(MODS);
            }
            if (at.getIsRedistributable() == null || !at.getIsRedistributable().equals(isRedistributable)) {
                at.setIsRedistributable(isRedistributable);
                changeLog.log(MODS);
            }

            i++;
        }

        existingAttachmentRefs.removeAll(processedAttachmentRefs);
        for (int j = 0; j < rights.getAttachmentArray().length; j++) {
            if (existingAttachmentRefs.contains(rights.getAttachmentArray(j).getRef())) {
                rights.removeAttachment(j);
            }
        }
    }

}
