/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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

package com.docdoku.core.product;

import java.io.Serializable;

/**
 *
 * @author Florent Garin
 */
public class MasterPartKey implements Serializable, Comparable<MasterPartKey>, Cloneable {

    private String workspace;
    private String number;
    private String version;


    public MasterPartKey() {
    }
    
    public MasterPartKey(String pWorkspaceId, String pNumber, String pVersion) {
        workspace=pWorkspaceId;
        number=pNumber;
        version = pVersion;
    }



    public String getWorkspaceId() {
        return workspace;
    }
    
    public void setWorkspaceId(String pWorkspaceId) {
        workspace = pWorkspaceId;
    }
    
    public String getNumber() {
        return number;
    }
    
    public void setNumber(String pNumber) {
        number = pNumber;
    }
    
    public String getVersion() {
        return version;
    }

    public void setVersion(String pVersion){
        version=pVersion;
    }
    
    @Override
    public String toString() {
        return workspace + "-" + number + "-" + version;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof MasterPartKey))
            return false;
        MasterPartKey key = (MasterPartKey) pObj;
        return ((key.number.equals(number)) && (key.workspace.equals(workspace)) && (key.version.equals(version)));
    }

    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + workspace.hashCode();
	hash = 31 * hash + number.hashCode();
        hash = 31 * hash + version.hashCode();
	return hash;
    }

    public int compareTo(MasterPartKey pMDocKey) {
        int wksComp = workspace.compareTo(pMDocKey.workspace);
        if (wksComp != 0)
            return wksComp;
        int idComp = number.compareTo(pMDocKey.number);
        if (idComp != 0)
            return idComp;
        else
            return version.compareTo(pMDocKey.version);
    }
    
    @Override
    public MasterPartKey clone() {
        MasterPartKey clone = null;
        try {
            clone = (MasterPartKey) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }
}