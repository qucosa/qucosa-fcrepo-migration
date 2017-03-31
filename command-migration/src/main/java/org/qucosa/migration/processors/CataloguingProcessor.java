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

import de.slubDresden.InfoDocument;
import gov.loc.mods.v3.AbstractDefinition;
import gov.loc.mods.v3.ClassificationDefinition;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.ModsDocument;
import gov.loc.mods.v3.TableOfContentsDefinition;
import noNamespace.Document;
import noNamespace.OpusDocument;
import noNamespace.Subject;
import noNamespace.Title;

import static org.qucosa.migration.mappings.MappingFunctions.multiline;
import static org.qucosa.migration.mappings.XmlFunctions.select;
import static org.qucosa.migration.mappings.MappingFunctions.singleline;

public class CataloguingProcessor extends MappingProcessor {
    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        Document opus = opusDocument.getOpus().getOpusDocument();
        ModsDefinition mods = modsDocument.getMods();

        mapTitleAbstract(opus, mods);
        mapTableOfContent(opus, mods);
        mapSubject("ddc", opus, mods);
        mapSubject("rvk", opus, mods);
        mapSubject("swd", opus, mods);
        mapSubject("uncontrolled", opus, mods);
    }

    private void mapSubject(String type, Document opus, ModsDefinition mods) {
        Subject[] subjects;
        String query = "[@authority='%s' and text()='%s']";
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
                break;
            case "uncontrolled":
                subjects = opus.getSubjectUncontrolledArray();
                mappedType = "z";
                break;
            default:
                return;
        }

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
                signalChanges(MODS_CHANGES);
            }

            if (!cl.getStringValue().equals(value)) {
                cl.setStringValue(value);
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private void mapTableOfContent(Document opus, ModsDefinition mods) {
        String opusTableOfContent = opus.getTableOfContent();
        final String mappedToc = multiline(opusTableOfContent);
        if (opusTableOfContent != null && !opusTableOfContent.isEmpty()) {
            TableOfContentsDefinition toc = (TableOfContentsDefinition)
                    select("mods:tableOfContents", mods);

            if (toc == null) {
                toc = mods.addNewTableOfContents();
                toc.setStringValue(mappedToc);
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private void mapTitleAbstract(Document opus, ModsDefinition mods) {
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
                signalChanges(MODS_CHANGES);
            }
        }
    }
}
