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
import com.docdoku.core.common.*;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.*;
import com.docdoku.core.services.IContextManagerLocal;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IPlatformOptionsManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.util.NamingConvention;
import com.docdoku.server.dao.*;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.events.*;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@Local(IUserManagerLocal.class)
@Stateless(name = "UserManagerBean")
public class UserManagerBean implements IUserManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private ESIndexer esIndexer;

    @Inject
    private Event<WorkspaceAccessEvent> workspaceAccessEvent;

    @Inject
    private Event<UserEvent> userEvent;

    @Inject
    private Event<UserGroupEvent> groupEvent;

    @Inject
    private IContextManagerLocal contextManager;

    @Inject
    private IMailerLocal mailer;

    @Inject
    private IPlatformOptionsManagerLocal platformOptionsManager;


    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void addUserInGroup(UserGroupKey pGroupKey, String pLogin) throws AccessRightException, UserGroupNotFoundException, AccountNotFoundException, WorkspaceNotFoundException, UserAlreadyExistsException, FolderAlreadyExistsException, CreationException {
        Account account = checkAdmin(pGroupKey.getWorkspaceId());
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        User userToAdd = em.find(User.class, new UserKey(pGroupKey.getWorkspaceId(), pLogin));
        if (userToAdd == null) {
            Account userAccount = new AccountDAO(em).loadAccount(pLogin);
            Workspace workspace = em.getReference(Workspace.class, pGroupKey.getWorkspaceId());
            userToAdd = new User(workspace, userAccount);
            userDAO.createUser(userToAdd);
        }
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        UserGroup group = groupDAO.loadUserGroup(pGroupKey);

        userDAO.removeUserMembership(new WorkspaceUserMembershipKey(pGroupKey.getWorkspaceId(), pGroupKey.getWorkspaceId(), pLogin));
        group.addUser(userToAdd);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void addUserInWorkspace(String pWorkspaceId, String pLogin) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, UserAlreadyExistsException, FolderAlreadyExistsException, CreationException {
        Account account = checkAdmin(pWorkspaceId);
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        User userToAdd = em.find(User.class, new UserKey(pWorkspaceId, pLogin));
        Workspace workspace = em.getReference(Workspace.class, pWorkspaceId);
        if (userToAdd == null) {
            Account userAccount = new AccountDAO(em).loadAccount(pLogin);
            userToAdd = new User(workspace, userAccount);
            userDAO.createUser(userToAdd);
        }
        userDAO.addUserMembership(workspace, userToAdd);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Workspace removeUser(String pWorkspaceId, String login) throws UserNotFoundException, AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, FolderNotFoundException, ESServerException, EntityConstraintException, UserNotActiveException, DocumentRevisionNotFoundException {
        Account account = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());
        Workspace workspace = new WorkspaceDAO(new Locale(account.getLanguage()), em).loadWorkspace(pWorkspaceId);
        checkAdmin(workspace, account);

        Locale locale = new Locale(account.getLanguage());
        UserDAO userDAO = new UserDAO(locale, em);
        User user = userDAO.loadUser(new UserKey(pWorkspaceId, login));

        userEvent.select(new AnnotationLiteral<Removed>() {
        }).fire(new UserEvent(user));
        userDAO.removeUser(user);

        return workspace;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void removeUserFromGroup(UserGroupKey pGroupKey, String[] pLogins) throws AccessRightException, UserGroupNotFoundException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pGroupKey.getWorkspaceId());
        UserGroup group = new UserGroupDAO(new Locale(account.getLanguage()), em).loadUserGroup(pGroupKey);
        for (String login : pLogins) {
            User userToRemove = em.getReference(User.class, new UserKey(pGroupKey.getWorkspaceId(), login));
            group.removeUser(userToRemove);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public UserGroup removeUserFromGroup(UserGroupKey pGroupKey, String login) throws AccessRightException, UserGroupNotFoundException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pGroupKey.getWorkspaceId());
        UserGroup group = new UserGroupDAO(new Locale(account.getLanguage()), em).loadUserGroup(pGroupKey);
        User userToRemove = em.getReference(User.class, new UserKey(pGroupKey.getWorkspaceId(), login));
        group.removeUser(userToRemove);
        return group;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public UserGroup createUserGroup(String pId, Workspace pWorkspace) throws UserGroupAlreadyExistsException, AccessRightException, AccountNotFoundException, CreationException {
        Account account = checkAdmin(pWorkspace);
        UserGroup groupToCreate = new UserGroup(pWorkspace, pId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        groupDAO.createUserGroup(groupToCreate);
        groupDAO.addUserGroupMembership(pWorkspace, groupToCreate);
        return groupToCreate;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public UserGroup createUserGroup(String pId, String workspaceId) throws UserGroupAlreadyExistsException, AccessRightException, AccountNotFoundException, CreationException, WorkspaceNotFoundException {
        Workspace workspace = new WorkspaceDAO(em).loadWorkspace(workspaceId);
        return createUserGroup(pId, workspace);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Workspace createWorkspace(String pID, Account pAdmin, String pDescription, boolean pFolderLocked) throws WorkspaceAlreadyExistsException, FolderAlreadyExistsException, UserAlreadyExistsException, CreationException, ESIndexNamingException, NotAllowedException {
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
            esIndexer.createIndex(pID);
        } catch (ESServerException e) {
            // When ElasticSearch have not start
        } catch (ESIndexAlreadyExistsException e) {
            throw new WorkspaceAlreadyExistsException(new Locale(pAdmin.getLanguage()), workspace);                      // Send if the workspace have the same index name that another
        }

        return workspace;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Workspace[] getAdministratedWorkspaces() throws AccountNotFoundException {
        if (contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            return new AccountDAO(em).getAllWorkspaces();
        } else {
            Account account = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());
            Workspace[] workspaces = new AccountDAO(em).getAdministratedWorkspaces(account);
            return Stream.of(workspaces).filter(Workspace::isEnabled).toArray(Workspace[]::new);
        }
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
    public UserGroup[] getUserGroups(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccountNotFoundException, WorkspaceNotEnabledException {
        if (contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            Account account = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());
            return new UserGroupDAO(new Locale(account.getLanguage()), em).findAllUserGroups(pWorkspaceId);
        } else {
            User user = checkWorkspaceReadAccess(pWorkspaceId);
            return new UserGroupDAO(new Locale(user.getLanguage()), em).findAllUserGroups(pWorkspaceId);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public UserGroup getUserGroup(UserGroupKey pKey) throws WorkspaceNotFoundException, UserGroupNotFoundException, UserNotFoundException, UserNotActiveException, AccountNotFoundException, WorkspaceNotEnabledException {
        if (contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            Account account = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());
            return new UserGroupDAO(new Locale(account.getLanguage()), em).loadUserGroup(pKey);
        } else {
            User user = checkWorkspaceReadAccess(pKey.getWorkspaceId());
            return new UserGroupDAO(new Locale(user.getLanguage()), em).loadUserGroup(pKey);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public WorkspaceUserMembership getWorkspaceSpecificUserMemberships(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new UserDAO(new Locale(user.getLanguage()), em).loadUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, user.getLogin()));
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public WorkspaceUserMembership[] getWorkspaceUserMemberships(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccountNotFoundException, WorkspaceNotEnabledException {
        if (contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            Account account = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());
            return new UserDAO(new Locale(account.getLanguage()), em).findAllWorkspaceUserMemberships(pWorkspaceId);
        } else {
            User user = checkWorkspaceReadAccess(pWorkspaceId);
            return new UserDAO(new Locale(user.getLanguage()), em).findAllWorkspaceUserMemberships(pWorkspaceId);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public WorkspaceUserGroupMembership[] getWorkspaceSpecificUserGroupMemberships(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, UserGroupNotFoundException, WorkspaceNotEnabledException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        UserGroupDAO userGroupDAO = new UserGroupDAO(new Locale(user.getLanguage()), em);
        List<UserGroup> userGroups = userGroupDAO.getUserGroups(pWorkspaceId, user);
        WorkspaceUserGroupMembership[] workspaceUserGroupMembership = new WorkspaceUserGroupMembership[userGroups.size()];
        for (int i = 0; i < userGroups.size(); i++) {
            workspaceUserGroupMembership[i] = userGroupDAO.loadUserGroupMembership(new WorkspaceUserGroupMembershipKey(pWorkspaceId, pWorkspaceId, userGroups.get(i).getId()));
        }
        return workspaceUserGroupMembership;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public WorkspaceUserGroupMembership[] getWorkspaceUserGroupMemberships(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccountNotFoundException, WorkspaceNotEnabledException {
        if (contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            Account account = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());
            return new UserGroupDAO(new Locale(account.getLanguage()), em).findAllWorkspaceUserGroupMemberships(pWorkspaceId);
        } else {
            User user = checkWorkspaceReadAccess(pWorkspaceId);
            return new UserGroupDAO(new Locale(user.getLanguage()), em).findAllWorkspaceUserGroupMemberships(pWorkspaceId);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void grantUserAccess(String pWorkspaceId, String[] pLogins, boolean pReadOnly) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        for (String login : pLogins) {
            WorkspaceUserMembership ms = userDAO.loadUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
            if (ms != null) {
                ms.setReadOnly(pReadOnly);
            }
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public WorkspaceUserMembership grantUserAccess(String pWorkspaceId, String login, boolean pReadOnly) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        WorkspaceUserMembership ms = userDAO.loadUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
        if (ms != null) {
            ms.setReadOnly(pReadOnly);
        }
        return ms;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public WorkspaceUserGroupMembership grantGroupAccess(String pWorkspaceId, String groupId, boolean pReadOnly) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, UserGroupNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        WorkspaceUserGroupMembership ms = groupDAO.loadUserGroupMembership(new WorkspaceUserGroupMembershipKey(pWorkspaceId, pWorkspaceId, groupId));
        if (ms != null) {
            ms.setReadOnly(pReadOnly);
        }

        return ms;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void grantGroupAccess(String pWorkspaceId, String[] pGroupIds, boolean pReadOnly) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, UserGroupNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        for (String id : pGroupIds) {
            WorkspaceUserGroupMembership ms = groupDAO.loadUserGroupMembership(new WorkspaceUserGroupMembershipKey(pWorkspaceId, pWorkspaceId, id));
            if (ms != null) {
                ms.setReadOnly(pReadOnly);
            }
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void activateUsers(String pWorkspaceId, String[] pLogins) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        Workspace workspace = em.getReference(Workspace.class, pWorkspaceId);
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        for (String login : pLogins) {
            User member = em.getReference(User.class, new UserKey(pWorkspaceId, login));
            userDAO.addUserMembership(workspace, member);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void activateUserGroups(String pWorkspaceId, String[] pGroupIds) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        Workspace workspace = em.getReference(Workspace.class, pWorkspaceId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        for (String id : pGroupIds) {
            UserGroup member = em.getReference(UserGroup.class, new UserGroupKey(pWorkspaceId, id));
            groupDAO.addUserGroupMembership(workspace, member);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void activateUser(String pWorkspaceId, String login) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        Workspace workspace = em.getReference(Workspace.class, pWorkspaceId);
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        User member = em.getReference(User.class, new UserKey(pWorkspaceId, login));
        userDAO.addUserMembership(workspace, member);

    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void activateUserGroup(String pWorkspaceId, String groupId) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        Workspace workspace = em.getReference(Workspace.class, pWorkspaceId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        UserGroup member = em.getReference(UserGroup.class, new UserGroupKey(pWorkspaceId, groupId));
        groupDAO.addUserGroupMembership(workspace, member);

    }


    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void passivateUserGroups(String pWorkspaceId, String[] pGroupIds) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        for (String id : pGroupIds) {
            groupDAO.removeUserGroupMembership(new WorkspaceUserGroupMembershipKey(pWorkspaceId, pWorkspaceId, id));
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void passivateUsers(String pWorkspaceId, String[] pLogins) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        for (String login : pLogins) {
            userDAO.removeUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void passivateUserGroup(String pWorkspaceId, String groupId) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        groupDAO.removeUserGroupMembership(new WorkspaceUserGroupMembershipKey(pWorkspaceId, pWorkspaceId, groupId));

    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void removeUsers(String pWorkspaceId, String[] pLogins) throws UserNotFoundException, AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, FolderNotFoundException, ESServerException, EntityConstraintException, UserNotActiveException, DocumentRevisionNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        Locale locale = new Locale(account.getLanguage());
        UserDAO userDAO = new UserDAO(locale, em);

        for (String login : pLogins) {
            User user = userDAO.loadUser(new UserKey(pWorkspaceId, login));
            userEvent.select(new AnnotationLiteral<Removed>() {
            }).fire(new UserEvent(user));
            userDAO.removeUser(user);
        }

    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void passivateUser(String pWorkspaceId, String login) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        userDAO.removeUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));

    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void removeUserGroups(String pWorkspaceId, String[] pIds) throws UserGroupNotFoundException, AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, EntityConstraintException {
        Account account = checkAdmin(pWorkspaceId);
        Locale locale = new Locale(account.getLanguage());
        UserGroupDAO groupDAO = new UserGroupDAO(locale, em);
        for (String id : pIds) {
            UserGroupKey userGroupKey = new UserGroupKey(pWorkspaceId, id);
            if (groupDAO.hasACLConstraint(userGroupKey)) {
                throw new EntityConstraintException(locale, "EntityConstraintException11");
            }
            UserGroup group = groupDAO.loadUserGroup(userGroupKey);
            groupEvent.select(new AnnotationLiteral<Removed>() {
            }).fire(new UserGroupEvent(group));
            groupDAO.removeUserGroup(group);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void updateWorkspace(Workspace pWorkspace) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspace);
        new WorkspaceDAO(new Locale(account.getLanguage()), em).updateWorkspace(pWorkspace);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    public Workspace updateWorkspace(String workspaceId, String description, boolean isFolderLocked) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());
        Locale locale = new Locale(account.getLanguage());
        WorkspaceDAO workspaceDAO = new WorkspaceDAO(locale, em);
        Workspace workspace = workspaceDAO.loadWorkspace(workspaceId);
        checkAdmin(workspace, account);
        workspace.setDescription(description);
        workspace.setFolderLocked(isFolderLocked);

        return workspace;
    }


    @Override
    public void recoverPassword(String pPasswdRRUuid, String pPassword) throws PasswordRecoveryRequestNotFoundException {
        PasswordRecoveryRequestDAO passwdRRequestDAO = new PasswordRecoveryRequestDAO(em);
        PasswordRecoveryRequest passwdRR = passwdRRequestDAO.loadPasswordRecoveryRequest(pPasswdRRUuid);
        AccountDAO accountDAO = new AccountDAO(em);
        accountDAO.updateCredential(passwdRR.getLogin(), pPassword);
        passwdRRequestDAO.removePasswordRecoveryRequest(passwdRR);
    }

    @Override
    public PasswordRecoveryRequest createPasswordRecoveryRequest(Account account) {
        PasswordRecoveryRequest passwdRR = PasswordRecoveryRequest.createPasswordRecoveryRequest(account.getLogin());
        em.persist(passwdRR);
        mailer.sendPasswordRecovery(account, passwdRR.getUuid());
        return passwdRR;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public User checkWorkspaceReadAccess(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        String login = contextManager.getCallerPrincipalLogin();
        UserDAO userDAO = new UserDAO(em);
        WorkspaceUserMembership userMS = userDAO.loadUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
        Workspace wks = new WorkspaceDAO(em).loadWorkspace(pWorkspaceId);
        User user = userDAO.loadUser(new UserKey(pWorkspaceId, login));
        Locale locale = new Locale(user.getLanguage());

        if (!wks.isEnabled()) {
            throw new WorkspaceNotEnabledException(locale, pWorkspaceId);
        } else if (userMS != null) {
            user = userMS.getMember();
        } else if (!wks.getAdmin().getLogin().equals(login)) {
            WorkspaceUserGroupMembership[] groupMS = new UserGroupDAO(em).getUserGroupMemberships(pWorkspaceId, user);
            if (groupMS.length == 0) {
                throw new UserNotActiveException(locale, login);
            }
        }

        workspaceAccessEvent.select(new AnnotationLiteral<Read>() {
        }).fire(new WorkspaceAccessEvent(user));

        return user;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public User checkWorkspaceWriteAccess(String pWorkspaceId) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        String login = contextManager.getCallerPrincipalLogin();

        UserDAO userDAO = new UserDAO(em);
        User user = userDAO.loadUser(new UserKey(pWorkspaceId, login));
        if (!hasWorkspaceWriteAccess(user, pWorkspaceId)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        workspaceAccessEvent.select(new AnnotationLiteral<Write>() {
        }).fire(new WorkspaceAccessEvent(user));

        return user;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public boolean hasWorkspaceWriteAccess(User user, String pWorkspaceId) throws WorkspaceNotFoundException, WorkspaceNotEnabledException {
        String login = contextManager.getCallerPrincipalLogin();

        UserDAO userDAO = new UserDAO(em);

        Workspace wks = new WorkspaceDAO(em).loadWorkspace(pWorkspaceId);
        if (!wks.isEnabled()) {
            throw new WorkspaceNotEnabledException(new Locale(user.getLanguage()), pWorkspaceId);
        }
        if (!wks.getAdmin().getLogin().equals(login)) {
            WorkspaceUserMembership userMS = userDAO.loadUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
            if (userMS != null) {
                if (userMS.isReadOnly()) {
                    return false;
                }
            } else {
                WorkspaceUserGroupMembership[] groupMS = new UserGroupDAO(em).getUserGroupMemberships(pWorkspaceId, user);
                boolean readOnly = true;
                for (WorkspaceUserGroupMembership ms : groupMS) {
                    if (!ms.isReadOnly()) {
                        readOnly = false;
                        break;
                    }
                }
                if (readOnly) {
                    return false;
                }
            }
        }
        return true;
    }


    /*
    * Don't expose this method on remote.
    * Method returns true if given users have a common workspace, false otherwise.
    */
    @Override
    public boolean hasCommonWorkspace(String userLogin1, String userLogin2) {
        return userLogin1 != null && userLogin2 != null && new UserDAO(em).hasCommonWorkspace(userLogin1, userLogin2);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public UserGroup[] getUserGroupsForUser(UserKey userKey) throws UserNotFoundException {
        User user = new UserDAO(em).loadUser(userKey);
        List<UserGroup> userGroups = new UserGroupDAO(em).getUserGroups(userKey.getWorkspace(), user);
        return userGroups.toArray(new UserGroup[userGroups.size()]);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Workspace[] getWorkspacesWhereCallerIsActive() {
        String callerLogin = contextManager.getCallerPrincipalLogin();
        List<Workspace> workspaces = new WorkspaceDAO(em).findWorkspacesWhereUserIsActive(callerLogin);
        return workspaces.stream().filter(Workspace::isEnabled).toArray(Workspace[]::new);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Account checkAdmin(Workspace pWorkspace) throws AccessRightException, AccountNotFoundException {
        Account account = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());
        checkAdmin(pWorkspace, account);
        return account;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Account checkAdmin(String pWorkspaceId) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());
        Workspace wks = new WorkspaceDAO(new Locale(account.getLanguage()), em).loadWorkspace(pWorkspaceId);
        checkAdmin(wks, account);
        return account;
    }

    private void checkAdmin(Workspace workspace, Account account) throws AccessRightException {
        if (!contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID) && !workspace.getAdmin().equals(account)) {
            throw new AccessRightException(new Locale(account.getLanguage()), account);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public User whoAmI(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        return checkWorkspaceReadAccess(pWorkspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public User[] getReachableUsers() throws AccountNotFoundException {
        String callerLogin = contextManager.getCallerPrincipalLogin();
        Account account = new AccountDAO(em).loadAccount(callerLogin);
        return new UserDAO(new Locale(account.getLanguage()), em).findReachableUsersForCaller(callerLogin);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public User[] getUsers(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        if (contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            Account account = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());
            return new UserDAO(new Locale(account.getLanguage()), em).findAllUsers(pWorkspaceId);
        }
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new UserDAO(new Locale(user.getLanguage()), em).findAllUsers(pWorkspaceId);
    }
}
