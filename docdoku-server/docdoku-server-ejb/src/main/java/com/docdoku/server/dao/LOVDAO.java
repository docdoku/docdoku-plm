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

import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.ListOfValuesAlreadyExistsException;
import com.docdoku.core.exceptions.ListOfValuesNotFoundException;
import com.docdoku.core.meta.ListOfValues;
import com.docdoku.core.meta.ListOfValuesKey;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Locale;

public class LOVDAO {

    private EntityManager em;
    private Locale mLocale;

    public LOVDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public LOVDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }
    
    public ListOfValues loadLOV(ListOfValuesKey lovKey) throws ListOfValuesNotFoundException {
        ListOfValues lov=em.find(ListOfValues.class,lovKey);
        if (lov == null) {
            throw new ListOfValuesNotFoundException(mLocale, lovKey.getName());
        } else {
            return lov;
        }
    }

    public List<ListOfValues> loadLOVList(String pWorkspaceId){
        TypedQuery<ListOfValues> query = em.createQuery("SELECT DISTINCT l FROM ListOfValues l WHERE l.workspaceId = :workspaceId", ListOfValues.class);
        query.setParameter("workspaceId", pWorkspaceId);
        return query.getResultList();
    }

    public void createLOV(ListOfValues pLov) throws CreationException, ListOfValuesAlreadyExistsException {
        try{
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pLov);
            em.flush();
        }catch(EntityExistsException pEEEx){
            throw new ListOfValuesAlreadyExistsException(mLocale, pLov);
        }catch(PersistenceException pPEx){
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public void deleteLOV(ListOfValues pLov) {
        em.remove(pLov);
        em.flush();
    }

    public ListOfValues updateLOV(ListOfValues pLov){
        return em.merge(pLov);
    }
}
