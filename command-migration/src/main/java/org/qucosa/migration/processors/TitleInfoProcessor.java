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

import de.slubDresden.InfoType;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.RelatedItemDefinition;
import gov.loc.mods.v3.StringPlusLanguage;
import gov.loc.mods.v3.TitleInfoDefinition;
import gov.loc.mods.v3.TitleInfoDefinition.Type;
import noNamespace.Document;
import noNamespace.Title;
import org.apache.xmlbeans.XmlString;

import javax.xml.xpath.XPathExpressionException;

import static org.qucosa.migration.mappings.MappingFunctions.singleline;
import static org.qucosa.migration.mappings.XmlFunctions.nodeExists;
import static org.qucosa.migration.mappings.XmlFunctions.select;

public class TitleInfoProcessor extends MappingProcessor {

    @Override
    public void process(Document opus, ModsDefinition mods, InfoType info) throws Exception {
        mapTitleMainElements(opus, mods);
        mapTitleSubElements(opus, mods);
        mapTitleAlternativeElements(opus, mods);
        mapTitleParentElements(opus, mods);
    }

    private TitleInfoDefinition ensureTitleInfoElement(ModsDefinition modsDefinition, String lang) {
        TitleInfoDefinition titleInfoDefinition =
                (TitleInfoDefinition) select("mods:titleInfo[@lang='" + lang + "']", modsDefinition);

        if (titleInfoDefinition == null) {
            titleInfoDefinition = modsDefinition.addNewTitleInfo();
            titleInfoDefinition.setLang(lang);
            signalChanges(MODS_CHANGES);
        }

        return titleInfoDefinition;
    }

    private TitleInfoDefinition ensureTitleInfoElement(RelatedItemDefinition relatedItemDefinition, String lang) {
        TitleInfoDefinition titleInfoDefinition =
                (TitleInfoDefinition) select("mods:titleInfo[@lang='" + lang + "']", relatedItemDefinition);

        if (titleInfoDefinition == null) {
            titleInfoDefinition = relatedItemDefinition.addNewTitleInfo();
            titleInfoDefinition.setLang(lang);
            signalChanges(MODS_CHANGES);
        }

        return titleInfoDefinition;
    }

    private TitleInfoDefinition ensureTitleInfoElement(ModsDefinition modsDefinition, String lang, Type.Enum type) {
        TitleInfoDefinition titleInfoDefinition =
                (TitleInfoDefinition) select("mods:titleInfo[@lang='" + lang + "' and @type='" + type + "']",
                        modsDefinition);

        if (titleInfoDefinition == null) {
            titleInfoDefinition = modsDefinition.addNewTitleInfo();
            titleInfoDefinition.setType(type);
            titleInfoDefinition.setLang(lang);
            signalChanges(MODS_CHANGES);
        }

        return titleInfoDefinition;
    }

    private void mapTitleAlternativeElements(Document opus, ModsDefinition mods) throws XPathExpressionException {
        for (Title ot : opus.getTitleAlternativeArray()) {
            final String encLang = languageEncoding(ot.getLanguage());

            TitleInfoDefinition tid = ensureTitleInfoElement(mods, encLang, Type.ALTERNATIVE);

            final String value = singleline(ot.getValue());
            if (!nodeExists("mods:title[text()='" + value + "']", tid)) {
                StringPlusLanguage mt = tid.addNewTitle();
                mt.setStringValue(value);
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private void mapTitleParentElements(Document opus, ModsDefinition mods) throws XPathExpressionException {
        if (nodeExists("TitleParent", opus)) {
            RelatedItemDefinition relatedItemDefinition =
                    (RelatedItemDefinition) select("mods:relatedItem[@type='series']", mods);

            if (relatedItemDefinition == null) {
                relatedItemDefinition = mods.addNewRelatedItem();
                relatedItemDefinition.setType(RelatedItemDefinition.Type.SERIES);
                signalChanges(MODS_CHANGES);
            }

            for (Title ot : opus.getTitleParentArray()) {
                final String encLang = languageEncoding(ot.getLanguage());

                TitleInfoDefinition tid = ensureTitleInfoElement(relatedItemDefinition, encLang);

                final String value = singleline(ot.getValue());
                if (!nodeExists("mods:title[text()='" + value + "']", tid)) {
                    StringPlusLanguage mt = tid.addNewTitle();
                    mt.setStringValue(value);
                    signalChanges(MODS_CHANGES);
                }
            }
        }
    }

    private void mapTitleSubElements(Document opus, ModsDefinition mods) throws XPathExpressionException {
        for (Title ot : opus.getTitleSubArray()) {
            final String encLang = languageEncoding(ot.getLanguage());

            TitleInfoDefinition tid = ensureTitleInfoElement(mods, encLang);

            final String value = singleline(ot.getValue());
            if (!nodeExists("mods:subTitle[text()='" + value + "']", tid)) {
                StringPlusLanguage mt = tid.addNewSubTitle();
                mt.setStringValue(value);
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private void mapTitleMainElements(Document opus, ModsDefinition mods) throws XPathExpressionException {
        for (Title ot : opus.getTitleMainArray()) {
            String encLang = languageEncoding(ot.getLanguage());

            TitleInfoDefinition tid = ensureTitleInfoElement(mods, encLang);
            tid.setUsage(XmlString.Factory.newValue("primary"));

            final String value = singleline(ot.getValue());
            if (!nodeExists("mods:title[text()='" + value + "']", tid)) {
                StringPlusLanguage mt = tid.addNewTitle();
                mt.setStringValue(value);
                signalChanges(MODS_CHANGES);
            }
        }
    }
}
