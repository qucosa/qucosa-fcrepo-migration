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

import de.slubDresden.YesNo;
import org.qucosa.migration.stringfilters.StringFilter;
import org.qucosa.migration.stringfilters.StringFilterChain;
import org.qucosa.migration.stringfilters.TextInputStringFilters;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import static de.slubDresden.YesNo.NO;
import static de.slubDresden.YesNo.YES;

public class MappingFunctions {

    public static final String LOC_GOV_VOCABULARY_RELATORS = "http://id.loc.gov/vocabulary/relators";

    private static final StringFilter multiLineFilter = new StringFilterChain(
            TextInputStringFilters.TRIM_FILTER,
            TextInputStringFilters.TRIM_TAB_FILTER,
            TextInputStringFilters.SINGLE_QUOTE_Filter,
            TextInputStringFilters.NFC_NORMALIZATION_FILTER);

    private static final StringFilter singleLineFilter = new StringFilterChain(
            TextInputStringFilters.NEW_LINE_FILTER,
            TextInputStringFilters.TRIM_FILTER,
            TextInputStringFilters.TRIM_TAB_FILTER,
            TextInputStringFilters.SINGLE_QUOTE_Filter,
            TextInputStringFilters.NFC_NORMALIZATION_FILTER);


    public static String dateEncoding(BigInteger year) {
        if ((year == null) || year.equals(BigInteger.ZERO)) return null;

        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, year.intValue());
        return dateFormat.format(cal.getTime());
    }

    public static String dateEncoding(noNamespace.Date date) {
        if (date != null && (date.isSetYear() && date.isSetMonth() && date.isSetDay())) {

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            TimeZone tz;
            GregorianCalendar cal;
            if (date.getTimezone() != null) {
                tz = SimpleTimeZone.getTimeZone(date.getTimezone());
                cal = new GregorianCalendar(tz);
            } else {
                cal = new GregorianCalendar();
            }

            cal.set(Calendar.YEAR, date.getYear().intValue());
            cal.set(Calendar.MONTH, date.getMonth().intValue() - 1);
            cal.set(Calendar.DAY_OF_MONTH, date.getDay().intValue());

            return dateFormat.format(cal.getTime());
        } else {
            return null;
        }
    }

    public static String singleline(String s) {
        return (s == null) ? null : singleLineFilter.apply(s);
    }

    public static String multiline(String s) {
        return (s == null) ? null : multiLineFilter.apply(s);
    }

    public static String buildTokenFrom(String prefix, String... strings) {
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            if (s != null) {
                sb.append(s);
            }
        }
        return prefix + String.format("%02X", sb.toString().hashCode());
    }

    public static YesNo.Enum yesNoBooleanMapping(boolean oaiExport) {
        return (oaiExport) ? YES : NO;
    }

    static String combineName(String firstName, String lastName) {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(firstName).append(' ');
        }
        sb.append(lastName);
        return sb.toString();
    }

}
