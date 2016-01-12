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

package com.docdoku.server.filters;


import com.docdoku.core.common.Account;
import com.docdoku.core.common.Organization;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IOrganizationManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.jsf.actions.AccountBean;

import java.util.*;

public class FilterUtils {

    private FilterUtils() {
    }

    public static void hookAccountBeanData(String remoteUser, IUserManagerLocal userManager, IAccountManagerLocal accountManager, IOrganizationManagerLocal organizationManager, AccountBean accountBean) throws AccountNotFoundException {

        Account account = accountManager.getAccount(remoteUser);
        boolean isAdmin = userManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID);
        accountBean.setLogin(account.getLogin());
        accountBean.setEmail(account.getEmail());
        accountBean.setLanguage(account.getLanguage());
        accountBean.setName(account.getName());
        accountBean.setTimeZone(account.getTimeZone());
        Organization organization = organizationManager.getOrganizationOfAccount(remoteUser);
        if(organization!=null){
            accountBean.setOrganizationName(organization.getName());
            accountBean.setOrganizationAdmin(organization.getOwner().getLogin());
        }

        accountBean.setSuperAdmin(isAdmin);

        Map<String, Workspace> administeredWorkspaces = new HashMap<>();
        for (Workspace wks : userManager.getAdministratedWorkspaces()) {
            administeredWorkspaces.put(wks.getId(), wks);
        }
        accountBean.setAdministeredWorkspaces(administeredWorkspaces);

        if(!isAdmin){
            Set<Workspace> regularWorkspaces = new HashSet<>();
            Workspace[] workspaces = userManager.getWorkspacesWhereCallerIsActive();
            regularWorkspaces.addAll(Arrays.asList(workspaces));
            regularWorkspaces.removeAll(administeredWorkspaces.values());
            accountBean.setRegularWorkspaces(regularWorkspaces);
        }
    }

}
