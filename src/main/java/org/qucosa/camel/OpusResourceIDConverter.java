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

package org.qucosa.camel;

import org.apache.camel.Converter;
import org.qucosa.opus.OpusResourceID;

/**
 * @author claussni
 * @date 23.03.15.
 */
@Converter
public class OpusResourceIDConverter {

    @Converter
    public OpusResourceID toOpusResourceId(String s) {
        return OpusResourceID.create(s);
    }

}

