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

import de.slubDresden.InfoType;
import noNamespace.Document;

import static org.qucosa.migration.mappings.ChangeLog.Type.SLUB_INFO;
import static org.qucosa.migration.mappings.MappingFunctions.documentTypeEncoding;

public class DocumentTypeMapping {

    public void mapDocumentType(Document opus, InfoType info, ChangeLog changeLog) {
        final String type = opus.getType();
        if (type != null && !type.isEmpty()) {
            final String encodedType = documentTypeEncoding(type, opus.getDocumentId());
            if (info.getDocumentType() == null || !info.getDocumentType().equals(encodedType)) {
                info.setDocumentType(encodedType);
                changeLog.log(SLUB_INFO);
            }
        }
    }

}
