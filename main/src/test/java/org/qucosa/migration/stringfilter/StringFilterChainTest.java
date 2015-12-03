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

public class StringFilterChainTest {

    @Test
    public void executes_filters_in_order() {
        final StringFilterChain filterChain = new StringFilterChain(
                in -> in.replace("a", "b"),
                in -> in.replace("b", "c"),
                in -> in.replace("c", "d")
        );

        final String result = filterChain.apply("a");

        assertEquals("d", result);
    }

}
