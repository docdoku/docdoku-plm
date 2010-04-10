/*
 * InstanceURLAttribute.java
 * 
 * Copyright (c) 2009 Docdoku. All rights reserved.
 * 
 * This file is part of Docdoku.
 * 
 * Docdoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Docdoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Docdoku.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.core.entities;

import javax.persistence.Entity;

/**
 * Defines an URL type custom attribute of a document.
 * @author Emmanuel Nhan <emmanuel.nhan@insa-lyon.fr>
 * @version 1.1 23/07/2009
 * @since 1.1
 */
@Entity
public class InstanceURLAttribute extends InstanceAttribute {

    private String urlValue;

    public InstanceURLAttribute() {
    }

    public InstanceURLAttribute(Document pDoc, String pName, String pValue) {
        super(pDoc, pName);
        setUrlValue(pValue);
    }

    @Override
    public Object getValue() {
        return urlValue;
    }

    @Override
    public boolean setValue(Object pValue) {
        urlValue = pValue + "";
        attributeValue=urlValue;
        return true;
    }

    public String getUrlValue() {
        return urlValue;
    }

    public void setUrlValue(String urlValue) {
        this.urlValue = urlValue;
        attributeValue=urlValue;
    }

    
}
