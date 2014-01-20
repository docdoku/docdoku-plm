/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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



public class InstanceAttributeDAO {

    private EntityManager em;

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
        }
    }
}