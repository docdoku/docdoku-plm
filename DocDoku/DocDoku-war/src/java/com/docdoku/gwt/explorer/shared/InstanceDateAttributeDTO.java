/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.shared;

import java.util.Date;

/**
 *
 * @author Florent GARIN
 */
public class InstanceDateAttributeDTO extends InstanceAttributeDTO{

    private Date dateValue;

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    @Override
    public Object getValue() {
        return dateValue;
    }

    @Override
    public boolean setValue(Object value) {
        if(value instanceof Date){
            dateValue=(Date)value;
            return true;
        }else
            return false;
    }
    
}
