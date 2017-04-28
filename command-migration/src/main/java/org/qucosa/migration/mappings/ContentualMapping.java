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

import gov.loc.mods.v3.AbstractDefinition;
import gov.loc.mods.v3.ClassificationDefinition;
import gov.loc.mods.v3.DetailDefinition;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.PartDefinition;
import gov.loc.mods.v3.StringPlusLanguage;
import gov.loc.mods.v3.TableOfContentsDefinition;
import noNamespace.Document;
import noNamespace.Subject;
import noNamespace.Title;

import static org.qucosa.migration.mappings.MappingFunctions.languageEncoding;
import static org.qucosa.migration.mappings.MappingFunctions.multiline;
import static org.qucosa.migration.mappings.MappingFunctions.singleline;
import static org.qucosa.migration.mappings.XmlFunctions.select;

public class ContentualMapping {

    public boolean mapTitleAbstract(Document opus, ModsDefinition mods) {
        boolean change = false;
        for (Title ot : opus.getTitleAbstractArray()) {
            final String lang = languageEncoding(ot.getLanguage());
            final String abst = multiline(ot.getValue());

            AbstractDefinition ad = (AbstractDefinition) select(
                    String.format("mods:abstract[@lang='%s' and @type='%s' and text()='%s']",
                            lang, "summary", abst), mods);

            if (ad == null) {
                ad = mods.addNewAbstract();
                ad.setLang(lang);
                ad.setType("summary");
                ad.setStringValue(abst);
                change = true;
            }
        }
        return change;
    }

    public boolean mapSubject(String type, Document opus, ModsDefinition mods) {
        boolean change = false;
        Subject[] subjects;

        String mappedType = type;
        switch (type) {
            case "ddc":
                subjects = opus.getSubjectDdcArray();
                break;
            case "rvk":
                subjects = opus.getSubjectRvkArray();
                break;
            case "swd":
                subjects = opus.getSubjectSwdArray();
                mappedType = "sswd";
                break;
            case "uncontrolled":
                subjects = opus.getSubjectUncontrolledArray();
                mappedType = "z";
                break;
            default:
                throw new IllegalArgumentException(String.format("Unkown subject type `%s`", type));
        }

        String query = "[@authority='%s' and text()='%s']";
        for (Subject subject : subjects) {
            final String value = singleline(subject.getValue());
            final String lang = languageEncoding(subject.getLanguage());

            ClassificationDefinition cl;
            cl = (ClassificationDefinition)
                    select("mods:classification" + String.format(query, mappedType, value), mods);

            if (cl == null) {
                cl = mods.addNewClassification();
                cl.setAuthority(mappedType);
                if (lang != null) cl.setLang(lang);
                change = true;
            }

            if (!cl.getStringValue().equals(value)) {
                cl.setStringValue(value);
                change = true;
            }
        }
        return change;
    }

    public boolean mapTableOfContent(Document opus, ModsDefinition mods) {
        boolean change = false;
        String opusTableOfContent = opus.getTableOfContent();
        final String mappedToc = multiline(opusTableOfContent);
        if (opusTableOfContent != null && !opusTableOfContent.isEmpty()) {
            TableOfContentsDefinition toc = (TableOfContentsDefinition) select("mods:tableOfContents", mods);
            if (toc == null) {
                toc = mods.addNewTableOfContents();
                toc.setStringValue(mappedToc);
                change = true;
            }
        }
        return change;
    }

    public boolean mapIssue(Document opus, ModsDefinition mods) {
        boolean change = false;
        String issue = opus.getIssue();
        if (issue != null && !issue.isEmpty()) {
            PartDefinition partDefinition = (PartDefinition) select("mods:part[@type='issue']", mods);
            if (partDefinition == null) {
                partDefinition = mods.addNewPart();
                partDefinition.setType("issue");
                change = true;
            }

            DetailDefinition detailDefinition = (DetailDefinition) select("mods:detail", partDefinition);
            if (detailDefinition == null) {
                detailDefinition = partDefinition.addNewDetail();
                change = true;
            }

            StringPlusLanguage number = (StringPlusLanguage) select("mods:number", detailDefinition);
            if (number == null) {
                number = detailDefinition.addNewNumber();
                change = true;
            }

            if (!issue.equals(number.getStringValue())) {
                number.setStringValue(issue);
                change = true;
            }
        }
        return change;
    }
}
