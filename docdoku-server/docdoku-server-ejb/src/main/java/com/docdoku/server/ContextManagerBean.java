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
 * Created by morgan on 07/09/15.
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
