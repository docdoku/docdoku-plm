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
package com.docdoku.core.services;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.*;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.sharing.SharedEntityKey;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.TaskKey;

import javax.jws.WebService;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author Florent Garin
 */
@WebService
public interface IDocumentManagerWS {


    DocumentMaster approveTaskOnDocument(String workspaceId, TaskKey taskKey, String comment, String signature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, WorkflowNotFoundException;

    DocumentMaster checkInDocument(DocumentMasterKey docMPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException;

    DocumentMaster checkOutDocument(DocumentMasterKey docMPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, AccessRightException, FileAlreadyExistsException, UserNotFoundException, CreationException, UserNotActiveException;

    Folder createFolder(String parentFolder, String folder) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, FolderAlreadyExistsException, UserNotFoundException, CreationException;


    DocumentMasterTemplate createDocumentMasterTemplate(String workspaceId, String id, String documentType, String mask, InstanceAttributeTemplate[] attributeTemplates, boolean idGenerated) throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateAlreadyExistsException, UserNotFoundException, NotAllowedException, CreationException;

    DocumentMaster[] createDocumentVersion(DocumentMasterKey pOriginalDocMPK, String pTitle, String pDescription, String pWorkflowModelId, ACLUserEntry[] aclUserEntries, ACLUserGroupEntry[] aclUserGroupEntries, Map<String, String> roleMappings) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, WorkflowModelNotFoundException, AccessRightException, DocumentMasterAlreadyExistsException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException, RoleNotFoundException;

    DocumentMasterKey[] deleteFolder(String completePath) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, FolderNotFoundException;

    DocumentMasterKey[] moveFolder(String completePath, String destParentFolder, String destFolder)throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, FolderNotFoundException, CreationException, FolderAlreadyExistsException;
    
    void deleteDocumentMaster(DocumentMasterKey docMPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException;

    void deleteDocumentMasterTemplate(DocumentMasterTemplateKey key) throws WorkspaceNotFoundException, WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateNotFoundException, UserNotFoundException;

    void deleteTag(TagKey key) throws WorkspaceNotFoundException, AccessRightException, TagNotFoundException, UserNotFoundException;

    void createTag(String workspaceId, String label) throws WorkspaceNotFoundException, AccessRightException, CreationException, TagAlreadyExistsException, UserNotFoundException;

    DocumentMaster[] findDocumentMastersByFolder(String completePath) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentMaster[] findDocumentMastersByTag(TagKey key) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentMaster[] getCheckedOutDocumentMasters(String workspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    String[] getFolders(String completePath) throws WorkspaceNotFoundException, FolderNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentMasterKey[] getIterationChangeEventSubscriptions(String workspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentMaster getDocumentMaster(DocumentMasterKey docMPK) throws WorkspaceNotFoundException, DocumentMasterNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, AccessRightException;

    DocumentMasterTemplate getDocumentMasterTemplate(DocumentMasterTemplateKey key) throws WorkspaceNotFoundException, DocumentMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentMasterTemplate[] getDocumentMasterTemplates(String workspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentMasterKey[] getStateChangeEventSubscriptions(String workspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    String[] getTags(String workspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    Task[] getTasks(String workspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    User[] getUsers(String workspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    User[] getReachableUsers() throws AccountNotFoundException;

    Workspace getWorkspace(String workspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentMaster moveDocumentMaster(String parentFolder, DocumentMasterKey docMPK) throws WorkspaceNotFoundException, DocumentMasterNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentMaster rejectTaskOnDocument(String workspaceId, TaskKey taskKey, String comment, String signature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, WorkflowNotFoundException;

    DocumentMaster removeFileFromDocument(String fullName) throws WorkspaceNotFoundException, DocumentMasterNotFoundException, NotAllowedException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentMasterTemplate removeFileFromTemplate(String fullName) throws WorkspaceNotFoundException, DocumentMasterTemplateNotFoundException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException;

    User savePersonalInfo(String workspaceId, String name, String email, String language) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    DocumentMaster saveTags(DocumentMasterKey docMPK, String[] tags) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException;

    String generateId(String workspaceId, String docMTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, DocumentMasterTemplateNotFoundException;

    DocumentMaster[] searchDocumentMasters(DocumentSearchQuery query) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    void subscribeToIterationChangeEvent(DocumentMasterKey docMPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException;

    void subscribeToStateChangeEvent(DocumentMasterKey docMPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException;

    DocumentMaster undoCheckOutDocument(DocumentMasterKey docMPK) throws WorkspaceNotFoundException, DocumentMasterNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, AccessRightException;

    void unsubscribeToIterationChangeEvent(DocumentMasterKey docMPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, DocumentMasterNotFoundException;

    void unsubscribeToStateChangeEvent(DocumentMasterKey docMPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, DocumentMasterNotFoundException;

    DocumentMaster updateDocument(DocumentIterationKey key, String revisionNote, InstanceAttribute[] attributes, DocumentIterationKey[] linkKeys) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException;

    DocumentMasterTemplate updateDocumentMasterTemplate(DocumentMasterTemplateKey key, String documentType, String mask, InstanceAttributeTemplate[] attributeTemplates, boolean idGenerated) throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateNotFoundException, UserNotFoundException;

    User whoAmI(String workspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    boolean isUserStateChangeEventSubscribedForGivenDocument(String workspaceId, DocumentMaster docM) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException ;

    boolean isUserIterationChangeEventSubscribedForGivenDocument(String workspaceId, DocumentMaster docM) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException ;

    DocumentMaster[] getDocumentMastersWithAssignedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentMaster[] getDocumentMastersWithReference(String pWorkspaceId, String reference, int maxResults) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    int getDocumentsCountInWorkspace(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    Long getDiskUsageForDocumentsInWorkspace(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    Long getDiskUsageForDocumentTemplatesInWorkspace(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    DocumentMaster createDocumentMaster(String pParentFolder, String pDocMId, String pTitle, String pDescription, String pDocMTemplateId, String pWorkflowModelId, ACLUserEntry[] pACLUserEntries, ACLUserGroupEntry[] pACLUserGroupEntries, Map<String,String> roleMappings) throws WorkspaceNotFoundException, WorkflowModelNotFoundException, NotAllowedException, DocumentMasterTemplateNotFoundException, AccessRightException, DocumentMasterAlreadyExistsException, FolderNotFoundException, FileAlreadyExistsException, UserNotFoundException, CreationException, RoleNotFoundException;

    DocumentMaster[] getAllCheckedOutDocumentMasters(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    SharedDocument createSharedDocument(DocumentMasterKey pDocMPK, String pPassword, Date pExpireDate) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, DocumentMasterNotFoundException, UserNotActiveException, NotAllowedException;

    void deleteSharedDocument(SharedEntityKey sharedEntityKey) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, SharedEntityNotFoundException;

    DocumentIteration findDocumentIterationByBinaryResource(BinaryResource pBinaryResource) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    void updateDocumentACL(String pWorkspaceId, DocumentMasterKey docKey, Map<String,String> userEntries, Map<String,String> userGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentMasterNotFoundException, AccessRightException;

    void removeACLFromDocumentMaster(DocumentMasterKey documentMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentMasterNotFoundException, AccessRightException;


}
