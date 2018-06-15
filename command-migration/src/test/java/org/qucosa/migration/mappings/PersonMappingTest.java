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

import gov.loc.mods.v3.NameDefinition;
import gov.loc.mods.v3.NamePartDefinition;
import noNamespace.Date;
import noNamespace.Person;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static gov.loc.mods.v3.NameDefinition.Type.PERSONAL;
import static gov.loc.mods.v3.NamePartDefinition.Type.DATE;
import static gov.loc.mods.v3.NamePartDefinition.Type.FAMILY;
import static gov.loc.mods.v3.NamePartDefinition.Type.GIVEN;
import static org.junit.Assert.assertTrue;

public class PersonMappingTest extends MappingTestBase {

    private PersonMapping personMapping;

    @Before
    public void setup() {
        personMapping = new PersonMapping();
    }

    @Test
    public void extractsAdvisor() {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "advisor", 1965, 11, 5);

        personMapping.mapPersons(opus.getPersonAdvisorArray(), mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='personal']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", mods);
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='dgs']", mods);
    }

    @Test
    public void extractsAuthor() {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "author", 1965, 11, 5);

        personMapping.mapPersons(opus.getPersonAuthorArray(), mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='personal']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", mods);
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='aut']", mods);
    }

    @Test
    public void extractsContributor() {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "contributor", 1965, 11, 5);

        personMapping.mapPersons(opus.getPersonContributorArray(), mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='personal']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", mods);
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='ctb']", mods);
    }

    @Test
    public void extractsEditor() {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "editor", 1965, 11, 5);

        personMapping.mapPersons(opus.getPersonEditorArray(), mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='personal']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", mods);
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='edt']", mods);
    }

    @Test
    public void extractsReferee() {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "referee", 1965, 11, 5);

        personMapping.mapPersons(opus.getPersonRefereeArray(), mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='personal']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", mods);
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='rev']", mods);
    }

    @Test
    public void extractsOther() {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "other", 1965, 11, 5);

        personMapping.mapPersons(opus.getPersonOtherArray(), mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='personal']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", mods);
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='oth']", mods);
    }

    @Test
    public void extractsTranslator() {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "translator", 1965, 11, 5);

        personMapping.mapPersons(opus.getPersonTranslatorArray(), mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='personal']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", mods);
        assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", mods);
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='trl']", mods);
    }

    @Test
    public void updatesNameparts() {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "author", 1965, 11, 5);

        {
            NameDefinition nd = mods.addNewName();
            nd.setType2(PERSONAL);
            {
                NamePartDefinition np = nd.addNewNamePart();
                np.setType(GIVEN);
                np.setStringValue("Hans");
            }
            {
                NamePartDefinition np = nd.addNewNamePart();
                np.setType(FAMILY);
                np.setStringValue("Mustermann");
            }
            {
                NamePartDefinition np = nd.addNewNamePart();
                np.setType(DATE);
                np.setStringValue("1965-11-05");
            }
        }
        {
            NameDefinition nd = mods.addNewName();
            nd.setType2(PERSONAL);
            NamePartDefinition np = nd.addNewNamePart();
            np.setType(FAMILY);
            np.setStringValue("Schneider");
        }

        personMapping.mapPersons(opus.getPersonAuthorArray(), mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='personal'" +
                " and mods:namePart[@type='family' and text()='Mustermann']" +
                " and mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']]", mods);
    }

    @Test
    public void extractsRole() {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "author", 1965, 11, 5);

        personMapping.mapPersons(opus.getPersonAuthorArray(), mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='personal']" +
                "/mods:role/mods:roleTerm[" +
                "@type='code' and @authority='marcrelator'" +
                " and @authorityURI='http://id.loc.gov/vocabulary/relators'" +
                " and text()='aut']", mods);
    }

    @Test
    public void extensionFoafElementLinksToNameElement() {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "author", 1965, 11, 5);

        personMapping.mapPersons(opus.getPersonAuthorArray(), mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:extension/foaf:Person[@rdf:about=//mods:name/@ID]", mods);
    }

    @Test
    public void Skip_extension_when_no_FOAF_information_available() {
        createPerson("Prof. Dr.", null, null, null, null, "Hans",
                "Mustermann", "author", 1965, 11, 5);

        personMapping.mapPersons(opus.getPersonAuthorArray(), mods, changeLog);

        assertXpathNotExists("//mods:extension/foaf:Person[@rdf:about=//mods:name/@ID]", mods);
    }

    @Test
    public void extractsFoafInfos() {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Musterstadt", "Hans", "Mustermann", "author", 1965,
                11, 5);

        personMapping.mapPersons(opus.getPersonAuthorArray(), mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:extension/foaf:Person[foaf:phone='+49(0)1234567890']", mods);
        assertXpathExists("//mods:extension/foaf:Person[foaf:mbox='mustermann@musteruni.de']", mods);
        assertXpathExists("//mods:extension/foaf:Person[foaf:gender='male']", mods);
        assertXpathExists("//mods:extension/foaf:Person[person:placeOfBirth='Musterstadt']", mods);
    }

    private void createPerson(String academicTitle, String gender, String phone, String email, String firstName,
                              String lastName, String role, int yearOfBirth, int monthOfBirth, int dayOfBirth) {
        createPerson(academicTitle, gender, phone, email, null, firstName,
                lastName, role, yearOfBirth, monthOfBirth, dayOfBirth);
    }

    private void createPerson(String academicTitle, String gender, String phone, String email, String birthplace, String firstName,
                              String lastName, String role, int yearOfBirth, int monthOfBirth, int dayOfBirth) {
        Person person;
        switch (role) {
            case "advisor":
                person = opus.addNewPersonAdvisor();
                break;
            case "author":
                person = opus.addNewPersonAuthor();
                break;
            case "contributor":
                person = opus.addNewPersonContributor();
                break;
            case "editor":
                person = opus.addNewPersonEditor();
                break;
            case "referee":
                person = opus.addNewPersonReferee();
                break;
            case "other":
                person = opus.addNewPersonOther();
                break;
            case "translator":
                person = opus.addNewPersonTranslator();
                break;
            default:
                person = opus.addNewPersonOther();
        }

        person.setAcademicTitle(academicTitle);
        {
            Date date = person.addNewDateOfBirth();
            date.setYear(BigInteger.valueOf(yearOfBirth));
            date.setMonth(BigInteger.valueOf(monthOfBirth));
            date.setDay(BigInteger.valueOf(dayOfBirth));
        }

        person.setGender(gender);
        person.setPhone(phone);
        person.setEmail(email);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setRole(role);
        if (birthplace != null && !birthplace.isEmpty()) person.setPlaceOfBirth(birthplace);
    }

}
