/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

package com.docdoku.core;

import com.docdoku.core.entities.*;
import com.docdoku.core.entities.keys.*;
import java.util.Date;
import java.util.Map;
import javax.jws.WebService;

/**
 *
 * @author Florent GARIN
 */
@WebService
public interface ICommandWS {

    
    public MasterDocument approve(String pWorkspaceId, TaskKey pTaskKey, String pComment) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException;
    
    public MasterDocument checkIn(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException;
    
    public MasterDocument checkOut(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException;
    
    public void createFolder(String pParentFolder, String pFolder) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, FolderAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException;
    
    public MasterDocument createMDoc(String pParentFolder, String pMDocID, String pTitle, String pDescription, String pMDocTemplateId, String pWorkflowModelId, ACLUserEntry[] aclUserEntries, ACLUserGroupEntry[] aclUserGroupEntries) throws WorkspaceNotFoundException, WorkflowModelNotFoundException, NotAllowedException, MasterDocumentTemplateNotFoundException, AccessRightException, MasterDocumentAlreadyExistsException, FolderNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException;
    
    public MasterDocumentTemplate createMDocTemplate(String pWorkspaceId, String pId, String pDocumentType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated) throws WorkspaceNotFoundException, AccessRightException, MasterDocumentTemplateAlreadyExistsException, UserNotFoundException, UserNotActiveException, NotAllowedException, CreationException;
    
    public MasterDocument[] createVersion(MasterDocumentKey pOriginalMDocPK, String pTitle, String pDescription, String pWorkflowModelId, ACLUserEntry[] aclUserEntries, ACLUserGroupEntry[] aclUserGroupEntries) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, WorkflowModelNotFoundException, AccessRightException, MasterDocumentAlreadyExistsException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException;
    
    public MasterDocumentKey[] delFolder(String pCompletePath) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, UserNotActiveException, FolderNotFoundException;
    
    public void delMDoc(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException;
    
    public void delMDocTemplate(BasicElementKey pKey) throws WorkspaceNotFoundException, WorkspaceNotFoundException, AccessRightException, MasterDocumentTemplateNotFoundException, UserNotFoundException, UserNotActiveException;

    public void delWorkflowModel(BasicElementKey pKey) throws WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, UserNotFoundException, UserNotActiveException;
    
    public void delTag(TagKey pKey) throws WorkspaceNotFoundException, AccessRightException, TagNotFoundException, UserNotFoundException, UserNotActiveException;
    
    public void createTag(String pWorkspaceId, String pLabel) throws WorkspaceNotFoundException, AccessRightException, CreationException, TagAlreadyExistsException, UserNotFoundException, UserNotActiveException;

    public MasterDocument[] findMDocsByFolder(String pCompletePath) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    public MasterDocument[] findMDocsByTag(TagKey pKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    public MasterDocument[] getCheckedOutMDocs(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;
    
    public String[] getFolders(String pCompletePath) throws WorkspaceNotFoundException, FolderNotFoundException, UserNotFoundException, UserNotActiveException;

    public MasterDocumentKey[] getIterationChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    public MasterDocument getMDoc(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException;

    public MasterDocumentTemplate getMDocTemplate(BasicElementKey pKey) throws WorkspaceNotFoundException, MasterDocumentTemplateNotFoundException, UserNotFoundException, UserNotActiveException;

    public MasterDocumentTemplate[] getMDocTemplates(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    public MasterDocumentKey[] getStateChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    public String[] getTags(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    public Task[] getTasks(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    public User[] getUsers(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    public WorkflowModel getWorkflowModel(BasicElementKey pKey) throws WorkspaceNotFoundException, WorkflowModelNotFoundException, UserNotFoundException, UserNotActiveException;

    public WorkflowModel[] getWorkflowModels(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    public Workspace getWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    public MasterDocument moveMDoc(String pParentFolder, MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, UserNotFoundException, UserNotActiveException;

    public MasterDocument reject(String pWorkspaceId, TaskKey pTaskKey, String pComment) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException;

    public MasterDocument removeFileFromDocument(String pFullName) throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException;

    public MasterDocumentTemplate removeFileFromTemplate(String pFullName) throws WorkspaceNotFoundException, MasterDocumentTemplateNotFoundException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException;

    public User savePersonalInfo(String pWorkspaceId, String pName, String pEmail, String pLanguage) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    public MasterDocument saveTags(MasterDocumentKey pMDocPK, String[] pTags) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException;

    public WorkflowModel createWorkflowModel(String pWorkspaceId, String pId, String pFinalLifeCycleState, ActivityModel[] pActivityModels) throws WorkspaceNotFoundException, AccessRightException, WorkflowModelAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException;
    
    public String generateId(String pWorkspaceId, String pMDocTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, MasterDocumentTemplateNotFoundException;
    
    public MasterDocument[] searchMDocs(SearchQuery pQuery) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    public void subscribeToIterationChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, UserNotFoundException, UserNotActiveException;

    public void subscribeToStateChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, UserNotFoundException, UserNotActiveException;

    public MasterDocument undoCheckOut(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException;

    public void unsubscribeToIterationChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    public void unsubscribeToStateChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;
    
    public MasterDocument updateDoc(DocumentKey pKey, String pRevisionNote, InstanceAttribute[] pAttributes, DocumentKey[] pLinkKeys) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException;

    public MasterDocumentTemplate updateMDocTemplate(BasicElementKey pKey, String pDocumentType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates,  boolean idGenerated) throws WorkspaceNotFoundException, AccessRightException, MasterDocumentTemplateNotFoundException, UserNotFoundException, UserNotActiveException;

    public User whoAmI(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;
    
}
