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
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Defines a text type custom attribute of a document, part, product and other objects.
 *
 * 
 * @author Florent Garin
 * @version 2.0, 29/08/16
 * @since   V2.0
 */
@Table(name="INSTANCELONGTEXTATTRIBUTE")
@Entity
public class InstanceLongTextAttribute extends InstanceAttribute{


    @Lob
    private String longTextValue;

    public InstanceLongTextAttribute() {
    }

    public InstanceLongTextAttribute(String pName, String pValue, boolean pMandatory) {
        super(pName, pMandatory);
        setLongTextValue(pValue);
    }

    @Override
    public String getValue() {
        return longTextValue;
    }
    @Override
    public boolean setValue(Object pValue) {
        longTextValue = pValue != null ? pValue + "" : "";
        return true;
    }

    public String getLongTextValue() {
        return longTextValue;
    }
    public void setLongTextValue(String longTextValue) {
        this.longTextValue = longTextValue;
    }
}
