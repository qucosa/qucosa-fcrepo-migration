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
import gov.loc.mods.v3.DetailDefinition;
import gov.loc.mods.v3.ExtensionDefinition;
import gov.loc.mods.v3.IdentifierDefinition;
import gov.loc.mods.v3.LocationDefinition;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.NoteDefinition;
import gov.loc.mods.v3.PartDefinition;
import gov.loc.mods.v3.RelatedItemDefinition;
import gov.loc.mods.v3.StringPlusLanguage;
import gov.loc.mods.v3.TitleInfoDefinition;
import gov.loc.mods.v3.UrlDefinition;
import noNamespace.Document;
import noNamespace.Reference;
import noNamespace.Title;

import java.util.ArrayList;

import static gov.loc.mods.v3.RelatedItemDefinition.Type.OTHER_VERSION;
import static gov.loc.mods.v3.RelatedItemDefinition.Type.SERIES;
import static org.qucosa.migration.mappings.ChangeLog.Type.MODS;
import static org.qucosa.migration.mappings.MappingFunctions.firstOf;
import static org.qucosa.migration.mappings.MappingFunctions.singleline;
import static org.qucosa.migration.mappings.MappingFunctions.volume;
import static org.qucosa.migration.mappings.MappingFunctions.volumeTitle;
import static org.qucosa.migration.xml.XmlFunctions.formatXPath;
import static org.qucosa.migration.xml.XmlFunctions.insertNode;
import static org.qucosa.migration.xml.XmlFunctions.select;

public class ReferencesMapping {

    public void mapSeriesReference(Document opus, ModsDefinition mods, ChangeLog changeLog) {
        // gathering data
        Title firstTitleParent = (Title) firstOf(opus.getTitleParentArray());

        String volume = null;
        String volumeTitle = null;
        if (firstTitleParent != null) {
            volume = volume(firstTitleParent.getValue());
            volumeTitle = volumeTitle(firstTitleParent.getValue());
        }

        // find first series reference
        Reference firstReferenceUrn = null;
        for (Reference ref : opus.getReferenceUrnArray()) {
            if (SERIES.toString().equals(ref.getRelation())) {
                firstReferenceUrn = ref;
                break;
            }
        }

        String referenceUrn = null;
        String referenceSortOrder = null;
        if (firstReferenceUrn != null) {
            referenceUrn = firstReferenceUrn.getValue();
            referenceSortOrder = firstReferenceUrn.getSortOrder();
        }

        // stop here, if there is no information to map
        if (volumeTitle == null && referenceUrn == null) {
            return;
        }

        // constructing relatedItem
        RelatedItemDefinition rid = (RelatedItemDefinition) select("mods:relatedItem[@type='series']", mods);
        if (rid == null) {
            rid = mods.addNewRelatedItem();
            rid.setType(SERIES);
            changeLog.log(MODS);
        }

        PartDefinition pd = (PartDefinition) select("mods:part[@type='volume']", mods);
        if (pd == null) {
            pd = mods.addNewPart();
            pd.setType("volume");
            changeLog.log(MODS);
        }

        mapIdentifier(referenceUrn, rid, "urn", changeLog);
        mapSortingKeyToRelatedItemExtension(referenceSortOrder, rid, changeLog);
        mapVolume(volume, pd, changeLog);
        mapVolumeTitle(volumeTitle, rid, changeLog);
    }

    public void mapExternalReferenceElements(Reference[] references, String type, ModsDefinition mods, ChangeLog changeLog) {
        for (Reference r : references) {
            final String url = r.getValue();
            if (type.equals("url") && (url == null || url.isEmpty())) {
                continue;
            }

            final String label = singleline(r.getLabel());

            StringBuilder queryBuilder = new StringBuilder();
            ArrayList<String> queryParameters = new ArrayList<>();

            queryBuilder.append("mods:relatedItem[");
            queryBuilder.append("@type='otherVersion'");

            if (label != null && !label.isEmpty()) {
                queryBuilder.append(" and mods:note='%s'");
                queryParameters.add(label);
            }

            if ("url".equals(type)) {
                queryBuilder.append(" and mods:location/mods:url='%s'");
                queryParameters.add(url);
            } else {
                queryBuilder.append(" and mods:identifier[@type='%s']='%s'");
                queryParameters.add(type);
                queryParameters.add(url);
            }

            queryBuilder.append("]");
            String query = formatXPath(queryBuilder.toString(), queryParameters.toArray());

            RelatedItemDefinition rid = (RelatedItemDefinition) select(query, mods);
            if (rid == null) {
                rid = mods.addNewRelatedItem();
                rid.setType(OTHER_VERSION);
                changeLog.log(MODS);
            }

            if ("url".equals(type)) {
                locationElement(url, rid, changeLog);
            } else {
                mapIdentifier(url, rid, type, changeLog);
            }

            if (label != null && !label.isEmpty()) {
                noteElement(label, rid, changeLog);
            }

        }
    }

    public void mapHostAndPredecessorReferences(Document opus, ModsDefinition mods, ChangeLog changeLog) {
        for (Reference r : opus.getReferenceUrnArray()) {

            // constructing mods:relatedItem

            if ("series".equals(r.getRelation())) {
                // skip if document type is series
                return;
            }

            String relatedItemType = ("predecessor".equals(r.getRelation())) ? "preceding" : "host";
            RelatedItemDefinition rd = (RelatedItemDefinition) select(
                    formatXPath("mods:relatedItem[@type='%s']", relatedItemType), mods);
            if (rd == null) {
                rd = mods.addNewRelatedItem();
                rd.setType(RelatedItemDefinition.Type.Enum.forString(relatedItemType));
                changeLog.log(MODS);
            }

            String urn = r.getValue();
            if (urn == null || urn.isEmpty()) return;

            // constructing mods:identifier

            IdentifierDefinition id = (IdentifierDefinition)
                    select(formatXPath("mods:identifier[@type='urn' and text()='%s']", urn), rd);
            if (id == null) {
                id = rd.addNewIdentifier();
                id.setType("urn");
                id.setStringValue(urn);
                changeLog.log(MODS);
            }

            mapSortingKeyToRelatedItemExtension(r.getSortOrder(), rd, changeLog);

            // constructing mods:part
            String issue = opus.getIssue();
            boolean hasIssue = (issue != null) && !issue.isEmpty();
            if (hasIssue) {
                PartDefinition pd = (PartDefinition) select("mods:part[@type='issue']", mods);
                if (pd == null) {
                    pd = mods.addNewPart();
                    if (hasIssue) pd.setType("issue");
                    changeLog.log(MODS);
                }

                // consructing mods:detail/mods:number

                DetailDefinition dd = (DetailDefinition) firstOf(pd.getDetailArray());
                if (dd == null) {
                    dd = pd.addNewDetail();
                    changeLog.log(MODS);
                }

                StringPlusLanguage number = (StringPlusLanguage) firstOf(dd.getNumberArray());
                if (number == null) {
                    number = dd.addNewNumber();
                    changeLog.log(MODS);
                }

                if (!issue.equals(number.getStringValue())) {
                    number.setStringValue(issue);
                    changeLog.log(MODS);
                }
            }
        }
    }

    private void noteElement(String label, RelatedItemDefinition rid, ChangeLog changeLog) {
        NoteDefinition nd = (NoteDefinition) firstOf(rid.getNoteArray());
        if (nd == null) {
            nd = rid.addNewNote();
            changeLog.log(MODS);
        }
        if (!label.equals(nd.getStringValue())) {
            nd.setStringValue(label);
            changeLog.log(MODS);
        }
    }

    private void locationElement(String url, RelatedItemDefinition rid, ChangeLog changeLog) {
        LocationDefinition ld = (LocationDefinition) select("mods:location[mods:url]", rid);
        if (ld == null) {
            ld = rid.addNewLocation();
            changeLog.log(MODS);
        }

        UrlDefinition ud = (UrlDefinition) firstOf(ld.getUrlArray());
        if (ud == null) {
            ud = ld.addNewUrl();
            changeLog.log(MODS);
        }
        if (!url.equals(ud.getStringValue())) {
            ud.setStringValue(url);
            changeLog.log(MODS);
        }
    }

    private void mapVolumeTitle(String volumeTitle, RelatedItemDefinition rid, ChangeLog changeLog) {
        if (volumeTitle != null && !volumeTitle.isEmpty()) {
            TitleInfoDefinition tid = (TitleInfoDefinition) select("mods:titleInfo", rid);
            if (tid == null) {
                tid = rid.addNewTitleInfo();
                changeLog.log(MODS);
            }

            StringPlusLanguage title = (StringPlusLanguage) select("mods:title", tid);
            if (title == null) {
                title = tid.addNewTitle();
                changeLog.log(MODS);
            }

            if (!volumeTitle.equals(title.getStringValue())) {
                title.setStringValue(volumeTitle);
                changeLog.log(MODS);
            }
        }
    }

    private void mapVolume(String volume, PartDefinition pd, ChangeLog changeLog) {
        if (volume != null && !volume.isEmpty()) {
            DetailDefinition dd = (DetailDefinition) select("mods:detail", pd);
            if (dd == null) {
                dd = pd.addNewDetail();
                changeLog.log(MODS);
            }

            StringPlusLanguage spl = (StringPlusLanguage) select("mods:number", dd);
            if (spl == null) {
                spl = dd.addNewNumber();
                spl.setStringValue(volume);
                changeLog.log(MODS);
            }
        }
    }

    private void mapSortingKeyToRelatedItemExtension(String sortingKey, RelatedItemDefinition rid, ChangeLog changeLog) {
        if (sortingKey != null && !sortingKey.isEmpty()) {
            ExtensionDefinition extensionDefinition = (ExtensionDefinition) select("mods:extension", rid);
            if (extensionDefinition == null) {
                extensionDefinition = rid.addNewExtension();
                changeLog.log(MODS);
            }

            boolean _importInfoDocument = false;
            InfoDocument infoDocument = InfoDocument.Factory.newInstance();

            InfoType infoType = (InfoType) select("slub:info", extensionDefinition);
            if (infoType == null) {
                infoType = infoDocument.addNewInfo();
                changeLog.log(MODS);
                _importInfoDocument = true;
            }

            if (!sortingKey.equals(infoType.getSortingKey())) {
                infoType.setSortingKey(sortingKey);
                changeLog.log(MODS);
            }

            if (_importInfoDocument) {
                insertNode(infoDocument, extensionDefinition);
                changeLog.log(MODS);
            }
        }
    }

    private void mapIdentifier(String uri, RelatedItemDefinition rid, final String type, ChangeLog changeLog) {
        if (uri != null && !uri.isEmpty()) {
            IdentifierDefinition id = (IdentifierDefinition)
                    select(formatXPath("mods:identifier[@type='%s' and text()='%s']", type, uri), rid);
            if (id == null) {
                id = rid.addNewIdentifier();
                id.setType(type);
                id.setStringValue(uri);
                changeLog.log(MODS);
            }
        }
    }

}
