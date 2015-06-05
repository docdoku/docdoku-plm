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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Defines a date type custom attribute of a document, part, product and other objects.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="INSTANCEDATEATTRIBUTE")
@Entity
public class InstanceDateAttribute extends InstanceAttribute{

    
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dateValue;
    
    public InstanceDateAttribute() {
    }
    
    public InstanceDateAttribute(String pName, Date pValue, boolean pMandatory) {
        super(pName, pMandatory);
        setDateValue(pValue);
    }

    @Override
    public Date getValue() {
        return dateValue;
    }
    @Override
    public boolean setValue(Object pValue) {
        if(pValue instanceof Date){
            dateValue=(Date)pValue;
            return true;
        }else if(pValue instanceof String){
            try {
                //TODO: could use DateAdpater instead
                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                df.setTimeZone(tz);
                Date date = df.parse((String) pValue);
                dateValue = date;
                return true;
            } catch (ParseException pe) {
                try {
                    dateValue = new Date(Long.parseLong((String) pValue));
                    return true;
                }catch(NumberFormatException nfe){
                    return false;
                }
            }

        }else{
            dateValue=null;
            return false;
        }
    }

    public Date getDateValue() {
        return dateValue;
    }
    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }
    
    
    /**
     * perform a deep clone operation
     */
    @Override
    public InstanceDateAttribute clone() {
        InstanceDateAttribute clone;
        clone = (InstanceDateAttribute) super.clone();
        
        if(dateValue!=null)
            clone.dateValue = (Date) dateValue.clone();
        return clone;
    }
}