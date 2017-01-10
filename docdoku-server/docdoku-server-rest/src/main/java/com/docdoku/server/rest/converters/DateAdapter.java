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

package com.docdoku.server.rest.converters;


import com.docdoku.core.util.DateUtils;

import javax.ws.rs.ext.ParamConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Charles Fallourd on 01/06/15.
 */
public class DateAdapter extends XmlAdapter<String, Date> implements ParamConverter<Date> {


    private static final Logger LOGGER = Logger.getLogger(DateAdapter.class.getName());

    public String marshal(Date date) {
        if (date == null) {
            return null;
        }
        return DateUtils.format(date);
    }

    public Date unmarshal(String dateString) {
        Date d = null;
        try {
            d = DateUtils.parse(dateString);
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, "Error unmarshalling date", e);
        }
        return d;
    }

    @Override
    public Date fromString(String s) {
        return unmarshal(s);
    }

    @Override
    public String toString(Date date) {
        return marshal(date);
    }
}
