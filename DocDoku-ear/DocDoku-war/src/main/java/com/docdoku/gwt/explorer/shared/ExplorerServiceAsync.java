/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

package com.docdoku.gwt.explorer.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Date;


/**
 *
 * @author Florent Garin
 */
public interface ExplorerServiceAsync {
    void getFolders(String parentFolder, AsyncCallback<String[]> callback);
    void getTags(String workspaceId, AsyncCallback<String[]> callback);
    void getWorkflowModels(String workspaceId, AsyncCallback<WorkflowModelDTO[]> callback);
    void getDocMTemplates(String workspaceId, AsyncCallback<DocumentMasterTemplateDTO[]> callback);
    void getCheckedOutDocMs(String workspaceId, AsyncCallback<DocumentMasterDTO[]> callback);
    void findDocMsByFolder(String completePath, AsyncCallback<DocumentMasterDTO[]> callback);
    void findDocMsByTag(String workspaceId, String label, AsyncCallback<DocumentMasterDTO[]> callback);
    void delFolder(String completePath, AsyncCallback<DocumentMasterDTO[]> callback);
    void createFolder(String pParentFolder, String pFolder, AsyncCallback<Void> callback);
    void removeFileFromDocument(String pFullName, AsyncCallback<DocumentMasterDTO> callback);
    void removeFilesFromDocument(String[] pFullNames, AsyncCallback<DocumentMasterDTO> callback);
    void getDocM(String workspaceId, String id, String version, AsyncCallback<DocumentMasterDTO> callback);
    void checkIn(String workspaceId, String id, String version, AsyncCallback<DocumentMasterDTO> callback);
    void checkOut(String workspaceId, String id, String version, AsyncCallback<DocumentMasterDTO> callback);
    void undoCheckOut(String workspaceId, String id, String version, AsyncCallback<DocumentMasterDTO> callback);
    void delDocM(String workspaceId, String id, String version, AsyncCallback<Void> callback);
    void whoAmI(String pWorkspaceId, AsyncCallback<UserDTO> callback);
    void createDocM(String parentFolder, String docMId, String title, String description, String docMTemplateId, String workflowModelId, ACLDTO acl, AsyncCallback<DocumentMasterDTO> callback);
    void createVersion(String workspaceId, String id, String version, String title, String description, String workflowModelId, ACLDTO acl, AsyncCallback<DocumentMasterDTO[]> callback);
    void updateDoc(String workspaceId, String id, String version, int iteration, String revisionNote, InstanceAttributeDTO[] attributes, DocumentDTO[] links, AsyncCallback<DocumentMasterDTO> callback);
    void createDocMTemplate(String workspaceId, String id, String documentType, String mask, InstanceAttributeTemplateDTO[] attributeTemplates, boolean idGenerated, AsyncCallback<DocumentMasterTemplateDTO> callback);
    void delDocMTemplate(String workspaceId, String id, AsyncCallback<Void> callback);
    void updateDocMTemplate(String workspaceId, String id, String documentType, String mask, InstanceAttributeTemplateDTO[] attributeTemplates,  boolean idGenerated, AsyncCallback<DocumentMasterTemplateDTO> callback);
    void delTag(String workspaceId, String label,AsyncCallback<Void> callback);
    void createTag(String workspaceId, String label, AsyncCallback<Void> callback);
    void generateId(String workspaceId, String docMTemplateId, AsyncCallback<String> callback);
    void saveTags(String workspaceId, String id, String version, String[] tags, AsyncCallback<DocumentMasterDTO> callback);
    void delWorkflowModel(String workspaceId, String id, AsyncCallback<Void> callback);
    void getUsers(String pWorkspaceId, AsyncCallback<UserDTO[]> callback);
    void createWorkflowModel(String workspaceId, String id, String finalLifeCycleState, ActivityModelDTO[] activityModels, AsyncCallback<WorkflowModelDTO> callback);
    void getIterationChangeEventSubscriptions(String workspaceId, AsyncCallback<DocumentMasterDTO[]> callback);
    void getStateChangeEventSubscriptions(String workspaceId, AsyncCallback<DocumentMasterDTO[]> callback);
    void subscribeToIterationChangeEvent(String workspaceId, String id, String version,AsyncCallback<Void> callback);
    void subscribeToStateChangeEvent(String workspaceId, String id, String version,AsyncCallback<Void> callback);
    void unsubscribeToIterationChangeEvent(String workspaceId, String id, String version,AsyncCallback<Void> callback);
    void unsubscribeToStateChangeEvent(String workspaceId, String id, String version,AsyncCallback<Void> callback);

    void removeFileFromTemplate(String pFullName, AsyncCallback<DocumentMasterTemplateDTO> callback);
    void removeFilesFromTemplate(String[] pFullNames, AsyncCallback<DocumentMasterTemplateDTO> callback);
    void getDocMTemplate(String workspaceId, String id, AsyncCallback<DocumentMasterTemplateDTO> callback);

    void approve(String workspaceId, int workflowId, int activityStep, int num, String comment, AsyncCallback<DocumentMasterDTO> callback);
    void reject(String workspaceId, int workflowId, int activityStep, int num, String comment, AsyncCallback<DocumentMasterDTO> callback);
    void searchDocMs(String workspaceId, String docMId, String title, String version, String author, String type, Date creationDateFrom, Date creationDateTo, SearchQueryDTO.AbstractAttributeQueryDTO[] attributes, String[] tags, String content, AsyncCallback<DocumentMasterDTO[]> callback);
    void moveDocM(String parentFolder, String workspaceId, String id, String version, AsyncCallback<DocumentMasterDTO> callback);
    void getWorkspaceUserMemberships(String workspaceId, AsyncCallback<UserDTO[]> callback);
    void getWorkspaceUserGroupMemberships(String workspaceId, AsyncCallback<UserGroupDTO[]> callback);


    // services using ExplorerServiceResponse
    void getCheckedOutDocMs(String workspaceId, int startOffset, int chunkSize, AsyncCallback<DocMResponse> callback);
    void findDocMsByFolder(String completePath, int startOffset, int chunkSize,AsyncCallback<DocMResponse> callback);
    void findDocMsByTag(String workspaceId, String label, int startOffset, int chunkSize,AsyncCallback<DocMResponse> callback);
    void searchDocMs(String workspaceId, String docMId, String title, String version, String author, String type, Date creationDateFrom, Date creationDateTo, SearchQueryDTO.AbstractAttributeQueryDTO[] attributes, String[] tags, String content, int startOffset, int chunkSize,AsyncCallback<DocMResponse> callback);

    void getDocMTemplates(String workspaceId, int startPoint, int chunkSize, AsyncCallback<DocMTemplateResponse> callback);
    void getWorkflowModels(String workspaceId, int startPoint, int chunkSize, AsyncCallback<WorkflowResponse> callback);
}
