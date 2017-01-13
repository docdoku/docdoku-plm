package com.docdoku.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Helper class to deal with dates, use a single date pattern defined here
 */
public class DateUtils {

    public DateUtils(){
    }

    private static final java.lang.String GLOBAL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final SimpleDateFormat SDF = new SimpleDateFormat(GLOBAL_DATE_FORMAT);

    static {
        SDF.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    static public Date parse(String s) throws ParseException {
        return SDF.parse(s);
    }

    static public String format(Date d) {
        return SDF.format(d);
    }
}
