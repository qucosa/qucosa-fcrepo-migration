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
import org.w3c.dom.Document;

import java.math.BigInteger;

import static gov.loc.mods.v3.NameDefinition.Type.PERSONAL;
import static gov.loc.mods.v3.NamePartDefinition.Type.DATE;
import static gov.loc.mods.v3.NamePartDefinition.Type.FAMILY;
import static gov.loc.mods.v3.NamePartDefinition.Type.GIVEN;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.junit.Assert.assertTrue;

public class PersonMappingTest extends MappingTestBase {

    private PersonMapping personMapping;

    @Before
    public void setup() {
        personMapping = new PersonMapping();
    }

    @Test
    public void extractsAdvisor() throws Exception {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "advisor", 1965, 11, 5);

        boolean result = personMapping.mapPersons(opus.getPersonAdvisorArray(), mods);

        assertTrue("Mapper should signal successful change", result);
        Document xml = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:name[@type='personal']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", xml);
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='dgs']", xml);
    }

    @Test
    public void extractsAuthor() throws Exception {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "author", 1965, 11, 5);

        boolean result = personMapping.mapPersons(opus.getPersonAuthorArray(), mods);

        assertTrue("Mapper should signal successful change", result);
        Document xml = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:name[@type='personal']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", xml);
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='aut']", xml);
    }

    @Test
    public void extractsContributor() throws Exception {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "contributor", 1965, 11, 5);

        boolean result = personMapping.mapPersons(opus.getPersonContributorArray(), mods);

        assertTrue("Mapper should signal successful change", result);
        Document xml = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:name[@type='personal']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", xml);
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='ctb']", xml);
    }

    @Test
    public void extractsEditor() throws Exception {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "editor", 1965, 11, 5);

        boolean result = personMapping.mapPersons(opus.getPersonEditorArray(), mods);

        assertTrue("Mapper should signal successful change", result);
        Document xml = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:name[@type='personal']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", xml);
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='pbl']", xml);
    }

    @Test
    public void extractsReferee() throws Exception {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "referee", 1965, 11, 5);

        boolean result = personMapping.mapPersons(opus.getPersonRefereeArray(), mods);

        assertTrue("Mapper should signal successful change", result);
        Document xml = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:name[@type='personal']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", xml);
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='rev']", xml);
    }

    @Test
    public void extractsOther() throws Exception {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "other", 1965, 11, 5);

        boolean result = personMapping.mapPersons(opus.getPersonOtherArray(), mods);

        assertTrue("Mapper should signal successful change", result);
        Document xml = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:name[@type='personal']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", xml);
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='oth']", xml);
    }

    @Test
    public void extractsTranslator() throws Exception {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "translator", 1965, 11, 5);

        boolean result = personMapping.mapPersons(opus.getPersonTranslatorArray(), mods);

        assertTrue("Mapper should signal successful change", result);
        Document xml = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:name[@type='personal']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='given' and text()='Hans']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='family' and text()='Mustermann']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']", xml);
        assertXpathExists("//mods:name/mods:namePart[@type='date' and text()='1965-11-05']", xml);
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='trl']", xml);
    }

    @Test
    public void updatesNameparts() throws Exception {
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

        boolean result = personMapping.mapPersons(opus.getPersonAuthorArray(), mods);

        assertTrue("Mapper should signal successful change", result);
        Document xml = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:name[@type='personal'" +
                " and mods:namePart[@type='family' and text()='Mustermann']" +
                " and mods:namePart[@type='termsOfAddress' and text()='Prof. Dr.']]", xml);
    }

    @Test
    public void extractsRole() throws Exception {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "author", 1965, 11, 5);

        boolean result = personMapping.mapPersons(opus.getPersonAuthorArray(), mods);

        assertTrue("Mapper should signal successful change", result);
        Document xml = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:name[@type='personal']" +
                "/mods:role/mods:roleTerm[" +
                "@type='code' and @authority='marcrelator'" +
                " and @authorityURI='http://id.loc.gov/vocabulary/relators'" +
                " and @valueURI='http://id.loc.gov/vocabulary/relators/aut'" +
                " and text()='aut']", xml);
    }

    @Test
    public void extensionFoafElementLinksToNameElement() throws Exception {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "author", 1965, 11, 5);

        boolean result = personMapping.mapPersons(opus.getPersonAuthorArray(), mods);

        assertTrue("Mapper should signal successful change", result);
        assertXpathExists("//mods:extension/foaf:Person[@rdf:about=//mods:name/@ID]",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void Skip_extension_when_no_FOAF_information_available() throws Exception {
        createPerson("Prof. Dr.", null, null, null, "Hans", "Mustermann", "author", 1965, 11, 5);

        personMapping.mapPersons(opus.getPersonAuthorArray(), mods);

        assertXpathNotExists("//mods:extension/foaf:Person[@rdf:about=//mods:name/@ID]",
                mods.getDomNode().getOwnerDocument());

    }

    @Test
    public void extractsFoafInfos() throws Exception {
        createPerson("Prof. Dr.", "m", "+49(0)1234567890", "mustermann@musteruni.de",
                "Hans", "Mustermann", "author", 1965, 11, 5);

        boolean result = personMapping.mapPersons(opus.getPersonAuthorArray(), mods);

        assertTrue("Mapper should signal successful change", result);
        Document xml = mods.getDomNode().getOwnerDocument();
        assertXpathExists("//mods:extension/foaf:Person[foaf:phone='+49(0)1234567890']", xml);
        assertXpathExists("//mods:extension/foaf:Person[foaf:mbox='mustermann@musteruni.de']", xml);
        assertXpathExists("//mods:extension/foaf:Person[foaf:gender='male']", xml);
    }

    private void createPerson(String academicTitle, String gender, String phone, String email, String firstName,
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
    }

}
