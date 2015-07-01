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
import com.docdoku.core.configuration.ProductInstanceIteration;
import com.docdoku.core.configuration.ProductInstanceMaster;
import com.docdoku.core.configuration.ProductInstanceMasterKey;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.ProductInstanceAlreadyExistsException;
import com.docdoku.core.exceptions.ProductInstanceMasterNotFoundException;
import com.docdoku.core.product.PartRevision;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Locale;

public class ProductInstanceMasterDAO {

    private EntityManager em;
    private Locale mLocale;

    public ProductInstanceMasterDAO(EntityManager pEM) {
        em = pEM;
    }

    public ProductInstanceMasterDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public List<ProductInstanceMaster> findProductInstanceMasters(String workspaceId) {
        return em.createQuery("SELECT pim FROM ProductInstanceMaster pim WHERE pim.instanceOf.workspace.id = :workspaceId", ProductInstanceMaster.class)
                .setParameter("workspaceId",workspaceId)
                .getResultList();
    }

    public List<ProductInstanceMaster> findProductInstanceMasters(String ciId, String workspaceId) {
        return em.createNamedQuery("ProductInstanceMaster.findByConfigurationItemId", ProductInstanceMaster.class)
                .setParameter("ciId", ciId)
                .setParameter("workspaceId",workspaceId)
                .getResultList();
    }

    public List<ProductInstanceMaster> findProductInstanceMasters(PartRevision partRevision) {
        return em.createNamedQuery("ProductInstanceMaster.findByPart", ProductInstanceMaster.class)
                .setParameter("partRevision", partRevision)
                .getResultList();
    }

    public void createProductInstanceMaster(ProductInstanceMaster productInstanceMaster) throws ProductInstanceAlreadyExistsException, CreationException {
        try{
            em.persist(productInstanceMaster);
            em.flush();
        }catch (EntityExistsException e){
            throw new ProductInstanceAlreadyExistsException(mLocale, productInstanceMaster);
        }catch (PersistenceException e){
            throw new CreationException(mLocale);
        }

    }

    public ProductInstanceMaster loadProductInstanceMaster(ProductInstanceMasterKey pId) throws ProductInstanceMasterNotFoundException {
        ProductInstanceMaster productInstanceMaster = em.find(ProductInstanceMaster.class, pId);
        if (productInstanceMaster == null) {
            throw new ProductInstanceMasterNotFoundException(mLocale, pId);
        } else {
            return productInstanceMaster;
        }
    }

    public void deleteProductInstanceMaster(ProductInstanceMaster productInstanceMaster) {
        for(ProductInstanceIteration productInstanceIteration : productInstanceMaster.getProductInstanceIterations()){
            for(BaselinedPart baselinedPart : productInstanceIteration.getBaselinedParts().values()){
                em.remove(baselinedPart);
            }
            em.refresh(productInstanceIteration.getPartCollection());
            em.remove(productInstanceIteration.getPartCollection());
            em.remove(productInstanceIteration);
        }
        // todo remove path data
        em.remove(productInstanceMaster);
        em.flush();
    }
}