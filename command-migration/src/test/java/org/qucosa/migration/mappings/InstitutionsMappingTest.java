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

import noNamespace.Organisation;
import org.junit.Test;

import java.util.HashMap;

import static noNamespace.Organisation.Type.CHAIR;
import static noNamespace.Organisation.Type.FACULTY;
import static noNamespace.Organisation.Type.INSTITUTE;
import static noNamespace.Organisation.Type.OTHER;
import static noNamespace.Organisation.Type.UNIVERSITY;
import static org.junit.Assert.assertTrue;

public class InstitutionsMappingTest extends MappingTestBase {

    private InstitutionsMapping institutionsMapping = new InstitutionsMapping();

    @Test
    public void extractsRole() throws Exception {
        createOrganisation(OTHER,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name/mods:role/mods:roleTerm[text()='pbl']", mods);
    }

    @Test
    public void corporationReferencesModsName() throws Exception {
        createOrganisation(OTHER,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@ref=//mods:name/@ID]", mods);
    }

    @Test
    public void No_new_corporation_reference_if_one_already_exists() throws Exception {
        createOrganisation(OTHER,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);
        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathNotExists("//mods:extension/slub:info/slub:corporation[@ref=//mods:name/@ID][2]", mods);
    }

    @Test
    public void Recognizes_type_other() throws Exception {
        createOrganisation(OTHER,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='other']", mods);
    }

    @Test
    public void Recognizes_type_university() throws Exception {
        createOrganisation(UNIVERSITY,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='university']", mods);
    }

    @Test
    public void Recognizes_type_chair_and_maps_to_university() throws Exception {
        createOrganisation(CHAIR,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='university']", mods);
    }

    @Test
    public void Recognizes_type_faculty_and_maps_to_university() throws Exception {
        createOrganisation(FACULTY,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='university']", mods);
    }

    @Test
    public void Recognizes_type_institute_and_maps_to_university() throws Exception {
        createOrganisation(INSTITUTE,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='university']", mods);
    }

    @Test
    public void extractsPlace() throws Exception {
        createOrganisation(OTHER,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@place='Chemnitz']", mods);
    }

    @Test
    public void extractsUnitsForTypeOther() throws Exception {
        createOrganisation(OTHER,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name/mods:namePart[text()='TU Chemnitz']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:section='Rektorat']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:section='Abteilung Foo']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:section='Gruppe Baz']", mods);
    }

    @Test
    public void extractsUnitsForTypeUniversity() throws Exception {
        createOrganisation(UNIVERSITY,
                "Dresden", "publisher", "Technische Universität Dresden", "Fakultät Informatik",
                "Institut für Systemarchitektur", "Professur für Datenbanken");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name/mods:namePart[text()='Technische Universität Dresden']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:faculty='Fakultät Informatik']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:institute='Institut für Systemarchitektur']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:chair='Professur für Datenbanken']", mods);
    }

    @Test
    public void extractsUnitsForTypeChair() throws Exception {
        createOrganisation(CHAIR,
                "Dresden", "publisher", "Technische Universität Dresden", "Fakultät Informatik",
                "Institut für Systemarchitektur", "Professur für Datenbanken");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name/mods:namePart[text()='Technische Universität Dresden']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:faculty='Fakultät Informatik']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:institute='Institut für Systemarchitektur']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:chair='Professur für Datenbanken']", mods);
    }

    @Test
    public void extractsUnitsForTypeFaculty() throws Exception {
        createOrganisation(FACULTY,
                "Dresden", "publisher", "Technische Universität Dresden", "Fakultät Informatik", null, null);

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name/mods:namePart[text()='Technische Universität Dresden']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:faculty='Fakultät Informatik']", mods);
    }

    @Test
    public void extractsUnitsForTypeInstitute() throws Exception {
        createOrganisation(INSTITUTE,
                "Dresden", "publisher", "Technische Universität Dresden", "Fakultät Informatik",
                "Institut für Systemarchitektur", null);

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name/mods:namePart[text()='Technische Universität Dresden']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:faculty='Fakultät Informatik']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:institute='Institut für Systemarchitektur']", mods);
    }

    @Test
    public void adds_mapping_hack_to_distinguish_other_corporations() throws Exception {
        createOrganisation(OTHER,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='corporate' and @displayLabel='mapping-hack-other']/mods:namePart[text()='TU Chemnitz']", mods);
    }

    @Test
    public void adds_mapping_hack_to_distinguish_universities() throws Exception {
        createOrganisation(UNIVERSITY,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Baz");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='corporate' and @displayLabel='mapping-hack-university']/mods:namePart[text()='TU Chemnitz']", mods);
    }

    @Test
    public void no_role_mapping_if_role_is_null() throws Exception {
        String firstLevelName = "Deutsche Zentralbibliothek für Blinde Leipzig (DZB)";
        createOrganisation(firstLevelName, "Leipzig", OTHER, null);

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='corporate']/mods:namePart[.='" + firstLevelName + "']", mods);
        assertXpathNotExists("//mods:name[@type='other']/mods:role", mods);
    }


    /*

        Reworking mapping bit by bit

     */


    @Test
    public void Type_other_role_contributor() throws Exception {
        createOrganisation("Landesamt für Umwelt", "Dresden", OTHER, "contributor");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='corporate']/mods:namePart[text()='Landesamt für Umwelt']", mods);
        assertXpathExists("//mods:name[@type='corporate']/mods:role/mods:roleTerm[text()='oth']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='other']", mods);
    }

    @Test
    public void Type_other_role_publisher() throws Exception {
        createOrganisation("Landesamt für Umwelt", "Dresden", OTHER, "publisher");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='corporate']/mods:namePart[text()='Landesamt für Umwelt']", mods);
        assertXpathExists("//mods:name[@type='corporate']/mods:role/mods:roleTerm[text()='pbl']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='other']", mods);
    }

    @Test
    public void Type_university_role_contributor() throws Exception {
        createOrganisation("Technische Universität Dresden", "Dresden", UNIVERSITY, "contributor");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='corporate']/mods:namePart[text()='Technische Universität Dresden']", mods);
        assertXpathExists("//mods:name[@type='corporate']/mods:role/mods:roleTerm[text()='oth']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='university']", mods);
    }

    @Test
    public void Type_university_role_publisher() throws Exception {
        createOrganisation("Technische Universität Dresden", "Dresden", UNIVERSITY, "publisher");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='corporate']/mods:namePart[text()='Technische Universität Dresden']", mods);
        assertXpathExists("//mods:name[@type='corporate']/mods:role/mods:roleTerm[text()='pbl']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='university']", mods);
    }

    @Test
    public void Type_university_role_publisher_thesis() throws Exception {
        setDocumentType("bachelor_thesis");
        createOrganisation("Technische Universität Dresden", "Dresden", UNIVERSITY, "publisher");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='corporate']/mods:namePart[text()='Technische Universität Dresden']", mods);
        assertXpathExists("//mods:name[@type='corporate']/mods:role/mods:roleTerm[text()='dgg']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='university']", mods);
    }

    @Test
    public void Type_university_role_publisher_paper() throws Exception {
        setDocumentType("paper");
        createOrganisation("Technische Universität Dresden", "Dresden", UNIVERSITY, "publisher");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='corporate']/mods:namePart[text()='Technische Universität Dresden']", mods);
        assertXpathExists("//mods:name[@type='corporate']/mods:role/mods:roleTerm[text()='dgg']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='university']", mods);
    }

    @Test
    public void Type_chair_role_contributor_mapped_same_as_university() throws Exception {
        createOrganisation("Technische Universität Dresden", "Dresden", CHAIR, "contributor");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='corporate']/mods:namePart[text()='Technische Universität Dresden']", mods);
        assertXpathExists("//mods:name[@type='corporate']/mods:role/mods:roleTerm[text()='oth']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='university']", mods);
    }

    @Test
    public void Type_faculty_role_publisher_mapped_same_as_university() throws Exception {
        createOrganisation("Technische Universität Dresden", "Dresden", FACULTY, "publisher");

        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='corporate']/mods:namePart[text()='Technische Universität Dresden']", mods);
        assertXpathExists("//mods:name[@type='corporate']/mods:role/mods:roleTerm[text()='pbl']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@type='university']", mods);
    }

    @Test
    public void Changes_institution_name_according_to_configured_map() throws Exception {
        createOrganisation(UNIVERSITY,
                "Chemnitz", "publisher", "TU Chemnitz", "Rektorat", "Abteilung Foo", "Gruppe Bar");

        institutionsMapping.setInstitutionNameMap(new HashMap<String, String>() {{
            put("TU Chemnitz", "Technische Universität Chemnitz");
        }});
        institutionsMapping.mapOrgansiations(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name/mods:namePart[text()='Technische Universität Chemnitz']", mods);
    }

    @Test
    public void Ignores_address_if_place_is_not_given() throws Exception {
        createOrganisation("Technische Universität Dresden", null, FACULTY, "publisher");
        institutionsMapping.mapOrgansiations(opus, mods, changeLog);
        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name/mods:namePart[text()='Technische Universität Dresden']", mods);
    }


    private void setDocumentType(String doctype) {
        opus.setType(doctype);
    }

    private void createOrganisation(String name, String place, Organisation.Type.Enum type, String role) {
        createOrganisation(type, place, role, name, null, null, null);
    }

    private void createOrganisation(
            Organisation.Type.Enum type, String address, String role, String firstLevelName,
            String secondLevelName, String thirdLevelName, String fourthLevelName) {
        Organisation org = opus.addNewOrganisation();
        org.setType(type);
        org.setAddress(address);
        org.setRole(role);
        org.setFirstLevelName(firstLevelName);
        org.setSecondLevelName(secondLevelName);
        org.setThirdLevelName(thirdLevelName);
        org.setFourthLevelName(fourthLevelName);

        // not mapped...
        org.setTudFisKeyFaculty("0");
        org.setTudFisKeyChair("0");
        org.setFreeSubmission(false);
    }

}
