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


import com.docdoku.server.configuration.spec.EffectivityConfigSpec;
import com.docdoku.core.exceptions.ConfigurationItemAlreadyExistsException;
import com.docdoku.core.exceptions.ConfigurationItemNotFoundException;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.LayerNotFoundException;
import com.docdoku.core.product.*;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Locale;

public class ConfigurationItemDAO {

    private EntityManager em;
    private Locale mLocale;

    public ConfigurationItemDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public ConfigurationItemDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public void updateConfigurationItem(ConfigurationItem pCI) {
        em.merge(pCI);
    }

    public ConfigurationItem removeConfigurationItem(ConfigurationItemKey pKey) throws ConfigurationItemNotFoundException, LayerNotFoundException {
        ConfigurationItem ci = loadConfigurationItem(pKey);

        removeLayersFromConfigurationItem(pKey);
        removeEffectivitiesFromConfigurationItem(pKey);
        removeEffectivityConfigSpecFromConfigurationItem(pKey);

        em.remove(ci);
        return ci;
    }

    public void removeLayersFromConfigurationItem(ConfigurationItemKey pKey){
        TypedQuery<Layer> query = em.createNamedQuery("Layer.removeLayersFromConfigurationItem", Layer.class);
        query.setParameter("workspaceId", pKey.getWorkspace());
        query.setParameter("configurationItemId", pKey.getId());
        query.executeUpdate();
    }

    public void removeEffectivitiesFromConfigurationItem(ConfigurationItemKey pKey){
        TypedQuery<Effectivity> query = em.createNamedQuery("Effectivity.removeEffectivitiesFromConfigurationItem", Effectivity.class);
        query.setParameter("workspaceId", pKey.getWorkspace());
        query.setParameter("configurationItemId", pKey.getId());
        query.executeUpdate();
    }

    public void removeEffectivityConfigSpecFromConfigurationItem(ConfigurationItemKey pKey){
        TypedQuery<EffectivityConfigSpec> query = em.createNamedQuery("EffectivityConfigSpec.removeEffectivityConfigSpecFromConfigurationItem", EffectivityConfigSpec.class);
        query.setParameter("workspaceId", pKey.getWorkspace());
        query.setParameter("configurationItemId", pKey.getId());
        query.executeUpdate();
    }

    public List<ConfigurationItem> findAllConfigurationItems(String pWorkspaceId) {
        TypedQuery<ConfigurationItem> query = em.createQuery("SELECT DISTINCT ci FROM ConfigurationItem ci WHERE ci.workspace.id = :workspaceId", ConfigurationItem.class);
        return query.setParameter("workspaceId", pWorkspaceId).getResultList();
    }

    public ConfigurationItem loadConfigurationItem(ConfigurationItemKey pKey)
            throws ConfigurationItemNotFoundException {
        ConfigurationItem ci = em.find(ConfigurationItem.class, pKey);
        if (ci == null) {
            throw new ConfigurationItemNotFoundException(mLocale, pKey.getId());
        } else {
            return ci;
        }
    }

    public void createConfigurationItem(ConfigurationItem pCI) throws ConfigurationItemAlreadyExistsException, CreationException {
        try {
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pCI);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            throw new ConfigurationItemAlreadyExistsException(mLocale, pCI);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public List<ConfigurationItem> findConfigurationItemsByDesignItem(PartMaster partMaster) {

        TypedQuery<ConfigurationItem> query = em.createNamedQuery("ConfigurationItem.findByDesignItem", ConfigurationItem.class);
        return query.setParameter("designItem", partMaster).getResultList();

    }

    public boolean isPartMasterLinkedToConfigurationItem(PartMaster partMaster){
        return findConfigurationItemsByDesignItem(partMaster).size() > 0;
    }
}
