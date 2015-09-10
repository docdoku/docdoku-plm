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

package com.docdoku.server;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.*;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IPSFilterManagerLocal;
import com.docdoku.core.services.IPSFilterManagerWS;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.configuration.filter.LatestPSFilter;
import com.docdoku.server.configuration.filter.LatestReleasedPSFilter;
import com.docdoku.server.configuration.filter.ReleasedPSFilter;
import com.docdoku.server.configuration.filter.WIPPSFilter;
import com.docdoku.server.configuration.spec.ProductBaselineConfigSpec;
import com.docdoku.server.configuration.spec.ProductInstanceConfigSpec;
import com.docdoku.server.dao.ProductBaselineDAO;
import com.docdoku.server.dao.ProductInstanceMasterDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(IPSFilterManagerLocal.class)
@Stateless(name = "PSFilterManagerBean")
@WebService(endpointInterface = "com.docdoku.core.services.IPSFilterManagerWS")
public class PSFilterManagerBean implements IPSFilterManagerLocal,IPSFilterManagerWS {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private IUserManagerLocal userManager;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PSFilter getBaselinePSFilter(int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException {
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(em);
        ProductBaseline productBaseline = productBaselineDAO.loadBaseline(baselineId);
        User user = userManager.checkWorkspaceReadAccess(productBaseline.getConfigurationItem().getWorkspaceId());
        return new ProductBaselineConfigSpec(productBaseline, user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PSFilter getProductInstanceConfigSpec(ConfigurationItemKey ciKey, String serialNumber) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        ProductInstanceMasterKey pimk = new ProductInstanceMasterKey(serialNumber, ciKey);
        ProductInstanceMaster productIM = new ProductInstanceMasterDAO(em).loadProductInstanceMaster(pimk);
        ProductInstanceIteration productII = productIM.getLastIteration();
        return new ProductInstanceConfigSpec(productII, user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PSFilter getPSFilter(ConfigurationItemKey ciKey, String filterType, boolean diverge) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, BaselineNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());

        if (filterType == null) {
            return new WIPPSFilter(user);
        }

        PSFilter filter;

        switch (filterType) {

            case "wip":
            case "undefined":
                filter = new WIPPSFilter(user, diverge);
                break;
            case "latest":
                filter = new LatestPSFilter(user, diverge);
                break;
            case "released":
                filter = new ReleasedPSFilter(user, diverge);
                break;
            case "latest-released":
                filter = new LatestReleasedPSFilter(user, diverge);
                break;
            default:
                if (filterType.startsWith("pi-")) {
                    String serialNumber = filterType.substring(3);
                    filter = getProductInstanceConfigSpec(ciKey, serialNumber);
                } else {
                    filter = getBaselinePSFilter(Integer.parseInt(filterType));
                }
                break;
        }
        return filter;
    }

}