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

package com.docdoku.core.product;

import java.io.Serializable;

/**
 * Identity class of {@link PartMaster} objects.
 * 
 * @author Florent Garin
 */
public class PartMasterKey implements Serializable, Comparable<PartMasterKey>, Cloneable {

    private String workspace;
    private String number;


    public PartMasterKey() {
    }
    
    public PartMasterKey(String pWorkspaceId, String pNumber) {
        workspace=pWorkspaceId;
        number=pNumber;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }
  
    
    public String getNumber() {
        return number;
    }
    
    public void setNumber(String pNumber) {
        number = pNumber;
    }
    
    
    @Override
    public String toString() {
        return workspace + "-" + number;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof PartMasterKey)) {
            return false;
        }
        PartMasterKey key = (PartMasterKey) pObj;
        return key.number.equals(number) && key.workspace.equals(workspace);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspace.hashCode();
        hash = 31 * hash + number.hashCode();
        return hash;
    }

    public int compareTo(PartMasterKey pKey) {
        int wksComp = workspace.compareTo(pKey.workspace);
        if (wksComp != 0) {
            return wksComp;
        }else {
            return number.compareTo(pKey.number);
        }
    }
    
    @Override
    public PartMasterKey clone() {
        PartMasterKey clone;
        try {
            clone = (PartMasterKey) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }
}