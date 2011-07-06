/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */
package com.docdoku.core.services;

import com.docdoku.core.document.SearchQuery;
import com.docdoku.core.document.MasterDocumentTemplate;
import com.docdoku.core.document.InstanceAttributeTemplate;
import com.docdoku.core.document.MasterDocumentKey;
import com.docdoku.core.document.TagKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.document.DocumentKey;
import com.docdoku.core.document.MasterDocument;
import com.docdoku.core.workflow.TaskKey;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.MasterDocumentTemplateKey;
import com.docdoku.core.workflow.ActivityModel;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.WorkflowModelKey;
import javax.jws.WebService;

/**
 *
 * @author Florent Garin
 */
@WebService
public interface ICommandWS {

    MasterDocument approve(String pWorkspaceId, TaskKey pTaskKey, String pComment) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException;

    MasterDocument checkIn(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException;

    MasterDocument checkOut(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, FileAlreadyExistsException, UserNotFoundException, CreationException;

    Folder createFolder(String pParentFolder, String pFolder) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, FolderAlreadyExistsException, UserNotFoundException, CreationException;

    MasterDocument createMDoc(String pParentFolder, String pMDocID, String pTitle, String pDescription, String pMDocTemplateId, String pWorkflowModelId, ACLUserEntry[] aclUserEntries, ACLUserGroupEntry[] aclUserGroupEntries) throws WorkspaceNotFoundException, WorkflowModelNotFoundException, NotAllowedException, MasterDocumentTemplateNotFoundException, AccessRightException, MasterDocumentAlreadyExistsException, FolderNotFoundException, FileAlreadyExistsException, UserNotFoundException, CreationException;

    MasterDocumentTemplate createMDocTemplate(String pWorkspaceId, String pId, String pDocumentType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated) throws WorkspaceNotFoundException, AccessRightException, MasterDocumentTemplateAlreadyExistsException, UserNotFoundException, NotAllowedException, CreationException;

    MasterDocument[] createVersion(MasterDocumentKey pOriginalMDocPK, String pTitle, String pDescription, String pWorkflowModelId, ACLUserEntry[] aclUserEntries, ACLUserGroupEntry[] aclUserGroupEntries) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, WorkflowModelNotFoundException, AccessRightException, MasterDocumentAlreadyExistsException, FileAlreadyExistsException, UserNotFoundException, CreationException;

    MasterDocumentKey[] delFolder(String pCompletePath) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, FolderNotFoundException;

    MasterDocumentKey[] moveFolder(String pCompletePath, String pDestParentFolder, String pDestFolder)throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, FolderNotFoundException, CreationException, FolderAlreadyExistsException;
    void delMDoc(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException;

    void delMDocTemplate(MasterDocumentTemplateKey pKey) throws WorkspaceNotFoundException, WorkspaceNotFoundException, AccessRightException, MasterDocumentTemplateNotFoundException, UserNotFoundException;

    void delWorkflowModel(WorkflowModelKey pKey) throws WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, UserNotFoundException;

    void delTag(TagKey pKey) throws WorkspaceNotFoundException, AccessRightException, TagNotFoundException, UserNotFoundException;

    void createTag(String pWorkspaceId, String pLabel) throws WorkspaceNotFoundException, AccessRightException, CreationException, TagAlreadyExistsException, UserNotFoundException;

    MasterDocument[] findMDocsByFolder(String pCompletePath) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    MasterDocument[] findMDocsByTag(TagKey pKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    MasterDocument[] getCheckedOutMDocs(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    String[] getFolders(String pCompletePath) throws WorkspaceNotFoundException, FolderNotFoundException, UserNotFoundException, UserNotActiveException;

    MasterDocumentKey[] getIterationChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    MasterDocument getMDoc(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException;

    MasterDocumentTemplate getMDocTemplate(MasterDocumentTemplateKey pKey) throws WorkspaceNotFoundException, MasterDocumentTemplateNotFoundException, UserNotFoundException, UserNotActiveException;

    MasterDocumentTemplate[] getMDocTemplates(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    MasterDocumentKey[] getStateChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    String[] getTags(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    Task[] getTasks(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    User[] getUsers(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    WorkflowModel getWorkflowModel(WorkflowModelKey pKey) throws WorkspaceNotFoundException, WorkflowModelNotFoundException, UserNotFoundException, UserNotActiveException;

    WorkflowModel[] getWorkflowModels(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    Workspace getWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    MasterDocument moveMDoc(String pParentFolder, MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, UserNotFoundException, UserNotActiveException;

    MasterDocument reject(String pWorkspaceId, TaskKey pTaskKey, String pComment) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException;

    MasterDocument removeFileFromDocument(String pFullName) throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException;

    MasterDocumentTemplate removeFileFromTemplate(String pFullName) throws WorkspaceNotFoundException, MasterDocumentTemplateNotFoundException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException;

    User savePersonalInfo(String pWorkspaceId, String pName, String pEmail, String pLanguage) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    MasterDocument saveTags(MasterDocumentKey pMDocPK, String[] pTags) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException;

    WorkflowModel createWorkflowModel(String pWorkspaceId, String pId, String pFinalLifeCycleState, ActivityModel[] pActivityModels) throws WorkspaceNotFoundException, AccessRightException, WorkflowModelAlreadyExistsException, UserNotFoundException, CreationException;

    String generateId(String pWorkspaceId, String pMDocTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, MasterDocumentTemplateNotFoundException;

    MasterDocument[] searchMDocs(SearchQuery pQuery) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    void subscribeToIterationChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, UserNotFoundException, UserNotActiveException;

    void subscribeToStateChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, UserNotFoundException, UserNotActiveException;

    MasterDocument undoCheckOut(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException;

    void unsubscribeToIterationChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    void unsubscribeToStateChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    MasterDocument updateDoc(DocumentKey pKey, String pRevisionNote, InstanceAttribute[] pAttributes, DocumentKey[] pLinkKeys) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException;

    MasterDocumentTemplate updateMDocTemplate(MasterDocumentTemplateKey pKey, String pDocumentType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated) throws WorkspaceNotFoundException, AccessRightException, MasterDocumentTemplateNotFoundException, UserNotFoundException;

    User whoAmI(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;
}
