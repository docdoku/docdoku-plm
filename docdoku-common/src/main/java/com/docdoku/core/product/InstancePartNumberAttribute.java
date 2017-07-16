/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.core.product;

import com.docdoku.core.common.Workspace;
import com.docdoku.core.meta.InstanceAttribute;

import javax.persistence.*;

/**
 * Defines a custom attribute which holds a reference to an existing
 * {@link PartMaster} object.
 * 
 * @author Florent Garin
 * @version 2.5, 26/09/16
 * @since   V2.5
 */
@Table(name="INSTANCEPARTNUMBERATTRIBUTE")
@Entity
public class InstancePartNumberAttribute extends InstanceAttribute{


    @ManyToOne
    @JoinColumns({
            @JoinColumn(name="PARTMASTER_PARTNUMBER", referencedColumnName="PARTNUMBER"),
            @JoinColumn(name="PARTMASTER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private PartMaster partMasterValue;

    public InstancePartNumberAttribute() {
    }

    public InstancePartNumberAttribute(String pName, PartMaster pValue, boolean pMandatory) {
        super(pName, pMandatory);
        setPartMasterValue(pValue);
    }

    @Override
    public PartMaster getValue() {
        return partMasterValue;
    }

    @Override
    public boolean setValue(Object pValue) {
        if(pValue instanceof PartMaster){
            partMasterValue=(PartMaster)pValue;
            return true;
        }else if(pValue instanceof PartMasterKey){
            PartMasterKey key=(PartMasterKey)pValue;
            partMasterValue=new PartMaster(new Workspace(key.getWorkspace()),key.getNumber());
            return true;
        }else{
            partMasterValue=null;
            return false;
        }

    }

    public PartMaster getPartMasterValue() {
        return partMasterValue;
    }
    public void setPartMasterValue(PartMaster partMasterValue) {
        this.partMasterValue = partMasterValue;
    }
}
