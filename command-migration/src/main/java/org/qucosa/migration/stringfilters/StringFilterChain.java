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

package org.qucosa.migration.stringfilters;

import java.util.Collections;
import java.util.LinkedList;

public class StringFilterChain implements StringFilter {

    final private LinkedList<StringFilter> filters = new LinkedList<>();

    public StringFilterChain(StringFilter... filters) {
        Collections.addAll(this.filters, filters);
    }

    @Override
    public String apply(String in) {
        String w = in;
        for (StringFilter f : filters) w = f.apply(w);
        return w;
    }
}
