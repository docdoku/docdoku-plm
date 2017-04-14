/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Helper class to deal with dates, use a single date pattern defined here
 */
public class DateUtils {

    public DateUtils() {
    }

    private static final java.lang.String GLOBAL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final java.lang.String SHORT_DATE_FORMAT = "yyyy-MM-dd";

    private static final SimpleDateFormat GLOBAL_DATE_FORMAT_SDF = new SimpleDateFormat(GLOBAL_DATE_FORMAT);
    private static final SimpleDateFormat SHORT_DATE_FORMAT_SDF = new SimpleDateFormat(SHORT_DATE_FORMAT);

    static {
        GLOBAL_DATE_FORMAT_SDF.setTimeZone(TimeZone.getTimeZone("UTC"));
        SHORT_DATE_FORMAT_SDF.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    static public Date parse(String s) throws ParseException {
        if (s.length() == SHORT_DATE_FORMAT.length()) {
            return SHORT_DATE_FORMAT_SDF.parse(s);
        }
        return GLOBAL_DATE_FORMAT_SDF.parse(s);
    }

    static public Date parse(String s, String timeZone) throws ParseException {
        SimpleDateFormat sdf;

        if (s.length() == SHORT_DATE_FORMAT.length()) {
            sdf = new SimpleDateFormat(SHORT_DATE_FORMAT);
        } else {
            sdf = new SimpleDateFormat(GLOBAL_DATE_FORMAT);
        }

        sdf.setTimeZone(TimeZone.getTimeZone(timeZone));

        return sdf.parse(s);
    }


    static public String format(Date d) {
        return GLOBAL_DATE_FORMAT_SDF.format(d);
    }

    static public String formatShort(Date d) {
        return SHORT_DATE_FORMAT_SDF.format(d);
    }
}
