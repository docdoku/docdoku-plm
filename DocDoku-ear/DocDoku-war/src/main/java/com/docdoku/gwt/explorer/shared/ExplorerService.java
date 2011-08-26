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

package com.docdoku.gwt.explorer.shared;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.Date;

/**
 *
 * @author Florent Garin
 */

@RemoteServiceRelativePath("service")
public interface ExplorerService extends RemoteService{
    String[] getFolders(String completePath) throws ApplicationException;
    String[] getTags(String workspaceId) throws ApplicationException;
    WorkflowModelDTO[] getWorkflowModels(String workspaceId) throws ApplicationException;
    MasterDocumentTemplateDTO[] getMDocTemplates(String workspaceId) throws ApplicationException;
    MasterDocumentDTO[] getCheckedOutMDocs(String workspaceId) throws ApplicationException;
    MasterDocumentDTO[] findMDocsByFolder(String completePath) throws ApplicationException;
    MasterDocumentDTO[] findMDocsByTag(String workspaceId, String label) throws ApplicationException;
    MasterDocumentDTO[] createVersion(String workspaceId, String id, String version, String title, String description, String workflowModelId, ACLDTO acl) throws ApplicationException;
    MasterDocumentDTO[] delFolder(String completePath) throws ApplicationException;
    void createFolder(String pParentFolder, String pFolder) throws ApplicationException;    
    MasterDocumentDTO checkIn(String workspaceId, String id, String version) throws ApplicationException;    
    MasterDocumentDTO checkOut(String workspaceId, String id, String version) throws ApplicationException;  
    MasterDocumentDTO undoCheckOut(String workspaceId, String id, String version) throws ApplicationException;
    void delMDoc(String workspaceId, String id, String version) throws ApplicationException;  
    UserDTO whoAmI(String pWorkspaceId) throws ApplicationException;
    MasterDocumentDTO createMDoc(String pParentFolder, String mdocId, String title, String description, String mdocTemplateId, String pWorkflowModelId, ACLDTO acl) throws ApplicationException;
    MasterDocumentDTO removeFileFromDocument(String pFullName) throws ApplicationException;
    MasterDocumentDTO removeFilesFromDocument(String[] pFullNames) throws ApplicationException;
    public MasterDocumentTemplateDTO getMDocTemplate(String workspaceId, String id) throws ApplicationException;
    MasterDocumentTemplateDTO removeFileFromTemplate(String pFullName) throws ApplicationException;
    MasterDocumentTemplateDTO removeFilesFromTemplate(String[] pFullNames) throws ApplicationException;
    MasterDocumentDTO getMDoc(String workspaceId, String id, String version) throws ApplicationException;
    MasterDocumentDTO updateDoc(String workspaceId, String id, String version, int iteration, String pComment, InstanceAttributeDTO[] attributes, DocumentDTO[] links) throws ApplicationException;
    MasterDocumentTemplateDTO createMDocTemplate(String workspaceId, String id, String documentType, String mask, InstanceAttributeTemplateDTO[] attributeTemplates, boolean idGenerated) throws ApplicationException;
    void delMDocTemplate(String workspaceId, String id) throws ApplicationException;
    MasterDocumentTemplateDTO updateMDocTemplate(String workspaceId, String id, String documentType, String mask, InstanceAttributeTemplateDTO[] attributeTemplates,  boolean idGenerated) throws ApplicationException;
    void delWorkflowModel(String workspaceId, String id) throws ApplicationException;
    UserDTO[] getUsers(String pWorkspaceId) throws ApplicationException;
    void delTag(String workspaceId, String label) throws ApplicationException;
    void createTag(String workspaceId, String label) throws ApplicationException;

    MasterDocumentDTO saveTags(String workspaceId, String id, String version, String[] pTags) throws ApplicationException;
    WorkflowModelDTO createWorkflowModel(String workspaceId, String id, String finalLifeCycleState, ActivityModelDTO[] activityModels) throws ApplicationException;
    String generateId(String workspaceId, String mdocTemplateId) throws ApplicationException;

    MasterDocumentDTO[] getIterationChangeEventSubscriptions(String workspaceId) throws ApplicationException;
    MasterDocumentDTO[] getStateChangeEventSubscriptions(String workspaceId) throws ApplicationException;
    void subscribeToIterationChangeEvent(String workspaceId, String id, String version) throws ApplicationException;
    void subscribeToStateChangeEvent(String workspaceId, String id, String version) throws ApplicationException;
    void unsubscribeToIterationChangeEvent(String workspaceId, String id, String version) throws ApplicationException;
    void unsubscribeToStateChangeEvent(String workspaceId, String id, String version) throws ApplicationException;
    MasterDocumentDTO approve(String workspaceId, int workflowId, int activityStep, int num, String comment) throws ApplicationException;
    MasterDocumentDTO reject(String workspaceId, int workflowId, int activityStep, int num, String comment) throws ApplicationException;
    MasterDocumentDTO[] searchMDocs(String workspaceId, String mdocId, String title, String version, String author, String type, Date creationDateFrom, Date creationDateTo, SearchQueryDTO.AbstractAttributeQueryDTO[] attributes, String[] tags, String content) throws ApplicationException;
    MasterDocumentDTO moveMDoc(String parentFolder, String workspaceId, String id, String version) throws ApplicationException;
    UserDTO[] getWorkspaceUserMemberships(String workspaceId) throws ApplicationException;
    UserGroupDTO[] getWorkspaceUserGroupMemberships(String workspaceId) throws ApplicationException;


    /*
    public Task[] getTasks(String pWorkspaceId) throws ApplicationException;
    public WorkflowModelDTO getWorkflowModel(WorkflowModelKey pKey) throws ApplicationException;
    public Workspace getWorkspace(String pWorkspaceId) throws ApplicationException;
    public User savePersonalInfo(String pWorkspaceId, String pName, String pEmail, String pLanguage) throws ApplicationException;
        
    */

    // services using ExplorerServiceResponse
    MDocResponse getCheckedOutMDocs(String workspaceId, int startOffset, int chunkSize) throws ApplicationException;
    MDocResponse findMDocsByFolder(String completePath, int startOffset, int chunkSize) throws ApplicationException;
    MDocResponse findMDocsByTag(String workspaceId, String label, int startOffset, int chunkSize) throws ApplicationException;
    MDocResponse searchMDocs(String workspaceId, String mdocId, String title, String version, String author, String type, Date creationDateFrom, Date creationDateTo, SearchQueryDTO.AbstractAttributeQueryDTO[] attributes, String[] tags, String content, int startOffset, int chunkSize) throws ApplicationException;

    MDocTemplateResponse getMDocTemplates(String workspaceId, int startOffset, int chunkSize) throws ApplicationException;
    WorkflowResponse getWorkflowModels(String workspaceId, int startOffser, int chunkSize) throws ApplicationException;

}
