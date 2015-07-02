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

import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceAttributeDescriptor;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstanceAttributeDAO {
    private static final Logger LOGGER = Logger.getLogger(InstanceAttributeDAO.class.getName());

    private final EntityManager em;

    public InstanceAttributeDAO(EntityManager pEM) {
        em=pEM;
    }

    public void removeAttribute(InstanceAttribute pAttr){
        em.remove(pAttr);
    }

    public void createAttribute(InstanceAttribute pAttr){
        try{
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pAttr);
            em.flush();
        }catch(EntityExistsException pEEEx){
            //already created
            LOGGER.log(Level.FINER,null,pEEEx);
        }
    }

    public List<InstanceAttributeDescriptor> getPartIterationsInstanceAttributesInWorkspace(String workspaceId){

        List<InstanceAttribute> partsAttributesInWorkspace = em.createNamedQuery("PartIteration.findDistinctInstanceAttributes", InstanceAttribute.class)
                .setParameter("workspaceId", workspaceId)
                .getResultList();

        Set<InstanceAttributeDescriptor> descriptors = new HashSet<>();

        for(InstanceAttribute attribute: partsAttributesInWorkspace){
            descriptors.add(new InstanceAttributeDescriptor(attribute));
        }

        return new ArrayList<>(descriptors);
    }

    public List<InstanceAttributeDescriptor> getPathDataInstanceAttributesInWorkspace(String workspaceId){

        List<InstanceAttribute> partsAttributesInWorkspace = em.createNamedQuery("PathDataIteration.findDistinctInstanceAttributes", InstanceAttribute.class)
                .setParameter("workspaceId", workspaceId)
                .getResultList();

        Set<InstanceAttributeDescriptor> descriptors = new HashSet<>();

        for(InstanceAttribute attribute: partsAttributesInWorkspace){
            descriptors.add(new InstanceAttributeDescriptor(attribute));
        }

        return new ArrayList<>(descriptors);
    }
}