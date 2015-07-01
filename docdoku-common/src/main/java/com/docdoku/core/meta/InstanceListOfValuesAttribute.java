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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a closed-ended type custom attribute of a document, part, product and other objects.
 * 
 * @author Florent Garin
 * @version 2.0, 27/02/15
 * @since   V2.0
 */
@Table(name="INSTANCELOVATTRIBUTE")
@Entity
public class InstanceListOfValuesAttribute extends InstanceAttribute{


    private int indexValue;

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn(name = "NAMEVALUE_ORDER")
    @CollectionTable(
            name = "ATTRIBUTE_NAMEVALUE",
            joinColumns = {
                    @JoinColumn(name = "ATTRIBUTE_ID", referencedColumnName = "ID")
            }
    )
    private List<NameValuePair> items=new ArrayList<>();

    public InstanceListOfValuesAttribute() {
    }

    public InstanceListOfValuesAttribute(String pName, int pValue, boolean pMandatory) {
        super(pName, pMandatory);
        setIndexValue(pValue);
    }
    @Override
    public Integer getValue() {
        return indexValue;
    }
    @Override
    public boolean setValue(Object pValue) {
        try{
            int index=Integer.parseInt(pValue + "");
            return setIndexValue(index);
        }catch(NumberFormatException ex){
            return false;
        }
    }

    @XmlElement(name="indexValue")
    private int getUncheckedIndexValue(){
        return indexValue;
    }

    private void setUncheckedIndexValue(int value){
        indexValue = value;
    }

    @XmlTransient
    public int getIndexValue() {
        return indexValue;
    }

    public boolean setIndexValue(int indexValue) {
        if(indexValue < 0 || indexValue > items.size()-1)
            return false;
        else {
            this.indexValue=indexValue;
            return true;
        }
    }

    public void setItems(List<NameValuePair> items) {
        this.items = items;
    }

    public List<NameValuePair> getItems() {
        return items;
    }

    public String getSelectedName(){
        return items.get(indexValue).getName();
    }

    public String getSelectedValue(){
        return items.get(indexValue).getValue();
    }

}
