/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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
import com.docdoku.core.services.IWorkspaceManagerLocal;
import com.docdoku.core.services.UserNotFoundException;
import com.docdoku.server.dao.UserDAO;
import com.docdoku.server.dao.WorkspaceDAO;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Locale;
import java.util.logging.Logger;

@DeclareRoles("admin")
@Local(IWorkspaceManagerLocal.class)
@Stateless(name = "WorkspaceManagerBean")
public class WorkspaceManagerBean implements IWorkspaceManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @Resource
    private SessionContext ctx;

    private final static Logger LOGGER = Logger.getLogger(WorkspaceManagerBean.class.getName());

    @PostConstruct
    private void init() {

    }

    @RolesAllowed("admin")
    @Override
    public Long getDiskUsageInWorkspace(String workspaceId) throws UserNotFoundException {
        User admin = new UserDAO(em).loadAdmin(ctx.getCallerPrincipal().getName());
        return new WorkspaceDAO(new Locale(admin.getLanguage()),em).getDiskUsageForWorkspace(workspaceId);
    }

}
