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

public class IdentifierMappingTest extends MappingTestBase {

    private IdentifierMapping identifierMapping;

    @Before
    public void setup() {
        identifierMapping = new IdentifierMapping();
    }

    @Test
    public void extractsIsbn() {
        String isbn = "978-3-8439-2186-2";
        opus.addNewIdentifierIsbn().setValue(isbn);

        identifierMapping.mapIdentifiers(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:identifier[@type='isbn' and text()='" + isbn + "']", mods);
    }

    @Test
    public void extractsUrn() {
        String urn = "urn:nbn:de:bsz:14-ds-1229936868096-20917";
        opus.addNewIdentifierUrn().setValue(urn);

        identifierMapping.mapIdentifiers(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:identifier[@type='qucosa:urn' and text()='" + urn + "']", mods);
    }

    @Test
    public void extracts_Qucosa_Urn() {
        String urn = "urn:nbn:de:bsz:14-qucosa-172331";
        opus.addNewIdentifierUrn().setValue(urn);

        identifierMapping.mapIdentifiers(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:identifier[@type='qucosa:urn' and text()='" + urn + "']", mods);
    }

    @Test
    public void extractsDoi() {
        String doi = "10.3389/fnins.2015.00227";
        opus.addNewIdentifierDoi().setValue(doi);

        identifierMapping.mapIdentifiers(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathNotExists("/mods:mods/mods:identifier[@type='doi' and text()='" + doi + "']", mods);
        assertXpathExists(
                "/mods:mods/mods:relatedItem[@type='otherVersion']/mods:identifier[@type='doi' and text()='" + doi + "']",
                mods);
    }

    @Test
    public void extractsIssn() {
        String issn = "1662-453X";
        opus.addNewIdentifierIssn().setValue(issn);

        identifierMapping.mapIdentifiers(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:identifier[@type='issn' and text()='" + issn + "']", mods);
    }

    @Test
    public void Extract_ISSN_only_if_document_type_is_not_article() {
        String issn = "1662-453X";
        opus.addNewIdentifierIssn().setValue(issn);
        opus.setType("article");

        identifierMapping.mapIdentifiers(opus, mods, changeLog);

        assertFalse("Changelog should be empty", changeLog.hasChanges());
        assertXpathNotExists("//mods:identifier[@type='issn']", mods);
    }

    @Test
    public void extractsPpn() {
        String ppn = "303072784";
        opus.addNewIdentifierPpn().setValue(ppn);

        identifierMapping.mapIdentifiers(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:identifier[@type='swb-ppn' and text()='" + ppn + "']", mods);
    }

    @Test
    public void Filters_whitespace_from_identifier() {
        String doi = "10.1371/ journal.pone.0137353";
        opus.addNewIdentifierDoi().setValue(doi);

        identifierMapping.mapIdentifiers(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(String.format(
                "//mods:identifier[@type='doi' and text()='%s']", doi.replaceAll(" ", "")),
                mods);
    }

}
