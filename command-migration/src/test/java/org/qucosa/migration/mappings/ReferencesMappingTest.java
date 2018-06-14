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

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReferencesMappingTest extends MappingTestBase {

    private ReferencesMapping referencesMapping;

    @Before
    public void setup() {
        referencesMapping = new ReferencesMapping();
    }

    @Test
    public void Generates_no_relatedItems_when_no_references_exist() {
        referencesMapping.mapHostAndPredecessorReferences(opus, mods, changeLog);
        referencesMapping.mapExternalReferenceElements(opus.getReferenceUrlArray(), "url", mods, changeLog);
        referencesMapping.mapSeriesReference(opus, mods, changeLog);
        assertFalse("Changelog should be empty", changeLog.hasChanges());
    }

    @Test
    public void Reference_to_parent_series_via_Qucosa_URN_generates_relatedItem_type_series() {
        Reference ru = opus.addNewReferenceUrn();
        String referenceToSeries = "urn:nbn:de:bsz:14-qucosa-38419";
        ru.setValue(referenceToSeries);
        ru.setRelation("series");

        referencesMapping.mapSeriesReference(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:relatedItem[@type='series']/mods:identifier[@type='urn' and text()='" + referenceToSeries + "']",
                mods);
    }

    @Test
    public void Reference_to_parent_series_via_Qucosa_URN_extracts_sorting_key() {
        Reference ru = opus.addNewReferenceUrn();
        String referenceToSeries = "urn:nbn:de:bsz:14-qucosa-38419";
        ru.setValue(referenceToSeries);
        ru.setRelation("series");
        String order = "201009";
        ru.setSortOrder(order);

        referencesMapping.mapSeriesReference(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                "/mods:mods/mods:relatedItem[@type='series']/mods:extension/slub:info[slub:sortingKey='" + order + "']",
                mods);
    }

    @Test
    public void Reference_to_parent_series_via_Qucosa_URN_extracts_volume_information() {
        Title tp = opus.addNewTitleParent();
        String volumeInfo = "Heft 9/2010";
        tp.setValue("Schriftenreihe des Landesamtes für Umwelt, Landwirtschaft und Geologie ; " + volumeInfo);

        Reference ru = opus.addNewReferenceUrn();
        ru.setRelation("series");

        referencesMapping.mapSeriesReference(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("/mods:mods/mods:part[@type='volume']/mods:detail/mods:number[text()='" + volumeInfo + "']",
                mods);
    }

    @Test
    public void Reference_to_parent_series_without_Qucosa_URN_copies_title() {
        Title tp = opus.addNewTitleParent();
        String volumeInfo = "Heft 9/2010";
        String volumeTitle = "Schriftenreihe des Landesamtes für Umwelt, Landwirtschaft und Geologie";
        tp.setValue(volumeTitle + " ; " + volumeInfo);

        Reference ru = opus.addNewReferenceUrn();
        ru.setRelation("series");

        referencesMapping.mapSeriesReference(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:relatedItem[@type='series']/mods:titleInfo/mods:title[text()='" + volumeTitle + "']",
                mods);
    }

    @Test
    public void Reference_to_parent_series_without_URN_extracts_title_and_volume() {
        Title tp = opus.addNewTitleParent();
        String volumeInfo = "Heft 9/2010";
        String volumeTitle = "Schriftenreihe des Landesamtes für Umwelt, Landwirtschaft und Geologie";
        tp.setValue(volumeTitle + " ; " + volumeInfo);

        referencesMapping.mapSeriesReference(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:relatedItem[@type='series']/mods:titleInfo/mods:title[text()='" + volumeTitle + "']",
                mods);
        assertXpathNotExists("//mods:relatedItem/mods:identifier", mods);
        assertXpathExists("/mods:mods/mods:part[@type='volume']/mods:detail/mods:number[text()='" + volumeInfo + "']",
                mods);
    }

    @Test
    public void ReferenceIsbn_mapped_to_relatedItem() {
        final String value = "978-989-95079-6-8";
        Reference refUrl = opus.addNewReferenceIsbn();
        refUrl.setValue(value);

        referencesMapping.mapExternalReferenceElements(opus.getReferenceIsbnArray(), "isbn", mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathNotExists("//mods:relatedItem[@type='series']", mods);
        assertXpathExists("//mods:relatedItem[@type='otherVersion']" +
                "/mods:identifier[@type='isbn' and text()='" + value + "']", mods);
    }

    @Test
    public void ReferenceIssn_mapped_to_relatedItem() {
        final String value = "0340-2444";
        Reference refUrl = opus.addNewReferenceIssn();
        refUrl.setValue(value);

        referencesMapping.mapExternalReferenceElements(opus.getReferenceIssnArray(), "issn", mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:relatedItem[@type='otherVersion']" +
                "/mods:identifier[@type='issn' and text()='" + value + "']", mods);
    }

    @Test
    public void ReferenceUrl_mapped_to_relatedItem_with_location() {
        final String value = "http://dx.doi.org/10.13141/jve.vol5.no1.pp1-7";
        final String label = "Der Artikel ist zuerst in der Open Access-Zeitschrift \"Journal of Vietnamese Environment\" erschienen.";
        Reference refUrl = opus.addNewReferenceUrl();
        refUrl.setValue(value);
        refUrl.setLabel(label);

        referencesMapping.mapExternalReferenceElements(opus.getReferenceUrlArray(), "url", mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:relatedItem[@type='otherVersion']/mods:note[text()='" + label + "']", mods);
        assertXpathExists("//mods:relatedItem[@type='otherVersion']/mods:location/mods:url[text()='" + value + "']",
                mods);
    }

    @Test
    public void Multiple_ReferenceUrl_mapped_to_separate_relatedItem_with_location() {
        Reference r1 = opus.addNewReferenceUrl();
        r1.setLabel("L1");
        r1.setValue("http://L1");
        Reference r2 = opus.addNewReferenceUrl();
        r2.setLabel("L2");
        r2.setValue("http://L2");

        referencesMapping.mapExternalReferenceElements(opus.getReferenceUrlArray(), "url", mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:relatedItem[@type='otherVersion'" +
                " and mods:note='L1'" +
                " and mods:location/mods:url='http://L1']", mods);
        assertXpathExists("//mods:relatedItem[@type='otherVersion'" +
                " and mods:note='L2'" +
                " and mods:location/mods:url='http://L2']", mods);
    }

    @Test
    public void Reference_to_journal_mapped_to_relatedItem_and_part() {
        String urn = "urn:nbn:de:bsz:ch1-qucosa-62094";
        String sortOrder = "20031";
        String issue = "Jg. 6. 2013, H. 1";

        opus.setIssue(issue);
        Reference refUrl = opus.addNewReferenceUrn();
        refUrl.setValue(urn);
        refUrl.setRelation("journal");
        refUrl.setSortOrder(sortOrder);

        referencesMapping.mapHostAndPredecessorReferences(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                format("//mods:relatedItem[@type='host']/mods:identifier[@type='urn' and text()='%s']", urn),
                mods);
        assertXpathExists(
                format("//mods:relatedItem[@type='host']/mods:extension/slub:info[slub:sortingKey='%s']", sortOrder),
                mods);
        assertXpathExists(
                format("/mods:mods/mods:part[@type='issue']/mods:detail/mods:number[text()='%s']", issue),
                mods);
    }

    @Test
    public void Reference_to_book_mapped_to_relatedItem_with_sorting_key() {
        String urn = "urn:nbn:de:bsz:14-qucosa-23464";
        String sortOrder = "095";

        Reference refUrl = opus.addNewReferenceUrn();
        refUrl.setValue(urn);
        refUrl.setRelation("book");
        refUrl.setSortOrder(sortOrder);

        referencesMapping.mapHostAndPredecessorReferences(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                format("//mods:relatedItem[@type='host']/mods:identifier[@type='urn' and text()='%s']", urn), mods);
        assertXpathExists(
                format("//mods:relatedItem[@type='host']/mods:extension/slub:info[slub:sortingKey='%s']", sortOrder), mods);
    }

    @Test
    public void Reference_to_proceeding_mapped_to_relatedItem_with_sorting_key() {
        String urn = "urn:nbn:de:bsz:14-qucosa-151820";
        String sortOrder = "061";

        Reference refUrl = opus.addNewReferenceUrn();
        refUrl.setValue(urn);
        refUrl.setRelation("proceeding");
        refUrl.setSortOrder(sortOrder);

        referencesMapping.mapHostAndPredecessorReferences(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                format("//mods:relatedItem[@type='host']/mods:identifier[@type='urn' and text()='%s']", urn), mods);
        assertXpathExists(
                format("//mods:relatedItem[@type='host']/mods:extension/slub:info[slub:sortingKey='%s']", sortOrder), mods);
    }

    @Test
    public void Reference_to_issue_mapped_to_relatedItem_with_sorting_key() {
        String urn = "urn:nbn:de:bsz:14-ds-1206548602741-00127";
        String sortOrder = "008";

        Reference refUrl = opus.addNewReferenceUrn();
        refUrl.setValue(urn);
        refUrl.setRelation("issue");
        refUrl.setSortOrder(sortOrder);

        referencesMapping.mapHostAndPredecessorReferences(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                format("//mods:relatedItem[@type='host']/mods:identifier[@type='urn' and text()='%s']", urn), mods);
        assertXpathExists(
                format("//mods:relatedItem[@type='host']/mods:extension/slub:info[slub:sortingKey='%s']", sortOrder), mods);
    }

    @Test
    public void Reference_to_predecessor_mapped_to_relatedItem() {
        String urn = "urn:nbn:de:bsz:14-qucosa-74328";

        Reference refUrl = opus.addNewReferenceUrn();
        refUrl.setValue(urn);
        refUrl.setRelation("predecessor");

        referencesMapping.mapHostAndPredecessorReferences(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathNotExists("//mods:relatedItem[@type='host']", mods);
        assertXpathNotExists("//mods:relatedItem[@type='series']", mods);
        assertXpathExists(
                format("//mods:relatedItem[@type='preceding']/mods:identifier[@type='urn' and text()='%s']", urn),
                mods);
    }

    @Test
    public void No_series_mapping_if_reference_is_not_of_type_series() {
        String urn = "urn:nbn:de:bsz:14-qucosa-74328";

        Reference refUrn = opus.addNewReferenceUrn();
        refUrn.setValue(urn);
        refUrn.setRelation("proceeding");
        refUrn.setLabel("isPartOf");

        referencesMapping.mapSeriesReference(opus, mods, changeLog);
        referencesMapping.mapHostAndPredecessorReferences(opus, mods, changeLog);

        assertXpathNotExists("//mods:relatedItem[@type='series']", mods);
        assertXpathExists("//mods:relatedItem[@type='host']", mods);
    }

}
