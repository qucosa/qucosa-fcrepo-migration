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
import gov.loc.mods.v3.ModsDocument;
import noNamespace.OpusDocument;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;

import java.util.HashMap;

import static org.qucosa.migration.mappings.Namespaces.NS_FOAF;
import static org.qucosa.migration.mappings.Namespaces.NS_MODS_V3;
import static org.qucosa.migration.mappings.Namespaces.NS_RDF;
import static org.qucosa.migration.mappings.Namespaces.NS_SLUB;
import static org.qucosa.migration.mappings.Namespaces.NS_XLINK;

abstract class MappingTestBase {

    private static final SimpleNamespaceContext simpleNamespaceContext = new SimpleNamespaceContext(
            new HashMap<String, String>() {{
                put("mods", NS_MODS_V3);
                put("slub", NS_SLUB);
                put("foaf", NS_FOAF);
                put("rdf", NS_RDF);
                put("xlink", NS_XLINK);
            }});

    ModsDocument modsDocument;
    InfoDocument infoDocument;
    OpusDocument opusDocument;

    @Before
    public void setupModsDocument() {
        modsDocument = ModsDocument.Factory.newInstance();
        modsDocument.addNewMods();
    }

    @Before
    public void setupSlubInfoDocument() {
        infoDocument = InfoDocument.Factory.newInstance();
        infoDocument.addNewInfo();
    }

    @Before
    public void setupOpusDocument() {
        opusDocument = OpusDocument.Factory.newInstance();
        opusDocument.addNewOpus().addNewOpusDocument();
    }

    @Before
    public void setupXMLUnitNamespaceContext() {
        // This could be a @BeforeClass setup, but static functions cannot be called from derived test classes
        XMLUnit.setXpathNamespaceContext(simpleNamespaceContext);
    }
}
