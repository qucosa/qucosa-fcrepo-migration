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

import noNamespace.Reference;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

public class RelationInfoProcessorTest extends ProcessorTestBase {

    final private MappingProcessor processor = new RelationInfoProcessor();

    @Test
    public void mapSeriesReferenceToSeriesRelatedItem() throws Exception {
        final String urn = "urn:nbn:de:bsz:14-qucosa-38419";
        final String link = "http://nbn-resolving.de/" + urn;
        final String label = "Link zur Schriftenreihe";
        final String sortOrder = "201009";
        createReferenceUrn(urn, label, "series", sortOrder);

        runProcessor(processor);

        final Document ownerDocument = mods.getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:relatedItem[" +
                "@type='series'" +
                " and @displayLabel='" + label + "'" +
                " and @xlink:href='" + link + "' ]", ownerDocument);
        XMLAssert.assertXpathExists("//mods:relatedItem/mods:identifier[@type='urn'" +
                " and text()='" + urn + "']", ownerDocument);
        XMLAssert.assertXpathExists(
                String.format("//mods:relatedItem/mods:part/mods:detail[mods:number='%s']", sortOrder),
                ownerDocument);
    }

    @Test
    public void mapJournalReferenceToHostRelatedItem() throws Exception {
        final String urn = "urn:nbn:de:bsz:ch1-qucosa-62094";
        final String link = "http://nbn-resolving.de/" + urn;
        final String sortOrder = "20031";
        createReferenceUrn(urn, null, "journal", sortOrder);

        runProcessor(processor);

        final Document ownerDocument = mods.getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:relatedItem[" +
                "@type='host'" +
                " and @xlink:href='" + link + "' ]", ownerDocument);
        XMLAssert.assertXpathExists("//mods:relatedItem/mods:identifier[@type='urn'" +
                " and text()='" + urn + "']", ownerDocument);
        XMLAssert.assertXpathExists(
                String.format("//mods:relatedItem/mods:part/mods:detail[mods:number='%s']", sortOrder),
                ownerDocument);
    }

    @Test
    public void mapProceedingReferenceToHostRelatedItem() throws Exception {
        final String urn = "urn:nbn:de:swb:ch1-200300619";
        final String label = "isPartOf";
        final String link = "http://nbn-resolving.de/" + urn;
        createReferenceUrn(urn, label, "proceeding", null);

        runProcessor(processor);

        final Document ownerDocument = mods.getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:relatedItem[" +
                "@type='host'" +
                " and @xlink:href='" + link + "' ]", ownerDocument);
        XMLAssert.assertXpathExists("//mods:relatedItem/mods:identifier[@type='urn'" +
                " and text()='" + urn + "']", ownerDocument);
    }

    @Test
    public void mapIssueReferenceToHostRelatedItem() throws Exception {
        final String urn = "urn:nbn:de:bsz:14-qucosa-32825";
        final String link = "http://nbn-resolving.de/" + urn;
        final String sortOrder = "001";
        createReferenceUrn(urn, null, "issue", sortOrder);

        runProcessor(processor);

        final Document ownerDocument = mods.getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:relatedItem[" +
                "@type='host'" +
                " and @xlink:href='" + link + "' ]", ownerDocument);
        XMLAssert.assertXpathExists("//mods:relatedItem/mods:identifier[@type='urn'" +
                " and text()='" + urn + "']", ownerDocument);
        XMLAssert.assertXpathExists(
                String.format("//mods:relatedItem/mods:part/mods:detail[mods:number='%s']", sortOrder),
                ownerDocument);
    }

    @Test
    public void mapPredecessorReferenceToPrecedingRelatedItem() throws Exception {
        final String urn = "urn:nbn:de:bsz:14-qucosa-25559";
        final String link = "http://nbn-resolving.de/" + urn;
        createReferenceUrn(urn, null, "predecessor", null);

        runProcessor(processor);

        final Document ownerDocument = mods.getDomNode().getOwnerDocument();
        XMLAssert.assertXpathExists("//mods:relatedItem[" +
                "@type='preceding'" +
                " and @xlink:href='" + link + "' ]", ownerDocument);
        XMLAssert.assertXpathExists("//mods:relatedItem/mods:identifier[@type='urn'" +
                " and text()='" + urn + "']", ownerDocument);
    }

    @Test
    public void extractsReferenceUrl() throws Exception {
        final String value = "http://dx.doi.org/10.13141/jve.vol5.no1.pp1-7";
        final String label = "Der Artikel ist zuerst in der Open Access-Zeitschrift \"Journal of Vietnamese Environment\" erschienen.";
        Reference refUrl = opus.addNewReferenceUrl();
        refUrl.setValue(value);
        refUrl.setLabel(label);

        runProcessor(processor);

        final Document ownerDocument = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:relatedItem[@type='original' and @displayLabel='" + label + "']", ownerDocument);
        assertXpathExists("//mods:relatedItem/mods:identifier[@type='uri' and text()='" + value + "']", ownerDocument);
        assertXpathExists("//mods:relatedItem/mods:identifier[@type='uri' and text()='" + value + "']", ownerDocument);
    }

    @Test
    public void extractsReferenceIsbn() throws Exception {
        final String value = "978-989-95079-6-8";
        Reference refUrl = opus.addNewReferenceIsbn();
        refUrl.setValue(value);

        runProcessor(processor);

        final Document document = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:relatedItem[@type='original']", document);
        assertXpathExists("//mods:relatedItem/mods:identifier[@type='isbn' and text()='" + value + "']", document);
    }

    @Test
    public void extractsReferenceIssn() throws Exception {
        final String value = "0340-2444";
        final String label = "Some label";
        Reference refUrl = opus.addNewReferenceIssn();
        refUrl.setValue(value);
        refUrl.setLabel(label);

        runProcessor(processor);

        final Document document = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:relatedItem[@type='original' and @displayLabel='" + label + "']", document);
        assertXpathExists("//mods:relatedItem/mods:identifier[@type='issn' and text()='" + value + "']", document);
    }

    private void createReferenceUrn(String urn, String label, String relation, String sortOrder) {
        Reference refUrl = opus.addNewReferenceUrn();
        refUrl.setValue(urn);
        refUrl.setLabel(label);
        refUrl.setRelation(relation);
        refUrl.setSortOrder(sortOrder);
    }

}
