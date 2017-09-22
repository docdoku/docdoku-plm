/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.server;

import com.docdoku.core.exceptions.PlatformHealthException;
import com.docdoku.core.services.IBinaryStorageManagerLocal;
import com.docdoku.core.services.IIndexerManagerLocal;
import com.docdoku.core.services.IPlatformHealthManagerLocal;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Morgan Guimard
 */
@Local(IPlatformHealthManagerLocal.class)
@Stateless(name = "PlatformHealthManagerBean")
public class PlatformHealthManagerBean implements IPlatformHealthManagerLocal {

    private static final Logger LOGGER = Logger.getLogger(PlatformHealthManagerBean.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Inject
    private IIndexerManagerLocal indexerManager;

    @Inject
    private IBinaryStorageManagerLocal storageManager;


    @Override
    public void runHealthCheck() throws PlatformHealthException {

        // Database check
        try {
            Long one = (Long) em.createNativeQuery("select 1 from dual").getSingleResult();
            if (one != 1) {
                throw new PlatformHealthException("Cannot reach database.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database doesn't seem to be reachable", e);
            throw new PlatformHealthException(e);
        }

        // Indexer check
        boolean ping = indexerManager.ping();
        if (!ping) {
            LOGGER.log(Level.WARNING, "Indexer doesn't seem to be reachable");
            throw new PlatformHealthException("Cannot reach indexer");
        }

    }
}
