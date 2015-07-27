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

import com.docdoku.core.exceptions.PartIterationNotFoundException;
import com.docdoku.core.meta.ListOfValuesKey;
import com.docdoku.core.product.*;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Locale;



public class PartIterationDAO {

    private EntityManager em;
    private Locale mLocale;

    public PartIterationDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public PartIterationDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }



    public PartIteration loadPartI(PartIterationKey pKey) throws PartIterationNotFoundException {
        PartIteration partI = em.find(PartIteration.class, pKey);
        if (partI == null) {
            throw new PartIterationNotFoundException(mLocale, pKey);
        } else {
            return partI;
        }
    }

    
    public void updateIteration(PartIteration pPartI){
        em.merge(pPartI);
    }

    public void removeIteration(PartIteration pPartI){
        new ConversionDAO(em).removePartIterationConversion(pPartI);
        for(PartUsageLink partUsageLink:pPartI.getComponents()){
            if(!partLinkIsUsedInPreviousIteration(partUsageLink,pPartI)){
                em.remove(partUsageLink);
            }
        }
        em.remove(pPartI);
    }

    public boolean partLinkIsUsedInPreviousIteration(PartUsageLink partUsageLink, PartIteration partIte) {
        int iteration = partIte.getIteration();
        if(iteration == 1){
            return false;
        }
        PartIteration previousIteration = partIte.getPartRevision().getIteration(iteration-1);
        return previousIteration.getComponents().contains(partUsageLink);
    }

    public List<PartIteration> findUsedByAsComponent(PartMasterKey pPart) {
        return findUsedByAsComponent(em.getReference(PartMaster.class,pPart));
    }

    public List<PartIteration> findUsedByAsComponent(PartMaster pPart) {
        List<PartIteration> usedByParts =  em.createNamedQuery("PartIteration.findUsedByAsComponent", PartIteration.class)
                .setParameter("partMaster", pPart).getResultList();
        return usedByParts;
    }

    public List<PartIteration> findUsedByAsSubstitute(PartMasterKey pPart) {
        return findUsedByAsSubstitute(em.getReference(PartMaster.class,pPart));
    }

    public List<PartIteration> findUsedByAsSubstitute(PartMaster pPart) {
        List<PartIteration> usedByParts =  em.createNamedQuery("PartIteration.findUsedByAsSubstitute", PartIteration.class)
                .setParameter("partMaster", pPart).getResultList();
        return usedByParts;
    }


    public List<PartIteration> findAllPartIterationFromLOV(ListOfValuesKey lovKey) {
        return em.createNamedQuery("PartIteration.findWhereLOV", PartIteration.class)
                .setParameter("lovName", lovKey.getName())
                .setParameter("workspace_id", lovKey.getWorkspaceId())
                .getResultList();
    }
}