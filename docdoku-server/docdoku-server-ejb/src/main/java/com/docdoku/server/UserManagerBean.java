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

import com.docdoku.core.common.*;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.Folder;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.*;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.services.IUserManagerWS;
import com.docdoku.core.util.NamingConvention;
import com.docdoku.server.dao.*;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.events.Read;
import com.docdoku.server.events.WorkspaceAccessEvent;
import com.docdoku.server.events.Write;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Locale;

@DeclareRoles({UserGroupMapping.GUEST_PROXY_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@Local(IUserManagerLocal.class)
@Stateless(name = "UserManagerBean")
@WebService(endpointInterface = "com.docdoku.core.services.IUserManagerWS")
public class UserManagerBean implements IUserManagerLocal, IUserManagerWS {

    @PersistenceContext
    private EntityManager em;
    @Resource
    private SessionContext ctx;
    @EJB
    private ESIndexer esIndexer;
    @EJB
    private IDataManagerLocal dataManager;
    @EJB
    private IDocumentManagerLocal documentService;

    @Inject
    private Event<WorkspaceAccessEvent> workspaceAccessEvent;

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void addUserInGroup(UserGroupKey pGroupKey, String pLogin) throws AccessRightException, UserGroupNotFoundException, AccountNotFoundException, WorkspaceNotFoundException, UserAlreadyExistsException, FolderAlreadyExistsException, CreationException {
        Account account = checkAdmin(pGroupKey.getWorkspaceId());
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        User userToAdd = em.find(User.class, new UserKey(pGroupKey.getWorkspaceId(), pLogin));
        if (userToAdd == null) {
            Account userAccount = new AccountDAO(em).loadAccount(pLogin);
            Workspace workspace = em.getReference(Workspace.class, pGroupKey.getWorkspaceId());
            userToAdd = new User(workspace, userAccount.getLogin(), userAccount.getName(), userAccount.getEmail(), userAccount.getLanguage());
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
            userToAdd = new User(workspace, userAccount.getLogin(), userAccount.getName(), userAccount.getEmail(), userAccount.getLanguage());
            userDAO.createUser(userToAdd);
        }
        userDAO.addUserMembership(workspace, userToAdd);
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
    public Workspace createWorkspace(String pID, Account pAdmin, String pDescription, boolean pFolderLocked) throws WorkspaceAlreadyExistsException, FolderAlreadyExistsException, UserAlreadyExistsException, CreationException, ESIndexNamingException, NotAllowedException {
        if (!NamingConvention.correct(pID)) {
            throw new NotAllowedException(new Locale(pAdmin.getLanguage()), "NotAllowedException9");
        }
        Workspace workspace = new Workspace(pID, pAdmin, pDescription, pFolderLocked);
        new WorkspaceDAO(em).createWorkspace(workspace);
        User userToCreate = new User(workspace, pAdmin.getLogin(), pAdmin.getName(), pAdmin.getEmail(), pAdmin.getLanguage());
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
        if (isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            return new AccountDAO(em).getAllWorkspaces();
        } else {
            Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
            return new AccountDAO(em).getAdministratedWorkspaces(account);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Workspace getWorkspace(String workspaceId) throws WorkspaceNotFoundException, AccountNotFoundException {

        if (isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            return new WorkspaceDAO(em).loadWorkspace(workspaceId);
        }

        User[] users = new UserDAO(em).getUsers(ctx.getCallerPrincipal().toString());
        Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
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
    public UserGroup[] getUserGroups(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccountNotFoundException {
        if (isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
            return new UserGroupDAO(new Locale(account.getLanguage()), em).findAllUserGroups(pWorkspaceId);
        } else {
            User user = checkWorkspaceReadAccess(pWorkspaceId);
            return new UserGroupDAO(new Locale(user.getLanguage()), em).findAllUserGroups(pWorkspaceId);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public UserGroup getUserGroup(UserGroupKey pKey) throws WorkspaceNotFoundException, UserGroupNotFoundException, UserNotFoundException, UserNotActiveException, AccountNotFoundException {
        if (isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
            return new UserGroupDAO(new Locale(account.getLanguage()), em).loadUserGroup(pKey);
        } else {
            User user = checkWorkspaceReadAccess(pKey.getWorkspaceId());
            return new UserGroupDAO(new Locale(user.getLanguage()), em).loadUserGroup(pKey);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public WorkspaceUserMembership getWorkspaceSpecificUserMemberships(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new UserDAO(new Locale(user.getLanguage()), em).loadUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, user.getLogin()));
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public WorkspaceUserMembership[] getWorkspaceUserMemberships(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccountNotFoundException {
        if (isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
            return new UserDAO(new Locale(account.getLanguage()), em).findAllWorkspaceUserMemberships(pWorkspaceId);
        } else {
            User user = checkWorkspaceReadAccess(pWorkspaceId);
            return new UserDAO(new Locale(user.getLanguage()), em).findAllWorkspaceUserMemberships(pWorkspaceId);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public WorkspaceUserGroupMembership[] getWorkspaceSpecificUserGroupMemberships(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
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
    public WorkspaceUserGroupMembership[] getWorkspaceUserGroupMemberships(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccountNotFoundException {
        if (isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
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
    public void grantGroupAccess(String pWorkspaceId, String[] pGroupIds, boolean pReadOnly) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
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
    public void removeUsers(String pWorkspaceId, String[] pLogins) throws UserNotFoundException, NotAllowedException, AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, FolderNotFoundException, ESServerException, EntityConstraintException, UserNotActiveException, DocumentRevisionNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        Locale locale = new Locale(account.getLanguage());
        UserDAO userDAO = new UserDAO(locale, em);

        for (String login : pLogins) {
            FolderDAO folderDAO = new FolderDAO(locale, em);
            User user = userDAO.loadUser(new UserKey(pWorkspaceId, login));
            String folderCompletePath = user.getWorkspaceId() + "/~" + user.getLogin();
            Folder folder = folderDAO.loadFolder(folderCompletePath);

            List<DocumentRevision> allDocRevision = folderDAO.findDocumentRevisionsInFolder(folder);
            for (DocumentRevision documentRevision : allDocRevision) {
                documentService.deleteDocumentRevision(documentRevision.getKey());
            }

            folderDAO.removeFolder(folderCompletePath);
            userDAO.removeUser(user);
        }
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
            groupDAO.removeUserGroup(userGroupKey);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void updateWorkspace(Workspace pWorkspace) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspace.getId());
        new WorkspaceDAO(new Locale(account.getLanguage()), em).updateWorkspace(pWorkspace);
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
    public PasswordRecoveryRequest createPasswordRecoveryRequest(String login) {
        PasswordRecoveryRequest passwdRR = PasswordRecoveryRequest.createPasswordRecoveryRequest(login);
        em.persist(passwdRR);
        return passwdRR;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public User checkWorkspaceReadAccess(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        String login = ctx.getCallerPrincipal().toString();
        User user;
        UserDAO userDAO = new UserDAO(em);
        WorkspaceUserMembership userMS = userDAO.loadUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
        if (userMS != null) {
            user = userMS.getMember();
        } else {
            Workspace wks = new WorkspaceDAO(em).loadWorkspace(pWorkspaceId);
            user = userDAO.loadUser(new UserKey(pWorkspaceId, login));
            if (!wks.getAdmin().getLogin().equals(login)) {
                WorkspaceUserGroupMembership[] groupMS = new UserGroupDAO(em).getUserGroupMemberships(pWorkspaceId, user);
                if (groupMS.length == 0) {
                    throw new UserNotActiveException(new Locale(user.getLanguage()), login);
                }
            }
        }
        workspaceAccessEvent.select(new AnnotationLiteral<Read>() {
        }).fire(new WorkspaceAccessEvent(user));

        return user;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public User checkWorkspaceWriteAccess(String pWorkspaceId) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException {
        String login = ctx.getCallerPrincipal().toString();

        UserDAO userDAO = new UserDAO(em);

        Workspace wks = new WorkspaceDAO(em).loadWorkspace(pWorkspaceId);
        User user = userDAO.loadUser(new UserKey(pWorkspaceId, login));
        if (!wks.getAdmin().getLogin().equals(login)) {
            WorkspaceUserMembership userMS = userDAO.loadUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
            if (userMS != null) {
                if (userMS.isReadOnly()) {
                    throw new AccessRightException(new Locale(user.getLanguage()), user);
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
                if (readOnly)
                    throw new AccessRightException(new Locale(user.getLanguage()), user);
            }
        }
        workspaceAccessEvent.select(new AnnotationLiteral<Write>() {
        }).fire(new WorkspaceAccessEvent(user));

        return user;
    }


    /*
    * Don't expose this method on remote.
    * Method returns true if given users have a common workspace, false otherwise.
    */
    @Override
    public boolean hasCommonWorkspace(String userLogin1, String userLogin2) {
        return userLogin1 != null && userLogin2 != null && new UserDAO(em).hasCommonWorkspace(userLogin1, userLogin2);
    }

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

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public UserGroup[] getUserGroupsForUser(UserKey userKey) throws UserNotFoundException {
        User user = new UserDAO(em).loadUser(userKey);
        List<UserGroup> userGroups = new UserGroupDAO(em).getUserGroups(userKey.getWorkspaceId(), user);
        return userGroups.toArray(new UserGroup[userGroups.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Workspace[] getWorkspacesWhereCallerIsActive() {
        String callerLogin = ctx.getCallerPrincipal().toString();
        List<Workspace> workspaces = new WorkspaceDAO(em).findWorkspacesWhereUserIsActive(callerLogin);
        return workspaces.toArray(new Workspace[workspaces.size()]);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Account checkAdmin(Workspace pWorkspace) throws AccessRightException, AccountNotFoundException {
        Account account = new AccountDAO(em).loadAccount(getCallerPrincipalLogin());
        if (!isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID) && !pWorkspace.getAdmin().equals(account)) {
            throw new AccessRightException(new Locale(account.getLanguage()), account);
        }
        return account;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Account checkAdmin(String pWorkspaceId) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = new AccountDAO(em).loadAccount(getCallerPrincipalLogin());

        if (isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            return account;
        }

        Workspace wks = new WorkspaceDAO(new Locale(account.getLanguage()), em).loadWorkspace(pWorkspaceId);
        if (!wks.getAdmin().equals(account)) {
            throw new AccessRightException(new Locale(account.getLanguage()), account);
        }

        return account;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public User whoAmI(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        return checkWorkspaceReadAccess(pWorkspaceId);
    }
}
