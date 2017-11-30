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

import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.RelatedItemDefinition;
import gov.loc.mods.v3.StringPlusLanguage;
import gov.loc.mods.v3.TitleInfoDefinition;
import noNamespace.Document;
import noNamespace.Title;
import org.apache.xmlbeans.XmlString;

import javax.xml.xpath.XPathExpressionException;

import static gov.loc.mods.v3.TitleInfoDefinition.Type.TRANSLATED;
import static org.qucosa.migration.mappings.ChangeLog.Type.MODS;
import static org.qucosa.migration.mappings.MappingFunctions.firstOf;
import static org.qucosa.migration.mappings.MappingFunctions.languageEncoding;
import static org.qucosa.migration.mappings.MappingFunctions.singleline;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.nodeExists;
import static org.qucosa.migration.org.qucosa.migration.xml.XmlFunctions.select;

public class TitleMapping {

    public void mapTitleAlternativeElements(Document opus, ModsDefinition mods, ChangeLog changeLog) throws XPathExpressionException {
        for (Title ot : opus.getTitleAlternativeArray()) {
            final String encLang = languageEncoding(ot.getLanguage());

            TitleInfoDefinition tid = ensureTitleInfoElement(mods, encLang, TitleInfoDefinition.Type.ALTERNATIVE);

            final String value = singleline(ot.getValue());
            if (!nodeExists("mods:title[text()='" + value + "']", tid)) {
                StringPlusLanguage mt = tid.addNewTitle();
                mt.setStringValue(value);
                changeLog.log(MODS);
            }
        }
    }

    public void mapTitleSubElements(Document opus, ModsDefinition mods, ChangeLog changeLog) throws XPathExpressionException {
        for (Title ot : opus.getTitleSubArray()) {
            final String encLang = languageEncoding(ot.getLanguage());

            TitleInfoDefinition tid = ensureTitleInfoElement(mods, encLang);

            final String value = singleline(ot.getValue());
            if (!nodeExists("mods:subTitle[text()='" + value + "']", tid)) {
                StringPlusLanguage mt = tid.addNewSubTitle();
                mt.setStringValue(value);
                changeLog.log(MODS);
            }
        }
    }

    public void mapTitleMainElements(Document opus, ModsDefinition mods, ChangeLog changeLog) throws XPathExpressionException {
        String[] langCodeArray = ((String) firstOf(opus.getLanguageArray())).split(",");
        String firstDocumentLanguage = (String) firstOf(langCodeArray);
        Title[] titleMainArray = opus.getTitleMainArray();

        boolean titleMatchingDocumentLanguageExists = false;
        for (Title ot : titleMainArray) {
            String encLang = languageEncoding(ot.getLanguage());
            if (encLang.equals(firstDocumentLanguage)) {
                titleMatchingDocumentLanguageExists = true;
                break;
            }
        }

        for (Title ot : titleMainArray) {
            String encLang = languageEncoding(ot.getLanguage());
            TitleInfoDefinition tid;
            if (encLang.equals(firstDocumentLanguage)
                    || titleMainArray.length == 1
                    || !titleMatchingDocumentLanguageExists) {
                tid = ensureTitleInfoElement(mods, encLang, "primary");
            } else {
                tid = ensureTitleInfoElement(mods, encLang, TRANSLATED);
            }

            final String value = singleline(ot.getValue());
            if (!nodeExists("mods:title[text()='" + value + "']", tid)) {
                StringPlusLanguage mt = tid.addNewTitle();
                mt.setStringValue(value);
                changeLog.log(MODS);
            }
        }
    }

    private TitleInfoDefinition ensureTitleInfoElement(ModsDefinition modsDefinition, String lang) {
        TitleInfoDefinition titleInfoDefinition =
                (TitleInfoDefinition) select("mods:titleInfo[@lang='" + lang + "']", modsDefinition);
        if (titleInfoDefinition == null) {
            titleInfoDefinition = modsDefinition.addNewTitleInfo();
            titleInfoDefinition.setLang(lang);
        }
        return titleInfoDefinition;
    }

    private TitleInfoDefinition ensureTitleInfoElement(RelatedItemDefinition relatedItemDefinition, String lang) {
        TitleInfoDefinition titleInfoDefinition =
                (TitleInfoDefinition) select("mods:titleInfo[@lang='" + lang + "']", relatedItemDefinition);
        if (titleInfoDefinition == null) {
            titleInfoDefinition = relatedItemDefinition.addNewTitleInfo();
            titleInfoDefinition.setLang(lang);
        }
        return titleInfoDefinition;
    }

    private TitleInfoDefinition ensureTitleInfoElement(ModsDefinition modsDefinition, String lang, TitleInfoDefinition.Type.Enum type) {
        TitleInfoDefinition titleInfoDefinition =
                (TitleInfoDefinition) select("mods:titleInfo[@lang='" + lang + "' and @type='" + type + "']",
                        modsDefinition);
        if (titleInfoDefinition == null) {
            titleInfoDefinition = modsDefinition.addNewTitleInfo();
            titleInfoDefinition.setType(type);
            titleInfoDefinition.setLang(lang);
        }
        return titleInfoDefinition;
    }

    private TitleInfoDefinition ensureTitleInfoElement(ModsDefinition modsDefinition, String lang, String usage) {
        TitleInfoDefinition titleInfoDefinition =
                (TitleInfoDefinition) select("mods:titleInfo[@lang='" + lang + "' and @usage='" + usage + "']",
                        modsDefinition);
        if (titleInfoDefinition == null) {
            titleInfoDefinition = modsDefinition.addNewTitleInfo();
            titleInfoDefinition.setLang(lang);
            titleInfoDefinition.setUsage(XmlString.Factory.newValue(usage));
        }
        return titleInfoDefinition;
    }

}
