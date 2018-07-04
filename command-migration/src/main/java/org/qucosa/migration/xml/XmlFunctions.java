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

package org.qucosa.migration.xml;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.qucosa.migration.mappings.Namespaces.NS_FOAF;
import static org.qucosa.migration.mappings.Namespaces.NS_MODS_V3;
import static org.qucosa.migration.mappings.Namespaces.NS_PERSON;
import static org.qucosa.migration.mappings.Namespaces.NS_SLUB;
import static org.qucosa.migration.mappings.Namespaces.NS_XLINK;
import static org.qucosa.migration.stringfilters.TextInputStringFilters.SINGLE_QUOTE_Filter;

public class XmlFunctions {

    private static final String xpathNSDeclaration =
            "declare namespace mods='" + NS_MODS_V3 + "'; " +
                    "declare namespace slub='" + NS_SLUB + "'; " +
                    "declare namespace foaf='" + NS_FOAF + "'; " +
                    "declare namespace xlink='" + NS_XLINK + "'; " +
                    "declare namespace person='" + NS_PERSON + "'; ";

    public static String formatXPath(String xpath, Object... values) {
        return String.format(xpath,
                Arrays.stream(values)
                        .map(o -> (o instanceof String) ? SINGLE_QUOTE_Filter.apply((String) o) : o)
                        .toArray());
    }

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

    public static void insertNode(XmlObject source, XmlObject target) {
        XmlCursor targetCursor = target.newCursor();
        XmlCursor sourceCursor = source.newCursor();
        if (target.getDomNode().getOwnerDocument() == null) {
            // when target is XML-fragment, move to first child
            targetCursor.toFirstChild();
        }
        targetCursor.toEndToken();
        sourceCursor.toFirstChild();
        sourceCursor.copyXml(targetCursor);
    }

}
