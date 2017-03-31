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

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.ArrayList;
import java.util.List;

import static org.qucosa.migration.mappings.Namespaces.NS_FOAF;
import static org.qucosa.migration.mappings.Namespaces.NS_MODS_V3;
import static org.qucosa.migration.mappings.Namespaces.NS_SLUB;
import static org.qucosa.migration.mappings.Namespaces.NS_XLINK;

public class XmlFunctions {

    private static final String xpathNSDeclaration =
            "declare namespace mods='" + NS_MODS_V3 + "'; " +
                    "declare namespace slub='" + NS_SLUB + "'; " +
                    "declare namespace foaf='" + NS_FOAF + "'; " +
                    "declare namespace xlink='" + NS_XLINK + "'; ";

    public static Boolean nodeExists(String expression, XmlObject object) {
        return (XmlFunctions.select(expression, object) != null);
    }

    public static Boolean nodeExistsAndHasChildNodes(String expression, XmlObject object) {
        XmlObject node = XmlFunctions.select(expression, object);
        return (node != null && node.getDomNode().hasChildNodes());
    }

    public static List<XmlObject> selectAll(String query, XmlObject xmlObject) {
        List<XmlObject> results = new ArrayList<>();
        XmlCursor cursor = xmlObject.newCursor();
        cursor.selectPath(xpathNSDeclaration + query);
        while (cursor.toNextSelection()) {
            results.add(cursor.getObject());
        }
        cursor.dispose();
        return results;
    }

    public static XmlObject select(String query, XmlObject xmlObject) {
        XmlCursor cursor = xmlObject.newCursor();
        cursor.selectPath(xpathNSDeclaration + query);
        XmlObject result = cursor.toNextSelection() ? cursor.getObject() : null;
        cursor.dispose();
        return result;
    }

}
