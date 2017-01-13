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


import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.server.dao.AccountDAO;
import com.docdoku.server.dao.WorkspaceDAO;
import com.docdoku.server.esindexer.ESIndexer;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@Local(IWorkspaceManagerLocal.class)
@Stateless(name = "WorkspaceManagerBean")
public class WorkspaceManagerBean implements IWorkspaceManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private IBinaryStorageManagerLocal storageManager;

    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private IContextManagerLocal contextManager;

    @Inject
    private IMailerLocal mailerManager;

    @Inject
    private ESIndexer esIndexer;

    private static final Logger LOGGER = Logger.getLogger(WorkspaceManagerBean.class.getName());

    @RolesAllowed(UserGroupMapping.ADMIN_ROLE_ID)
    @Override
    public long getDiskUsageInWorkspace(String workspaceId) throws AccountNotFoundException {
        Account account = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());
        return new WorkspaceDAO(new Locale(account.getLanguage()), em).getDiskUsageForWorkspace(workspaceId);
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Asynchronous
    public void deleteWorkspace(String workspaceId) {
        try {

            Workspace workspace = new WorkspaceDAO(em, storageManager).loadWorkspace(workspaceId);
            String callerLogin = contextManager.getCallerPrincipalLogin();

            boolean isAllowedToDeleteWorkspace =
                    contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID) ||
                            workspace.getAdmin().getLogin().equals(callerLogin);

            if (isAllowedToDeleteWorkspace) {
                doWorkspaceDeletion(workspace);
                esIndexer.deleteWorkspace(workspaceId);
            } else {
                User user = userManager.whoAmI(workspaceId);
                LOGGER.log(Level.SEVERE, "Caller (" + user.getLogin() + ") not authorized to delete workspace : (" + workspaceId + ")");
                throw new AccessRightException(new Locale(user.getLanguage()), user);
            }

        } catch (UserNotFoundException | UserNotActiveException | AccessRightException e) {
            LOGGER.log(Level.SEVERE, "Caller not authorized to delete workspace : (" + workspaceId + ")", e);
        } catch (WorkspaceNotFoundException e) {
            LOGGER.log(Level.WARNING, "Attempt to delete a workspace which does not exist : (" + workspaceId + ")", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception deleting workspace " + workspaceId, e);
        }
    }

    @Override
    @RolesAllowed({UserGroupMapping.ADMIN_ROLE_ID})
    public void synchronizeIndexer(String workspaceId) {
        esIndexer.indexWorkspace(workspaceId);
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    public Workspace changeAdmin(String workspaceId, String login) throws WorkspaceNotFoundException, AccountNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, WorkspaceNotEnabledException {
        Workspace workspace = new WorkspaceDAO(em).loadWorkspace(workspaceId);
        Account account = new AccountDAO(em).loadAccount(login);

        if (contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            workspace.setAdmin(account);

        } else {
            if (workspace.getAdmin().getLogin().equals(contextManager.getCallerPrincipalLogin())) {
                workspace.setAdmin(account);
            } else {
                User user = userManager.whoAmI(workspaceId);
                throw new AccessRightException(new Locale(user.getLanguage()), user);
            }
        }

        return workspace;
    }

    @Override
    @RolesAllowed(UserGroupMapping.ADMIN_ROLE_ID)
    public Workspace enableWorkspace(String workspaceId, boolean enabled) throws WorkspaceNotFoundException {
        Workspace workspace = new WorkspaceDAO(em).loadWorkspace(workspaceId);
        workspace.setEnabled(enabled);
        return workspace;
    }

    private void doWorkspaceDeletion(Workspace workspace) throws Exception {
        Account admin = workspace.getAdmin();
        String workspaceId = workspace.getId();
        try {
            new WorkspaceDAO(em, storageManager).removeWorkspace(workspace);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "IOException while deleting the workspace : " + workspaceId, e);
        } catch (StorageException e) {
            LOGGER.log(Level.SEVERE, "StorageException while deleting the workspace : " + workspaceId, e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while deleting the workspace : " + workspaceId, e);
            //TODO : create own exception
            mailerManager.sendWorkspaceDeletionErrorNotification(admin, workspaceId);
            throw new Exception("Runtime exception while deleting the workspace : " + workspaceId);

        }

        mailerManager.sendWorkspaceDeletionNotification(admin, workspaceId);
    }
}
