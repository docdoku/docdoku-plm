/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.common;

/**
 *
 * @author Florent GARIN
 */
public class InstanceTextAttributeDTO extends InstanceAttributeDTO{

    private String textValue;

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    @Override
    public Object getValue() {
        return textValue;
    }

    @Override
    public boolean setValue(Object value) {
        textValue=value + "";
        return true;
    }
    
}
