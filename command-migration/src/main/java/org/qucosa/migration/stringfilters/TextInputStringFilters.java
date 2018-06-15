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

import java.text.Normalizer;

public class TextInputStringFilters {
    static public final StringFilter NEW_LINE_FILTER = in -> in.replaceAll("\\n|\\r", " ");
    static public final StringFilter NFC_NORMALIZATION_FILTER = in -> Normalizer.normalize(in, Normalizer.Form.NFC);
    static public final StringFilter SINGLE_QUOTE_Filter = in -> in.replace("'", "''");
    static public final StringFilter TRIM_FILTER = String::trim;
    static public final StringFilter TRIM_TAB_FILTER = in -> in.replaceAll("^(\\t)+|(\\t)+$", "");
    public static final StringFilter WHITESPACE_FILTER = in -> in.replaceAll(" ", "");
}
