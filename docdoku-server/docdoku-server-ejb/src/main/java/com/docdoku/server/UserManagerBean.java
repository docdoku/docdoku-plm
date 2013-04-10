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

import com.docdoku.core.services.UserGroupNotFoundException;
import com.docdoku.core.services.WorkspaceAlreadyExistsException;
import com.docdoku.core.services.AccountNotFoundException;
import com.docdoku.core.services.UserAlreadyExistsException;
import com.docdoku.core.services.AccountAlreadyExistsException;
import com.docdoku.core.services.UserGroupAlreadyExistsException;
import com.docdoku.core.services.WorkspaceNotFoundException;
import com.docdoku.core.services.UserNotFoundException;
import com.docdoku.core.services.UserNotActiveException;
import com.docdoku.core.services.NotAllowedException;
import com.docdoku.core.services.FolderNotFoundException;
import com.docdoku.core.services.FolderAlreadyExistsException;
import com.docdoku.core.services.AccessRightException;
import com.docdoku.core.services.CreationException;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.security.WorkspaceUserGroupMembershipKey;
import com.docdoku.core.security.WorkspaceUserMembershipKey;
import com.docdoku.core.security.WorkspaceUserMembership;
import com.docdoku.core.security.WorkspaceUserGroupMembership;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.common.UserKey;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Account;
import com.docdoku.core.common.UserGroupKey;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.security.PasswordRecoveryRequest;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.PasswordRecoveryRequestNotFoundException;
import com.docdoku.server.dao.*;
import com.docdoku.server.vault.DataManager;
import com.docdoku.server.vault.filesystem.DataManagerImpl;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.annotation.security.DeclareRoles;

@DeclareRoles({"users","admin"})
@Local(IUserManagerLocal.class)
@Stateless(name = "UserManagerBean")
public class UserManagerBean implements IUserManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @Resource
    private SessionContext ctx;
    @Resource(name = "vaultPath")
    private String vaultPath;
    @EJB
    private IMailerLocal mailer;
    @EJB
    private IndexerBean indexer;
    private DataManager dataManager;
    private final static Logger LOGGER = Logger.getLogger(UserManagerBean.class.getName());

    @PostConstruct
    private void init() {
        dataManager = new DataManagerImpl(new File(vaultPath));
    }

    @Override
    public Account createAccount(String pLogin, String pName, String pEmail, String pLanguage, String pPassword) throws AccountAlreadyExistsException, CreationException {
        Date now = new Date();
        Account account = new Account(pLogin, pName, pEmail, pLanguage, now);
        new AccountDAO(new Locale(pLanguage), em).createAccount(account, pPassword);
        return account;
    }

    @RolesAllowed("users")
    @Override
    public void addUserInGroup(UserGroupKey pGroupKey, String pLogin) throws AccessRightException, UserGroupNotFoundException, UserGroupNotFoundException, AccountNotFoundException, WorkspaceNotFoundException, UserAlreadyExistsException, FolderAlreadyExistsException, CreationException {
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

    @RolesAllowed("users")
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

    @RolesAllowed("users")
    @Override
    public void removeUserFromGroup(UserGroupKey pGroupKey, String[] pLogins) throws AccessRightException, UserGroupNotFoundException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pGroupKey.getWorkspaceId());
        UserGroup group = new UserGroupDAO(new Locale(account.getLanguage()), em).loadUserGroup(pGroupKey);
        for (String login : pLogins) {
            User userToRemove = em.getReference(User.class, new UserKey(pGroupKey.getWorkspaceId(), login));
            group.removeUser(userToRemove);
        }
    }

    @RolesAllowed("users")
    @Override
    public UserGroup createUserGroup(String pId, Workspace pWorkspace) throws UserGroupAlreadyExistsException, AccessRightException, AccountNotFoundException, CreationException {
        Account account = checkAdmin(pWorkspace);
        UserGroup groupToCreate = new UserGroup(pWorkspace, pId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        groupDAO.createUserGroup(groupToCreate);
        groupDAO.addUserGroupMembership(pWorkspace, groupToCreate);
        return groupToCreate;
    }

    @RolesAllowed("users")
    @Override
    public Workspace createWorkspace(String pID, Account pAdmin, String pDescription, Workspace.VaultType pVaultType, boolean pFolderLocked) throws WorkspaceAlreadyExistsException, FolderAlreadyExistsException, UserAlreadyExistsException, CreationException {
        Workspace workspace = new Workspace(pID, pAdmin, pDescription, pVaultType, pFolderLocked);
        new WorkspaceDAO(em).createWorkspace(workspace);
        User userToCreate = new User(workspace, pAdmin.getLogin(), pAdmin.getName(), pAdmin.getEmail(), pAdmin.getLanguage());
        UserDAO userDAO = new UserDAO(new Locale(pAdmin.getLanguage()), em);
        userDAO.createUser(userToCreate);
        userDAO.addUserMembership(workspace, userToCreate);
        return workspace;
    }

    @Override
    public Account getAccount(String pLogin) throws AccountNotFoundException {
        return new AccountDAO(em).loadAccount(pLogin);
    }

    @RolesAllowed({"users","admin"})
    @Override
    public Workspace[] getAdministratedWorkspaces() throws AccountNotFoundException {
        if(ctx.isCallerInRole("admin")){
            return new AccountDAO(em).getAllWorkspaces();
        }else{
            Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
            return new AccountDAO(em).getAdministratedWorkspaces(account);
        }
    }

    @RolesAllowed({"users","admin"})
    @Override
    public Workspace[] getWorkspaces() {

        if(ctx.isCallerInRole("admin")){
            return new Workspace[0];
        }

        User[] users = new UserDAO(em).getUsers(ctx.getCallerPrincipal().toString());
        Workspace[] workspaces = new Workspace[users.length];
        for (int i = 0; i < users.length; i++) {
            workspaces[i] = users[i].getWorkspace();
        }

        return workspaces;
    }

    @RolesAllowed({"users","admin"})
    @Override
    public Workspace getWorkspace(String workspaceId) {
        User[] users = new UserDAO(em).getUsers(ctx.getCallerPrincipal().toString());
        Workspace workspace=null;
        for (int i = 0; i < users.length; i++) {
            if(users[i].getWorkspace().getId().equals(workspaceId)){
                workspace=users[i].getWorkspace();
                break;
            }
        }

        return workspace;
    }

    @RolesAllowed({"users","admin"})
    @Override
    public UserGroup[] getUserGroups(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new UserGroupDAO(new Locale(user.getLanguage()), em).findAllUserGroups(pWorkspaceId);
    }

    @RolesAllowed({"users","admin"})
    @Override
    public UserGroup getUserGroup(UserGroupKey pKey) throws WorkspaceNotFoundException, UserGroupNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pKey.getWorkspaceId());
        return new UserGroupDAO(new Locale(user.getLanguage()), em).loadUserGroup(pKey);
    }

    @RolesAllowed({"users","admin"})
    @Override
    public WorkspaceUserMembership[] getWorkspaceUserMemberships(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new UserDAO(new Locale(user.getLanguage()), em).findAllWorkspaceUserMemberships(pWorkspaceId);
    }

    @RolesAllowed({"users","admin"})
    @Override
    public WorkspaceUserGroupMembership[] getWorkspaceUserGroupMemberships(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new UserGroupDAO(new Locale(user.getLanguage()), em).findAllWorkspaceUserGroupMemberships(pWorkspaceId);
    }

    @RolesAllowed("users")
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

    @RolesAllowed("users")
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

    @RolesAllowed("users")
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

    @RolesAllowed("users")
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

    @RolesAllowed("users")
    @Override
    public void passivateUserGroups(String pWorkspaceId, String[] pGroupIds) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        for (String id : pGroupIds) {
            groupDAO.removeUserGroupMembership(new WorkspaceUserGroupMembershipKey(pWorkspaceId, pWorkspaceId, id));
        }
    }

    @RolesAllowed("users")
    @Override
    public void passivateUsers(String pWorkspaceId, String[] pLogins) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        for (String login : pLogins) {
            userDAO.removeUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
        }
    }

    @RolesAllowed("users")
    @Override
    public void removeUsers(String pWorkspaceId, String[] pLogins) throws UserNotFoundException, NotAllowedException, AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, FolderNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        for (String login : pLogins) {
            DocumentMaster[] docMs = userDAO.removeUser(new UserKey(pWorkspaceId, login));
            for (DocumentMaster docM : docMs) {
                for (DocumentIteration doc : docM.getDocumentIterations()) {
                    for (BinaryResource file : doc.getAttachedFiles()) {
                        indexer.removeFromIndex(file.getFullName());
                        dataManager.delData(file);
                    }
                }
            }
        }
    }

    @RolesAllowed("users")
    @Override
    public void removeUserGroups(String pWorkspaceId, String[] pIds) throws UserGroupNotFoundException, AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        for (String id : pIds) {
            groupDAO.removeUserGroup(new UserGroupKey(pWorkspaceId, id));
        }
    }

    @RolesAllowed("users")
    @Override
    public void updateWorkspace(Workspace pWorkspace) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspace.getId());
        new WorkspaceDAO(new Locale(account.getLanguage()), em).updateWorkspace(pWorkspace);
    }

    @RolesAllowed("users")
    @Override
    public void updateAccount(String pName, String pEmail, String pLanguage, String pPassword) throws AccountNotFoundException {
        AccountDAO accountDAO = new AccountDAO(new Locale(pLanguage), em);
        Account account = accountDAO.loadAccount(ctx.getCallerPrincipal().toString());
        account.setName(pName);
        account.setEmail(pEmail);
        account.setLanguage(pLanguage);
        if (pPassword != null) {
            accountDAO.updateCredential(account.getLogin(), pPassword);
        }
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

    @RolesAllowed({"users","admin"})
    @Override
    public User checkWorkspaceReadAccess(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        String login = ctx.getCallerPrincipal().toString();

        if(ctx.isCallerInRole("admin")){
            return new UserDAO(em).loadAdmin(login);
        }

        UserDAO userDAO = new UserDAO(em);
        WorkspaceUserMembership userMS = userDAO.loadUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
        if (userMS != null) {
            return userMS.getMember();
        }
        Workspace wks = new WorkspaceDAO(em).loadWorkspace(pWorkspaceId);
        User user = userDAO.loadUser(new UserKey(pWorkspaceId, login));
        if (wks.getAdmin().getLogin().equals(login)) {
            return user;
        }
        WorkspaceUserGroupMembership[] groupMS = new UserGroupDAO(em).getUserGroupMemberships(pWorkspaceId, user);
        if (groupMS.length > 0) {
            return user;
        } else {
            throw new UserNotActiveException(Locale.getDefault(), login);
        }
    }

    @RolesAllowed({"users","admin"})
    @Override
    public User checkWorkspaceWriteAccess(String pWorkspaceId) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException {
        String login = ctx.getCallerPrincipal().toString();

        if(ctx.isCallerInRole("admin")){
            return new UserDAO(em).loadAdmin(login);
        }

        UserDAO userDAO = new UserDAO(em);

        Workspace wks = new WorkspaceDAO(em).loadWorkspace(pWorkspaceId);
        User user = userDAO.loadUser(new UserKey(pWorkspaceId, login));
        if (wks.getAdmin().getLogin().equals(login)) {
            return user;
        }

        WorkspaceUserMembership userMS = userDAO.loadUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
        if (userMS != null) {
            if (userMS.isReadOnly()) {
                throw new AccessRightException(new Locale(user.getLanguage()), user);
            } else {
                return userMS.getMember();
            }
        }

        WorkspaceUserGroupMembership[] groupMS = new UserGroupDAO(em).getUserGroupMemberships(pWorkspaceId, user);
        for (WorkspaceUserGroupMembership ms : groupMS) {
            if (!ms.isReadOnly()) {
                return user;
            }
        }
        throw new AccessRightException(new Locale(user.getLanguage()), user);
    }



    /*
    * Don't expose this method on remote.
    * Method returns true if given users have a common workspace, false otherwise.
    */
    @Override
    public boolean hasCommonWorkspace(String userLogin1, String userLogin2) {

        if( userLogin1 != null && userLogin2 != null){
            return ! em.createNamedQuery("findCommonWorkspacesForGivenUsers").
                    setParameter("userLogin1", userLogin1).
                    setParameter("userLogin2", userLogin2).
                    getResultList().
                    isEmpty();
        }

        return false;
    }

    @Override
    public boolean isCallerInRole(String role) {
        return ctx.isCallerInRole(role);
    }

    private Account checkAdmin(Workspace pWorkspace) throws AccessRightException, AccountNotFoundException {
        Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
        if (!pWorkspace.getAdmin().equals(account)) {
            throw new AccessRightException(new Locale(account.getLanguage()), account);
        }
        return account;
    }

    private Account checkAdmin(String pWorkspaceId) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
        Workspace wks = new WorkspaceDAO(new Locale(account.getLanguage()), em).loadWorkspace(pWorkspaceId);
        if (!wks.getAdmin().equals(account)) {
            throw new AccessRightException(new Locale(account.getLanguage()), account);
        }
        return account;
    }
}
