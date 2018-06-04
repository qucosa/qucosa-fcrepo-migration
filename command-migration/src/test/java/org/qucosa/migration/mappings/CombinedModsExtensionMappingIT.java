package org.qucosa.migration.mappings;

import noNamespace.Date;
import noNamespace.Organisation;
import noNamespace.Person;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CombinedModsExtensionMappingIT extends MappingTestBase {

    private AdministrativeInformationMapping aim;
    private InstitutionsMapping im;
    private PersonMapping pm;

    @Before
    public void setup() {
        aim = new AdministrativeInformationMapping();
        im = new InstitutionsMapping();
        pm = new PersonMapping();
    }

    @Test
    public void Mappers_generate_default_publisher_and_organisation_extension_element() throws Exception {
        setupPublisher();
        Organisation org = setupOrganisation();

        executeMappings();

        assertModsChanges();
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@ref=//mods:name/@ID and @type='other']" +
                "/slub:section[text()='" + org.getSecondLevelName() + "']", mods);
    }

    @Test
    public void Mappers_generate_organisation_and_person_extension_element() throws Exception {
        Organisation org = setupOrganisation();
        setupPerson();

        executeMappings();

        assertModsChanges();
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@ref=//mods:name[@type='corporate']/@ID]" +
                "/slub:section[text()='" + org.getSecondLevelName() + "']", mods);
        assertXpathExists("//mods:extension/foaf:Person[@rdf:about=//mods:name[@type='personal']/@ID]", mods);
    }


    private void assertModsChanges() {
        assertTrue("Mapper should log change for MODS", changeLog.hasModsChanges());
        assertFalse("Mapper should not log change for SLUB-INFO", changeLog.hasSlubInfoChanges());
    }

    private void executeMappings() throws Exception {
        pm.mapPersons(opus.getPersonAuthorArray(), mods, changeLog);
        im.mapOrgansiations(opus, mods, changeLog);
        aim.mapDefaultPublisherInfo(opus, mods, changeLog);
    }

    private void setupPerson() {
        Person person = opus.addNewPersonAuthor();
        person.setAcademicTitle("Prof. Dr.");
        {
            Date date = person.addNewDateOfBirth();
            date.setYear(BigInteger.valueOf(1965));
            date.setMonth(BigInteger.valueOf(11));
            date.setDay(BigInteger.valueOf(5));
        }
        person.setGender("m");
        person.setPhone("+49(0)1234567890");
        person.setEmail("mustermann@musteruni.de");
        person.setFirstName("Hans");
        person.setLastName("Mustermann");
        person.setRole("author");
    }

    private Organisation setupOrganisation() {
        Organisation org = opus.addNewOrganisation();
        org.setType(Organisation.Type.OTHER);
        org.setAddress("Chemnitz");
        org.setRole("publisher");
        org.setFirstLevelName("TU Chemnitz");
        org.setSecondLevelName("Rektorat");
        org.setThirdLevelName("Abteilung Foo");
        org.setFourthLevelName("Gruppe Baz");
        org.setTudFisKeyFaculty("0");
        org.setTudFisKeyChair("0");
        org.setFreeSubmission(false);
        return org;
    }

    private void setupPublisher() {
        opus.setPublisherName("Saechsische Landesbibliothek- Staats- und Universitaetsbibliothek Dresden");
        opus.setPublisherPlace("Dresden");
        opus.setPublisherAddress("Zellescher Weg 18, 01069 Dresden, Germany");
    }

}
