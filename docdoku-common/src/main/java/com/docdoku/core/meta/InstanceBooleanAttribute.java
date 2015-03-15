/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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

package com.docdoku.core.meta;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Defines a boolean type custom attribute of a document, part, product and other objects.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="INSTANCEBOOLEANATTRIBUTE")
@Entity
public class InstanceBooleanAttribute extends InstanceAttribute{
   
    private boolean booleanValue;
    
    public InstanceBooleanAttribute() {
    }
    
    public InstanceBooleanAttribute(String pName, boolean pValue, boolean pMandatory) {
        super(pName, pMandatory);
        setBooleanValue(pValue);
    }

    @Override
    public Boolean getValue() {
        return booleanValue;
    }
    @Override
    public boolean setValue(Object pValue) {
        booleanValue=Boolean.parseBoolean(pValue + "");
        return true;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }
    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }
}
