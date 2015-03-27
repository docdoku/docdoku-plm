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

import com.docdoku.core.configuration.BaselinedPart;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.exceptions.BaselineNotFoundException;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.product.PartRevision;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductBaselineDAO {

    private final EntityManager em;
    private final Locale mLocale;
    private static final Logger LOGGER = Logger.getLogger(ProductBaselineDAO.class.getName());

    public ProductBaselineDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public ProductBaselineDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public List<ProductBaseline> findBaselines(String workspaceId) {
        return em.createQuery("SELECT b FROM ProductBaseline b WHERE b.configurationItem.workspace.id = :workspaceId", ProductBaseline.class)
                .setParameter("workspaceId",workspaceId)
                .getResultList();
    }

    public List<ProductBaseline> findBaselines(String ciId, String workspaceId){
        return em.createNamedQuery("ProductBaseline.findByConfigurationItemId", ProductBaseline.class)
                .setParameter("ciId", ciId)
                .setParameter("workspaceId",workspaceId)
                .getResultList();
    }

    public void createBaseline(ProductBaseline productBaseline) throws CreationException {
        em.persist(productBaseline);
        em.flush();

        try {
            em.persist(productBaseline);
            em.flush();
        } catch (PersistenceException pPEx) {
            LOGGER.log(Level.FINEST,null,pPEx);
            throw new CreationException(mLocale);
        }
    }

    public ProductBaseline loadBaseline(int pId) throws BaselineNotFoundException {
        ProductBaseline productBaseline = em.find(ProductBaseline.class, pId);
        if (productBaseline == null) {
            throw new BaselineNotFoundException(mLocale, pId);
        } else {
            return productBaseline;
        }
    }

    public void deleteBaseline(ProductBaseline productBaseline) {
        flushBaselinedParts(productBaseline);
        em.remove(productBaseline);
        em.flush();
    }

    public boolean existBaselinedPart(String workspaceId, String partNumber) {
        return em.createNamedQuery("BaselinedPart.existBaselinedPart", Long.class)
            .setParameter("partNumber", partNumber)
            .setParameter("workspaceId", workspaceId)
            .getSingleResult() > 0;
    }

    public void flushBaselinedParts(ProductBaseline productBaseline) {
        productBaseline.removeAllBaselinedParts();
        em.flush();
    }

    public List<ProductBaseline> findBaselineWherePartRevisionHasIterations(PartRevision partRevision) {
        return em.createNamedQuery("ProductBaseline.getBaselinesForPartRevision", ProductBaseline.class)
                .setParameter("partRevision", partRevision)
                .getResultList();
    }

    public ProductBaseline findBaselineById(int baselineId) {
        return em.find(ProductBaseline.class,baselineId);
    }

    public List<BaselinedPart> findBaselinedPartWithReferenceLike(int collectionId, String q, int maxResults) throws BaselineNotFoundException {
        List<BaselinedPart> baselinedPartList = em.createNamedQuery("BaselinedPart.findByReference",BaselinedPart.class)
                                                  .setParameter("id", "%" + q + "%")
                                                  .getResultList();
        List<BaselinedPart> returnList = new ArrayList<>();
        for(BaselinedPart baselinedPart : baselinedPartList){
            if(baselinedPart.getPartCollection().getId()==collectionId){
                returnList.add(baselinedPart);
                if(returnList.size()>=maxResults){
                    break;
                }
            }
        }
        return returnList;
    }
}
