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

package com.docdoku.arquillian.tests;

import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.UserGroupKey;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.services.*;
import com.docdoku.core.exceptions.*;
import com.sun.appserv.security.ProgrammaticLogin;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author: Asmae CHADID
 */


@LocalBean
@Stateless
public class TestEJBBean {

    @EJB
    private IDocumentManagerLocal documentManagerLocal;

    @EJB
    private IUserManagerLocal userManagerLocal;

    @PersistenceContext
    private EntityManager em;

    private com.sun.appserv.security.ProgrammaticLogin loginP = new ProgrammaticLogin();
    private String password = "password";


    public Workspace testWorkspaceCreation(String login, String pWorkspace) throws AccountNotFoundException, UserAlreadyExistsException, CreationException, WorkspaceAlreadyExistsException, FolderAlreadyExistsException {
        loginP.login(login, password.toCharArray());
        Workspace workspace = userManagerLocal.createWorkspace(pWorkspace, userManagerLocal.getAccount(login), "", Workspace.VaultType.LARGE, false);
        loginP.logout();
        return workspace;
    }

    public Folder testFolderCreation(String login, String pWorkspace, String pFolder) throws UserNotActiveException, WorkspaceNotFoundException, AccessRightException, CreationException, FolderNotFoundException, UserNotFoundException, FolderAlreadyExistsException, NotAllowedException {
        loginP.login(login, password.toCharArray());
        Folder folder = documentManagerLocal.createFolder(pWorkspace, pFolder);
        loginP.logout();
        return folder;
    }

    public DocumentMaster testDocumentCreation(String login, String path, String documentId, ACLUserEntry[] pACLUserEntries, ACLUserGroupEntry[] pACLUserGroupEntries) throws WorkspaceNotFoundException, RoleNotFoundException, AccessRightException, CreationException, FolderNotFoundException, UserNotFoundException, NotAllowedException, DocumentMasterAlreadyExistsException, DocumentMasterTemplateNotFoundException, WorkflowModelNotFoundException, FileAlreadyExistsException {
        loginP.login(login, password.toCharArray());
        DocumentMaster documentMaster = documentManagerLocal.createDocumentMaster(path, documentId, "", "", null, null, pACLUserEntries, pACLUserGroupEntries, null);
        loginP.logout();
        return documentMaster;
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

    public void testDocumentCheckIn(String login, DocumentMasterKey documentMasterKey) throws UserNotActiveException, WorkspaceNotFoundException, AccessRightException, UserNotFoundException, NotAllowedException, DocumentMasterNotFoundException {
        loginP.login(login, password.toCharArray());
        documentManagerLocal.checkInDocument(documentMasterKey);
        loginP.logout();
    }

    public void testDocumentCheckOut(String login, DocumentMasterKey documentMasterKey) throws UserNotActiveException, WorkspaceNotFoundException, AccessRightException, UserNotFoundException, NotAllowedException, DocumentMasterNotFoundException, CreationException, FileAlreadyExistsException {
        loginP.login(login, password.toCharArray());
        documentManagerLocal.checkOutDocument(documentMasterKey);
        loginP.logout();
    }
}