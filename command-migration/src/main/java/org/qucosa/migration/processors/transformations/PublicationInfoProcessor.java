/*
 * Copyright (C) 2015 Saxon State and University Library Dresden (SLUB)
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

package org.qucosa.migration.processors.transformations;

import de.slubDresden.InfoDocument;
import gov.loc.mods.v3.DateDefinition;
import gov.loc.mods.v3.DateOtherDefinition;
import gov.loc.mods.v3.LanguageDefinition;
import gov.loc.mods.v3.LanguageTermDefinition;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.ModsDocument;
import gov.loc.mods.v3.OriginInfoDefinition;
import noNamespace.Document;
import noNamespace.OpusDocument;
import org.apache.xmlbeans.XmlString;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;

import static gov.loc.mods.v3.CodeOrText.CODE;
import static gov.loc.mods.v3.DateDefinition.Encoding.ISO_8601;
import static gov.loc.mods.v3.LanguageTermDefinition.Authority.ISO_639_2_B;

public class PublicationInfoProcessor extends MappingProcessor {
    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        final Document opus = opusDocument.getOpus().getOpusDocument();
        final ModsDefinition mods = modsDocument.getMods();

        mapLanguageElement(opus, mods);
        mapOriginInfoElements(opus, mods);
    }

    private void mapOriginInfoElements(Document opus, ModsDefinition mods) throws XPathExpressionException {
        OriginInfoDefinition oid = (OriginInfoDefinition)
                select("mods:originInfo[@eventType='publication']", mods);

        if (oid == null) {
            oid = mods.addNewOriginInfo();
            oid.setEventType("publication");
            signalChanges(MODS_CHANGES);
        }

        final Boolean hasPublishedDate = nodeExistsAndHasChildNodes("PublishedDate", opus);
        final Boolean hasPublishedYear = nodeExistsAndHasChildNodes("PublishedYear", opus);
        final Boolean hasDateAccepted = nodeExistsAndHasChildNodes("DateAccepted", opus);
        final Boolean hasEdition = nodeExistsAndHasChildNodes("Edition", opus);

        if (hasPublishedDate || hasPublishedYear || hasDateAccepted || hasEdition) {
            if (hasPublishedDate) mapPublishedDate(opus, oid);
            if (hasPublishedYear) mapPublishedYear(opus, oid);
            if (hasDateAccepted) mapDateAccepted(opus, oid);
            if (hasEdition) mapEdition(opus, oid);
        } else {
            // Fallback to ServerDatePublished
            mapServerDatePublished(opus, oid);
        }
    }

    private void mapDateAccepted(Document opus, OriginInfoDefinition oid) {
        final String mappedDateEncoding = dateEncoding(opus.getDateAccepted());

        if (mappedDateEncoding != null) {
            DateOtherDefinition dateOther = (DateOtherDefinition)
                    select(String.format("mods:dateOther[@encoding='%s' and @type='%s']",
                            "iso8601", "defense"), oid);

            if (dateOther == null) {
                dateOther = oid.addNewDateOther();
                dateOther.setEncoding(ISO_8601);
                dateOther.setType("defense");
                signalChanges(MODS_CHANGES);
            }

            if (!dateOther.getStringValue().equals(mappedDateEncoding)) {
                dateOther.setStringValue(mappedDateEncoding);
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private void mapEdition(Document opus, OriginInfoDefinition oid) {
        String opusEdition = opus.getEdition();

        if (opusEdition != null && !opusEdition.isEmpty()) {
            XmlString edition = (XmlString)
                    select("mods:edition", oid);

            if (edition == null) {
                edition = oid.addNewEdition();
                signalChanges(MODS_CHANGES);
            }

            if (!edition.getStringValue().equals(opusEdition)) {
                edition.setStringValue(opusEdition);
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private void mapPublishedYear(Document opus, OriginInfoDefinition oid) {
        final String mappedPublishedYear = dateEncoding(opus.getPublishedYear());

        if (mappedPublishedYear != null) {
            DateDefinition dateIssuedDefinition = (DateDefinition)
                    select("mods:dateIssued[@encoding='iso8601']", oid);

            if (dateIssuedDefinition == null) {
                dateIssuedDefinition = oid.addNewDateIssued();
                dateIssuedDefinition.setEncoding(ISO_8601);
                signalChanges(MODS_CHANGES);
            }

            if (!dateIssuedDefinition.getStringValue().equals(mappedPublishedYear)) {
                dateIssuedDefinition.setStringValue(mappedPublishedYear);
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private void mapServerDatePublished(Document opus, OriginInfoDefinition oid) {
        final String mappedServerDatePublished = dateEncoding(opus.getServerDatePublished());

        if (mappedServerDatePublished != null) {
            DateDefinition dateIssuedDefinition = (DateDefinition)
                    select("mods:dateIssued[@encoding='iso8601']", oid);

            if (dateIssuedDefinition == null) {
                dateIssuedDefinition = oid.addNewDateIssued();
                dateIssuedDefinition.setEncoding(ISO_8601);
                signalChanges(MODS_CHANGES);
            }

            if (!dateIssuedDefinition.getStringValue().equals(mappedServerDatePublished)) {
                dateIssuedDefinition.setStringValue(mappedServerDatePublished);
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private void mapPublishedDate(Document opus, OriginInfoDefinition oid) {
        final String mappedDateEncoding = dateEncoding(opus.getPublishedDate());

        if (mappedDateEncoding != null) {
            DateOtherDefinition dateOther = (DateOtherDefinition)
                    select(String.format("mods:dateOther[@encoding='%s' and @type='%s']",
                            "iso8601", "submission"), oid);

            if (dateOther == null) {
                dateOther = oid.addNewDateOther();
                dateOther.setEncoding(ISO_8601);
                dateOther.setType("submission");
                signalChanges(MODS_CHANGES);
            }

            if (!dateOther.getStringValue().equals(mappedDateEncoding)) {
                dateOther.setStringValue(mappedDateEncoding);
                signalChanges(MODS_CHANGES);
            }
        }
    }

    private void mapLanguageElement(Document opus, ModsDefinition mods) throws XPathExpressionException {
        ArrayList<String> languageCodes = new ArrayList<>();

        for (String opusLanguage : opus.getLanguageArray()) {
            for (String code : opusLanguage.split(",")) {
                final String mappedCode = languageEncoding(code);
                if (mappedCode != null) {
                    languageCodes.add(mappedCode.trim().toLowerCase());
                }
            }
        }

        for (String languageCode : languageCodes) {
            LanguageDefinition ld = (LanguageDefinition)
                    select("mods:language", mods);

            if (ld == null) {
                ld = mods.addNewLanguage();
                signalChanges(MODS_CHANGES);
            }

            final String query = String.format("//mods:language/mods:languageTerm[@authority='%s' and @type='%s' and text()='%s']",
                    "iso639-2b", "code", languageCode);
            if (!nodeExists(query, ld)) {
                LanguageTermDefinition lngtd = ld.addNewLanguageTerm();
                lngtd.setAuthority(ISO_639_2_B);
                lngtd.setType(CODE);
                lngtd.setStringValue(languageCode);
                signalChanges(MODS_CHANGES);
            }
        }
    }

}
