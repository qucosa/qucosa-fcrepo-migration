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

import com.xmlns.foaf.x01.PersonDocument;
import gov.loc.mods.v3.ExtensionDefinition;
import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.NameDefinition;
import gov.loc.mods.v3.NamePartDefinition;
import gov.loc.mods.v3.RoleDefinition;
import gov.loc.mods.v3.RoleTermDefinition;
import noNamespace.Person;

import javax.xml.namespace.QName;

import static gov.loc.mods.v3.CodeOrText.CODE;
import static gov.loc.mods.v3.NameDefinition.Type.PERSONAL;
import static gov.loc.mods.v3.NamePartDefinition.Type.DATE;
import static gov.loc.mods.v3.NamePartDefinition.Type.FAMILY;
import static gov.loc.mods.v3.NamePartDefinition.Type.GIVEN;
import static gov.loc.mods.v3.NamePartDefinition.Type.TERMS_OF_ADDRESS;
import static org.qucosa.migration.mappings.MappingFunctions.LOC_GOV_VOCABULARY_RELATORS;
import static org.qucosa.migration.mappings.MappingFunctions.buildTokenFrom;
import static org.qucosa.migration.mappings.MappingFunctions.dateEncoding;
import static org.qucosa.migration.mappings.MappingFunctions.singleline;
import static org.qucosa.migration.mappings.Namespaces.NS_RDF;
import static org.qucosa.migration.mappings.XmlFunctions.select;

public class PersonMapping {

    private final ThreadLocal<ChangeSignal> change = ThreadLocal.withInitial(() -> new ChangeSignal());

    public boolean mapPersons(Person[] persons, ModsDefinition mods) {
        change.get().reset();
        for (Person person : persons) {
            final String given = person.getFirstName();
            final String family = person.getLastName();
            final String termsOfAddress = person.getAcademicTitle();
            final String date = dateEncoding(person.getDateOfBirth());
            final String marcRoleTerm = marcrelatorEncoding(person.getRole());

            NameDefinition nd = findOrCreateNameDefinition(mods, given, family, date);
            setNameParts(given, family, termsOfAddress, date, nd);
            setNodeIdForReferencing(given, family, termsOfAddress, nd);
            setRole(marcRoleTerm, nd);
            setExtension(person, nd, mods);
        }
        return change.get().signaled();
    }

    private void setExtension(Person person, NameDefinition nd, ModsDefinition mods) {
        final String phone = emptyIfNull(person.getPhone());
        final String mbox = emptyIfNull(person.getEmail());
        final String gender = emptyIfNull(genderMapping(person.getGender()));

        if (phone.isEmpty() && mbox.isEmpty() && gender.isEmpty()) {
            return;
        }

        ExtensionDefinition ext = (ExtensionDefinition) select("mods:extension", mods);
        if (ext == null) {
            ext = mods.addNewExtension();
            change.get().signal();
        }

        PersonDocument.Person foafPerson = (PersonDocument.Person)
                select("foaf:Person[@about='" + nd.getID() + "']", mods);

        boolean _importPd = false;
        PersonDocument pd = PersonDocument.Factory.newInstance();

        if (foafPerson == null) {
            foafPerson = pd.addNewPerson();
            foafPerson.newCursor().setAttributeText(
                    new QName(NS_RDF, "about"), nd.getID());
            _importPd = true;
            change.get().signal();
        }

        if (!phone.isEmpty()) {
            if (foafPerson.getPhone() == null
                    || !foafPerson.getPhone().equals(phone)) {
                foafPerson.setPhone(phone);
                change.get().signal();
            }
        }

        if (!mbox.isEmpty()) {
            if (foafPerson.getMbox() == null
                    || !foafPerson.getMbox().equals(mbox)) {
                foafPerson.setMbox(mbox);
                change.get().signal();
            }
        }

        if (gender != null) {
            if (foafPerson.getGender() == null
                    || !foafPerson.getGender().equals(gender)) {
                foafPerson.setGender(gender);
                change.get().signal();
            }
        }

        if (_importPd) {
            ext.set(pd);
        }
    }

    private String emptyIfNull(String s) {
        return (s == null) ? "" : s;
    }

    private String genderMapping(String gender) {
        if (gender == null) return null;
        switch (gender) {
            case "m":
                return "male";
            case "f":
                return "female";
            default:
                return null;
        }
    }

    private void setRole(String marcRoleTerm, NameDefinition nd) {
        if (marcRoleTerm != null) {
            RoleDefinition rd = (RoleDefinition) select("mods:role", nd);
            if (rd == null) {
                rd = nd.addNewRole();
                change.get().signal();
            }

            RoleTermDefinition rtd = (RoleTermDefinition)
                    select(String.format("mods:roleTerm[@type='%s'" +
                                    " and @authority='%s'" +
                                    " and @authorityURI='%s'" +
                                    " and @valueURI='%s'" +
                                    " and text()='%s']",
                            "code", "marcrelator",
                            LOC_GOV_VOCABULARY_RELATORS,
                            LOC_GOV_VOCABULARY_RELATORS + "/" + marcRoleTerm, marcRoleTerm), rd);

            if (rtd == null) {
                rtd = rd.addNewRoleTerm();
                rtd.setType(CODE);
                rtd.setAuthority("marcrelator");
                rtd.setAuthorityURI(LOC_GOV_VOCABULARY_RELATORS);
                rtd.setValueURI(LOC_GOV_VOCABULARY_RELATORS + "/" + marcRoleTerm);
                rtd.setStringValue(marcRoleTerm);
                change.get().signal();
            }
        }
    }

    private void setNameParts(String given, String family, String termsOfAddress, String date, NameDefinition nd) {
        if (given != null && !given.isEmpty()) {
            checkOrSetNamePart(GIVEN, given, nd);
        }
        if (family != null && !family.isEmpty()) {
            checkOrSetNamePart(FAMILY, family, nd);
        }
        if (termsOfAddress != null && !termsOfAddress.isEmpty()) {
            checkOrSetNamePart(TERMS_OF_ADDRESS, termsOfAddress, nd);
        }
        if (date != null) {
            checkOrSetNamePart(DATE, date, nd);
        }
    }

    private void setNodeIdForReferencing(String given, String family, String termsOfAddress, NameDefinition nd) {
        String ndid = nd.getID();
        if (ndid == null || ndid.isEmpty()) {
            String token = buildTokenFrom("PERS_", given, family, termsOfAddress);
            nd.setID(token);
            change.get().signal();
        }
    }

    private NameDefinition findOrCreateNameDefinition(ModsDefinition mods, String given, String family, String date) {
        StringBuilder sb = new StringBuilder();
        sb.append("mods:name[");
        sb.append("@type='personal'");
        final String mGiven = singleline(given);
        if (mGiven != null && !mGiven.isEmpty()) {
            sb.append(" and mods:namePart[@type='given' and text()='" + mGiven + "']");
        }
        final String mFamily = singleline(family);
        if (mFamily != null && !mFamily.isEmpty()) {
            sb.append(" and mods:namePart[@type='family' and text()='" + mFamily + "']");
        }
        if (date != null) {
            sb.append(" and mods:namePart[@type='date' and text()='" + date + "']");
        }
        sb.append(']');

        NameDefinition nd = (NameDefinition)
                select(sb.toString(), mods);

        if (nd == null) {
            nd = mods.addNewName();
            nd.setType2(PERSONAL);
            change.get().signal();
        }
        return nd;
    }

    private void checkOrSetNamePart(NamePartDefinition.Type.Enum type, String value, NameDefinition nd) {
        final String mValue = singleline(value);
        NamePartDefinition np = (NamePartDefinition)
                select("mods:namePart[@type='" + type + "' and text()='" + mValue + "']", nd);

        if (np == null) {
            np = nd.addNewNamePart();
            np.setType(type);
            np.setStringValue(mValue);
            change.get().signal();
        }
    }

    private String marcrelatorEncoding(String role) {
        switch (role) {
            case "advisor":
                return "dgs";
            case "author":
                return "aut";
            case "contributor":
                return "ctb";
            case "editor":
                return "pbl";
            case "referee":
                return "rev";
            case "other":
                return "oth";
            case "translator":
                return "trl";
            default:
                return null;
        }
    }

}
