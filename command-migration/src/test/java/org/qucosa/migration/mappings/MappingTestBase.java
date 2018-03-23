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

import de.slubDresden.InfoDocument;
import de.slubDresden.InfoType;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.Document;
import noNamespace.OpusDocument;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.qucosa.migration.mappings.Namespaces.NS_FOAF;
import static org.qucosa.migration.mappings.Namespaces.NS_MODS_V3;
import static org.qucosa.migration.mappings.Namespaces.NS_PERSON;
import static org.qucosa.migration.mappings.Namespaces.NS_RDF;
import static org.qucosa.migration.mappings.Namespaces.NS_SLUB;
import static org.qucosa.migration.mappings.Namespaces.NS_XLINK;
import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;

abstract class MappingTestBase {

    private static final Map<String, String> namespaceContext = new HashMap<String, String>() {{
                put("mods", NS_MODS_V3);
                put("slub", NS_SLUB);
                put("foaf", NS_FOAF);
                put("rdf", NS_RDF);
                put("xlink", NS_XLINK);
                put("person", NS_PERSON);
    }};

    ModsDefinition mods;
    InfoType info;
    Document opus;
    ChangeLog changeLog;

    @Before
    public void setupModsDocument() {
        mods = ModsDocument.Factory.newInstance().addNewMods();
    }

    @Before
    public void setupSlubInfoDocument() {
        info = InfoDocument.Factory.newInstance().addNewInfo();
    }

    @Before
    public void setupOpusDocument() {
        opus = OpusDocument.Factory.newInstance().addNewOpus().addNewOpusDocument();
    }

    @Before
    public void setupChangeLog() {
        changeLog = new ChangeLog();
    }

    protected void assertXpathExists(String xpath, XmlObject xmlObject) {
        assertThat(xmlObject.getDomNode().getOwnerDocument(), hasXPath(xpath).withNamespaceContext(namespaceContext));
    }

    void assertXpathNotExists(String xpath, XmlObject xmlObject) {
        assertThat(xmlObject.getDomNode().getOwnerDocument(), not(hasXPath(xpath).withNamespaceContext(namespaceContext)));
    }
}
