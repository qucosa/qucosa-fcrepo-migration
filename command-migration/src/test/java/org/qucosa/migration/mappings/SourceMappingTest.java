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

import org.junit.Before;
import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.junit.Assert.assertTrue;

public class SourceMappingTest extends MappingTestBase {

    private SourceMapping sourceMapping;

    @Before
    public void setup() {
        sourceMapping = new SourceMapping();
    }

    @Test
    public void Maps_source_reference_to_MODS() throws Exception {
        String sourceRef = "Netzwerk Bibliothek : Tagungsband zum 95. Deutschen Bibliothekartag";
        opus.setSource(sourceRef);

        boolean result = sourceMapping.mapSource(opus, mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists(String.format(
                "//mods:relatedItem[@type='original']/mods:note[@type='z' and text()='%s']", sourceRef),
                mods.getDomNode().getOwnerDocument());
    }

}
