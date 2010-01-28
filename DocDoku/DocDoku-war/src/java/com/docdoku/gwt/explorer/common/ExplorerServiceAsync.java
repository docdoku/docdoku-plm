package com.docdoku.gwt.explorer.common;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Date;


/**
 *
 * @author Florent GARIN
 */
public interface ExplorerServiceAsync {
    void getFolders(String parentFolder, AsyncCallback<String[]> callback);
    void getTags(String workspaceId, AsyncCallback<String[]> callback);
    void getWorkflowModels(String workspaceId, AsyncCallback<WorkflowModelDTO[]> callback);
    void getMDocTemplates(String workspaceId, AsyncCallback<MasterDocumentTemplateDTO[]> callback);
    void getCheckedOutMDocs(String workspaceId, AsyncCallback<MasterDocumentDTO[]> callback);
    void findMDocsByFolder(String completePath, AsyncCallback<MasterDocumentDTO[]> callback);
    void findMDocsByTag(String workspaceId, String label, AsyncCallback<MasterDocumentDTO[]> callback);
    void delFolder(String completePath, AsyncCallback<MasterDocumentDTO[]> callback);
    void createFolder(String pParentFolder, String pFolder, AsyncCallback<Void> callback);
    void removeFileFromDocument(String pFullName, AsyncCallback<MasterDocumentDTO> callback);
    void removeFilesFromDocument(String[] pFullNames, AsyncCallback<MasterDocumentDTO> callback);
    void getMDoc(String workspaceId, String id, String version, AsyncCallback<MasterDocumentDTO> callback);
    void checkIn(String workspaceId, String id, String version, AsyncCallback<MasterDocumentDTO> callback);
    void checkOut(String workspaceId, String id, String version, AsyncCallback<MasterDocumentDTO> callback);
    void undoCheckOut(String workspaceId, String id, String version, AsyncCallback<MasterDocumentDTO> callback);
    void delMDoc(String workspaceId, String id, String version, AsyncCallback<Void> callback);
    void whoAmI(String pWorkspaceId, AsyncCallback<UserDTO> callback);
    void createMDoc(String parentFolder, String mdocId, String title, String description, String mdocTemplateId, String workflowModelId, ACLDTO acl, AsyncCallback<MasterDocumentDTO> callback);
    void createVersion(String workspaceId, String id, String version, String title, String description, String workflowModelId, ACLDTO acl, AsyncCallback<MasterDocumentDTO[]> callback);
    void updateDoc(String workspaceId, String id, String version, int iteration, String revisionNote, InstanceAttributeDTO[] attributes, DocumentDTO[] links, AsyncCallback<MasterDocumentDTO> callback);
    void createMDocTemplate(String workspaceId, String id, String documentType, String mask, InstanceAttributeTemplateDTO[] attributeTemplates, boolean idGenerated, AsyncCallback<MasterDocumentTemplateDTO> callback);
    void delMDocTemplate(String workspaceId, String id, AsyncCallback<Void> callback);
    void updateMDocTemplate(String workspaceId, String id, String documentType, String mask, InstanceAttributeTemplateDTO[] attributeTemplates,  boolean idGenerated, AsyncCallback<MasterDocumentTemplateDTO> callback);
    void delTag(String workspaceId, String label,AsyncCallback<Void> callback);
    void createTag(String workspaceId, String label, AsyncCallback<Void> callback);
    void generateId(String workspaceId, String mdocTemplateId, AsyncCallback<String> callback);
    void saveTags(String workspaceId, String id, String version, String[] tags, AsyncCallback<MasterDocumentDTO> callback);
    void delWorkflowModel(String workspaceId, String id, AsyncCallback<Void> callback);
    void getUsers(String pWorkspaceId, AsyncCallback<UserDTO[]> callback);
    void createWorkflowModel(String workspaceId, String id, String finalLifeCycleState, AbstractActivityModelDTO[] activityModels, AsyncCallback<WorkflowModelDTO> callback);
    void getIterationChangeEventSubscriptions(String workspaceId, AsyncCallback<MasterDocumentDTO[]> callback);
    void getStateChangeEventSubscriptions(String workspaceId, AsyncCallback<MasterDocumentDTO[]> callback);
    void subscribeToIterationChangeEvent(String workspaceId, String id, String version,AsyncCallback<Void> callback);
    void subscribeToStateChangeEvent(String workspaceId, String id, String version,AsyncCallback<Void> callback);
    void unsubscribeToIterationChangeEvent(String workspaceId, String id, String version,AsyncCallback<Void> callback);
    void unsubscribeToStateChangeEvent(String workspaceId, String id, String version,AsyncCallback<Void> callback);

    void removeFileFromTemplate(String pFullName, AsyncCallback<MasterDocumentTemplateDTO> callback);
    void removeFilesFromTemplate(String[] pFullNames, AsyncCallback<MasterDocumentTemplateDTO> callback);
    void getMDocTemplate(String workspaceId, String id, AsyncCallback<MasterDocumentTemplateDTO> callback);

    void approve(String workspaceId, int workflowId, int activityStep, int num, String comment, AsyncCallback<MasterDocumentDTO> callback);
    void reject(String workspaceId, int workflowId, int activityStep, int num, String comment, AsyncCallback<MasterDocumentDTO> callback);
    void searchMDocs(String workspaceId, String mdocId, String title, String version, String author, String type, Date creationDateFrom, Date creationDateTo, SearchQueryDTO.AbstractAttributeQueryDTO[] attributes, String[] tags, String content, AsyncCallback<MasterDocumentDTO[]> callback);
    void moveMDoc(String parentFolder, String workspaceId, String id, String version, AsyncCallback<MasterDocumentDTO> callback);
    void getWorkspaceUserMemberships(String workspaceId, AsyncCallback<UserDTO[]> callback);
    void getWorkspaceUserGroupMemberships(String workspaceId, AsyncCallback<UserGroupDTO[]> callback);


    // services using ExplorerServiceResponse
    void getCheckedOutMDocs(String workspaceId, int startOffset, int chunkSize, AsyncCallback<MDocResponse> callback) ;
    void findMDocsByFolder(String completePath, int startOffset, int chunkSize,AsyncCallback<MDocResponse> callback);
    void findMDocsByTag(String workspaceId, String label, int startOffset, int chunkSize,AsyncCallback<MDocResponse> callback);
    void searchMDocs(String workspaceId, String mdocId, String title, String version, String author, String type, Date creationDateFrom, Date creationDateTo, SearchQueryDTO.AbstractAttributeQueryDTO[] attributes, String[] tags, String content, int startOffset, int chunkSize,AsyncCallback<MDocResponse> callback);

    void getMDocTemplates(String workspaceId, int startPoint, int chunkSize, AsyncCallback<MDocTemplateResponse> callback);
    void getWorkflowModels(String workspaceId, int startPoint, int chunkSize, AsyncCallback<WorkflowResponse> callback);
}
