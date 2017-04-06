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
import de.slubDresden.InfoType;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.Document;
import noNamespace.OpusDocument;
import org.apache.xmlbeans.XmlException;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;

import java.io.IOException;
import java.util.HashMap;

import static org.qucosa.migration.mappings.Namespaces.NS_FOAF;
import static org.qucosa.migration.mappings.Namespaces.NS_MODS_V3;
import static org.qucosa.migration.mappings.Namespaces.NS_RDF;
import static org.qucosa.migration.mappings.Namespaces.NS_SLUB;
import static org.qucosa.migration.mappings.Namespaces.NS_XLINK;

public class ProcessorTestBase {

    static {
        XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(
                new HashMap<String, String>() {{
                    put("mods", NS_MODS_V3);
                    put("slub", NS_SLUB);
                    put("foaf", NS_FOAF);
                    put("rdf", NS_RDF);
                    put("xlink", NS_XLINK);
                }}));
    }

    protected ModsDefinition mods;
    protected Document opus;
    InfoType info;

    @Before
    public void setupBasisDatastreams() throws IOException, XmlException {
        mods = ModsDocument.Factory.newInstance().addNewMods();
        opus = OpusDocument.Factory.newInstance().addNewOpus().addNewOpusDocument();
        info = InfoDocument.Factory.newInstance().addNewInfo();
    }

    void runProcessor(MappingProcessor processor) throws Exception {
        processor.process(opus, mods, info);
    }
}
