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

import noNamespace.Reference;
import noNamespace.Title;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import static java.lang.String.format;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReferencesMappingTest extends MappingTestBase {

    private ReferencesMapping referencesMapping;

    @Before
    public void setup() {
        referencesMapping = new ReferencesMapping();
    }

    @Test
    public void Generates_no_relatedItems_when_no_references_exist() throws Exception {
        referencesMapping.mapHostAndPredecessorReferences(opus, mods, changeLog);
        referencesMapping.mapExternalReferenceElements(opus.getReferenceUrlArray(), "url", mods, changeLog);
        referencesMapping.mapSeriesReference(opus, mods, changeLog);
        assertFalse("Changelog should be empty", changeLog.hasChanges());
    }

    @Test
    public void Reference_to_parent_series_via_Qucosa_URN_generates_relatedItem_type_series() throws Exception {
        Reference ru = opus.addNewReferenceUrn();
        String referenceToSeries = "urn:nbn:de:bsz:14-qucosa-38419";
        ru.setValue(referenceToSeries);
        ru.setRelation("series");

        referencesMapping.mapSeriesReference(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:relatedItem[@type='series']/mods:identifier[@type='urn' and text()='" + referenceToSeries + "']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void Reference_to_parent_series_via_Qucosa_URN_extracts_part_order_number() throws Exception {
        Reference ru = opus.addNewReferenceUrn();
        String referenceToSeries = "urn:nbn:de:bsz:14-qucosa-38419";
        ru.setValue(referenceToSeries);
        ru.setRelation("series");
        String order = "201009";
        ru.setSortOrder(order);

        referencesMapping.mapSeriesReference(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("/mods:mods/mods:part[@order='" + order + "' and @type='volume']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void Reference_to_parent_series_via_Qucosa_URN_extracts_volume_information() throws Exception {
        Title tp = opus.addNewTitleParent();
        String volumeInfo = "Heft 9/2010";
        tp.setValue("Schriftenreihe des Landesamtes für Umwelt, Landwirtschaft und Geologie ; " + volumeInfo);

        Reference ru = opus.addNewReferenceUrn();
        ru.setRelation("series");

        referencesMapping.mapSeriesReference(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("/mods:mods/mods:part[@type='volume']/mods:detail/mods:number[text()='" + volumeInfo + "']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void Reference_to_parent_series_without_Qucosa_URN_copies_title() throws Exception {
        Title tp = opus.addNewTitleParent();
        String volumeInfo = "Heft 9/2010";
        String volumeTitle = "Schriftenreihe des Landesamtes für Umwelt, Landwirtschaft und Geologie";
        tp.setValue(volumeTitle + " ; " + volumeInfo);

        Reference ru = opus.addNewReferenceUrn();
        ru.setRelation("series");

        referencesMapping.mapSeriesReference(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:relatedItem[@type='series']/mods:titleInfo/mods:title[text()='" + volumeTitle + "']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void Reference_to_parent_series_without_URN_extracts_title_and_volume() throws Exception {
        Title tp = opus.addNewTitleParent();
        String volumeInfo = "Heft 9/2010";
        String volumeTitle = "Schriftenreihe des Landesamtes für Umwelt, Landwirtschaft und Geologie";
        tp.setValue(volumeTitle + " ; " + volumeInfo);

        referencesMapping.mapSeriesReference(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        Document ownerDocument = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:relatedItem[@type='series']/mods:titleInfo/mods:title[text()='" + volumeTitle + "']",
                ownerDocument);
        assertXpathNotExists("//mods:relatedItem/mods:identifier", ownerDocument);
        assertXpathExists("/mods:mods/mods:part[@type='volume']/mods:detail/mods:number[text()='" + volumeInfo + "']",
                ownerDocument);
    }

    @Test
    public void ReferenceIsbn_mapped_to_relatedItem() throws Exception {
        final String value = "978-989-95079-6-8";
        Reference refUrl = opus.addNewReferenceIsbn();
        refUrl.setValue(value);

        referencesMapping.mapExternalReferenceElements(opus.getReferenceIsbnArray(), "isbn", mods, changeLog);

        final Document document = mods.getDomNode().getOwnerDocument();
        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathNotExists("//mods:relatedItem[@type='series']", mods.getDomNode().getOwnerDocument());
        assertXpathExists("//mods:relatedItem[@type='otherVersion']" +
                "/mods:identifier[@type='isbn' and text()='" + value + "']", document);
    }

    @Test
    public void ReferenceIssn_mapped_to_relatedItem() throws Exception {
        final String value = "0340-2444";
        Reference refUrl = opus.addNewReferenceIssn();
        refUrl.setValue(value);

        referencesMapping.mapExternalReferenceElements(opus.getReferenceIssnArray(), "issn", mods, changeLog);

        final Document document = mods.getDomNode().getOwnerDocument();
        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:relatedItem[@type='otherVersion']" +
                "/mods:identifier[@type='issn' and text()='" + value + "']", document);
    }

    @Test
    public void ReferenceUrl_mapped_to_relatedItem_with_location() throws Exception {
        final String value = "http://dx.doi.org/10.13141/jve.vol5.no1.pp1-7";
        final String label = "Der Artikel ist zuerst in der Open Access-Zeitschrift \"Journal of Vietnamese Environment\" erschienen.";
        Reference refUrl = opus.addNewReferenceUrl();
        refUrl.setValue(value);
        refUrl.setLabel(label);

        referencesMapping.mapExternalReferenceElements(opus.getReferenceUrlArray(), "url", mods, changeLog);

        final Document ownerDocument = mods.getDomNode().getOwnerDocument();
        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:relatedItem[@type='otherVersion']/mods:note[text()='" + label + "']", ownerDocument);
        assertXpathExists("//mods:relatedItem[@type='otherVersion']/mods:location/mods:url[text()='" + value + "']",
                ownerDocument);
    }

    @Test
    public void Multiple_ReferenceUrl_mapped_to_separate_relatedItem_with_location() throws Exception {
        Reference r1 = opus.addNewReferenceUrl();
        r1.setLabel("L1");
        r1.setValue("http://L1");
        Reference r2 = opus.addNewReferenceUrl();
        r2.setLabel("L2");
        r2.setValue("http://L2");

        referencesMapping.mapExternalReferenceElements(opus.getReferenceUrlArray(), "url", mods, changeLog);

        final Document ownerDocument = mods.getDomNode().getOwnerDocument();
        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:relatedItem[@type='otherVersion'" +
                " and mods:note='L1'" +
                " and mods:location/mods:url='http://L1']", ownerDocument);
        assertXpathExists("//mods:relatedItem[@type='otherVersion'" +
                " and mods:note='L2'" +
                " and mods:location/mods:url='http://L2']", ownerDocument);
    }

    @Test
    public void Reference_to_journal_mapped_to_relatedItem_and_part() throws Exception {
        String urn = "urn:nbn:de:bsz:ch1-qucosa-62094";
        String sortOrder = "20031";
        String issue = "Jg. 6. 2013, H. 1";

        opus.setIssue(issue);
        Reference refUrl = opus.addNewReferenceUrn();
        refUrl.setValue(urn);
        refUrl.setRelation("journal");
        refUrl.setSortOrder(sortOrder);

        referencesMapping.mapHostAndPredecessorReferences(opus, mods, changeLog);

        final Document ownerDocument = mods.getDomNode().getOwnerDocument();
        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                format("//mods:relatedItem[@type='host']/mods:identifier[@type='urn' and text()='%s']", urn),
                ownerDocument);
        assertXpathExists(
                format("/mods:mods/mods:part[@order='%s' and @type='issue']/mods:detail/mods:number[text()='%s']", sortOrder, issue),
                ownerDocument);
    }

    @Test
    public void Reference_to_book_mapped_to_relatedItem_and_part() throws Exception {
        String urn = "urn:nbn:de:bsz:14-qucosa-23464";
        String sortOrder = "095";
        String expectedSortOrder = "95";

        Reference refUrl = opus.addNewReferenceUrn();
        refUrl.setValue(urn);
        refUrl.setRelation("book");
        refUrl.setSortOrder(sortOrder);

        referencesMapping.mapHostAndPredecessorReferences(opus, mods, changeLog);

        final Document ownerDocument = mods.getDomNode().getOwnerDocument();
        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                format("//mods:relatedItem[@type='host']/mods:identifier[@type='urn' and text()='%s']", urn),
                ownerDocument);
        assertXpathExists(
                format("/mods:mods/mods:part[@order='%s' and not(@type)]", expectedSortOrder),
                ownerDocument);
    }

    @Test
    public void Reference_to_proceeding_mapped_to_relatedItem_and_part() throws Exception {
        String urn = "urn:nbn:de:bsz:14-qucosa-151820";
        String sortOrder = "061";
        String expectedSortOrder = "61";

        Reference refUrl = opus.addNewReferenceUrn();
        refUrl.setValue(urn);
        refUrl.setRelation("proceeding");
        refUrl.setSortOrder(sortOrder);

        referencesMapping.mapHostAndPredecessorReferences(opus, mods, changeLog);

        final Document ownerDocument = mods.getDomNode().getOwnerDocument();
        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                format("//mods:relatedItem[@type='host']/mods:identifier[@type='urn' and text()='%s']", urn),
                ownerDocument);
        assertXpathExists(
                format("/mods:mods/mods:part[@order='%s' and not(@type)]", expectedSortOrder),
                ownerDocument);
    }

    @Test
    public void Reference_to_issue_mapped_to_relatedItem_and_part() throws Exception {
        String urn = "urn:nbn:de:bsz:14-ds-1206548602741-00127";
        String sortOrder = "008";
        String expectedSortOrder = "8";

        Reference refUrl = opus.addNewReferenceUrn();
        refUrl.setValue(urn);
        refUrl.setRelation("issue");
        refUrl.setSortOrder(sortOrder);

        referencesMapping.mapHostAndPredecessorReferences(opus, mods, changeLog);

        final Document ownerDocument = mods.getDomNode().getOwnerDocument();
        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                format("//mods:relatedItem[@type='host']/mods:identifier[@type='urn' and text()='%s']", urn),
                ownerDocument);
        assertXpathExists(
                format("/mods:mods/mods:part[@order='%s' and not(@type)]", expectedSortOrder),
                ownerDocument);
    }

    @Test
    public void Reference_to_predecessor_mapped_to_relatedItem() throws Exception {
        String urn = "urn:nbn:de:bsz:14-qucosa-74328";

        Reference refUrl = opus.addNewReferenceUrn();
        refUrl.setValue(urn);
        refUrl.setRelation("predecessor");

        referencesMapping.mapHostAndPredecessorReferences(opus, mods, changeLog);

        final Document ownerDocument = mods.getDomNode().getOwnerDocument();
        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathNotExists("//mods:relatedItem[@type='host']", mods.getDomNode().getOwnerDocument());
        assertXpathNotExists("//mods:relatedItem[@type='series']", mods.getDomNode().getOwnerDocument());
        assertXpathExists(
                format("//mods:relatedItem[@type='preceding']/mods:identifier[@type='urn' and text()='%s']", urn),
                ownerDocument);
    }

    @Test
    public void No_series_mapping_if_reference_is_not_of_type_series() throws Exception {
        String urn = "urn:nbn:de:bsz:14-qucosa-74328";

        Reference refUrn = opus.addNewReferenceUrn();
        refUrn.setValue(urn);
        refUrn.setRelation("proceeding");
        refUrn.setLabel("isPartOf");

        referencesMapping.mapSeriesReference(opus, mods, changeLog);
        referencesMapping.mapHostAndPredecessorReferences(opus, mods, changeLog);

        assertXpathNotExists("//mods:relatedItem[@type='series']", mods.getDomNode().getOwnerDocument());
        assertXpathExists("//mods:relatedItem[@type='host']", mods.getDomNode().getOwnerDocument());
    }

}
