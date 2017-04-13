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


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.qucosa.migration.mappings.MappingFunctions.volume;
import static org.qucosa.migration.mappings.MappingFunctions.volumeTitle;

public class MappingFunctionsTest {

    @Test
    public void Volume_information_null_if_not_present_in_title_string() {
        String actual = volume("Schriftenreihe des Landesamtes für Umwelt, Landwirtschaft und Geologie");
        assertNull("Expected volume information", actual);
    }

    @Test
    public void Extracts_volume_information_from_title_string() {
        String expected = "Heft 9/2010";
        String actual = volume("Schriftenreihe des Landesamtes für Umwelt, Landwirtschaft und Geologie ; " + expected);
        assertEquals("Expected volume information", expected, actual);
    }

    @Test
    public void Extracts_volume_information_from_title_string_with_multiple_semicolons() {
        String expected = "Heft 9/2010";
        String actual = volume("Schriftenreihe des Landesamtes für Umwelt; Landwirtschaft und Geologie ;" + expected);
        assertEquals("Expected volume information", expected, actual);
    }

    @Test
    public void Extracts_volume_title_from_title_string_with_multiple_semicolons() {
        String expected = "Schriftenreihe des Landesamtes für Umwelt; Landwirtschaft und Geologie";
        String actual = volumeTitle(expected + " ;Heft 9/2010");
        assertEquals("Expected volume information", expected, actual);
    }

}
