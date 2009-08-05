/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.common;

import java.io.Serializable;

/**
 *
 * @author Florent GARIN
 */
public abstract class InstanceAttributeDTO implements Serializable{

    protected String name;

    public InstanceAttributeDTO(){

    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract Object getValue();
    public abstract boolean setValue(Object pValue);
    
    

}
