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

import com.docdoku.core.configuration.BaselinedPart;
import com.docdoku.core.configuration.ProductInstanceIteration;
import com.docdoku.core.configuration.ProductInstanceIterationKey;
import com.docdoku.core.configuration.ProductInstanceMasterKey;
import com.docdoku.core.exceptions.ProductInstanceIterationNotFoundException;
import com.docdoku.core.exceptions.ProductInstanceMasterNotFoundException;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductInstanceIterationDAO {

    private EntityManager em;
    private Locale mLocale;

    private static Logger LOGGER = Logger.getLogger(ProductInstanceIterationDAO.class.getName());

    public ProductInstanceIterationDAO(EntityManager pEM) {
        em = pEM;
    }

    public ProductInstanceIterationDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public void createProductInstanceIteration(ProductInstanceIteration productInstanceIteration){
        try {
            em.persist(productInstanceIteration);
            em.flush();
        }catch (Exception e){
            LOGGER.log(Level.SEVERE,"Fail to create product instance iteration",e);
        }
    }

    public List<ProductInstanceIteration> findProductInstanceIterationsByMaster(ProductInstanceMasterKey prodInstMKey) {
        return em.createQuery("SELECT pii " +
                              "FROM ProductInstanceIteration pii " +
                              "WHERE pii.productInstanceMaster.serialNumber = :serialNumber " +
                              "AND pii.productInstanceMaster.instanceOf = :configurationId", ProductInstanceIteration.class)
                .setParameter("serialNumber",prodInstMKey.getSerialNumber())
                .setParameter("configurationId",prodInstMKey.getInstanceOf())
                .getResultList();
    }

    public ProductInstanceIteration loadProductInstanceIteration(ProductInstanceIterationKey pId) throws ProductInstanceMasterNotFoundException, ProductInstanceIterationNotFoundException {
        ProductInstanceIteration productInstanceIteration = em.find(ProductInstanceIteration.class, pId);
        if (productInstanceIteration == null) {
            throw new ProductInstanceIterationNotFoundException(mLocale, pId);
        } else {
            return productInstanceIteration;
        }
    }

    public List<BaselinedPart> findBaselinedPartWithReferenceLike(int collectionId, String q, int maxResults) {
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