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

package com.docdoku.server.dao;

import com.docdoku.core.meta.InstanceAttribute;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.util.List;
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

    public List<InstanceAttribute> getPartIterationsInstanceAttributesInWorkspace(String workspaceId){

        return em.createNamedQuery("PartIteration.findDistinctInstanceAttributes", InstanceAttribute.class)
                .setParameter("workspaceId", workspaceId)
                .getResultList();

    }

    public List<InstanceAttribute> getPathDataInstanceAttributesInWorkspace(String workspaceId){

        return em.createNamedQuery("PathDataIteration.findDistinctInstanceAttributes", InstanceAttribute.class)
                .setParameter("workspaceId", workspaceId)
                .getResultList();

    }
}