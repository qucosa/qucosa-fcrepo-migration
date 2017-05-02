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
import de.slubDresden.InfoType;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.Document;
import noNamespace.OpusDocument;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.qucosa.migration.mappings.AdministrativeInformationMapping;
import org.qucosa.migration.mappings.ChangeLog;
import org.qucosa.migration.mappings.ContactInformationMapping;
import org.qucosa.migration.mappings.ContentualMapping;
import org.qucosa.migration.mappings.DocumentTypeMapping;
import org.qucosa.migration.mappings.IdentifierMapping;
import org.qucosa.migration.mappings.InstitutionsMapping;
import org.qucosa.migration.mappings.PersonMapping;
import org.qucosa.migration.mappings.PublicationInfoMapping;
import org.qucosa.migration.mappings.ReferencesMapping;
import org.qucosa.migration.mappings.RightsMapping;
import org.qucosa.migration.mappings.SourceMapping;
import org.qucosa.migration.mappings.TechnicalInformationMapping;
import org.qucosa.migration.mappings.TitleMapping;

import java.util.Map;

public class MappingProcessor implements Processor {

    private final AdministrativeInformationMapping aim = new AdministrativeInformationMapping();
    private final ContactInformationMapping cim = new ContactInformationMapping();
    private final ContentualMapping cm = new ContentualMapping();
    private final DocumentTypeMapping dm = new DocumentTypeMapping();
    private final IdentifierMapping im = new IdentifierMapping();
    private final InstitutionsMapping institutionsMapping = new InstitutionsMapping();
    private final PersonMapping pm = new PersonMapping();
    private final PublicationInfoMapping publicationInfoMapping = new PublicationInfoMapping();
    private final RightsMapping rightsMapping = new RightsMapping();
    private final ReferencesMapping rm = new ReferencesMapping();
    private final SourceMapping sourceMapping = new SourceMapping();
    private final TechnicalInformationMapping tim = new TechnicalInformationMapping();
    private final TitleMapping tm = new TitleMapping();

    @Override
    public void process(Exchange exchange) throws Exception {
        Map m = (Map) exchange.getIn().getBody();
        ChangeLog changelog = new ChangeLog();

        try {
            process(getOpusDocument(m), getModsDocument(m), getInfoDocument(m), changelog);
            exchange.getIn().setBody(m);
            exchange.setProperty("CHANGELOG", changelog);
        } catch (RuntimeException rte) {
            throw new Exception("Processor failed with RuntimeException", rte);
        }
    }

    public void process(Document opus, ModsDefinition mods, InfoType info, ChangeLog changeLog) throws Exception {
        aim.mapCompletedDate(opus.getCompletedDate(), mods, changeLog);
        aim.mapDefaultPublisherInfo(opus, mods, changeLog);

        cim.mapPersonSubmitter(opus.getPersonSubmitterArray(), info, changeLog);
        cim.mapNotes(opus.getNoteArray(), info, changeLog);

        aim.mapVgWortopenKey(opus, info, changeLog);

        cm.mapTitleAbstract(opus, mods, changeLog);
        cm.mapSubject("ddc", opus, mods, changeLog);
        cm.mapSubject("rvk", opus, mods, changeLog);
        cm.mapSubject("swd", opus, mods, changeLog);
        cm.mapSubject("uncontrolled", opus, mods, changeLog);
        cm.mapTableOfContent(opus, mods, changeLog);
        cm.mapIssue(opus, mods, changeLog);

        dm.mapDocumentType(opus, info, changeLog);

        im.mapIdentifiers(opus, mods, changeLog);

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        pm.mapPersons(opus.getPersonAuthorArray(), mods, changeLog);
        pm.mapPersons(opus.getPersonAdvisorArray(), mods, changeLog);
        pm.mapPersons(opus.getPersonContributorArray(), mods, changeLog);
        pm.mapPersons(opus.getPersonEditorArray(), mods, changeLog);
        pm.mapPersons(opus.getPersonRefereeArray(), mods, changeLog);
        pm.mapPersons(opus.getPersonOtherArray(), mods, changeLog);
        pm.mapPersons(opus.getPersonTranslatorArray(), mods, changeLog);

        publicationInfoMapping.mapLanguageElement(opus, mods, changeLog);
        publicationInfoMapping.mapOriginInfoElements(opus, mods, changeLog);

        rm.mapSeriesReference(opus, mods, changeLog);

        rm.mapHostAndPredecessorReferences(opus, mods, changeLog);
        rm.mapExternalReferenceElements(opus.getReferenceUrlArray(), "url", mods, changeLog);
        rm.mapExternalReferenceElements(opus.getReferenceIsbnArray(), "isbn", mods, changeLog);
        rm.mapExternalReferenceElements(opus.getReferenceIssnArray(), "issn", mods, changeLog);

        rightsMapping.mapFileAttachments(opus, info, changeLog);

        sourceMapping.mapSource(opus, mods, changeLog);

        tim.ensureEdition(mods, changeLog);
        tim.ensurePhysicalDescription(mods, changeLog);

        aim.ensureRightsAgreement(info, changeLog);

        tm.mapTitleMainElements(opus, mods, changeLog);
        tm.mapTitleSubElements(opus, mods, changeLog);
        tm.mapTitleAlternativeElements(opus, mods, changeLog);
        tm.mapTitleParentElements(opus, mods, changeLog);
    }

    private InfoType getInfoDocument(Map m) {
        InfoDocument infoXmlObject = (InfoDocument) m.get("SLUB-INFO");
        if (infoXmlObject != null) {
            if (infoXmlObject.getInfo() == null) {
                throw new IllegalArgumentException("SLUB-INFO XML has no <info> element");
            }
        } else {
            throw new IllegalArgumentException("SLUB-INFO XML is missing");
        }
        return infoXmlObject.getInfo();
    }

    private ModsDefinition getModsDocument(Map m) {
        ModsDocument modsXmlObject = (ModsDocument) m.get("MODS");
        if (modsXmlObject != null) {
            if (modsXmlObject.getMods() == null) {
                throw new IllegalArgumentException("MODS XML has no <mods> element");
            }
        } else {
            throw new IllegalArgumentException("MODS XML is missing");
        }
        return modsXmlObject.getMods();
    }

    private Document getOpusDocument(Map m) {
        OpusDocument opusXmlObject;
        opusXmlObject = (OpusDocument) m.get("QUCOSA-XML");
        if (opusXmlObject != null) {
            if (opusXmlObject.getOpus() != null) {
                if (opusXmlObject.getOpus().getOpusDocument() == null) {
                    throw new IllegalArgumentException("QUCOSA XML has no <Opus>/<Opus_Document> element");
                }
            } else {
                throw new IllegalArgumentException("QUCOSA XML has no <Opus> element");
            }
        } else {
            throw new IllegalArgumentException("QUCOSA XML is missing");
        }
        return opusXmlObject.getOpus().getOpusDocument();
    }

}
