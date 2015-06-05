package com.docdoku.server.rest.util;

/**
 * Created by kelto on 01/06/15.
 */

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import javax.xml.bind.annotation.adapters.XmlAdapter;


public class DateAdapter extends XmlAdapter<String, Date> {

    // the desired format
    private static String pattern = "yyyy-MM-dd'T'HH:mm:ss";

    public String marshal(Date date) throws Exception {
        if(date == null) {
            return null;
        }
        SimpleDateFormat df =  new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(date);
    }

    public Date unmarshal(String dateString) throws Exception {
        //TODO: check if we have to specify UTC
        SimpleDateFormat df =  new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.parse(dateString);
    }
}
