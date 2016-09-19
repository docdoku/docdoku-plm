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
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.UserGroupKey;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.UserGroupAlreadyExistsException;
import com.docdoku.core.exceptions.UserGroupNotFoundException;
import com.docdoku.core.security.WorkspaceUserGroupMembership;
import com.docdoku.core.security.WorkspaceUserGroupMembershipKey;

import javax.persistence.*;
import java.util.List;
import java.util.Locale;

public class UserGroupDAO {

    private EntityManager em;
    private Locale mLocale;

    public UserGroupDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public UserGroupDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public UserGroup loadUserGroup(UserGroupKey pKey) throws UserGroupNotFoundException {
        UserGroup group = em.find(UserGroup.class, pKey);
        if (group == null) {
            throw new UserGroupNotFoundException(mLocale, pKey);
        } else {
            return group;
        }
    }

    public WorkspaceUserGroupMembership[] getUserGroupMemberships(String pWorkspaceId, User pUser) {
        WorkspaceUserGroupMembership[] ms;
        TypedQuery<WorkspaceUserGroupMembership> query = em.createQuery("SELECT DISTINCT m FROM WorkspaceUserGroupMembership m WHERE m.workspaceId = :workspaceId AND :user MEMBER OF m.member.users", WorkspaceUserGroupMembership.class);
        query.setParameter("workspaceId", pWorkspaceId);
        query.setParameter("user", pUser);
        List<WorkspaceUserGroupMembership> listUserGroupMemberships = query.getResultList();
        ms = new WorkspaceUserGroupMembership[listUserGroupMemberships.size()];
        for (int i = 0; i < listUserGroupMemberships.size(); i++) {
            ms[i] = listUserGroupMemberships.get(i);
        }
        return ms;
    }

    public UserGroup[] findAllUserGroups(String pWorkspaceId) {
        UserGroup[] groups;
        TypedQuery<UserGroup> query = em.createQuery("SELECT DISTINCT g FROM UserGroup g WHERE g.workspaceId = :workspaceId", UserGroup.class);
        List<UserGroup> listUserGroups = query.setParameter("workspaceId", pWorkspaceId).getResultList();
        groups = new UserGroup[listUserGroups.size()];
        for (int i = 0; i < listUserGroups.size(); i++) {
            groups[i] = listUserGroups.get(i);
        }
        return groups;
    }

    public WorkspaceUserGroupMembership loadUserGroupMembership(WorkspaceUserGroupMembershipKey pKey) throws UserGroupNotFoundException {
        WorkspaceUserGroupMembership workspaceUserGroupMembership = em.find(WorkspaceUserGroupMembership.class, pKey);
        if (workspaceUserGroupMembership == null) {
            throw new UserGroupNotFoundException(mLocale,new UserGroupKey(pKey.getWorkspaceId(),pKey.getMemberId()));
        } else {
            return workspaceUserGroupMembership;
        }
    }

    public void addUserGroupMembership(Workspace pWorkspace, UserGroup pMember) {
        WorkspaceUserGroupMembership ms = em.find(WorkspaceUserGroupMembership.class, new WorkspaceUserGroupMembershipKey(pWorkspace.getId(), pWorkspace.getId(), pMember.getId()));
        if (ms == null) {
            ms = new WorkspaceUserGroupMembership(pWorkspace, pMember);
            em.persist(ms);
        }
    }

    public void removeUserGroupMembership(WorkspaceUserGroupMembershipKey pKey) {
        WorkspaceUserGroupMembership ms = em.find(WorkspaceUserGroupMembership.class, pKey);
        if (ms != null) {
            em.remove(ms);
        }
    }

    public void removeUserFromAllGroups(User pUser) {
        TypedQuery<UserGroup> query = em.createQuery("SELECT DISTINCT g FROM UserGroup g WHERE g.workspaceId = :workspaceId", UserGroup.class);
        List<UserGroup> listUserGroups = query.setParameter("workspaceId", pUser.getWorkspaceId()).getResultList();
        for (UserGroup listUserGroup : listUserGroups) {
            listUserGroup.removeUser(pUser);
        }
    }

    public WorkspaceUserGroupMembership[] findAllWorkspaceUserGroupMemberships(String pWorkspaceId) {
        WorkspaceUserGroupMembership[] memberships;
        TypedQuery<WorkspaceUserGroupMembership> query = em.createQuery("SELECT DISTINCT m FROM WorkspaceUserGroupMembership m WHERE m.workspaceId = :workspaceId", WorkspaceUserGroupMembership.class);
        List<WorkspaceUserGroupMembership> listMemberships = query.setParameter("workspaceId", pWorkspaceId).getResultList();
        memberships = new WorkspaceUserGroupMembership[listMemberships.size()];
        for (int i = 0; i < listMemberships.size(); i++) {
            memberships[i] = listMemberships.get(i);
        }

        return memberships;
    }

    public void removeUserGroup(UserGroup pUserGroup) throws UserGroupNotFoundException {
        removeUserGroupMembership(new WorkspaceUserGroupMembershipKey(pUserGroup.getWorkspaceId(), pUserGroup.getWorkspaceId(), pUserGroup.getId()));
        em.remove(pUserGroup);
    }

    public boolean hasACLConstraint(UserGroupKey pKey){
        Query query = em.createQuery("SELECT DISTINCT a FROM ACLUserGroupEntry a WHERE a.principal.id = :id AND a.principal.workspaceId = :workspaceId");
        query.setParameter("id",pKey.getId());
        query.setParameter("workspaceId",pKey.getWorkspaceId());
        return !query.getResultList().isEmpty();
    }
    
    public void createUserGroup(UserGroup pUserGroup) throws CreationException, UserGroupAlreadyExistsException {
        try {
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pUserGroup);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            throw new UserGroupAlreadyExistsException(mLocale, pUserGroup);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public List<UserGroup> getUserGroups(String workspaceId, User user) {
        return em.createNamedQuery("UserGroup.findUserGroups", UserGroup.class).
                setParameter("workspaceId", workspaceId).
                setParameter("user", user).
                getResultList();
    }
}
