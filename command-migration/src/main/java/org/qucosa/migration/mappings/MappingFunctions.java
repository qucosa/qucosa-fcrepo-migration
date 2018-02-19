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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import static de.slubDresden.YesNo.NO;
import static de.slubDresden.YesNo.YES;

public class MappingFunctions {

    static final String LOC_GOV_VOCABULARY_RELATORS = "http://id.loc.gov/vocabulary/relators";

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

    private static final StringFilter uriFilter = new StringFilterChain(
            singleLineFilter,
            TextInputStringFilters.WHITESPACE_FILTER);

    private static final HashMap<String, String> typeMapping = new HashMap<String, String>() {{
        put("article", "article");
        put("bachelor_thesis", "bachelor_thesis");
        put("book", "monograph");
        put("composition", "musical_notation");
        put("diploma_thesis", "diploma_thesis");
        put("doctoral_thesis", "doctoral_thesis");
        put("habilitation_thesis", "habilitation_thesis");
        put("in_book", "contained_work");
        put("in_proceeding", "in_proceeding");
        put("issue", "issue");
        put("journal", "periodical");
        put("lecture", "lecture");
        put("magister_thesis", "magister_thesis");
        put("master_thesis", "master_thesis");
        put("paper", "paper");
        put("preprint", "preprint");
        put("proceeding", "proceeding");
        put("report", "report");
        put("research_paper", "research_paper");
        put("study", "text");
    }};

    private static final HashMap<String, String> specificDocumentIdMapping = new HashMap<String, String>() {{
        put("22751", "series");
        put("11387", "series");
        put("11278", "series");
        put("11167", "series");
        put("14800", "series");
        put("15304", "series");
        put("17955", "series");
        put("18657", "series");
        put("11483", "series");
        put("20965", "series");
    }};

    static String dateEncoding(BigInteger year) {
        if ((year == null) || year.equals(BigInteger.ZERO)) return null;

        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, year.intValue());
        return dateFormat.format(cal.getTime());
    }

    static String dateEncoding(noNamespace.Date date) {
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

    static String singleline(String s) {
        return (s == null) ? null : singleLineFilter.apply(s);
    }

    static String multiline(String s) {
        return (s == null) ? null : multiLineFilter.apply(s);
    }

    static String uri(String s) {
        return (s == null) ? null : uriFilter.apply(s);
    }

    static String buildTokenFrom(String prefix, String... strings) {
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            if (s != null) {
                sb.append(s);
            }
        }
        return prefix + String.format("%02X", sb.toString().hashCode());
    }

    static YesNo.Enum yesNoBooleanMapping(boolean oaiExport) {
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

    static String languageEncoding(String code) {
        if (code != null) {
            if (code.length() != 3) {
                String result = Locale.forLanguageTag(code).getISO3Language();
                if (result == null || result.isEmpty()) return null;
            }
        }
        return code;
    }

    static Object firstOf(Object[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[0];
    }

    static Object firstOf(ArrayList list) {
        return (list.isEmpty()) ? null : list.get(0);
    }

    static String documentTypeEncoding(String type, String opusDocumentId) {
        if (specificDocumentIdMapping.containsKey(opusDocumentId)) {
            return specificDocumentIdMapping.get(opusDocumentId);
        }
        return typeMapping.get(type);
    }

    static String mapOrganizationName(String originalName, Map<String, String> institutionNameMap) {
        if (institutionNameMap != null && institutionNameMap.containsKey(originalName)) {
            return institutionNameMap.get(originalName);
        }
        return originalName;
    }

    static String volume(String t) {
        if (t != null && !t.isEmpty()) {
            int i = t.lastIndexOf(';') + 1;
            if (i > 0) return t.substring(i).trim();
        }
        return null;
    }

    static String volumeTitle(String t) {
        if (t != null && !t.isEmpty()) {
            int i = t.lastIndexOf(';') - 1;
            if (i > 0) {
                return t.substring(0, i).trim();
            } else {
                return t.trim();
            }
        }
        return null;
    }
}
