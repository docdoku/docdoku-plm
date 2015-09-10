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

import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IContextManagerLocal;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

/**
 * @author morgan on 07/09/15.
 */

@DeclareRoles({UserGroupMapping.GUEST_PROXY_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@Local(IContextManagerLocal.class)
@Stateless(name = "ContextManagerBean")
public class ContextManagerBean implements IContextManagerLocal{

    @Resource
    private SessionContext ctx;

    @RolesAllowed({UserGroupMapping.GUEST_PROXY_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public boolean isCallerInRole(String role) {
        return ctx.isCallerInRole(role);
    }

    @RolesAllowed({UserGroupMapping.GUEST_PROXY_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public String getCallerPrincipalLogin() {
        return ctx.getCallerPrincipal().toString();
    }

    @RolesAllowed({UserGroupMapping.GUEST_PROXY_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public String getCallerPrincipalName() {
        return ctx.getCallerPrincipal().getName();
    }

}
