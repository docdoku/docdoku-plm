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

import com.docdoku.core.admin.OperationSecurityStrategy;
import com.docdoku.core.admin.PlatformOptions;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IPlatformOptionsManagerLocal;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Morgan Guimard
 */
@DeclareRoles({UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID})
@Local(IPlatformOptionsManagerLocal.class)
@Stateless(name = "PlatformOptionsManagerBean")
public class PlatformOptionsManagerBean implements IPlatformOptionsManagerLocal {

    private static final Logger LOGGER = Logger.getLogger(PlatformOptionsManagerBean.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    public PlatformOptions getPlatformOptions() {
        return loadPlatformOptions();
    }

    @Override
    public OperationSecurityStrategy getWorkspaceCreationStrategy(){
        PlatformOptions platformOptions = loadPlatformOptions();
        return platformOptions.getWorkspaceCreationStrategy();
    }

    @Override
    public OperationSecurityStrategy getRegistrationStrategy(){
        PlatformOptions platformOptions = loadPlatformOptions();
        return platformOptions.getRegistrationStrategy();
    }

    @Override
    @RolesAllowed(UserGroupMapping.ADMIN_ROLE_ID)
    public void setWorkspaceCreationStrategy(OperationSecurityStrategy strategy){
        PlatformOptions platformOptions = loadPlatformOptions();
        platformOptions.setWorkspaceCreationStrategy(strategy);
    }

    @Override
    @RolesAllowed(UserGroupMapping.ADMIN_ROLE_ID)
    public void setRegistrationStrategy(OperationSecurityStrategy strategy){
        PlatformOptions platformOptions = loadPlatformOptions();
        platformOptions.setRegistrationStrategy(strategy);
    }

    private PlatformOptions loadPlatformOptions() {

        PlatformOptions platformOptions = em.find(PlatformOptions.class, PlatformOptions.UNIQUE_ID);

        if(platformOptions == null){
            LOGGER.log(Level.INFO, "No options set. Creating default options ...");
            platformOptions = new PlatformOptions();
            platformOptions.setDefaults();
            em.persist(platformOptions);
            em.flush();
        }

        return platformOptions;

    }

}
