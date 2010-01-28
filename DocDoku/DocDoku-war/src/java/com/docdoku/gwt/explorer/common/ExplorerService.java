package com.docdoku.gwt.explorer.common;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author Florent GARIN
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
    WorkflowModelDTO createWorkflowModel(String workspaceId, String id, String finalLifeCycleState, AbstractActivityModelDTO[] activityModels) throws ApplicationException;
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
    public WorkflowModelDTO getWorkflowModel(BasicElementKey pKey) throws ApplicationException;
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
