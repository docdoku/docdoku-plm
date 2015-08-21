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
package com.docdoku.server.dao;

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserKey;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.Folder;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.WorkspaceUserMembership;
import com.docdoku.core.security.WorkspaceUserMembershipKey;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class UserDAO {

    private EntityManager em;
    private Locale mLocale;

    public UserDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public UserDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public User loadUser(UserKey pUserKey) throws UserNotFoundException {
        User user = em.find(User.class, pUserKey);
        if (user == null) {
            throw new UserNotFoundException(mLocale, pUserKey.getLogin());
        } else {
            return user;
        }
    }

    public WorkspaceUserMembership loadUserMembership(WorkspaceUserMembershipKey pKey) {
        return em.find(WorkspaceUserMembership.class, pKey);
    }

    public void addUserMembership(Workspace pWorkspace, User pMember) {
        WorkspaceUserMembership ms = em.find(WorkspaceUserMembership.class, new WorkspaceUserMembershipKey(pWorkspace.getId(), pWorkspace.getId(), pMember.getLogin()));
        if (ms == null) {
            ms = new WorkspaceUserMembership(pWorkspace, pMember);
            em.persist(ms);
        }
    }

    public void removeUserMembership(WorkspaceUserMembershipKey pKey) {
        WorkspaceUserMembership ms = em.find(WorkspaceUserMembership.class, pKey);
        if (ms != null) {
            em.remove(ms);
        }
    }

    public void updateUser(User pUser) {
        em.merge(pUser);
    }

    public User[] findAllUsers(String pWorkspaceId) {
        User[] users;
        Query query = em.createQuery("SELECT DISTINCT u FROM User u WHERE u.workspaceId = :workspaceId");
        List listUsers = query.setParameter("workspaceId", pWorkspaceId).getResultList();
        users = new User[listUsers.size()];
        for (int i = 0; i < listUsers.size(); i++) {
            users[i] = (User) listUsers.get(i);
        }
        return users;
    }

    public WorkspaceUserMembership[] findAllWorkspaceUserMemberships(String pWorkspaceId) {
        WorkspaceUserMembership[] memberships;
        Query query = em.createQuery("SELECT DISTINCT m FROM WorkspaceUserMembership m WHERE m.workspaceId = :workspaceId");
        List listMemberships = query.setParameter("workspaceId", pWorkspaceId).getResultList();
        memberships = new WorkspaceUserMembership[listMemberships.size()];
        for (int i = 0; i < listMemberships.size(); i++) {
            memberships[i] = (WorkspaceUserMembership) listMemberships.get(i);
        }

        return memberships;
    }

    public void removeUser(User pUser) throws UserNotFoundException, NotAllowedException, FolderNotFoundException, EntityConstraintException {
        removeUserMembership(new WorkspaceUserMembershipKey(pUser.getWorkspaceId(), pUser.getWorkspaceId(), pUser.getLogin()));
        new SubscriptionDAO(em).removeAllSubscriptions(pUser);
        new UserGroupDAO(mLocale, em).removeUserFromAllGroups(pUser);
        new RoleDAO(mLocale,em).removeUserFromRoles(pUser);
        new ACLDAO(em).removeAclUserEntries(pUser);

        boolean author = isDocMAuthor(pUser) || isDocAuthor(pUser) || isDocMTemplateAuthor(pUser) || isWorkflowModelAuthor(pUser);
        boolean involved = isInvolvedInWF(pUser);

        if (author || involved) {
            throw new NotAllowedException(mLocale, "NotAllowedException8");
        } else {
            em.remove(pUser);
        }
    }

    public boolean isInvolvedInWF(User pUser) {
        Query query = em.createQuery("SELECT DISTINCT u FROM User u WHERE EXISTS (SELECT t FROM Task t WHERE t.worker = u AND t.worker = :user)");
        List listUsers = query.setParameter("user", pUser).getResultList();
        return !listUsers.isEmpty();
    }

    public boolean isDocMAuthor(User pUser) {
        Query query = em.createQuery("SELECT DISTINCT u FROM User u WHERE EXISTS (SELECT m FROM DocumentMaster m WHERE m.author = u AND m.author = :user)");
        List listUsers = query.setParameter("user", pUser).getResultList();
        return !listUsers.isEmpty();
    }

    public boolean isDocAuthor(User pUser) {
        Query query = em.createQuery("SELECT DISTINCT u FROM User u WHERE EXISTS (SELECT d FROM DocumentIteration d WHERE d.author = u AND d.author = :user)");
        List listUsers = query.setParameter("user", pUser).getResultList();
        return !listUsers.isEmpty();
    }

    public boolean isDocMTemplateAuthor(User pUser) {
        Query query = em.createQuery("SELECT DISTINCT u FROM User u WHERE EXISTS (SELECT t FROM DocumentMasterTemplate t WHERE t.author = u AND t.author = :user)");
        List listUsers = query.setParameter("user", pUser).getResultList();
        return !listUsers.isEmpty();
    }

    public boolean isWorkflowModelAuthor(User pUser) {
        Query query = em.createQuery("SELECT DISTINCT u FROM User u WHERE EXISTS (SELECT w FROM WorkflowModel w WHERE w.author = u AND w.author = :user)");
        List listUsers = query.setParameter("user", pUser).getResultList();
        return !listUsers.isEmpty();
    }

    public void createUser(User pUser) throws UserAlreadyExistsException, FolderAlreadyExistsException, CreationException {
        try {
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pUser);
            em.flush();
            new FolderDAO(mLocale, em).createFolder(new Folder(pUser.getWorkspaceId() + "/~" + pUser.getLogin()));
        } catch (EntityExistsException pEEEx) {
            throw new UserAlreadyExistsException(mLocale, pUser);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public User[] getUsers(String pLogin) {
        User[] users;
        Query query = em.createQuery("SELECT DISTINCT u FROM User u WHERE u.login = :login");
        List listUsers = query.setParameter("login", pLogin).getResultList();
        users = new User[listUsers.size()];
        for (int i = 0; i < listUsers.size(); i++) {
            users[i] = (User) listUsers.get(i);
        }

        return users;
    }

    public User[] findReachableUsersForCaller(String callerLogin, String workspaceId) {

        Map<String,User> users = new TreeMap<>();

        List<String> listWorkspaceId = em.createQuery("SELECT u.workspaceId FROM User u WHERE u.login = :login")
                .setParameter("login", callerLogin).getResultList();

        List<User> listUsers = em.createQuery("SELECT u FROM User u where u.workspaceId IN :workspacesId")
                .setParameter("workspacesId", listWorkspaceId).getResultList();


        for (User listUser : listUsers) {
            String loginUser = listUser.getLogin();
            if (!users.keySet().contains(loginUser)) {
                users.put(loginUser,listUser);
            } else if(workspaceId.equals(listUser.getWorkspaceId())){
                users.remove(loginUser);
                users.put(loginUser,listUser);
            }
        }

        return users.values().toArray(new User[users.size()]);

    }

    public boolean hasCommonWorkspace(String userLogin1, String userLogin2){
        return ! em.createNamedQuery("findCommonWorkspacesForGivenUsers").
                setParameter("userLogin1", userLogin1).
                setParameter("userLogin2", userLogin2).
                getResultList().
                isEmpty();
    }

}
