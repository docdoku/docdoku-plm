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
package com.docdoku.server.dao;

import com.docdoku.core.exceptions.PartUsageLinkNotFoundException;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.product.PartSubstituteLink;
import com.docdoku.core.product.PartUsageLink;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PartUsageLinkDAO {

    private EntityManager em;
    private Locale mLocale;

    public PartUsageLinkDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }
    
    public PartUsageLinkDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    
    public List<PartUsageLink[]> findPartUsagePaths(PartMasterKey pPartMKey){
        List<PartUsageLink> usages= findPartUsages(pPartMKey.getWorkspace(),pPartMKey.getNumber()); 
        List<PartUsageLink[]> usagePaths = new ArrayList<>();
        for(PartUsageLink usage:usages){
            List<PartUsageLink> path=new ArrayList<>();
            path.add(usage);
            createPath(usage,path,usagePaths);
        }
        
        return usagePaths;
    }

    private void createPath(PartUsageLink currentUsage, List<PartUsageLink> currentPath, List<PartUsageLink[]> usagePaths){
        
        PartIteration owner = em.createNamedQuery("PartUsageLink.getPartOwner",PartIteration.class)
                .setParameter("usage", currentUsage)
                .getSingleResult();
        List<PartUsageLink> parentUsages = findPartUsages(owner.getWorkspaceId(), owner.getPartNumber());
        
        for(PartUsageLink parentUsage:parentUsages){
            List<PartUsageLink> newPath=new ArrayList<>(currentPath);
            newPath.add(0,parentUsage);
            createPath(parentUsage, newPath, usagePaths);
        }
        if(parentUsages.isEmpty()) {
            usagePaths.add(currentPath.toArray(new PartUsageLink[currentPath.size()]));
        }
              
    }
    
    public List<PartUsageLink> findPartUsages(String workspaceId, String partNumber){
        return em.createNamedQuery("PartUsageLink.findByComponent",PartUsageLink.class)
            .setParameter("partNumber", partNumber)
            .setParameter("workspaceId", workspaceId)
            .getResultList();
    }

    public boolean hasPartUsages(String workspaceId, String partNumber){
        return !findPartUsages(workspaceId,partNumber).isEmpty();
    }
    
    public PartUsageLink loadPartUsageLink(int pId) throws PartUsageLinkNotFoundException {
        PartUsageLink link = em.find(PartUsageLink.class, pId);
        if (link == null) {
            throw new PartUsageLinkNotFoundException(mLocale, pId);
        } else {
            return link;
        }
    }

    public PartSubstituteLink loadPartSubstituteLink(int pId) throws PartUsageLinkNotFoundException {
        PartSubstituteLink link = em.find(PartSubstituteLink.class, pId);
        if (link == null) {
            throw new PartUsageLinkNotFoundException(mLocale, pId);
        } else {
            return link;
        }
    }

}
