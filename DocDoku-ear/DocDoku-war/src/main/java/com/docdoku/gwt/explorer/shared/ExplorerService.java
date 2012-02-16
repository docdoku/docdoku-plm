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
import com.docdoku.server.rest.dto.DocumentMasterTemplateDTO;
import com.docdoku.server.rest.dto.ActivityModelDTO;
import com.docdoku.server.rest.dto.WorkflowModelDTO;
import com.docdoku.server.rest.dto.UserDTO;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import com.docdoku.server.rest.dto.DocumentMasterDTO;
import com.docdoku.server.rest.dto.DocumentDTO;
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
    DocumentMasterTemplateDTO[] getDocMTemplates(String workspaceId) throws ApplicationException;
    DocumentMasterDTO[] getCheckedOutDocMs(String workspaceId) throws ApplicationException;
    DocumentMasterDTO[] findDocMsByFolder(String completePath) throws ApplicationException;
    DocumentMasterDTO[] findDocMsByTag(String workspaceId, String label) throws ApplicationException;
    DocumentMasterDTO[] createVersion(String workspaceId, String id, String version, String title, String description, String workflowModelId, ACLDTO acl) throws ApplicationException;
    DocumentMasterDTO[] delFolder(String completePath) throws ApplicationException;
    void createFolder(String pParentFolder, String pFolder) throws ApplicationException;    
    DocumentMasterDTO checkIn(String workspaceId, String id, String version) throws ApplicationException;    
    DocumentMasterDTO checkOut(String workspaceId, String id, String version) throws ApplicationException;  
    DocumentMasterDTO undoCheckOut(String workspaceId, String id, String version) throws ApplicationException;
    void delDocM(String workspaceId, String id, String version) throws ApplicationException;  
    UserDTO whoAmI(String pWorkspaceId) throws ApplicationException;
    DocumentMasterDTO createDocM(String pParentFolder, String docMId, String title, String description, String docMTemplateId, String pWorkflowModelId, ACLDTO acl) throws ApplicationException;
    DocumentMasterDTO removeFileFromDocument(String pFullName) throws ApplicationException;
    DocumentMasterDTO removeFilesFromDocument(String[] pFullNames) throws ApplicationException;
    public DocumentMasterTemplateDTO getDocMTemplate(String workspaceId, String id) throws ApplicationException;
    DocumentMasterTemplateDTO removeFileFromTemplate(String pFullName) throws ApplicationException;
    DocumentMasterTemplateDTO removeFilesFromTemplate(String[] pFullNames) throws ApplicationException;
    DocumentMasterDTO getDocM(String workspaceId, String id, String version) throws ApplicationException;
    DocumentMasterDTO updateDoc(String workspaceId, String id, String version, int iteration, String pComment, InstanceAttributeDTO[] attributes, DocumentDTO[] links) throws ApplicationException;
    DocumentMasterTemplateDTO createDocMTemplate(String workspaceId, String id, String documentType, String mask, InstanceAttributeTemplateDTO[] attributeTemplates, boolean idGenerated) throws ApplicationException;
    void delDocMTemplate(String workspaceId, String id) throws ApplicationException;
    DocumentMasterTemplateDTO updateDocMTemplate(String workspaceId, String id, String documentType, String mask, InstanceAttributeTemplateDTO[] attributeTemplates,  boolean idGenerated) throws ApplicationException;
    void delWorkflowModel(String workspaceId, String id) throws ApplicationException;
    UserDTO[] getUsers(String pWorkspaceId) throws ApplicationException;
    void delTag(String workspaceId, String label) throws ApplicationException;
    void createTag(String workspaceId, String label) throws ApplicationException;

    DocumentMasterDTO saveTags(String workspaceId, String id, String version, String[] pTags) throws ApplicationException;
    WorkflowModelDTO createWorkflowModel(String workspaceId, String id, String finalLifeCycleState, ActivityModelDTO[] activityModels) throws ApplicationException;
    String generateId(String workspaceId, String docMTemplateId) throws ApplicationException;

    DocumentMasterDTO[] getIterationChangeEventSubscriptions(String workspaceId) throws ApplicationException;
    DocumentMasterDTO[] getStateChangeEventSubscriptions(String workspaceId) throws ApplicationException;
    void subscribeToIterationChangeEvent(String workspaceId, String id, String version) throws ApplicationException;
    void subscribeToStateChangeEvent(String workspaceId, String id, String version) throws ApplicationException;
    void unsubscribeToIterationChangeEvent(String workspaceId, String id, String version) throws ApplicationException;
    void unsubscribeToStateChangeEvent(String workspaceId, String id, String version) throws ApplicationException;
    DocumentMasterDTO approve(String workspaceId, int workflowId, int activityStep, int num, String comment) throws ApplicationException;
    DocumentMasterDTO reject(String workspaceId, int workflowId, int activityStep, int num, String comment) throws ApplicationException;
    DocumentMasterDTO[] searchDocMs(String workspaceId, String docMId, String title, String version, String author, String type, Date creationDateFrom, Date creationDateTo, SearchQueryDTO.AbstractAttributeQueryDTO[] attributes, String[] tags, String content) throws ApplicationException;
    DocumentMasterDTO moveDocM(String parentFolder, String workspaceId, String id, String version) throws ApplicationException;
    UserDTO[] getWorkspaceUserMemberships(String workspaceId) throws ApplicationException;
    UserGroupDTO[] getWorkspaceUserGroupMemberships(String workspaceId) throws ApplicationException;


    /*
    public Task[] getTasks(String pWorkspaceId) throws ApplicationException;
    public WorkflowModelDTO getWorkflowModel(WorkflowModelKey pKey) throws ApplicationException;
    public Workspace getWorkspace(String pWorkspaceId) throws ApplicationException;
    public User savePersonalInfo(String pWorkspaceId, String pName, String pEmail, String pLanguage) throws ApplicationException;
        
    */

    // services using ExplorerServiceResponse
    DocMResponse getCheckedOutDocMs(String workspaceId, int startOffset, int chunkSize) throws ApplicationException;
    DocMResponse findDocMsByFolder(String completePath, int startOffset, int chunkSize) throws ApplicationException;
    DocMResponse findDocMsByTag(String workspaceId, String label, int startOffset, int chunkSize) throws ApplicationException;
    DocMResponse searchDocMs(String workspaceId, String docMId, String title, String version, String author, String type, Date creationDateFrom, Date creationDateTo, SearchQueryDTO.AbstractAttributeQueryDTO[] attributes, String[] tags, String content, int startOffset, int chunkSize) throws ApplicationException;

    DocMTemplateResponse getDocMTemplates(String workspaceId, int startOffset, int chunkSize) throws ApplicationException;
    WorkflowResponse getWorkflowModels(String workspaceId, int startOffser, int chunkSize) throws ApplicationException;

}
