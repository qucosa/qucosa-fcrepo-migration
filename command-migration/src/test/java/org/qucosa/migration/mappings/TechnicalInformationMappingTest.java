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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TechnicalInformationMappingTest extends MappingTestBase {

    private TechnicalInformationMapping technicalInformationMapping;

    @Before
    public void setup() {
        technicalInformationMapping = new TechnicalInformationMapping();
    }

    @Test
    public void setsEdition() throws Exception {
        technicalInformationMapping.ensureEdition(mods, changeLog);
        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:originInfo[@eventType='distribution']/mods:edition[text()='[Electronic ed.]']",
                mods);
    }

    @Test
    public void setsPhysicalDescription() throws Exception {
        technicalInformationMapping.ensurePhysicalDescription(mods, changeLog);
        assertTrue("Mapper should signalChange successful change", changeLog.hasChanges());
        assertXpathExists("//mods:physicalDescription/mods:digitalOrigin[text()='born digital']",
                mods);
    }

}
