/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.common;

/**
 *
 * @author Florent GARIN
 */
public class InstanceNumberAttributeDTO extends InstanceAttributeDTO{

    private float numberValue;

    public void setNumberValue(float numberValue) {
        this.numberValue = numberValue;
    }

    public float getNumberValue() {
        return numberValue;
    }

    @Override
    public Object getValue() {
        return numberValue;
    }

    @Override
    public boolean setValue(Object value) {
        try{
            numberValue=Float.parseFloat(value + "");
            return true;
        }catch(NumberFormatException ex){
            return false;
        }
    }



}
