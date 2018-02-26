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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SourceMappingTest extends MappingTestBase {

    private SourceMapping sourceMapping;

    @Before
    public void setup() {
        sourceMapping = new SourceMapping();
    }

    @Test
    public void Maps_source_reference_to_MODS() {
        String sourceRef = "Netzwerk Bibliothek : Tagungsband zum 95. Deutschen Bibliothekartag";
        opus.setSource(sourceRef);

        sourceMapping.mapSource(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(String.format(
                "//mods:relatedItem[@type='original']/mods:note[@type='z' and text()='%s']", sourceRef), mods);
    }

    @Test
    public void Maps_ISSN_identifier_to_source_reference() {
        String issn = "1662-453X";
        opus.addNewIdentifierIssn().setValue(issn);
        opus.setType("article");

        sourceMapping.mapISSN(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                "//mods:relatedItem[@type='original']/mods:identifier[@type='issn' and text()='" + issn + "']",
                mods);
    }

    @Test
    public void Dont_map_ISSN_identifier_if_document_type_is_not_article() {
        String issn = "1662-453X";
        opus.addNewIdentifierIssn().setValue(issn);
        opus.setType("not-article");

        sourceMapping.mapISSN(opus, mods, changeLog);

        assertFalse("Changelog should be empty", changeLog.hasChanges());
        assertXpathNotExists(
                "//mods:relatedItem[@type='original']/mods:identifier[@type='issn' and text()='" + issn + "']",
                mods);
    }

}
