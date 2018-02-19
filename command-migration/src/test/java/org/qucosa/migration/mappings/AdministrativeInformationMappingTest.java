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

import de.slubDresden.InfoDocument;
import gov.loc.mods.v3.ExtensionDefinition;
import gov.loc.mods.v3.NameDefinition;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static gov.loc.mods.v3.NameDefinition.Type.CORPORATE;
import static org.junit.Assert.assertTrue;
import static org.qucosa.migration.mappings.AdministrativeInformationMapping.SLUB_GND_IDENTIFIER;

public class AdministrativeInformationMappingTest extends MappingTestBase {

    private AdministrativeInformationMapping aim;

    @Before
    public void setup() {
        aim = new AdministrativeInformationMapping();
    }

    @Test
    public void Maps_completed_date_to_originInfo() {
        noNamespace.Date date = noNamespace.Date.Factory.newInstance();
        date.setYear(BigInteger.valueOf(2013));
        date.setMonth(BigInteger.valueOf(1));
        date.setDay(BigInteger.valueOf(31));

        aim.mapCompletedDate(date, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists(
                "//mods:originInfo[@eventType='distribution']/" +
                        "mods:dateIssued[@encoding='iso8601' and @keyDate='yes' and text()='2013-01-31']", mods);
    }

    @Test
    public void Maps_publisher_infos_to_MODS_name_element() {
        opus.setPublisherName("Saechsische Landesbibliothek- Staats- und Universitaetsbibliothek Dresden");
        opus.setPublisherPlace("Dresden");
        opus.setPublisherAddress("Zellescher Weg 18, 01069 Dresden, Germany");

        aim.mapDefaultPublisherInfo(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:name[@type='corporate' and @displayLabel='mapping-hack-default-publisher']", mods);
        assertXpathExists("//mods:name[@type='corporate' and @displayLabel='mapping-hack-default-publisher']/" +
                "mods:nameIdentifier[@type='gnd' and text()='" + SLUB_GND_IDENTIFIER + "']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@ref=//mods:name/@ID]", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@ref=//mods:name/@ID" +
                " and @place='" + opus.getPublisherPlace() + "'" +
                " and @address='" + opus.getPublisherAddress() + "']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@ref=//mods:name/@ID]" +
                "/slub:university[text()='" + opus.getPublisherName() + "']", mods);
    }

    @Test
    public void Adds_missing_publisher_infos_to_existing_extension_preserving_existing_elements() {
        opus.setPublisherName("Saechsische Landesbibliothek- Staats- und Universitaetsbibliothek Dresden");
        opus.setPublisherPlace("Dresden");
        opus.setPublisherAddress("Zellescher Weg 18, 01069 Dresden, Germany");

        NameDefinition name = mods.addNewName();
        name.setType2(CORPORATE);
        name.setID("SOMEID");
        name.setDisplayLabel("mapping-hack-default-publisher");

        ExtensionDefinition ext = mods.addNewExtension();
        InfoDocument infoDocument = InfoDocument.Factory.newInstance();
        infoDocument.addNewInfo().addNewCorporation().addNewUniversity().setStringValue("Foo University");
        ext.set(infoDocument);

        aim.mapDefaultPublisherInfo(opus, mods, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:extension/slub:info/slub:corporation[slub:university='Foo University']", mods);
        assertXpathExists("//mods:extension/slub:info/slub:corporation[@ref=//mods:name/@ID]", mods);
    }

    @Test
    public void Extracts_VgWortOpenKey() {
        String vgWortOpenKey = "6fd9288e617c4721b6f25624167249f6";
        opus.setVgWortOpenKey(vgWortOpenKey);

        aim.mapVgWortopenKey(opus, info, changeLog);

        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//slub:vgwortOpenKey[text()='" + vgWortOpenKey + "']", info);
    }

    @Test
    public void Filters_Url_Prefixes_from_VgWortOpenKey() {
        String prefix = "http://vg04.met.vgwort.de/";
        String vgWortOpenKey = "6fd9288e617c4721b6f25624167249f6";
        opus.setVgWortOpenKey(prefix + vgWortOpenKey);

        aim.mapVgWortopenKey(opus, info, changeLog);

        assertXpathExists("//slub:vgwortOpenKey[text()='" + vgWortOpenKey + "']", info);
    }

    @Test
    public void slubAgreementIsSetToYes() {
        aim.ensureRightsAgreement(info, changeLog);
        assertXpathExists("//slub:rights/slub:agreement[@given='yes']", info);
    }

}
