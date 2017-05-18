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

import java.util.ArrayList;

import static org.qucosa.migration.mappings.ChangeLog.Type.MODS;
import static org.qucosa.migration.mappings.MappingFunctions.languageEncoding;
import static org.qucosa.migration.mappings.MappingFunctions.multiline;
import static org.qucosa.migration.mappings.MappingFunctions.singleline;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.select;

public class ContentualMapping {

    public void mapTitleAbstract(Document opus, ModsDefinition mods, ChangeLog changeLog) {
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
                changeLog.log(MODS);
            }
        }
    }

    public void mapSubject(String type, Document opus, ModsDefinition mods, ChangeLog changeLog) {
        Subject[] subjects;

        String authority = type;
        switch (type) {
            case "ddc":
                subjects = opus.getSubjectDdcArray();
                break;
            case "rvk":
                subjects = opus.getSubjectRvkArray();
                break;
            case "swd":
                subjects = opus.getSubjectSwdArray();
                authority = "sswd";
                break;
            case "uncontrolled":
                subjects = opus.getSubjectUncontrolledArray();
                authority = "z";
                break;
            default:
                throw new IllegalArgumentException(String.format("Unkown subject type `%s`", type));
        }

        for (Subject subject : subjects) {
            final String value = singleline(subject.getValue());
            final String lang = languageEncoding(subject.getLanguage());

            ArrayList<String> queryParameters = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            sb.append("mods:classification");
            sb.append("[@authority='%s']");
            queryParameters.add(authority);
            sb.append("[text()='%s']");
            queryParameters.add(value);
            if (lang != null && !lang.isEmpty()) {
                sb.append("[@lang='%s']");
                queryParameters.add(lang);
            }
            String query = String.format(sb.toString(), queryParameters.toArray());

            ClassificationDefinition cl;
            cl = (ClassificationDefinition) select(query, mods);

            if (cl == null) {
                cl = mods.addNewClassification();
                cl.setAuthority(authority);
                if (lang != null) cl.setLang(lang);
                changeLog.log(MODS);
            }

            if (!cl.getStringValue().equals(value)) {
                cl.setStringValue(value);
                changeLog.log(MODS);
            }
        }
    }

    public void mapTableOfContent(Document opus, ModsDefinition mods, ChangeLog changeLog) {
        String opusTableOfContent = opus.getTableOfContent();
        final String mappedToc = multiline(opusTableOfContent);
        if (opusTableOfContent != null && !opusTableOfContent.isEmpty()) {
            TableOfContentsDefinition toc = (TableOfContentsDefinition) select("mods:tableOfContents", mods);
            if (toc == null) {
                toc = mods.addNewTableOfContents();
                toc.setStringValue(mappedToc);
                changeLog.log(MODS);
            }
        }
    }

    public void mapIssue(Document opus, ModsDefinition mods, ChangeLog changeLog) {
        String issue = opus.getIssue();
        if (issue != null && !issue.isEmpty()) {
            PartDefinition partDefinition = (PartDefinition) select("mods:part[@type='issue']", mods);
            if (partDefinition == null) {
                partDefinition = mods.addNewPart();
                partDefinition.setType("issue");
                changeLog.log(MODS);
            }

            DetailDefinition detailDefinition = (DetailDefinition) select("mods:detail", partDefinition);
            if (detailDefinition == null) {
                detailDefinition = partDefinition.addNewDetail();
                changeLog.log(MODS);
            }

            StringPlusLanguage number = (StringPlusLanguage) select("mods:number", detailDefinition);
            if (number == null) {
                number = detailDefinition.addNewNumber();
                changeLog.log(MODS);
            }

            if (!issue.equals(number.getStringValue())) {
                number.setStringValue(issue);
                changeLog.log(MODS);
            }
        }
    }
}
