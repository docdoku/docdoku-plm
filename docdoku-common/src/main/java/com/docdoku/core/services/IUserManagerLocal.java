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

package com.docdoku.core.services;

import com.docdoku.core.common.*;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.PasswordRecoveryRequest;
import com.docdoku.core.security.WorkspaceUserGroupMembership;
import com.docdoku.core.security.WorkspaceUserMembership;


/**
 *
 * @author Florent Garin
 */
public interface IUserManagerLocal{

    void recoverPassword(String pPasswdRRUuid, String pPassword) throws PasswordRecoveryRequestNotFoundException;
    PasswordRecoveryRequest createPasswordRecoveryRequest(String login);

    Workspace getWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, AccountNotFoundException;
    Workspace[] getWorkspacesWhereCallerIsActive();
    Workspace createWorkspace(String pID, Account pAdmin, String pDescription, boolean pFolderLocked) throws FolderAlreadyExistsException, UserAlreadyExistsException, WorkspaceAlreadyExistsException, CreationException, ESIndexNamingException, NotAllowedException;
    void updateWorkspace(Workspace pWorkspace) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException;

    User whoAmI(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;
    void addUserInWorkspace(String pWorkspaceId, String pLogin) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, UserAlreadyExistsException, FolderAlreadyExistsException, CreationException;
    void removeUsers(String pWorkspaceId, String[] pLogins) throws UserNotFoundException, NotAllowedException, AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, FolderNotFoundException, ESServerException, EntityConstraintException, UserNotActiveException, DocumentRevisionNotFoundException;
    Workspace[] getAdministratedWorkspaces() throws AccountNotFoundException;

    User[] getUsersWithCheckAdmin(String pWorkspaceId) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException;
    UserGroup[] getUserGroupsWithCheckAdmin(String pWorkspaceId) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException;
    WorkspaceUserMembership[] getWorkspaceUserMembershipsWithCheckAdmin(String pWorkspaceId) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException;
    WorkspaceUserGroupMembership[] getWorkspaceUserGroupMembershipsWithCheckAdmin(String pWorkspaceId) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException;
    UserGroup getUserGroupWithCheckAdmin(UserGroupKey pKey) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, UserGroupNotFoundException;

    UserGroup[] getUserGroups(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccountNotFoundException;
    UserGroup createUserGroup(String pId, Workspace pWorkspace) throws UserGroupAlreadyExistsException, AccessRightException, AccountNotFoundException, CreationException;
    void removeUserGroups(String pWorkspaceId, String[] pIds) throws UserGroupNotFoundException, AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, EntityConstraintException;
    void addUserInGroup(UserGroupKey pGroupKey, String pLogin) throws AccessRightException, UserGroupNotFoundException, AccountNotFoundException, WorkspaceNotFoundException, UserAlreadyExistsException, FolderAlreadyExistsException, CreationException;
    void removeUserFromGroup(UserGroupKey pGroupKey, String[] pLogins) throws AccessRightException, UserGroupNotFoundException, AccountNotFoundException, WorkspaceNotFoundException;
    boolean hasCommonWorkspace(String user1, String user2);

    void grantUserAccess(String pWorkspaceId, String[] pLogins, boolean pReadOnly) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException;
    void grantGroupAccess(String pWorkspaceId, String[] pGroupIds, boolean pReadOnly) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException;

    void activateUsers(String pWorkspaceId, String[] pLogins) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException;
    void passivateUsers(String pWorkspaceId, String[] pLogins) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException;

    void activateUserGroups(String pWorkspaceId, String[] pGroupIds) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException;
    void passivateUserGroups(String pWorkspaceId, String[] pGroupIds) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException;

    WorkspaceUserMembership getWorkspaceSpecificUserMemberships(String workspaceId) throws AccountNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    WorkspaceUserMembership[] getWorkspaceUserMemberships(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccountNotFoundException;
    WorkspaceUserGroupMembership[] getWorkspaceSpecificUserGroupMemberships(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    WorkspaceUserGroupMembership[] getWorkspaceUserGroupMemberships(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccountNotFoundException;

    Account checkAdmin(String pWorkspaceId) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException;
    Account checkAdmin(Workspace pWorkspace) throws AccessRightException, AccountNotFoundException;
    User checkWorkspaceReadAccess(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    User checkWorkspaceWriteAccess(String pWorkspaceId) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException;

    boolean isCallerInRole(String role);
    String getCallerPrincipalLogin();
    String getCallerPrincipalName();

    UserGroup[] getUserGroupsForUser(UserKey userKey) throws UserNotFoundException;
}