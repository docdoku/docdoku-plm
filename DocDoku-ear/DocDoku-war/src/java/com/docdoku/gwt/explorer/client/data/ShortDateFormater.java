/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client.data;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.DateBox;
import java.util.Date;

/**
 *
 * @author Emmanuel Nhan
 */
public class ShortDateFormater implements DateBox.Format{

    private static final String DATE_BOX_FORMAT_ERROR = "dateBoxFormatError";

    private final DateTimeFormat dateTimeFormat ;

    public ShortDateFormater() {
        dateTimeFormat = DateTimeFormat.getShortDateFormat();
    }
    public String format(DateBox box, Date date) {
      if (date == null) {
        return "";
      } else {
        return dateTimeFormat.format(date);
      }
    }

    public DateTimeFormat getDateTimeFormat() {
      return dateTimeFormat;
    }

    @SuppressWarnings("deprecation")
    public Date parse(DateBox dateBox, String dateText, boolean reportError) {
      Date date = null;
      try {
        if (dateText.length() > 0) {
          date = dateTimeFormat.parse(dateText);
        }
      } catch (IllegalArgumentException exception) {
        try {
          date = new Date(dateText);
        } catch (IllegalArgumentException e) {
          if (reportError) {
            dateBox.addStyleName(DATE_BOX_FORMAT_ERROR);
          }
          return null;
        }
      }
      return date;
    }

    public void reset(DateBox dateBox, boolean abandon) {
      dateBox.removeStyleName(DATE_BOX_FORMAT_ERROR);
    }

}