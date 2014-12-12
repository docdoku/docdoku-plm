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

package com.docdoku.arquillian.tests.services;

import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.UserGroupKey;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.services.IWorkspaceManagerLocal;
import com.docdoku.server.esindexer.ESIndexer;
import com.sun.enterprise.security.ee.auth.login.ProgrammaticLogin;


import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * @author Taylor LABEJOF
 */

@LocalBean
@Stateless
public class TestUserManagerBean {
    @EJB
    private IUserManagerLocal userManagerLocal;

    @EJB
    private IWorkspaceManagerLocal workspaceManagerLocal;

    @EJB
    private ESIndexer esIndexer;

    private ProgrammaticLogin loginP = new ProgrammaticLogin();
    private String password = "password";

    public Workspace testWorkspaceCreation(String login, String pWorkspace) throws AccountNotFoundException, UserAlreadyExistsException, CreationException, WorkspaceAlreadyExistsException, FolderAlreadyExistsException, ESIndexNamingException, NotAllowedException {
        loginP.login(login, password.toCharArray());
        try{
            esIndexer.deleteWorkspace(pWorkspace);
        }catch (Exception ignored){}
        Workspace workspace = userManagerLocal.createWorkspace(pWorkspace, userManagerLocal.getAccount(login), "", false);
        loginP.logout();
        return workspace;
    }

    public UserGroup testGroupCreation(String login, String workspaceId, String groupId) throws WorkspaceNotFoundException, AccessRightException, CreationException, UserGroupAlreadyExistsException, AccountNotFoundException {
        loginP.login(login, password.toCharArray());
        UserGroup userGroup = userManagerLocal.createUserGroup(groupId, userManagerLocal.getWorkspace(workspaceId));
        loginP.logout();
        return userGroup;
    }

    public void testAddingUserInWorkspace(String admin, String userToAdd, String workspace) throws UserAlreadyExistsException, WorkspaceNotFoundException, AccessRightException, CreationException, FolderAlreadyExistsException, AccountNotFoundException {
        loginP.login(admin, password.toCharArray());
        userManagerLocal.addUserInWorkspace(workspace, userToAdd);
        loginP.logout();
    }

    public void testGrantingUserAccessInWorkspace(String admin, String[] usersToGrant, String workspace, Boolean readOnly) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException {
        loginP.login(admin, password.toCharArray());
        userManagerLocal.grantUserAccess(workspace, usersToGrant, readOnly);
        loginP.logout();
    }

    public void testGrantingUserGroupAccessInWorkspace(String admin, String[] groupsToGrant, String workspace, Boolean readOnly) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException {
        loginP.login(admin, password.toCharArray());
        userManagerLocal.grantGroupAccess(workspace, groupsToGrant, readOnly);
        loginP.logout();
    }

    public void testPassivatingUserGroup(String login, String workspace, String[] groupsId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException {
        loginP.login(login, password.toCharArray());
        userManagerLocal.passivateUserGroups(workspace, groupsId);
    }

    public void testAddingUserInGroup(String login, String groupId, String groupWorkspace, String userToAdd) throws UserAlreadyExistsException, WorkspaceNotFoundException, AccessRightException, CreationException, FolderAlreadyExistsException, AccountNotFoundException, UserGroupNotFoundException {
        loginP.login(login, password.toCharArray());
        userManagerLocal.addUserInGroup(new UserGroupKey(groupWorkspace, groupId), userToAdd);
        loginP.logout();
    }
}