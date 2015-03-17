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

package com.docdoku.core.configuration;

import com.docdoku.core.product.ConfigurationItemKey;

import java.io.Serializable;

/**
 * Identity class of {@link ProductInstanceMaster} objects.
 * 
 * @author Florent Garin
 */
public class ProductInstanceMasterKey implements Serializable {

    private ConfigurationItemKey instanceOf;
    private String serialNumber;


    public ProductInstanceMasterKey() {
    }

    public ProductInstanceMasterKey(String serialNumber, String pWorkspaceId, String pId) {
        this.serialNumber=serialNumber;
        this.instanceOf=new ConfigurationItemKey(pWorkspaceId,pId);
    }

    public ProductInstanceMasterKey(String serialNumber, ConfigurationItemKey ciKey) {
        this.serialNumber=serialNumber;
        this.instanceOf=ciKey;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public ConfigurationItemKey getInstanceOf() {
        return instanceOf;
    }

    public void setInstanceOf(ConfigurationItemKey instanceOf) {
        this.instanceOf = instanceOf;
    }

    @Override
    public String toString() {
        return instanceOf + "-" + serialNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProductInstanceMasterKey that = (ProductInstanceMasterKey) o;

        return instanceOf.equals(that.instanceOf) && serialNumber.equals(that.serialNumber);

    }

    @Override
    public int hashCode() {
        int result = instanceOf.hashCode();
        result = 31 * result + serialNumber.hashCode();
        return result;
    }
}