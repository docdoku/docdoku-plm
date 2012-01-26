/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

package com.docdoku.gwt.explorer.client.ui.widget;

import com.google.gwt.user.datepicker.client.DateBox;
import java.util.Date;

/**
 * special date box with a short date format.
 * it is designed to provide a convinient way to choose a time interval.
 * 
 * @author Emmanuel Nhan
 */
public class RoundedDateBox extends DateBox {

    private final static long ONE_DAY = 86400000L;

    public enum RoundType {

        CEIL,
        FLOOR
    }
    private RoundType roundMethod;

    public RoundedDateBox(RoundType method) {
        roundMethod = method;

    }

    @Override
    public Date getValue() {
        if (super.getValue() != null) {
            switch (roundMethod) {
                case CEIL:
                    return getValueCeil();
                case FLOOR:
                    return getValueFloor();
            }
        }

        return null;
    }

    private Date getValueCeil() {
        return new Date(super.getValue().getTime() + ONE_DAY - 1000);
    }

    private Date getValueFloor() {
        return super.getValue();
    }
}
