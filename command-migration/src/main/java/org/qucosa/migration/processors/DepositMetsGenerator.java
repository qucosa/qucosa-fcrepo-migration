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

package org.qucosa.migration.processors;

import com.github.fge.uritemplate.URITemplate;
import com.github.fge.uritemplate.URITemplateException;
import com.github.fge.uritemplate.vars.VariableMap;
import de.slubDresden.InfoDocument;
import gov.loc.mets.AmdSecType;
import gov.loc.mets.FileType;
import gov.loc.mets.FileType.FLocat;
import gov.loc.mets.MdSecType;
import gov.loc.mets.MdSecType.MdWrap;
import gov.loc.mets.MetsDocument;
import gov.loc.mets.MetsDocument.Mets;
import gov.loc.mets.MetsType;
import gov.loc.mets.MetsType.FileSec.FileGrp;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.ModsDocument;
import gov.loc.mods.v3.StringPlusLanguage;
import gov.loc.mods.v3.TitleInfoDefinition;
import noNamespace.Document;
import noNamespace.File;
import noNamespace.Hash;
import noNamespace.OpusDocument;
import noNamespace.Title;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlString;
import org.qucosa.migration.mappings.ChangeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static gov.loc.mets.FileType.FLocat.LOCTYPE.URL;
import static gov.loc.mets.MdSecType.MdWrap.MDTYPE;
import static gov.loc.mets.MetsType.FileSec;
import static noNamespace.Document.ServerState.DELETED;
import static noNamespace.Document.ServerState.PUBLISHED;
import static noNamespace.Document.ServerState.UNPUBLISHED;

public class DepositMetsGenerator implements Processor {

    private static final String METS_SCHEMA_LOCATION = "http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd";
    private static final String MODS_SCHEMA_LOCATION = "http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-6.xsd";
    private static final Logger log = LoggerFactory.getLogger(DepositMetsGenerator.class);
    private static final XmlOptions xmlOptions;

    static {
        xmlOptions = new XmlOptions();
        xmlOptions.setSavePrettyPrint();
        xmlOptions.setSaveAggressiveNamespaces();
        xmlOptions.setSaveSuggestedPrefixes(new HashMap<String, String>() {{
            put("http://www.loc.gov/METS/", "mets");
            put("http://www.loc.gov/mods/v3", "mods");
        }});
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message msg = exchange.getIn();

        MetsDocument metsDocument = MetsDocument.Factory.newInstance();
        Mets metsRecord = metsDocument.addNewMets();
        addXsiSchemaLocation(metsRecord, String.format("%s %s",
                METS_SCHEMA_LOCATION, MODS_SCHEMA_LOCATION));

        boolean hasBody = false;
        if (msg.getBody() instanceof Map) {
            Map m = msg.getBody(Map.class);
            ModsDocument modsDocument = (ModsDocument) m.get("MODS");
            ChangeLog changeLog = exchange.getProperty("CHANGELOG", ChangeLog.class);

            if (changeLog.hasModsChanges() && modsDocument != null) {
                embedMods(metsRecord, modsDocument);
                hasBody = true;
            }

            InfoDocument infoDocument = (InfoDocument) m.get("SLUB-INFO");
            if (changeLog.hasSlubInfoChanges() && infoDocument != null) {
                embedInfo(metsRecord, infoDocument);
                hasBody = true;
            }
        } else {
            OpusDocument opusDocument = msg.getBody(OpusDocument.class);
            embedQucosaXml(metsRecord, opusDocument);
            embedMetsHeader(metsRecord, opusDocument);
            generateBasicMods(metsRecord, opusDocument);
            URL fileUrl = new URL(msg.getHeader("Qucosa-File-Url").toString());
            attachUploadFileSections(metsRecord, opusDocument, fileUrl);
            hasBody = true;
        }

        if (hasBody) {
            msg.setBody(metsDocument);
        } else {
            msg.setBody(null);
        }

        if (hasBody && log.isDebugEnabled()) {
            log.debug("\n" + metsDocument.xmlText(xmlOptions));
        }

    }

    private void attachUploadFileSections(Mets metsRecord, OpusDocument opusDocument, java.net.URL baseFileUrl)
            throws Exception {
        FileSec fileSec = metsRecord.addNewFileSec();
        FileGrp fileGrp = fileSec.addNewFileGrp();

        fileGrp.setUSE("ORIGINAL");

        int i = 0;
        for (noNamespace.File opusFile : opusDocument.getOpus().getOpusDocument().getFileArray()) {
            FileType metsFile = fileGrp.addNewFile();
            metsFile.setID("ATT-" + i);
            i++;
            metsFile.setMIMETYPE(opusFile.getMimeType());

            String fileLabel = opusFile.getLabel();
            if (fileLabel == null || fileLabel.isEmpty()) {
                fileLabel = opusFile.getPathName();
            }
            addMextLabelAttribute(fileLabel, metsFile);

            Hash bestHash = selectBestHash(opusFile);
            if (bestHash != null) {
                metsFile.setCHECKSUM(bestHash.getValue());
                metsFile.setCHECKSUMTYPE(
                        FileType.CHECKSUMTYPE.Enum.forString(
                                translateQucosaHashType(bestHash.getType())));
            }

            FLocat fLocat = metsFile.addNewFLocat();
            URI href = buildProperlyEscapedURI(baseFileUrl,
                    opusDocument.getOpus().getOpusDocument().getDocumentId(),
                    opusFile.getPathName());
            fLocat.setLOCTYPE(URL);
            fLocat.setHref(href.toASCIIString());
        }
    }

    private void addMextLabelAttribute(String label, FileType metsFile) {
        final XmlCursor cursor = metsFile.newCursor();
        cursor.toLastAttribute();
        cursor.insertAttributeWithValue(
                "LABEL", "http://slub-dresden.de/mets", label);
        cursor.dispose();
    }

    private String translateQucosaHashType(String qucosaHashType) throws Exception {
        switch (qucosaHashType) {
            case "md5":
                return "MD5";
            case "sha512":
                return "SHA-512";
            default:
                throw new Exception("Unknown hash type: " + qucosaHashType);
        }
    }

    private Hash selectBestHash(File opusFile) {
        Hash bestHash = null;
        for (Hash h : opusFile.getHashValueArray()) {
            if (bestHash == null) {
                bestHash = h;
            } else {
                if (h.getType().equals("sha512")) {
                    bestHash = h;
                }
            }
        }
        return bestHash;
    }

    private URI buildProperlyEscapedURI(URL baseFileUrl, String documentId, String opusFilePathName)
            throws URITemplateException, URISyntaxException {
        final VariableMap vars = VariableMap.newBuilder()
                .addScalarValue("documentId", documentId)
                .addScalarValue("pathName", opusFilePathName)
                .freeze();
        final URITemplate template = new URITemplate(baseFileUrl.toExternalForm() + "/{documentId}/{pathName}");
        return template.toURI(vars);
    }

    private void generateBasicMods(Mets metsDocument, OpusDocument opusDocument) {
        MdSecType dmdSection = metsDocument.addNewDmdSec();
        dmdSection.setID("MODS_XML");
        MdWrap mdWrap = dmdSection.addNewMdWrap();
        mdWrap.setMDTYPE(MDTYPE.MODS);
        mdWrap.setMIMETYPE("application/mods+xml");

        final ModsDocument modsDocument = ModsDocument.Factory.newInstance();
        final ModsDefinition modsRecord = modsDocument.addNewMods();

        String lang;
        String title;

        final Title[] titles = opusDocument.getOpus().getOpusDocument().getTitleMainArray();
        if (titles.length > 0) {
            lang = titles[0].getLanguage();
            title = titles[0].getValue();
        } else {
            lang = "";
            title = "";
        }

        final TitleInfoDefinition titleInfo = modsRecord.addNewTitleInfo();
        titleInfo.setLang(lang);
        titleInfo.setUsage(XmlString.Factory.newValue("primary"));
        StringPlusLanguage mt = titleInfo.addNewTitle();
        mt.setStringValue(title);

        mdWrap.addNewXmlData().set(modsDocument);
    }

    private void embedMetsHeader(Mets metsRecord, OpusDocument opusDocument) {
        final Document.ServerState.Enum opusState = opusDocument.getOpus().getOpusDocument().getServerState();
        String fedoraState = null;
        if (PUBLISHED.equals(opusState)) {
            fedoraState = "ACTIVE";
        } else if (UNPUBLISHED.equals(opusState)) {
            fedoraState = "INACTIVE";
        } else if (DELETED.equals(opusState)) {
            fedoraState = "DELETED";
        }
        if (fedoraState != null) {
            MetsType.MetsHdr metsHeader = metsRecord.addNewMetsHdr();
            metsHeader.setRECORDSTATUS(fedoraState);
        }
    }

    private void embedQucosaXml(Mets metsRecord, OpusDocument opusDocument) {
        MdSecType dmdSection = metsRecord.addNewDmdSec();
        dmdSection.setID("QUCOSA_XML");
        MdWrap mdWrap = dmdSection.addNewMdWrap();
        mdWrap.setMDTYPE(MDTYPE.OTHER);
        mdWrap.setOTHERMDTYPE("QUCOSA-XML");
        mdWrap.setMIMETYPE("application/xml");
        mdWrap.addNewXmlData().set(opusDocument);
    }

    private void embedMods(Mets metsRecord, ModsDocument modsDocument) {
        MdSecType dmdSection = metsRecord.addNewDmdSec();
        dmdSection.setID("MODS_XML");
        MdWrap mdWrap = dmdSection.addNewMdWrap();
        mdWrap.setMDTYPE(MDTYPE.MODS);
        mdWrap.setMIMETYPE("application/mods+xml");
        mdWrap.addNewXmlData().set(modsDocument);
    }

    private void embedInfo(Mets metsRecord, InfoDocument infoDocument) {
        AmdSecType amdSection = metsRecord.addNewAmdSec();
        amdSection.setID("AMD_SLUB-INFO");

        MdSecType techMd = amdSection.addNewTechMD();
        techMd.setID("TECH_SLUB-INFO");

        MdWrap mdWrap = techMd.addNewMdWrap();
        mdWrap.setMDTYPE(MDTYPE.OTHER);
        mdWrap.setOTHERMDTYPE("SLUBINFO");
        mdWrap.setMIMETYPE("application/vnd.slub-info+xml");
        mdWrap.addNewXmlData().set(infoDocument);
    }

    private void addXsiSchemaLocation(XmlObject xml, String schemaLocation) {
        xml.newCursor().setAttributeText(
                new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"),
                schemaLocation);
    }

}
