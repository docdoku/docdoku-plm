/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.common;

/**
 *
 * @author Florent GARIN
 */
public class InstanceBooleanAttributeDTO extends InstanceAttributeDTO{


    private boolean booleanValue;

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }

    @Override
    public Object getValue() {
        return booleanValue;
    }

    @Override
    public boolean setValue(Object value) {
        booleanValue=Boolean.parseBoolean(value + "");
        return true;
    }
    
}
