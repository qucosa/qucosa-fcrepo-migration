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

import gov.loc.mods.v3.ModsDefinition;
import gov.loc.mods.v3.OriginInfoDefinition;
import gov.loc.mods.v3.PhysicalDescriptionDefinition;

import javax.xml.xpath.XPathExpressionException;

import static gov.loc.mods.v3.DigitalOriginDefinition.BORN_DIGITAL;
import static org.qucosa.migration.mappings.XmlFunctions.nodeExists;
import static org.qucosa.migration.mappings.XmlFunctions.select;

public class TechnicalInformationMapping {

    public boolean ensurePhysicalDescription(ModsDefinition mods) throws XPathExpressionException {
        boolean change = false;
        PhysicalDescriptionDefinition pdd = (PhysicalDescriptionDefinition)
                select("mods:physicalDescription", mods);
        if (pdd == null) {
            pdd = mods.addNewPhysicalDescription();
            change = true;
        }
        if (!nodeExists("mods:digitalOrigin", pdd)) {
            pdd.addDigitalOrigin(BORN_DIGITAL);
            change = true;
        }
        return change;
    }

    public boolean ensureEdition(ModsDefinition mods) throws XPathExpressionException {
        boolean change = false;
        OriginInfoDefinition oid = (OriginInfoDefinition)
                select("mods:originInfo[@eventType='distribution']", mods);
        if (oid == null) {
            oid = mods.addNewOriginInfo();
            oid.setEventType("distribution");
            change = true;
        }
        if (!nodeExists("mods:edition[text()='[Electronic ed.]']", oid)) {
            oid.addNewEdition().setStringValue("[Electronic ed.]");
            change = true;
        }
        return change;
    }

}
