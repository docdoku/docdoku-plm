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


import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.services.ConfigurationItemAlreadyExistsException;
import com.docdoku.core.services.ConfigurationItemNotFoundException;
import com.docdoku.core.services.CreationException;
import java.util.List;
import java.util.Locale;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

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

    public ConfigurationItem removeConfigurationItem(ConfigurationItemKey pKey) throws ConfigurationItemNotFoundException {
        ConfigurationItem ci = loadConfigurationItem(pKey);
        em.remove(ci);
        return ci;
    }

    public ConfigurationItem[] findAllConfigurationItems(String pWorkspaceId) {
        ConfigurationItem[] cis;
        Query query = em.createQuery("SELECT DISTINCT ci FROM ConfigurationItem ci WHERE ci.workspace.id = :workspaceId");
        List listCIs = query.setParameter("workspaceId", pWorkspaceId).getResultList();
        cis = new ConfigurationItem[listCIs.size()];
        for (int i = 0; i < listCIs.size(); i++) {
            cis[i] = (ConfigurationItem) listCIs.get(i);
        }

        return cis;
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
}
