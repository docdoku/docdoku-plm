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
 * Identity class of {@link PartIteration} objects.
 * 
 * @author Florent Garin
 */
public class PartIterationKey implements Serializable {
    
    private PartRevisionKey partRevision;
    private int iteration;
    
    public PartIterationKey() {
    }

    public PartIterationKey(String pWorkspaceId, String pNumber, String pVersion, int pIteration) {
        partRevision= new PartRevisionKey(pWorkspaceId, pNumber, pVersion);
        iteration=pIteration;
    }

    public PartIterationKey(PartRevisionKey pPartRevisionKey, int pIteration) {
        partRevision=pPartRevisionKey;
        iteration=pIteration;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + partRevision.hashCode();
        hash = 31 * hash + iteration;
        return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof PartIterationKey)) {
            return false;
        }
        PartIterationKey key = (PartIterationKey) pObj;
        return key.partRevision.equals(partRevision) && key.iteration==iteration;
    }
    
    @Override
    public String toString() {
        return partRevision + "-" + iteration;
    }

    public PartRevisionKey getPartRevision() {
        return partRevision;
    }
    public void setPartRevision(PartRevisionKey partRevision) {
        this.partRevision = partRevision;
    }

    public int getIteration(){
        return iteration;
    }
    public void setIteration(int pIteration){
        iteration=pIteration;
    }

    public String getWorkspaceId() {
        return partRevision.getPartMaster().getWorkspace();
    }
    public String getPartMasterNumber() {
        return partRevision.getPartMaster().getNumber();
    }

    public String getPartRevisionVersion() {
        return partRevision.getVersion();
    }
}
