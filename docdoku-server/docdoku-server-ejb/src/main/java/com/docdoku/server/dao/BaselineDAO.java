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
import com.docdoku.core.exceptions.BaselineNotFoundException;
import com.docdoku.core.product.PartRevision;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Locale;

public class BaselineDAO {

    private EntityManager em;
    private Locale mLocale;

    public BaselineDAO(EntityManager pEM) {
        em = pEM;
    }

    public BaselineDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public List<Baseline> findBaselines(String workspaceId) {
        return em.createQuery("SELECT b FROM Baseline b WHERE b.configurationItem.workspace.id = :workspaceId", Baseline.class)
                .setParameter("workspaceId",workspaceId)
                .getResultList();
    }

    public List<Baseline> findBaselines(String ciId, String workspaceId){
        return em.createNamedQuery("Baseline.findByConfigurationItemId", Baseline.class)
                .setParameter("ciId", ciId)
                .setParameter("workspaceId",workspaceId)
                .getResultList();
    }

    public void createBaseline(Baseline baseline) {
        em.persist(baseline);
        em.flush();
    }

    public Baseline loadBaseline(int pId) throws BaselineNotFoundException {
        Baseline baseline = em.find(Baseline.class, pId);
        if (baseline == null) {
            throw new BaselineNotFoundException(mLocale, pId);
        } else {
            return baseline;
        }
    }

    public void deleteBaseline(Baseline baseline) {
        em.remove(baseline);
        em.flush();
    }

    public boolean existBaselinedPart(String workspaceId, String partNumber) {
        return em.createNamedQuery("BaselinedPart.existBaselinedPart", Long.class)
            .setParameter("partNumber", partNumber)
            .setParameter("workspaceId", workspaceId)
            .getSingleResult() > 0;
    }

    public void flushBaselinedParts(Baseline baseline) {
        baseline.removeAllBaselinedParts();
        em.flush();
    }

    public List<Baseline> findBaselineWherePartRevisionHasIterations(PartRevision partRevision) {
        return em.createNamedQuery("Baseline.getBaselinesForPartRevision", Baseline.class)
                .setParameter("partRevision", partRevision)
                .getResultList();
    }

    public Baseline findBaselineById(int baselineId) {
        return em.find(Baseline.class,baselineId);
    }
}
