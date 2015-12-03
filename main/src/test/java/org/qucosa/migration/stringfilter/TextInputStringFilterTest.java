/*
 * Copyright (C) 2015 Saxon State and University Library Dresden (SLUB)
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

package org.qucosa.migration.stringfilter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.qucosa.migration.stringfilter.TextInputStringFilters.*;

public class TextInputStringFilterTest {

    @Test
    public void Newlines_should_be_removed() {
        assertEquals("ABCDEF", NEW_LINE_FILTER.apply("ABC\nDEF\r\n"));
    }

    @Test
    public void Single_quotes_should_be_replaced_by_double_quotes() {
        assertEquals("A ''BCD'' EF", SINGLE_QUOTE_Filter.apply("A 'BCD' EF"));
    }

    @Test
    public void Spaces_should_get_trimmed_on_both_sides() {
        assertEquals("ABC DEF", TRIM_FILTER.apply(" ABC DEF  "));
    }

    @Test
    public void Tabs_should_get_trimmed_on_both_sides() {
        assertEquals("ABC\tDEF", TRIM_TAB_FILTER.apply("\t\tABC\tDEF\t"));
    }

}
