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

import com.docdoku.core.configuration.Baseline;
import com.docdoku.core.configuration.BaselinedPart;
import com.docdoku.core.configuration.BaselinedPartKey;
import com.docdoku.core.product.PartRevision;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaselineDAO {

    private EntityManager em;

    public BaselineDAO(EntityManager pEM) {
        em = pEM;
    }

    public List<Baseline> findBaselines(String ciId){
        return em.createNamedQuery("Baseline.findByConfigurationItemId", Baseline.class)
                .setParameter("ciId", ciId)
                .getResultList();
    }

    public void createBaseline(Baseline baseline) {
        em.persist(baseline);
        em.flush();
    }

    public Baseline findBaseline(String ciId, int baselineId) {
        try{
            return em.createNamedQuery("Baseline.findByConfigurationItemIdAndBaselineId", Baseline.class)
                .setParameter("ciId", ciId)
                .setParameter("baselineId", baselineId)
                .getSingleResult();
        }catch(NoResultException ex){
            return null;
        }
    }

    public void deleteBaseline(String ciId, int baselineId) {
        Baseline baseline = findBaseline(ciId,baselineId);
        if(baseline != null){
            em.remove(baseline);
            em.flush();
        }
    }

    public boolean existBaselinedPart(String partNumber) {
        return em.createNamedQuery("BaselinedPart.existBaselinedPart", Baseline.class)
            .setParameter("partNumber", partNumber)
            .executeUpdate() > 0;
    }

    public void flushBaselinedParts(Baseline baseline) {
        Set<Map.Entry<BaselinedPartKey,BaselinedPart>> entries = baseline.getBaselinedParts().entrySet();
        for(Iterator<Map.Entry<BaselinedPartKey,BaselinedPart>> it = entries.iterator(); it.hasNext(); ) {
            it.next();
            it.remove();
        }
        em.flush();
    }

    public List<Baseline> findBaselineWherePartRevisionHasIterations(PartRevision partRevision) {
        return em.createNamedQuery("BaselinedPart.getBaselinesForPartRevision", Baseline.class)
                .setParameter("partRevision", partRevision)
                .getResultList();
    }

    public Baseline findBaselineById(int baselineId) {
        try{
            return em.createNamedQuery("Baseline.findByBaselineId", Baseline.class)
                    .setParameter("baselineId", baselineId)
                    .getSingleResult();
        }catch(NoResultException ex){
            return null;
        }
    }
}
