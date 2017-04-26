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

    static final String MODS_CHANGES = "MODS_CHANGES";
    static final String SLUB_INFO_CHANGES = "SLUB-INFO_CHANGES";

    private boolean modsChanges;
    private boolean slubChanges;

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

        modsChanges = (boolean) exchange.getProperty(MODS_CHANGES, false);
        slubChanges = (boolean) exchange.getProperty(SLUB_INFO_CHANGES, false);

        try {
            OpusDocument opusXmlObject = getOpusDocument(m);
            ModsDocument modsXmlObject = getModsDocument(m);
            InfoDocument infoXmlObject = getInfoDocument(m);

            process(opusXmlObject.getOpus().getOpusDocument(),
                    modsXmlObject.getMods(),
                    infoXmlObject.getInfo());
        } catch (RuntimeException rte) {
            throw new Exception("Processor failed with RuntimeException", rte);
        }

        exchange.getIn().setBody(m);
        exchange.setProperty(MODS_CHANGES, modsChanges);
        exchange.setProperty(SLUB_INFO_CHANGES, slubChanges);
    }

    private InfoDocument getInfoDocument(Map m) {
        InfoDocument infoXmlObject = (InfoDocument) m.get("SLUB-INFO");
        if (infoXmlObject != null) {
            if (infoXmlObject.getInfo() == null) {
                throw new IllegalArgumentException("SLUB-INFO XML has no <info> element");
            }
        } else {
            throw new IllegalArgumentException("SLUB-INFO XML is missing");
        }
        return infoXmlObject;
    }

    private ModsDocument getModsDocument(Map m) {
        ModsDocument modsXmlObject = (ModsDocument) m.get("MODS");
        if (modsXmlObject != null) {
            if (modsXmlObject.getMods() == null) {
                throw new IllegalArgumentException("MODS XML has no <mods> element");
            }
        } else {
            throw new IllegalArgumentException("MODS XML is missing");
        }
        return modsXmlObject;
    }

    private OpusDocument getOpusDocument(Map m) {
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
        return opusXmlObject;
    }

    public void process(Document opus, ModsDefinition mods, InfoType info) throws Exception {
        if (aim.mapCompletedDate(opus.getCompletedDate(), mods)) signalChanges(MODS_CHANGES);
        if (aim.mapDefaultPublisherInfo(opus, mods)) signalChanges(MODS_CHANGES);
        if (cim.mapPersonSubmitter(opus.getPersonSubmitterArray(), info)) signalChanges(SLUB_INFO_CHANGES);
        if (cim.mapNotes(opus.getNoteArray(), info)) signalChanges(SLUB_INFO_CHANGES);
        if (aim.mapVgWortopenKey(opus, info)) signalChanges(SLUB_INFO_CHANGES);
        if (cm.mapTitleAbstract(opus, mods)) signalChanges(MODS_CHANGES);
        if (cm.mapSubject("ddc", opus, mods)) signalChanges(MODS_CHANGES);
        if (cm.mapSubject("rvk", opus, mods)) signalChanges(MODS_CHANGES);
        if (cm.mapSubject("swd", opus, mods)) signalChanges(MODS_CHANGES);
        if (cm.mapSubject("uncontrolled", opus, mods)) signalChanges(MODS_CHANGES);
        if (cm.mapTableOfContent(opus, mods)) signalChanges(MODS_CHANGES);
        if (cm.mapIssue(opus, mods)) signalChanges(MODS_CHANGES);
        if (dm.mapDocumentType(opus, info)) signalChanges(SLUB_INFO_CHANGES);
        if (im.mapIdentifiers(opus, mods)) signalChanges(MODS_CHANGES);
        if (institutionsMapping.mapOrgansiations(opus, mods)) signalChanges(MODS_CHANGES);
        if (pm.mapPersons(opus.getPersonAuthorArray(), mods)) signalChanges(MODS_CHANGES);
        if (pm.mapPersons(opus.getPersonAdvisorArray(), mods)) signalChanges(MODS_CHANGES);
        if (pm.mapPersons(opus.getPersonContributorArray(), mods)) signalChanges(MODS_CHANGES);
        if (pm.mapPersons(opus.getPersonEditorArray(), mods)) signalChanges(MODS_CHANGES);
        if (pm.mapPersons(opus.getPersonRefereeArray(), mods)) signalChanges(MODS_CHANGES);
        if (pm.mapPersons(opus.getPersonOtherArray(), mods)) signalChanges(MODS_CHANGES);
        if (pm.mapPersons(opus.getPersonTranslatorArray(), mods)) signalChanges(MODS_CHANGES);
        if (publicationInfoMapping.mapLanguageElement(opus, mods)) signalChanges(MODS_CHANGES);
        if (publicationInfoMapping.mapOriginInfoElements(opus, mods)) signalChanges(MODS_CHANGES);
        if (rm.mapSeriesReference(opus, mods)) signalChanges(MODS_CHANGES);
        if (rm.mapHostAndPredecessorReferences(opus, mods)) signalChanges(MODS_CHANGES);
        if (rm.mapExternalReferenceElements(opus.getReferenceUrlArray(), "url", mods)) signalChanges(MODS_CHANGES);
        if (rm.mapExternalReferenceElements(opus.getReferenceIsbnArray(), "isbn", mods)) signalChanges(MODS_CHANGES);
        if (rm.mapExternalReferenceElements(opus.getReferenceIssnArray(), "issn", mods)) signalChanges(MODS_CHANGES);
        if (rightsMapping.mapFileAttachments(opus, info)) signalChanges(SLUB_INFO_CHANGES);
        if (sourceMapping.mapSource(opus, mods)) signalChanges(MODS_CHANGES);
        if (tim.ensureEdition(mods)) signalChanges(MODS_CHANGES);
        if (tim.ensurePhysicalDescription(mods)) signalChanges(MODS_CHANGES);
        if (aim.ensureRightsAgreement(info)) signalChanges(SLUB_INFO_CHANGES);
        if (tm.mapTitleMainElements(opus, mods)) signalChanges(MODS_CHANGES);
        if (tm.mapTitleSubElements(opus, mods)) signalChanges(MODS_CHANGES);
        if (tm.mapTitleAlternativeElements(opus, mods)) signalChanges(MODS_CHANGES);
        if (tm.mapTitleParentElements(opus, mods)) signalChanges(MODS_CHANGES);
    }

    void signalChanges(String dsid) {
        if (dsid.equals(MODS_CHANGES)) {
            this.modsChanges = true;
        } else if (dsid.equals(SLUB_INFO_CHANGES)) {
            this.slubChanges = true;
        }
    }

}
