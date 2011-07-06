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
public class PartKey implements Serializable {
    
    private MasterPartKey masterPart;
    private int iteration;
    
    public PartKey() {
    }
    
    public PartKey(MasterPartKey pMasterPartKey, int pIteration) {
        masterPart=pMasterPartKey;
        iteration=pIteration;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + masterPart.hashCode();
        hash = 31 * hash + iteration;
	return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof PartKey))
            return false;
        PartKey key = (PartKey) pObj;
        return ((key.masterPart.equals(masterPart)) && (key.iteration==iteration));
    }
    
    @Override
    public String toString() {
        return masterPart + "-" + iteration;
    }

    public MasterPartKey getMasterPart() {
        return masterPart;
    }

    public void setMasterPart(MasterPartKey masterPartKey) {
        this.masterPart = masterPartKey;
    }
    
    
    public int getIteration(){
        return iteration;
    }
    
    public void setIteration(int pIteration){
        iteration=pIteration;
    }
}
