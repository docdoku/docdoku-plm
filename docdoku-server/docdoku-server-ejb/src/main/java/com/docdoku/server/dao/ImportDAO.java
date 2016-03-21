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

import com.docdoku.core.common.User;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.product.Import;

import javax.persistence.*;
import java.util.List;
import java.util.Locale;

public class ImportDAO {

    private EntityManager em;
    private Locale mLocale;

    public ImportDAO(Locale pLocale, EntityManager pEM) {
        mLocale=pLocale;
        em=pEM;
    }

    public ImportDAO(EntityManager pEM) {
        mLocale=Locale.getDefault();
        em=pEM;
    }

    public void createImport(Import importToPersist) throws  CreationException {
        try{
            //the EntityExistsException is thrown only when flush occurs
            em.persist(importToPersist);
            em.flush();
        }catch(EntityExistsException pEEEx){
            throw new CreationException(mLocale);
        }catch(PersistenceException pPEx){
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public Import findImport(User user, String id) {
        TypedQuery<Import> query = em.createQuery("SELECT DISTINCT i FROM Import i WHERE i.id = :id AND i.user = :user", Import.class);
        query.setParameter("user", user);
        query.setParameter("id", id);
        try{
            return query.getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }

    public List<Import> findImports(User user) {
        TypedQuery<Import> query = em.createQuery("SELECT DISTINCT i FROM Import i WHERE i.user = :user", Import.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    public void deleteImport(Import importToDelete) {
        em.remove(importToDelete);
        em.flush();
    }

}
