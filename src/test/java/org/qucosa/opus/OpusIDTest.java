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

package org.qucosa.opus;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OpusIDTest {

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionOnInvalidIdentifier() {
        OpusID.parse("Foo");
    }

    @Test
    public void returnsId() {
        assertEquals("1", OpusID.parse("Opus/Document/1").getId());
    }

}