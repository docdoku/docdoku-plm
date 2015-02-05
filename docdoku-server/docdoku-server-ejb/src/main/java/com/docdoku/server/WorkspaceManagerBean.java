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


import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.services.IWorkspaceManagerLocal;
import com.docdoku.server.dao.AccountDAO;
import com.docdoku.server.dao.WorkspaceDAO;
import com.docdoku.server.esindexer.ESIndexer;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
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

    @RolesAllowed(UserGroupMapping.ADMIN_ROLE_ID)
    @Override
    public long getDiskUsageInWorkspace(String workspaceId) throws AccountNotFoundException {
        Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
        return new WorkspaceDAO(new Locale(account.getLanguage()),em).getDiskUsageForWorkspace(workspaceId);
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Asynchronous
    public void deleteWorkspace(String workspaceId) {
        try{
            if(userManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)){
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
        } catch (UserNotFoundException | UserNotActiveException | AccessRightException e) {
            LOGGER.log(Level.SEVERE,"Attempt to delete a unauthorized workspace : ("+workspaceId+")",e);
        } catch (WorkspaceNotFoundException e) {
            LOGGER.log(Level.WARNING,"Attempt to delete a workspace which does not exist : ("+workspaceId+")",e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Exception deleting workspace "+workspaceId,e);
        }

    }

    @Override
    @RolesAllowed({UserGroupMapping.ADMIN_ROLE_ID})
    public void synchronizeIndexer(String workspaceId) {
        esIndexer.indexWorkspace(workspaceId);
    }

    private void doWorkspaceDeletion(Workspace workspace){
        Account admin = workspace.getAdmin();
        String workspaceId = workspace.getId();
        try {
            new WorkspaceDAO(em, dataManager).removeWorkspace(workspace);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"IOException while deleting the workspace : "+workspaceId,e);
        } catch (StorageException e) {
            LOGGER.log(Level.SEVERE,"StorageException while deleting the workspace : "+workspaceId,e);
        }

        mailerManager.sendWorkspaceDeletionNotification(admin,workspaceId);
    }
}
