/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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


import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.AccessRightException;
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.exceptions.StorageException;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.services.IWorkspaceManagerLocal;
import com.docdoku.server.dao.AccountDAO;
import com.docdoku.server.dao.WorkspaceDAO;
import com.docdoku.server.esindexer.ESIndexer;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

@DeclareRoles({"users","admin"})
@Local(IWorkspaceManagerLocal.class)
@Stateless(name = "WorkspaceManagerBean")
public class WorkspaceManagerBean implements IWorkspaceManagerLocal {

    @EJB
    private IDataManagerLocal dataManager;

    @EJB
    private IUserManagerLocal userManager;

    @EJB
    private IMailerLocal mailerManager;

    @EJB
    private ESIndexer esIndexer;

    @PersistenceContext
    private EntityManager em;

    @Resource
    private SessionContext ctx;

    private static final Logger LOGGER = Logger.getLogger(WorkspaceManagerBean.class.getName());

    @PostConstruct
    private void init() {

    }

    @RolesAllowed("admin")
    @Override
    public long getDiskUsageInWorkspace(String workspaceId) throws AccountNotFoundException {
        Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
        return new WorkspaceDAO(new Locale(account.getLanguage()),em).getDiskUsageForWorkspace(workspaceId);
    }

    @Override
    @RolesAllowed({"users","admin"})
    @Asynchronous
    public void deleteWorkspace(String workspaceId) {
        try{
            if(userManager.isCallerInRole("admin")){
                Workspace workspace = new WorkspaceDAO(em, dataManager).loadWorkspace(workspaceId);
                doWorkspaceDeletion(workspace);
                esIndexer.deleteWorkspace(workspaceId);
            }else{
                User user = userManager.checkWorkspaceReadAccess(workspaceId);
                Workspace workspace = new WorkspaceDAO(em, dataManager).loadWorkspace(workspaceId);
                if(workspace.getAdmin().getLogin().equals(ctx.getCallerPrincipal().getName())){
                   doWorkspaceDeletion(workspace);
                    esIndexer.deleteWorkspace(workspaceId);
                }else{
                    throw new AccessRightException(new Locale(user.getLanguage()),user);
                }
            }

        }catch(Exception e){
            LOGGER.severe("Exception deleting workspace " + e.getMessage());
        }

    }

    @Override
    @RolesAllowed({"admin"})
    public void synchronizeIndexer(String workspaceId) {
        esIndexer.indexWorkspace(workspaceId);
    }

    private void doWorkspaceDeletion(Workspace workspace){
        Account admin = workspace.getAdmin();
        String workspaceId = workspace.getId();
        try {
            new WorkspaceDAO(em, dataManager).removeWorkspace(workspace);
        } catch (IOException e) {
            LOGGER.severe("IOException while deleting the workspace : "+workspaceId);
            LOGGER.severe(e.getMessage());
        } catch (StorageException e) {
            LOGGER.severe("StorageException while deleting the workspace : "+workspaceId);
            LOGGER.severe(e.getMessage());
        }

        mailerManager.sendWorkspaceDeletionNotification(admin,workspaceId);

    }
}
