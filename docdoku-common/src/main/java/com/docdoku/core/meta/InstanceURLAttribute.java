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
 * Defines an URL type custom attribute of a document, part, product and other objects.
 * 
 * @author Emmanuel Nhan
 * @version 1.0 23/07/2009
 * @since   V1.0
 */
@Table(name="INSTANCEURLATTRIBUTE")
@Entity
public class InstanceURLAttribute extends InstanceAttribute {

    private String urlValue;

    public InstanceURLAttribute() {
    }

    public InstanceURLAttribute(String pName, String pValue, boolean pMandatory) {
        super(pName, pMandatory);
        setUrlValue(pValue);
    }

    @Override
    public String getValue() {
        return urlValue;
    }
    @Override
    public boolean setValue(Object pValue) {
        urlValue = pValue != null ? pValue + "" : "";
        return true;
    }

    public String getUrlValue() {
        return urlValue;
    }
    public void setUrlValue(String urlValue) {
        this.urlValue = urlValue;
    }
}
