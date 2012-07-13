/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.core.meta;

import javax.persistence.Entity;

/**
 * Defines an URL type custom attribute of a document.
 * 
 * @author Emmanuel Nhan <emmanuel.nhan@insa-lyon.fr>
 * @version 1.0 23/07/2009
 * @since   V1.0
 */
@Entity
public class InstanceURLAttribute extends InstanceAttribute {

    private String urlValue;

    public InstanceURLAttribute() {
    }

    public InstanceURLAttribute(String pName, String pValue) {
        super(pName);
        setUrlValue(pValue);
    }

    @Override
    public Object getValue() {
        return urlValue;
    }

    @Override
    public boolean setValue(Object pValue) {
        urlValue = pValue + "";
        return true;
    }

    public String getUrlValue() {
        return urlValue;
    }

    public void setUrlValue(String urlValue) {
        this.urlValue = urlValue;
    }

    
}
