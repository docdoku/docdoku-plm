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
import com.docdoku.core.admin.WorkspaceOptions;
import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.notification.NotificationOptions;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.core.util.NamingConvention;
import com.docdoku.server.dao.AccountDAO;
import com.docdoku.server.dao.NotificationOptionsDAO;
import com.docdoku.server.dao.UserDAO;
import com.docdoku.server.dao.WorkspaceDAO;

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
    private INotifierLocal mailerManager;

    @Inject
    private IIndexerManagerLocal indexerManager;

    @Inject
    private IPlatformOptionsManagerLocal platformOptionsManager;

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

        Workspace workspace;
        Account admin = null;
        Exception exceptionThrown = null;

        try {
            WorkspaceDAO workspaceDAO = new WorkspaceDAO(em, storageManager);
            workspace = workspaceDAO.loadWorkspace(workspaceId);
            admin = workspace.getAdmin();

            String callerLogin = contextManager.getCallerPrincipalLogin();

            boolean isAllowedToDeleteWorkspace =
                    contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID) ||
                            workspace.getAdmin().getLogin().equals(callerLogin);

            if (isAllowedToDeleteWorkspace) {
                workspaceDAO.removeWorkspace(workspace);
                indexerManager.deleteWorkspaceIndex(workspaceId);
                mailerManager.sendWorkspaceDeletionNotification(admin, workspaceId);
            } else {
                User user = userManager.whoAmI(workspaceId);
                LOGGER.log(Level.SEVERE, "Caller (" + user.getLogin() + ") is not authorized to delete workspace : (" + workspaceId + ")");
                throw new AccessRightException(new Locale(user.getLanguage()), user);
            }

        } catch (UserNotFoundException | UserNotActiveException | AccessRightException | WorkspaceNotEnabledException e) {
            LOGGER.log(Level.SEVERE, "Caller not authorized to delete workspace : (" + workspaceId + ")", e);
            exceptionThrown = e;
        } catch (WorkspaceNotFoundException e) {
            LOGGER.log(Level.WARNING, "Attempt to delete a workspace which does not exist : (" + workspaceId + ")", e);
            exceptionThrown = e;
        } catch (StorageException | IOException e) {
            LOGGER.log(Level.SEVERE, "Unhandled Exception deleting workspace " + workspaceId, e);
            exceptionThrown = e;
        } catch (FolderNotFoundException | AccountNotFoundException | EntityConstraintException e) {
            LOGGER.log(Level.SEVERE, "Application Exception deleting workspace " + workspaceId, e);
            exceptionThrown = e;
        }

        if (null != exceptionThrown && null != admin) {
            mailerManager.sendWorkspaceDeletionErrorNotification(admin, workspaceId);
        }

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


    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Workspace createWorkspace(String pID, Account pAdmin, String pDescription, boolean pFolderLocked) throws WorkspaceAlreadyExistsException, FolderAlreadyExistsException, UserAlreadyExistsException, CreationException, NotAllowedException {
        if (!NamingConvention.correct(pID)) {
            throw new NotAllowedException(new Locale(pAdmin.getLanguage()), "NotAllowedException9", pID);
        }
        OperationSecurityStrategy workspaceCreationStrategy = platformOptionsManager.getWorkspaceCreationStrategy();
        Workspace workspace = new Workspace(pID, pAdmin, pDescription, pFolderLocked);
        workspace.setEnabled(workspaceCreationStrategy.equals(OperationSecurityStrategy.NONE));
        new WorkspaceDAO(em).createWorkspace(workspace);
        User userToCreate = new User(workspace, pAdmin);
        UserDAO userDAO = new UserDAO(new Locale(pAdmin.getLanguage()), em);
        userDAO.createUser(userToCreate);
        userDAO.addUserMembership(workspace, userToCreate);

        try {
            indexerManager.createWorkspaceIndex(pID);
        } catch (Exception e) { // TODO review exception thrown
            throw new WorkspaceAlreadyExistsException(new Locale(pAdmin.getLanguage()), workspace);
        }

        return workspace;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Workspace getWorkspace(String workspaceId) throws WorkspaceNotFoundException, AccountNotFoundException {

        if (contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            return new WorkspaceDAO(em).loadWorkspace(workspaceId);
        }

        String login = contextManager.getCallerPrincipalLogin();

        User[] users = new UserDAO(em).getUsers(login);
        Account account = new AccountDAO(em).loadAccount(login);
        Locale locale = new Locale(account.getLanguage());

        Workspace workspace = null;
        for (User user : users) {
            if (user.getWorkspace().getId().equals(workspaceId)) {
                workspace = user.getWorkspace();
                break;
            }
        }

        if (workspace == null) {
            throw new WorkspaceNotFoundException(locale, workspaceId);
        }

        return workspace;
    }


    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public WorkspaceOptions getWorkspaceOptions(String workspaceId) throws AccountNotFoundException, WorkspaceNotFoundException {

        WorkspaceOptions settings = new WorkspaceDAO(em).loadWorkspaceOptions(workspaceId);
        if (contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            return settings;
        }

        String login = contextManager.getCallerPrincipalLogin();

        User[] users = new UserDAO(em).getUsers(login);
        Account account = new AccountDAO(em).loadAccount(login);
        Locale locale = new Locale(account.getLanguage());

        Workspace workspace = null;
        for (User user : users) {
            if (user.getWorkspace().getId().equals(workspaceId)) {
                workspace = user.getWorkspace();
                break;
            }
        }

        if (workspace == null) {
            throw new WorkspaceNotFoundException(locale, workspaceId);
        }
        return settings;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void updateWorkspaceOptions(WorkspaceOptions pWorkspaceOptions) throws AccessRightException, AccountNotFoundException {
        Workspace wks=em.find(Workspace.class,pWorkspaceOptions.getWorkspace().getId());
        pWorkspaceOptions.setWorkspace(wks);
        Account account = userManager.checkAdmin(wks);
        new WorkspaceDAO(new Locale(account.getLanguage()), em).updateWorkspaceOptions(pWorkspaceOptions);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    public Workspace updateWorkspace(String workspaceId, String description, boolean isFolderLocked) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = userManager.checkAdmin(workspaceId);
        Locale locale = new Locale(account.getLanguage());
        WorkspaceDAO workspaceDAO = new WorkspaceDAO(locale, em);
        Workspace workspace = workspaceDAO.loadWorkspace(workspaceId);

        workspace.setDescription(description);
        workspace.setFolderLocked(isFolderLocked);

        return workspace;
    }

    @Override
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    public void setNotificationOptions(String workspaceId, boolean sendEmails) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException {
        Account account = userManager.checkAdmin(workspaceId);
        Locale locale = new Locale(account.getLanguage());
        NotificationOptionsDAO notificationOptionsDAO = new NotificationOptionsDAO(locale, em);
        notificationOptionsDAO.updateNotificationOptions(workspaceId, sendEmails);
    }

    @Override
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    public NotificationOptions getNotificationOptions(String workspaceId) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException {
        Account account = userManager.checkAdmin(workspaceId);
        Locale locale = new Locale(account.getLanguage());
        NotificationOptionsDAO notificationOptionsDAO = new NotificationOptionsDAO(locale, em);
        return notificationOptionsDAO.getNotificationOptionsOrNew(workspaceId);
    }

}
