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

package org.qucosa.migration.org.qucosa.migration.xml;

import org.apache.xmlbeans.XmlObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.formatXPath;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.select;
import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;

public class XmlFunctionsTest {

    @Test
    public void Inserts_simple_node_into_target_node() throws Exception {
        XmlObject target = XmlObject.Factory.parse("<A/>");
        XmlObject source = XmlObject.Factory.parse("<B/>");
        XmlFunctions.insertNode(source, target);
        assertThat(target.xmlText(), hasXPath("/A/B"));
    }

    @Test
    public void Inserts_simple_node_into_target_node_with_attributes() throws Exception {
        XmlObject target = XmlObject.Factory.parse("<A attr='foo'><B attr='bar'>text</B></A>");
        XmlObject source = XmlObject.Factory.parse("<C/>");
        XmlFunctions.insertNode(source, target);
        assertThat(target.xmlText(), hasXPath("/A/B"));
    }

    @Test
    public void Inserts_simple_node_with_attribute_into_target_node() throws Exception {
        XmlObject target = XmlObject.Factory.parse("<A/>");
        XmlObject source = XmlObject.Factory.parse("<B attr='foo'/>");
        XmlFunctions.insertNode(source, target);
        assertThat(target.xmlText(), hasXPath("/A/B"));
    }

    @Test
    public void Inserts_multiple_simple_nodes_into_target_node() throws Exception {
        XmlObject target = XmlObject.Factory.parse("<A/>");
        XmlObject source1 = XmlObject.Factory.parse("<B/>");
        XmlObject source2 = XmlObject.Factory.parse("<C/>");
        XmlFunctions.insertNode(source1, target);
        XmlFunctions.insertNode(source2, target);
        assertThat(target.xmlText(), hasXPath("/A/B"));
        assertThat(target.xmlText(), hasXPath("/A/B"));
    }

    @Test
    public void Inserts_simple_nodes_into_selected_target_node() throws Exception {
        XmlObject xml = XmlObject.Factory.parse("<A><B/></A>");
        XmlObject source = XmlObject.Factory.parse("<C/>");
        XmlObject target = select("//B", xml);
        XmlFunctions.insertNode(source, target);
        assertThat(xml.xmlText(), hasXPath("/A/B"));
    }

    @Test
    public void Inserts_simple_nodes_into_selected_parent_node() throws Exception {
        XmlObject xml = XmlObject.Factory.parse("<A><B><C/></B></A>");
        XmlObject source = XmlObject.Factory.parse("<D/>");
        XmlObject target = select("//B", xml);
        XmlFunctions.insertNode(source, target);
        assertThat(xml.xmlText(), hasXPath("/A/B"));
    }

    @Test
    public void Inserts_complex_nodes_into_target_node() throws Exception {
        XmlObject target = XmlObject.Factory.parse("<A/>");
        XmlObject source = XmlObject.Factory.parse("<B><C/></B>");
        XmlFunctions.insertNode(source, target);
        assertThat(target.xmlText(), hasXPath("/A/B"));
    }

    @Test
    public void Inserts_multiple_complex_nodes_into_target_node() throws Exception {
        XmlObject target = XmlObject.Factory.parse("<A/>");
        XmlObject source1 = XmlObject.Factory.parse("<B><C/></B>");
        XmlObject source2 = XmlObject.Factory.parse("<D><E/></D>");
        XmlFunctions.insertNode(source1, target);
        XmlFunctions.insertNode(source2, target);
        assertThat(target.xmlText(), hasXPath("/A/B"));
        assertThat(target.xmlText(), hasXPath("/A/B"));
    }

    @Test
    public void Selects_element_via_xpath() throws Exception {
        XmlObject xml = XmlObject.Factory.parse("<B><C/></B>");
        XmlObject selection = select("//C", xml);
        assertEquals("C", selection.getDomNode().getLocalName());
    }

    @Test
    public void Formatting_XPath_escapes_apostroph_characters() {
        String v = "That's something";
        String result = formatXPath("//some/element[@a=%d and b='%s']", 1234, v);
        assertEquals("//some/element[@a=1234 and b='That''s something']", result);
    }

}
