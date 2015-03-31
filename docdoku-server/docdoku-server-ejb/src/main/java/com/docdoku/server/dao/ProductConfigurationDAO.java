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

import com.docdoku.core.configuration.ProductConfiguration;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.ProductConfigurationNotFoundException;
import com.docdoku.core.product.ConfigurationItemKey;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductConfigurationDAO {

    private final EntityManager em;
    private final Locale mLocale;
    private static final Logger LOGGER = Logger.getLogger(ProductConfigurationDAO.class.getName());

    public ProductConfigurationDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public ProductConfigurationDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public void createProductConfiguration(ProductConfiguration productConfiguration) throws CreationException {
        try {
            em.persist(productConfiguration);
            em.flush();
        } catch (PersistenceException pPEx) {
            LOGGER.log(Level.FINEST,null,pPEx);
            throw new CreationException(mLocale);
        }
    }

    public ProductConfiguration getProductConfiguration(int productConfigurationId) throws ProductConfigurationNotFoundException {
        ProductConfiguration productConfiguration = em.find(ProductConfiguration.class, productConfigurationId);
        if(productConfiguration != null){
            return productConfiguration;
        }else{
            throw new ProductConfigurationNotFoundException(mLocale,productConfigurationId);
        }
    }

    public List<ProductConfiguration> getAllProductConfigurations(String workspaceId) {
        return em.createNamedQuery("ProductConfiguration.findByWorkspace", ProductConfiguration.class)
                .setParameter("workspaceId", workspaceId)
                .getResultList();
    }

    public List<ProductConfiguration> getAllProductConfigurationsByConfigurationItem(ConfigurationItemKey ciKey) {
        return em.createNamedQuery("ProductConfiguration.findByConfigurationItem", ProductConfiguration.class)
                .setParameter("workspaceId", ciKey.getWorkspace())
                .setParameter("configurationItemId", ciKey.getId())
                .getResultList();
    }

    public void deleteProductConfiguration(ProductConfiguration productConfiguration) {
        em.remove(productConfiguration);
        em.flush();
    }
}
