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
import com.docdoku.core.product.Conversion;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;

import javax.persistence.*;
import java.util.Locale;

public class ConversionDAO {

    private EntityManager em;
    private Locale mLocale;

    public ConversionDAO(Locale pLocale, EntityManager pEM) {
        mLocale=pLocale;
        em=pEM;
    }

    public ConversionDAO(EntityManager pEM) {
        mLocale=Locale.getDefault();
        em=pEM;
    }

    public void createConversion(Conversion conversion) throws  CreationException {
        try{
            //the EntityExistsException is thrown only when flush occurs
            em.persist(conversion);
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

    public Conversion findConversion(PartIteration partIteration) {
        TypedQuery<Conversion> query = em.createQuery("SELECT DISTINCT c FROM Conversion c WHERE c.partIteration = :partIteration", Conversion.class);
        query.setParameter("partIteration", partIteration);
        try{
            return query.getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }

    public void deleteConversion(Conversion conversion) {
        em.remove(conversion);
        em.flush();
    }

    public void removePartRevisionConversions(PartRevision pPartR) {
        em.createQuery("DELETE FROM Conversion c WHERE c.partIteration.partRevision = :partRevision", Conversion.class)
                .setParameter("partRevision", pPartR)
                .executeUpdate();
    }

    public void removePartIterationConversion(PartIteration pPartI) {
        em.createQuery("DELETE FROM Conversion c WHERE c.partIteration = :partIteration", Conversion.class)
                .setParameter("partIteration", pPartI)
                .executeUpdate();
    }
}
