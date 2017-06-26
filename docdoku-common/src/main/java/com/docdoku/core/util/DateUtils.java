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

    private DateUtils() {
    }

    private static final String GLOBAL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIMEZONE = "UTC";

    public static Date parse(String s) throws ParseException {
        return parse(s, TIMEZONE);
    }

    public static Date parse(String s, String timeZone) throws ParseException {
        SimpleDateFormat sdf;

        if (s.length() == SHORT_DATE_FORMAT.length()) {
            sdf = getSimpleDateFormat(SHORT_DATE_FORMAT, timeZone);
        } else {
            sdf = getSimpleDateFormat(GLOBAL_DATE_FORMAT, timeZone);
        }

        return sdf.parse(s);
    }

    public static String format(Date d) {
        return getSimpleDateFormat(GLOBAL_DATE_FORMAT, TIMEZONE).format(d);
    }

    private static SimpleDateFormat getSimpleDateFormat(String format, String timeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
        return sdf;
    }
}
