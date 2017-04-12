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
import static org.qucosa.migration.mappings.MappingFunctions.firstOf;
import static org.qucosa.migration.mappings.MappingFunctions.languageEncoding;
import static org.qucosa.migration.mappings.MappingFunctions.singleline;
import static org.qucosa.migration.mappings.XmlFunctions.nodeExists;
import static org.qucosa.migration.mappings.XmlFunctions.select;

public class TitleMapping {

    public boolean mapTitleAlternativeElements(Document opus, ModsDefinition mods) throws XPathExpressionException {
        boolean change = false;
        for (Title ot : opus.getTitleAlternativeArray()) {
            final String encLang = languageEncoding(ot.getLanguage());

            TitleInfoDefinition tid = ensureTitleInfoElement(mods, encLang, TitleInfoDefinition.Type.ALTERNATIVE);

            final String value = singleline(ot.getValue());
            if (!nodeExists("mods:title[text()='" + value + "']", tid)) {
                StringPlusLanguage mt = tid.addNewTitle();
                mt.setStringValue(value);
                change = true;
            }
        }
        return change;
    }

    public boolean mapTitleParentElements(Document opus, ModsDefinition mods) throws XPathExpressionException {
        boolean change = false;
        if (nodeExists("TitleParent", opus)) {
            RelatedItemDefinition relatedItemDefinition =
                    (RelatedItemDefinition) select("mods:relatedItem[@type='series']", mods);

            if (relatedItemDefinition == null) {
                relatedItemDefinition = mods.addNewRelatedItem();
                relatedItemDefinition.setType(RelatedItemDefinition.Type.SERIES);
                change = true;
            }

            for (Title ot : opus.getTitleParentArray()) {
                final String encLang = languageEncoding(ot.getLanguage());

                TitleInfoDefinition tid = ensureTitleInfoElement(relatedItemDefinition, encLang);

                final String value = singleline(ot.getValue());
                if (!nodeExists("mods:title[text()='" + value + "']", tid)) {
                    StringPlusLanguage mt = tid.addNewTitle();
                    mt.setStringValue(value);
                    change = true;
                }
            }
        }
        return change;
    }

    public boolean mapTitleSubElements(Document opus, ModsDefinition mods) throws XPathExpressionException {
        boolean change = false;
        for (Title ot : opus.getTitleSubArray()) {
            final String encLang = languageEncoding(ot.getLanguage());

            TitleInfoDefinition tid = ensureTitleInfoElement(mods, encLang);

            final String value = singleline(ot.getValue());
            if (!nodeExists("mods:subTitle[text()='" + value + "']", tid)) {
                StringPlusLanguage mt = tid.addNewSubTitle();
                mt.setStringValue(value);
                change = true;
            }
        }
        return change;
    }

    public boolean mapTitleMainElements(Document opus, ModsDefinition mods) throws XPathExpressionException {
        boolean change = false;
        String documentLanguage = languageEncoding((String) firstOf(opus.getLanguageArray()));

        for (Title ot : opus.getTitleMainArray()) {
            String encLang = languageEncoding(ot.getLanguage());

            TitleInfoDefinition tid;
            boolean isTranslated = !encLang.equals(documentLanguage);
            if (isTranslated) {
                tid = ensureTitleInfoElement(mods, encLang, TRANSLATED);
            } else {
                tid = ensureTitleInfoElement(mods, encLang, "primary");
            }

            final String value = singleline(ot.getValue());
            if (!nodeExists("mods:title[text()='" + value + "']", tid)) {
                StringPlusLanguage mt = tid.addNewTitle();
                mt.setStringValue(value);
                change = true;
            }
        }

        return change;
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
