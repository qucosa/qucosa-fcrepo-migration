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

import gov.loc.mods.v3.DetailDefinition;
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

import java.math.BigInteger;
import java.util.ArrayList;

import static gov.loc.mods.v3.RelatedItemDefinition.Type.OTHER_VERSION;
import static gov.loc.mods.v3.RelatedItemDefinition.Type.SERIES;
import static org.qucosa.migration.mappings.MappingFunctions.firstOf;
import static org.qucosa.migration.mappings.MappingFunctions.volume;
import static org.qucosa.migration.mappings.MappingFunctions.volumeTitle;
import static org.qucosa.migration.mappings.XmlFunctions.select;

public class ReferencesMapping {

    private final ThreadLocal<ChangeSignal> change = ThreadLocal.withInitial(ChangeSignal::new);

    public boolean mapSeriesReference(Document opus, ModsDefinition mods) {
        change.get().reset();

        // gathering data
        Title firstTitleParent = (Title) firstOf(opus.getTitleParentArray());

        String volume = null;
        String volumeTitle = null;
        if (firstTitleParent != null) {
            volume = volume(firstTitleParent.getValue());
            volumeTitle = volumeTitle(firstTitleParent.getValue());
        }

        Reference firstReferenceUrn = (Reference) firstOf(opus.getReferenceUrnArray());
        String referenceUrn = null;
        String referenceSortOrder = null;
        if (firstReferenceUrn != null) {
            referenceUrn = firstReferenceUrn.getValue();
            referenceSortOrder = firstReferenceUrn.getSortOrder();
        }

        // stop here, if there is no information to map
        if (volumeTitle == null && referenceUrn == null) {
            return change.get().signaled();
        }

        // constructing relatedItem
        RelatedItemDefinition rid = (RelatedItemDefinition) select("mods:relatedItem[@type='series']", mods);
        if (rid == null) {
            rid = mods.addNewRelatedItem();
            rid.setType(SERIES);
            change.get().signal();
        }

        PartDefinition pd = (PartDefinition) select("mods:part", mods);
        if (pd == null) {
            pd = mods.addNewPart();
            change.get().signal();
        }

        mapIdentifier(referenceUrn, rid, "urn");
        mapSortOrderToPartOrder(referenceSortOrder, pd);
        mapVolume(volume, pd);
        mapVolumeTitle(volumeTitle, rid);

        return change.get().signaled();
    }

    public boolean mapExternalReferenceElements(Reference[] references, String type, ModsDefinition mods) {
        change.get().reset();
        for (Reference r : references) {
            final String url = r.getValue();
            if (type.equals("url") && (url == null || url.isEmpty())) {
                continue;
            }

            final String label = r.getLabel();

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
            String query = String.format(queryBuilder.toString(), queryParameters.toArray());

            RelatedItemDefinition rid = (RelatedItemDefinition) select(query, mods);
            if (rid == null) {
                rid = mods.addNewRelatedItem();
                rid.setType(OTHER_VERSION);
                change.get().signal();
            }

            if ("url".equals(type)) {
                locationElement(url, rid);
            } else {
                mapIdentifier(url, rid, type);
            }

            if (label != null && !label.isEmpty()) {
                noteElement(label, rid);
            }

            PartDefinition pd = (PartDefinition) select("mods:part", rid);
            if (pd == null) {
                pd = rid.addNewPart();
                change.get().signal();
            }

            mapSortOrderToPartOrder(r.getSortOrder(), pd);
        }
        return change.get().signaled();
    }

    public boolean mapHostAndPredecessorReferences(Document opus, ModsDefinition mods) {
        change.get().reset();
        for (Reference r : opus.getReferenceUrnArray()) {

            // constructing mods:relatedItem

            String relatedItemType = ("predecessor".equals(r.getRelation())) ? "preceding" : "host";
            RelatedItemDefinition rd = (RelatedItemDefinition) select("mods:relatedItem[@type='" + relatedItemType + "']", mods);
            if (rd == null) {
                rd = mods.addNewRelatedItem();
                rd.setType(RelatedItemDefinition.Type.Enum.forString(relatedItemType));
                change.get().signal();
            }

            String urn = r.getValue();
            if (urn == null || urn.isEmpty()) return change.get().signaled();

            // constructing mods:identifier

            IdentifierDefinition id = (IdentifierDefinition)
                    select("mods:identifier[@type='urn' and text()='" + urn + "']", rd);
            if (id == null) {
                id = rd.addNewIdentifier();
                id.setType("urn");
                id.setStringValue(urn);
                change.get().signal();
            }

            // constructing mods:part

            String issue = opus.getIssue();
            BigInteger order = null;
            try {
                order = new BigInteger(r.getSortOrder());
            } catch (NullPointerException | NumberFormatException ignored) {
            }
            boolean hasOrder = order != null;
            boolean hasIssue = (issue != null) && !issue.isEmpty();

            if (hasOrder || hasIssue) {
                PartDefinition pd = (PartDefinition) select("mods:part[@type='issue']", mods);
                if (pd == null) {
                    pd = mods.addNewPart();
                    if (hasIssue) pd.setType("issue");
                    change.get().signal();
                }

                if (hasOrder && !order.equals(pd.getOrder())) {
                    pd.setOrder(order);
                    change.get().signal();
                }

                // consructing mods:detail/mods:number

                if (hasIssue) {
                    DetailDefinition dd = (DetailDefinition) firstOf(pd.getDetailArray());
                    if (dd == null) {
                        dd = pd.addNewDetail();
                        change.get().signal();
                    }

                    StringPlusLanguage number = (StringPlusLanguage) firstOf(dd.getNumberArray());
                    if (number == null) {
                        number = dd.addNewNumber();
                        change.get().signal();
                    }

                    if (!issue.equals(number.getStringValue())) {
                        number.setStringValue(issue);
                        change.get().signal();
                    }
                }
            }
        }
        return change.get().signaled();
    }

    private void noteElement(String label, RelatedItemDefinition rid) {
        NoteDefinition nd = (NoteDefinition) firstOf(rid.getNoteArray());
        if (nd == null) {
            nd = rid.addNewNote();
            change.get().signal();
        }
        if (!label.equals(nd.getStringValue())) {
            nd.setStringValue(label);
            change.get().signal();
        }
    }

    private void locationElement(String url, RelatedItemDefinition rid) {
        LocationDefinition ld = (LocationDefinition) select("mods:location[mods:url]", rid);
        if (ld == null) {
            ld = rid.addNewLocation();
            change.get().signal();
        }

        UrlDefinition ud = (UrlDefinition) firstOf(ld.getUrlArray());
        if (ud == null) {
            ud = ld.addNewUrl();
            change.get().signal();
        }
        if (!url.equals(ud.getStringValue())) {
            ud.setStringValue(url);
            change.get().signal();
        }
    }

    private void mapVolumeTitle(String volumeTitle, RelatedItemDefinition rid) {
        if (volumeTitle != null && !volumeTitle.isEmpty()) {
            TitleInfoDefinition tid = (TitleInfoDefinition) select("mods:title", rid);
            if (tid == null) {
                tid = rid.addNewTitleInfo();
                change.get().signal();
            }

            StringPlusLanguage title = (StringPlusLanguage) select("mods:title", tid);
            if (title == null) {
                title = tid.addNewTitle();
                change.get().signal();
            }

            if (!volumeTitle.equals(title.getStringValue())) {
                title.setStringValue(volumeTitle);
                change.get().signal();
            }
        }
    }

    private void mapVolume(String volume, PartDefinition pd) {
        if (volume != null && !volume.isEmpty()) {
            DetailDefinition dd = (DetailDefinition) select("mods:detail", pd);
            if (dd == null) {
                dd = pd.addNewDetail();
                change.get().signal();
            }

            StringPlusLanguage spl = (StringPlusLanguage) select("mods:number", dd);
            if (spl == null) {
                spl = dd.addNewNumber();
                spl.setStringValue(volume);
                change.get().signal();
            }
        }
    }

    private void mapSortOrderToPartOrder(String orderString, PartDefinition pd) {
        try {
            BigInteger order = new BigInteger(orderString);
            if (!order.equals(pd.getOrder())) {
                pd.setOrder(order);
                change.get().signal();
            }
        } catch (NullPointerException | NumberFormatException ignored) {
        }
    }

    private void mapIdentifier(String uri, RelatedItemDefinition rid, final String type) {
        if (uri != null && !uri.isEmpty()) {
            IdentifierDefinition id = (IdentifierDefinition)
                    select("mods:identifier[@type='" + type + "' and text()='" + uri + "']", rid);
            if (id == null) {
                id = rid.addNewIdentifier();
                id.setType(type);
                id.setStringValue(uri);
                change.get().signal();
            }
        }
    }

}
