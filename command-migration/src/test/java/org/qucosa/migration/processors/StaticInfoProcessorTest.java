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

package org.qucosa.migration.processors;

import org.junit.Ignore;
import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

public class StaticInfoProcessorTest extends ProcessorTestBase {

    private MappingProcessor processor = new StaticInfoProcessor();

    @Test
    public void setsEdition() throws Exception {
        runProcessor(processor);

        assertXpathExists("//mods:originInfo[@eventType='distribution']/mods:edition[text()='[Electronic ed.]']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    @Ignore
    public void setsPhysicalDescription() throws Exception {
        runProcessor(processor);
        // TODO Remove mapping of physical description if confirmed
        assertXpathExists("//mods:physicalDescription/mods:digitalOrigin[text()='born digital']",
                mods.getDomNode().getOwnerDocument());
    }

    @Test
    public void slubAgreementIsSetToYes() throws Exception {
        runProcessor(processor);

        assertXpathExists("//slub:rights/slub:agreement[@given='yes']", info.getDomNode().getOwnerDocument());
    }

}
