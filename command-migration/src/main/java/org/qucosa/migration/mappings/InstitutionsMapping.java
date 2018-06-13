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

import de.slubDresden.CorporationType;
import de.slubDresden.InfoDocument;
import de.slubDresden.InfoType;
import gov.loc.mods.v3.ExtensionDefinition;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.NameDefinition;
import gov.loc.mods.v3.NamePartDefinition;
import gov.loc.mods.v3.RoleDefinition;
import gov.loc.mods.v3.RoleTermDefinition;
import noNamespace.Document;
import noNamespace.Organisation;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import static gov.loc.mods.v3.CodeOrText.CODE;
import static gov.loc.mods.v3.NameDefinition.Type.CORPORATE;
import static org.qucosa.migration.mappings.ChangeLog.Type.MODS;
import static org.qucosa.migration.mappings.MappingFunctions.LOC_GOV_VOCABULARY_RELATORS;
import static org.qucosa.migration.mappings.MappingFunctions.buildTokenFrom;
import static org.qucosa.migration.mappings.MappingFunctions.firstOf;
import static org.qucosa.migration.mappings.MappingFunctions.mapOrganizationName;
import static org.qucosa.migration.mappings.MappingFunctions.singleline;
import static org.qucosa.migration.xml.XmlFunctions.formatXPath;
import static org.qucosa.migration.xml.XmlFunctions.insertNode;
import static org.qucosa.migration.xml.XmlFunctions.nodeExists;
import static org.qucosa.migration.xml.XmlFunctions.select;

public class InstitutionsMapping {

    private Map<String, String> institutionNameMap;

    public void setInstitutionNameMap(Map<String, String> institutionNameMap) {
        this.institutionNameMap = institutionNameMap;
    }

    public void mapOrgansiations(Document opus, ModsDefinition mods, ChangeLog changeLog) throws Exception {
        for (Organisation org : opus.getOrganisationArray()) {
            final Organisation.Type.Enum type = org.getType();
            final String place = org.getAddress();
            final String doctype = opus.getType();
            final String role = mapRoleToMarcRelator(doctype, type, org.getRole());

            final ArrayList<String> nameArray = buildNameArray(org);
            final String significantName = mapOrganizationName(singleline((String) firstOf(nameArray)), institutionNameMap);

            if (significantName != null) {
                nameArray.remove(0);
                final String token = buildTokenFrom("CORP_", significantName);

                NameDefinition nd = getNameDefinition(mods, token, changeLog);
                setNamePart(significantName, nd, changeLog);

                // FIXME Bad mapping hack to make up for inaptness of TYPO3 mapping configuration
                addMappingHack(nd, type, changeLog);

                RoleDefinition rd = getRoleDefinition(nd, changeLog);
                setRoleTerm(role, rd, changeLog);

                if (!nameArray.isEmpty() || !place.isEmpty()) {
                    ExtensionDefinition ed = getExtensionDefinition(mods, changeLog);
                    doSlubInfoExtension(type, place, nameArray, token, ed, changeLog);
                }
            }
        }
    }

    private void doSlubInfoExtension(
            Organisation.Type.Enum type,
            String place,
            ArrayList<String> names,
            String token,
            ExtensionDefinition ed,
            ChangeLog changeLog) throws Exception {

        boolean insertSlubInfo = false;

        InfoDocument id;
        InfoType it = (InfoType) select("slub:info", ed);
        if (it == null) {
            id = InfoDocument.Factory.newInstance();
            it = id.addNewInfo();
            changeLog.log(MODS);
            insertSlubInfo = true;
        } else {
            id = InfoDocument.Factory.parse(it.getDomNode());
        }

        CorporationType ct = (CorporationType) select(formatXPath("slub:corporation[@ref='%s']", token), it);
        if (ct == null) {
            ct = it.addNewCorporation();
            ct.setRef(token);
            changeLog.log(MODS);
        }

        final String mappedType = isUniversityUnit(type) ? "university" : "other";
        if (ct.getType() == null || !ct.getType().equals(mappedType)) {
            ct.setType(mappedType);
            changeLog.log(MODS);
        }

        if (ct.getPlace() == null || !ct.getPlace().equals(place)) {
            ct.setPlace(place);
            changeLog.log(MODS);
        }

        final LinkedList<String> otherHierarchy = new LinkedList<String>() {{
            add("section");
            add("section");
            add("section");
        }};

        final LinkedList<String> universityHierarchy = new LinkedList<String>() {{
            add("faculty");
            add("institute");
            add("chair");
        }};

        Iterator<String> hi = (Organisation.Type.OTHER.equals(type) ? otherHierarchy : universityHierarchy).listIterator();
        for (String name : names) {
            String hierarchyLevel = (hi.hasNext()) ? hi.next() : null;
            if (hierarchyLevel != null) {
                createOrganizationType(ct, hierarchyLevel, name, changeLog);
            }
        }

        if (insertSlubInfo) {
            insertNode(id, ed);
            changeLog.log(MODS);
        }
    }

    private void createOrganizationType(CorporationType ct, String hierarchy, String name, ChangeLog changeLog) throws XPathExpressionException {
        final String mappedName = mapOrganizationName(singleline(name), institutionNameMap);
        switch (hierarchy) {
            case "institution":
                if (!nodeExists(formatXPath("slub:institution[text()='%s']", mappedName), ct)) {
                    ct.addInstitution(mappedName);
                    changeLog.log(MODS);
                }
                break;
            case "section":
                if (!nodeExists(formatXPath("slub:section[text()='%s']", mappedName), ct)) {
                    ct.addSection(mappedName);
                    changeLog.log(MODS);
                }
                break;
            case "university":
                if (!nodeExists(formatXPath("slub:university[text()='%s']", mappedName), ct)) {
                    ct.addUniversity(mappedName);
                    changeLog.log(MODS);
                }
                break;
            case "faculty":
                if (!nodeExists(formatXPath("slub:faculty[text()='%s']", mappedName), ct)) {
                    ct.addFaculty(mappedName);
                    changeLog.log(MODS);
                }
                break;
            case "institute":
                if (!nodeExists(formatXPath("slub:institute[text()='%s']", mappedName), ct)) {
                    ct.addInstitute(mappedName);
                    changeLog.log(MODS);
                }
                break;
            case "chair":
                if (!nodeExists(formatXPath("slub:chair[text()='%s']", mappedName), ct)) {
                    ct.addChair(mappedName);
                    changeLog.log(MODS);
                }
                break;
        }
    }

    private ExtensionDefinition getExtensionDefinition(ModsDefinition mods, ChangeLog changeLog) {
        ExtensionDefinition ed = (ExtensionDefinition) select("mods:extension", mods);
        if (ed == null) {
            ed = mods.addNewExtension();
            changeLog.log(MODS);
        }
        return ed;
    }

    private void setRoleTerm(String role, RoleDefinition rd, ChangeLog changeLog) {
        RoleTermDefinition rtd = (RoleTermDefinition) select(formatXPath("mods:roleTerm[text()='%s']", role), rd);
        if (rtd == null) {
            rtd = rd.addNewRoleTerm();
            rtd.setType(CODE);
            rtd.setAuthority("marcrelator");
            rtd.setAuthorityURI(LOC_GOV_VOCABULARY_RELATORS);
            rtd.setStringValue(role);
            changeLog.log(MODS);
        }
    }

    private RoleDefinition getRoleDefinition(NameDefinition nd, ChangeLog changeLog) {
        RoleDefinition rd = (RoleDefinition) select("mods:role", nd);
        if (rd == null) {
            rd = nd.addNewRole();
            changeLog.log(MODS);
        }
        return rd;
    }

    private void setNamePart(String significantName, NameDefinition nd, ChangeLog changeLog) {
        NamePartDefinition npd = (NamePartDefinition) select(
                formatXPath("mods:namePart[text()='%s']", significantName), nd);
        if (npd == null) {
            npd = nd.addNewNamePart();
            npd.setStringValue(significantName);
            changeLog.log(MODS);
        }
    }

    private NameDefinition getNameDefinition(ModsDefinition mods, String token, ChangeLog changeLog) {
        NameDefinition nd = (NameDefinition) select(formatXPath("mods:name[@ID='%s' and @type='corporate']", token), mods);
        if (nd == null) {
            nd = mods.addNewName();
            nd.setID(token);
            nd.setType2(CORPORATE);
            changeLog.log(MODS);
        }
        return nd;
    }

    private void addMappingHack(NameDefinition nd, Organisation.Type.Enum type, ChangeLog changeLog) {
        String mappingHack = "";
        if (Organisation.Type.OTHER.equals(type)) {
            mappingHack = "mapping-hack-other";
        } else if (isUniversityUnit(type)) {
            mappingHack = "mapping-hack-university";
        }
        if (!mappingHack.equals(nd.getDisplayLabel())) {
            nd.setDisplayLabel(mappingHack);
            changeLog.log(MODS);
        }
    }

    private ArrayList<String> buildNameArray(Organisation org) {
        ArrayList<String> names = new ArrayList<>();
        addIfNotEmpty(org.getFirstLevelName(), names);
        addIfNotEmpty(org.getSecondLevelName(), names);
        addIfNotEmpty(org.getThirdLevelName(), names);
        addIfNotEmpty(org.getFourthLevelName(), names);
        return names;
    }

    private void addIfNotEmpty(String s, ArrayList<String> ss) {
        if (s != null && !s.isEmpty()) ss.add(s);
    }

    private String mapRoleToMarcRelator(String doctype, Organisation.Type.Enum orgType, String role) {
        if (isUniversityUnit(orgType)) {
            if ("publisher".equals(role)) {
                if (isPaperOrThesis(doctype)) {
                    return "dgg";
                } else {
                    return "pbl";
                }
            } else {
                return "edt";
            }
        } else {
            if ("publisher".equals(role)) {
                return "pbl";
            } else if ("contributor".equals(role)) {
                return "edt";
            }
        }
        return null;
    }

    private boolean isUniversityUnit(Organisation.Type.Enum orgType) {
        return (Organisation.Type.UNIVERSITY.equals(orgType)
                || Organisation.Type.CHAIR.equals(orgType)
                || Organisation.Type.FACULTY.equals(orgType)
                || Organisation.Type.INSTITUTE.equals(orgType));
    }

    private boolean isPaperOrThesis(String doctype) {
        return doctype != null && ("paper".equals(doctype) || doctype.contains("_thesis"));
    }

}
