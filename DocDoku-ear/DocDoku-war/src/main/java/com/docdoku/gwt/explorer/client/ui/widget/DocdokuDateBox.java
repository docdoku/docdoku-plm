/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class DocdokuDateBox extends DateBox {

    private final static long ONE_DAY = 86400000L;

    public enum RoundType {

        CEIL,
        FLOOR
    }
    private RoundType roundMethod;

    public DocdokuDateBox(RoundType method) {
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

        Date tmp1 = new Date(super.getValue().getTime() + ONE_DAY);
        tmp1.setHours(0);
        tmp1.setMinutes(0);
        tmp1.setSeconds(0);
        Date tmp2 = new Date(tmp1.getTime()-1000) ;
        return tmp2;
    }

    private Date getValueFloor() {
        Date tmp = super.getValue();
        tmp.setHours(0);
        tmp.setMinutes(0);
        tmp.setSeconds(0);
        return tmp;
    }
}
