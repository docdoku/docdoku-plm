/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;

import javax.persistence.EntityManager;
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
        em.remove(pPartI);
    }
}