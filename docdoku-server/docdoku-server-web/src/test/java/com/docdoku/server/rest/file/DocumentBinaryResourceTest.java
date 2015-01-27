package com.docdoku.server.rest.file;


import com.docdoku.core.common.Account;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.util.Tools;
import com.docdoku.server.filters.GuestProxy;
import com.docdoku.server.util.PartImp;
import com.docdoku.server.util.ResourceUtil;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.*;

import javax.ejb.SessionContext;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

public class DocumentBinaryResourceTest {

    @InjectMocks
    DocumentBinaryResource documentBinaryResource = new DocumentBinaryResource();

    @Mock
    private IDataManagerLocal dataManager;
    @Mock
    private IDocumentManagerLocal documentService;
    @Mock
    private IDocumentResourceGetterManagerLocal documentResourceGetterService;
    @Mock
    private IDocumentPostUploaderManagerLocal documentPostUploaderService;
    @Mock
    private IShareManagerLocal shareService;
    @Mock
    private SessionContext ctx;
    @Mock
    private GuestProxy guestProxy;
    @Spy
    BinaryResource binaryResource;


    @Before
    public void setup() throws Exception {
        initMocks(this);
    }

    /**
     * Test the upload of file to a document
     *
     * @throws Exception
     */
    @Test
    public void uploadDocumentFiles() throws Exception {
        //Given
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> filesParts = new ArrayList<Part>();
        filesParts.add(new PartImp(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1).getFile())));

        BinaryResource binaryResource = new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date());

        File uploadedFile1 = new File(ResourceUtil.TARGET_FILE_STORAGE + "new_" + ResourceUtil.FILENAME1);
        if (!uploadedFile1.getParentFile().exists()){
            uploadedFile1.getParentFile().mkdirs();
        }
        if (!uploadedFile1.exists()){
            uploadedFile1.createNewFile();
        }

        OutputStream outputStream1 = new FileOutputStream(uploadedFile1);

        Mockito.when(request.getParts()).thenReturn(filesParts);
        Mockito.when(documentService.saveFileInDocument(Matchers.any(DocumentIterationKey.class), Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceOutputStream(Matchers.any(BinaryResource.class))).thenReturn(outputStream1);
        Mockito.when(request.getRequestURI()).thenReturn(ResourceUtil.WORKSPACE_ID + "/documents/" + ResourceUtil.DOCUMENT_ID + "/" + ResourceUtil.FILENAME1);

        //When
        Response response = documentBinaryResource.uploadDocumentFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION, ResourceUtil.ITERATION);

        //Then
        org.junit.Assert.assertNotNull(response);
        org.junit.Assert.assertEquals(response.getStatus(), 201);
        org.junit.Assert.assertEquals(response.getStatusInfo(), Response.Status.CREATED);
    }

    /**
     * Test to upload a file to a document with special characters
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void uploadFileWithSpecialCharactersToDocumentTemplates() throws Exception {

        //Given
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> filesParts = new ArrayList<Part>();
        filesParts.add(new PartImp(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE).getFile() + ResourceUtil.FILENAME2)));

        BinaryResource binaryResource = new BinaryResource(Tools.unAccent(ResourceUtil.FILENAME2), ResourceUtil.DOCUMENT_SIZE, new Date());

        File uploadedFile1 = new File(ResourceUtil.TARGET_FILE_STORAGE + "new_" + ResourceUtil.FILENAME2);
        if (!uploadedFile1.getParentFile().exists()){
            uploadedFile1.getParentFile().mkdirs();
        }

        OutputStream outputStream1 = new FileOutputStream(uploadedFile1);

        Mockito.when(request.getParts()).thenReturn(filesParts);
        Mockito.when(documentService.saveFileInDocument(Matchers.any(DocumentIterationKey.class), Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceOutputStream(Matchers.any(BinaryResource.class))).thenReturn(outputStream1);
        Mockito.when(request.getRequestURI()).thenReturn(ResourceUtil.WORKSPACE_ID + "/documents/" + ResourceUtil.DOCUMENT_ID + "/" + ResourceUtil.FILENAME2);


        //When
        Response response = documentBinaryResource.uploadDocumentFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION, ResourceUtil.ITERATION);

        //Then
        org.junit.Assert.assertNotNull(response);
        org.junit.Assert.assertEquals(response.getStatus(), 201);
        org.junit.Assert.assertEquals(response.getStatusInfo(), Response.Status.CREATED);
    }

    /**
     * Test to upload several file to a document
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void uploadSeveralFilesToDocumentsTemplates() throws Exception {

        //Given
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> filesParts = new ArrayList<Part>();
        filesParts.add(new PartImp(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1).getFile())));
        filesParts.add(new PartImp(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE).getFile() + ResourceUtil.FILENAME2)));
        filesParts.add(new PartImp(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME3).getFile())));

        BinaryResource binaryResource1 = new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date());
        BinaryResource binaryResource2 = new BinaryResource(ResourceUtil.FILENAME2, ResourceUtil.DOCUMENT_SIZE, new Date());
        BinaryResource binaryResource3 = new BinaryResource(ResourceUtil.FILENAME3, ResourceUtil.DOCUMENT_SIZE, new Date());

        File uploadedFile1 = new File(ResourceUtil.TARGET_FILE_STORAGE + "new_" + ResourceUtil.FILENAME1);
        File uploadedFile2 = new File(ResourceUtil.TARGET_FILE_STORAGE + "new_" + ResourceUtil.FILENAME2);
        File uploadedFile3 = new File(ResourceUtil.TARGET_FILE_STORAGE + "new_" + ResourceUtil.FILENAME3);
        if (!uploadedFile1.getParentFile().exists()){
            uploadedFile1.getParentFile().mkdirs();
        }
        OutputStream outputStream1 = new FileOutputStream(uploadedFile1);
        OutputStream outputStream2 = new FileOutputStream(uploadedFile2);
        OutputStream outputStream3 = new FileOutputStream(uploadedFile3);
        Mockito.when(request.getParts()).thenReturn(filesParts);
        Mockito.when(documentService.saveFileInDocument(Matchers.any(DocumentIterationKey.class), Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource1, binaryResource1, binaryResource2, binaryResource2, binaryResource3, binaryResource3);
        Mockito.when(dataManager.getBinaryResourceOutputStream(Matchers.any(BinaryResource.class))).thenReturn(outputStream1, outputStream2, outputStream3);
        //When
        Response response = documentBinaryResource.uploadDocumentFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION, ResourceUtil.ITERATION);

        //Then
        org.junit.Assert.assertNotNull(response);
        org.junit.Assert.assertEquals(response.getStatus(), 200);
        org.junit.Assert.assertEquals(response.getStatusInfo(), Response.Status.OK);
    }

    /**
     * Test to download a document file as a guest and the document is public
     *
     * @throws Exception
     */
    @Test
    public void downloadDocumentFileAsGuestDocumentIsPublic() throws Exception {

        //Given
        Request request = Mockito.mock(Request.class);


        String output = null;
        String fullName= ResourceUtil.WORKSPACE_ID + "/documents/" + ResourceUtil.DOCUMENT_ID + "/" + ResourceUtil.VERSION + "/" + ResourceUtil.ITERATION + "/" + ResourceUtil.FILENAME1;

        BinaryResource binaryResource = new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date());
        Mockito.when(documentService.canAccess(new DocumentIterationKey(ResourceUtil.WORKSPACE_ID, ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION,ResourceUtil.ITERATION))).thenReturn(false);
        Mockito.when(documentService.getBinaryResource(fullName)).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(new FileInputStream(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1).getFile())));
        Mockito.when(guestProxy.getBinaryResourceForDocument(fullName)).thenReturn(binaryResource);
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)).thenReturn(false);
        Mockito.when(guestProxy.canAccess(Matchers.any(DocumentIterationKey.class))).thenReturn(true);

        //When
        Response response = documentBinaryResource.downloadDocumentFile(request, ResourceUtil.RANGE,ResourceUtil.DOC_REFER, ResourceUtil.WORKSPACE_ID,ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION, ResourceUtil.ITERATION,ResourceUtil.FILENAME1,null,ResourceUtil.FILE_TYPE,output,null);

        //Then
        org.junit.Assert.assertNotNull(response);
        org.junit.Assert.assertEquals(response.getStatus(), 206);
        org.junit.Assert.assertEquals(response.getStatusInfo(), Response.Status.PARTIAL_CONTENT);
    }

    /**
     * Test to download a document file as a guest but the document is private
     *
     * @throws Exception
     */
    @Test
    public void downloadDocumentFileAsGuestDocumentIsPrivate() throws Exception {
        //Given
        Request request = Mockito.mock(Request.class);
        //Workspace workspace, User author, Date expireDate, String password, DocumentRevision documentRevision
        Account account = Mockito.spy(new Account("user2" , "user2", "user2@docdoku.com", "en",new Date(),null));
        Workspace workspace = new Workspace(ResourceUtil.WORKSPACE_ID,account, "pDescription", false);
        User user = new User(workspace, "user1" , "user1", "user1@docdoku.com", "en");
        DocumentMaster documentMaster = new DocumentMaster(workspace,ResourceUtil.DOCUMENT_ID,user);
        DocumentRevision documentRevision = new DocumentRevision(documentMaster,ResourceUtil.VERSION,user);
        List<DocumentIteration> iterations = new ArrayList<>();
        DocumentIteration documentIteration =new DocumentIteration(documentRevision,ResourceUtil.ITERATION,user);
        iterations.add(documentIteration);
        documentRevision.setDocumentIterations(iterations);
        SharedDocument sharedEntity = new SharedDocument(workspace,user,new Date(2025,12,02),"password",documentRevision);
        String output = null;
        String fullName= ResourceUtil.WORKSPACE_ID + "/documents/" + ResourceUtil.DOCUMENT_ID + "/" + ResourceUtil.VERSION + "/" + ResourceUtil.ITERATION + "/" + ResourceUtil.FILENAME1;

        BinaryResource binaryResource = new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date());
        Mockito.when(documentService.getBinaryResource(fullName)).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(new FileInputStream(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1).getFile())));
        Mockito.when(guestProxy.getBinaryResourceForDocument(fullName)).thenReturn(binaryResource);
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)).thenReturn(false);
        Mockito.when(shareService.findSharedEntityForGivenUUID(ResourceUtil.SHARED_DOC_ENTITY_UUID.split("/")[2])).thenReturn(sharedEntity);
        //When
        Response response = documentBinaryResource.downloadDocumentFile(request, ResourceUtil.RANGE,"refers/"+sharedEntity.getUuid(), ResourceUtil.WORKSPACE_ID,ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION, ResourceUtil.ITERATION,ResourceUtil.FILENAME1,null,ResourceUtil.FILE_TYPE,output,ResourceUtil.SHARED_DOC_ENTITY_UUID);

        //Then
        org.junit.Assert.assertNotNull(response);
        org.junit.Assert.assertEquals(response.getStatus(), 206);
        org.junit.Assert.assertEquals(response.getStatusInfo(), Response.Status.PARTIAL_CONTENT);


    }

    /**
     * Test to download a document file as a regular user who has read access on it
     *
     * @throws Exception
     */
    @Test
    public void downloadDocumentFileAsRegularUserWithAccessRights() throws Exception {
        //Given
        Request request = Mockito.mock(Request.class);
        //Workspace workspace, User author, Date expireDate, String password, DocumentRevision documentRevision
        Account account = Mockito.spy(new Account("user2" , "user2", "user2@docdoku.com", "en",new Date(),null));
        Workspace workspace = new Workspace(ResourceUtil.WORKSPACE_ID,account, "pDescription", false);
        User user = new User(workspace, "user1" , "user1", "user1@docdoku.com", "en");
        DocumentMaster documentMaster = new DocumentMaster(workspace,ResourceUtil.DOCUMENT_ID,user);
        DocumentRevision documentRevision = new DocumentRevision(documentMaster,ResourceUtil.VERSION,user);
        List<DocumentIteration> iterations = new ArrayList<>();
        DocumentIteration documentIteration =new DocumentIteration(documentRevision,ResourceUtil.ITERATION,user);
        iterations.add(documentIteration);
        documentRevision.setDocumentIterations(iterations);
        SharedDocument sharedEntity = new SharedDocument(workspace,user,new Date(2025,12,02),"password",documentRevision);
        String output = null;
        String fullName= ResourceUtil.WORKSPACE_ID + "/documents/" + ResourceUtil.DOCUMENT_ID + "/" + ResourceUtil.VERSION + "/" + ResourceUtil.ITERATION + "/" + ResourceUtil.FILENAME1;
        BinaryResource binaryResource = new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date());

        Mockito.when(documentService.getBinaryResource(fullName)).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(new FileInputStream(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1).getFile())));
        Mockito.when(guestProxy.getBinaryResourceForDocument(fullName)).thenReturn(binaryResource);
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)).thenReturn(true);

        Mockito.when(shareService.findSharedEntityForGivenUUID(ResourceUtil.SHARED_DOC_ENTITY_UUID.split("/")[2])).thenReturn(sharedEntity);
        //When
        Response response = documentBinaryResource.downloadDocumentFile(request, ResourceUtil.RANGE,"refers/"+sharedEntity.getUuid(), ResourceUtil.WORKSPACE_ID,ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION, ResourceUtil.ITERATION,ResourceUtil.FILENAME1,null,ResourceUtil.FILE_TYPE,output,ResourceUtil.SHARED_DOC_ENTITY_UUID);

        //Then
        org.junit.Assert.assertNotNull(response);
        org.junit.Assert.assertEquals(response.getStatus(), 206);
        org.junit.Assert.assertEquals(response.getStatusInfo(), Response.Status.PARTIAL_CONTENT);


    }

    /**
     * Test to download a document file as a regular user who has no read access on it
     *
     * @throws Exception
     */
    @Test
    public void downloadDocumentFileAsUserWithNoAccessRights() throws Exception {

        //Given
        Request request = Mockito.mock(Request.class);
        String output = null;
        String fullName= ResourceUtil.WORKSPACE_ID + "/documents/" + ResourceUtil.DOCUMENT_ID + "/" + ResourceUtil.VERSION + "/" + ResourceUtil.ITERATION + "/" + ResourceUtil.FILENAME1;

        BinaryResource binaryResource = new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date());
        Mockito.when(documentService.canAccess(new DocumentIterationKey(ResourceUtil.WORKSPACE_ID, ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION,ResourceUtil.ITERATION))).thenReturn(false);
        Mockito.when(documentService.getBinaryResource(fullName)).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(new FileInputStream(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1).getFile())));
        Mockito.when(guestProxy.getBinaryResourceForDocument(fullName)).thenReturn(binaryResource);
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)).thenReturn(true);
        Mockito.when(guestProxy.canAccess(Matchers.any(DocumentIterationKey.class))).thenReturn(false);

        //When
        Response response= documentBinaryResource.downloadDocumentFile(request, ResourceUtil.RANGE,ResourceUtil.DOC_REFER, ResourceUtil.WORKSPACE_ID,ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION, ResourceUtil.ITERATION,ResourceUtil.FILENAME1,null,ResourceUtil.FILE_TYPE,output,null);
        //Then
        assertNotNull(response);
        assertEquals(response.getStatus(), 401);
        assertEquals(response.getStatusInfo(), Response.Status.UNAUTHORIZED);


    }

    /**
     * Test to download a document SCORM sub-resource
     *
     * @throws Exception
     */
    @Test
    public void downloadDocumentScormSubResource() throws Exception {

        //Given
        Request request = Mockito.mock(Request.class);


        String output = "output";
        String fullName= ResourceUtil.WORKSPACE_ID + "/documents/" + ResourceUtil.DOCUMENT_ID + "/" + ResourceUtil.VERSION + "/" + ResourceUtil.ITERATION + "/" + ResourceUtil.FILENAME1;

        BinaryResource binaryResource = new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date());
        Mockito.when(documentService.canAccess(new DocumentIterationKey(ResourceUtil.WORKSPACE_ID, ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION,ResourceUtil.ITERATION))).thenReturn(false);
        Mockito.when(documentService.getBinaryResource(fullName)).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(new FileInputStream(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1).getFile())));
        Mockito.when(guestProxy.getBinaryResourceForDocument(fullName)).thenReturn(binaryResource);
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)).thenReturn(true);
        Mockito.when(documentService.canAccess(Matchers.any(DocumentIterationKey.class))).thenReturn(true);
        Mockito.when(documentResourceGetterService.getConvertedResource(output, binaryResource)).thenReturn(new FileInputStream(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.VIRTUAL_SUB_RESOURCE).getFile())));
        Mockito.when(dataManager.getBinarySubResourceInputStream(binaryResource, fullName+"/"+ResourceUtil.VIRTUAL_SUB_RESOURCE)).thenReturn(new FileInputStream(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.VIRTUAL_SUB_RESOURCE).getFile())));
        //When
        Response response = documentBinaryResource.downloadDocumentFile(request, ResourceUtil.RANGE,ResourceUtil.DOC_REFER, ResourceUtil.WORKSPACE_ID,ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION, ResourceUtil.ITERATION,ResourceUtil.FILENAME1,ResourceUtil.VIRTUAL_SUB_RESOURCE,ResourceUtil.FILE_TYPE,null,null);
        //Then
        org.junit.Assert.assertNotNull(response);
        org.junit.Assert.assertEquals(response.getStatus(), 206);
        org.junit.Assert.assertEquals(response.getStatusInfo(), Response.Status.PARTIAL_CONTENT);


    }
}