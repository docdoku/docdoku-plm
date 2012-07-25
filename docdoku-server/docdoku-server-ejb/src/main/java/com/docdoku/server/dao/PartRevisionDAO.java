/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.server.dao;

import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.PartRevisionNotFoundException;
import java.util.Locale;
import javax.persistence.EntityManager;



public class PartRevisionDAO {

    private EntityManager em;
    private Locale mLocale;

    public PartRevisionDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public PartRevisionDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    
    public PartRevision loadPartR(PartRevisionKey pKey) throws PartRevisionNotFoundException {
        PartRevision partR = em.find(PartRevision.class, pKey);
        if (partR == null) {
            throw new PartRevisionNotFoundException(mLocale, pKey);
        } else {
            return partR;
        }
    }
    
    
    public void updateRevision(PartRevision pPartR){
        em.merge(pPartR);
    }

    public void removeRevision(PartRevision pPartR){
        em.remove(pPartR);
    }
}