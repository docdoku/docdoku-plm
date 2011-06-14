/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
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