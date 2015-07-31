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

package com.docdoku.server.rest.file;


import com.docdoku.core.common.Account;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentRevision;
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

        File uploadedFile1 = File.createTempFile(ResourceUtil.TARGET_FILE_STORAGE + "new_" + ResourceUtil.FILENAME1, ResourceUtil.TEMP_SUFFIX);
        
        OutputStream outputStream1 = new FileOutputStream(uploadedFile1);

        Mockito.when(request.getParts()).thenReturn(filesParts);
        Mockito.when(documentService.saveFileInDocument(Matchers.any(DocumentIterationKey.class), Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceOutputStream(Matchers.any(BinaryResource.class))).thenReturn(outputStream1);
        Mockito.when(request.getRequestURI()).thenReturn(ResourceUtil.WORKSPACE_ID + "/documents/" + ResourceUtil.DOCUMENT_ID + "/" + ResourceUtil.FILENAME1);

        //When
        Response response = documentBinaryResource.uploadDocumentFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION, ResourceUtil.ITERATION);

        //Then
        assertNotNull(response);
        assertEquals(response.getStatus(), 201);
        assertEquals(response.getStatusInfo(), Response.Status.CREATED);
        
        //delete tem file
        uploadedFile1.deleteOnExit();
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

        File uploadedFile1 = File.createTempFile(ResourceUtil.TARGET_FILE_STORAGE + "new_" + ResourceUtil.FILENAME2,ResourceUtil.TEMP_SUFFIX);
       

        OutputStream outputStream1 = new FileOutputStream(uploadedFile1);

        Mockito.when(request.getParts()).thenReturn(filesParts);
        Mockito.when(documentService.saveFileInDocument(Matchers.any(DocumentIterationKey.class), Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceOutputStream(Matchers.any(BinaryResource.class))).thenReturn(outputStream1);
        Mockito.when(request.getRequestURI()).thenReturn(ResourceUtil.WORKSPACE_ID + "/documents/" + ResourceUtil.DOCUMENT_ID + "/" + ResourceUtil.FILENAME2);


        //When
        Response response = documentBinaryResource.uploadDocumentFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION, ResourceUtil.ITERATION);

        //Then
        assertNotNull(response);
        assertEquals(response.getStatus(), 201);
        assertEquals(response.getStatusInfo(), Response.Status.CREATED);

        //delete tem file
        uploadedFile1.deleteOnExit();
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

        File uploadedFile1 = File.createTempFile(ResourceUtil.TARGET_FILE_STORAGE + "new_" + ResourceUtil.FILENAME1, ResourceUtil.TEMP_SUFFIX);
        File uploadedFile2 = File.createTempFile(ResourceUtil.TARGET_FILE_STORAGE + "new_" + ResourceUtil.FILENAME2, ResourceUtil.TEMP_SUFFIX);
        File uploadedFile3 = File.createTempFile( ResourceUtil.TARGET_FILE_STORAGE + "new_" + ResourceUtil.FILENAME3,ResourceUtil.TEMP_SUFFIX);

        OutputStream outputStream1 = new FileOutputStream(uploadedFile1);
        OutputStream outputStream2 = new FileOutputStream(uploadedFile2);
        OutputStream outputStream3 = new FileOutputStream(uploadedFile3);
        Mockito.when(request.getParts()).thenReturn(filesParts);
        Mockito.when(documentService.saveFileInDocument(Matchers.any(DocumentIterationKey.class), Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource1, binaryResource1, binaryResource2, binaryResource2, binaryResource3, binaryResource3);
        Mockito.when(dataManager.getBinaryResourceOutputStream(Matchers.any(BinaryResource.class))).thenReturn(outputStream1, outputStream2, outputStream3);
        //When
        Response response = documentBinaryResource.uploadDocumentFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION, ResourceUtil.ITERATION);

        //Then
        assertNotNull(response);
        assertEquals(response.getStatus(), 200);
        assertEquals(response.getStatusInfo(), Response.Status.OK);

        //delete temp files
        uploadedFile1.deleteOnExit();
        uploadedFile2.deleteOnExit();
        uploadedFile3.deleteOnExit();

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
        assertNotNull(response);
        assertEquals(response.getStatus(), 206);
        assertEquals(response.getStatusInfo(), Response.Status.PARTIAL_CONTENT);
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
        DocumentIteration documentIteration = new DocumentIteration(documentRevision, user);
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
        assertNotNull(response);
        assertEquals(response.getStatus(), 206);
        assertEquals(response.getStatusInfo(), Response.Status.PARTIAL_CONTENT);


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
        DocumentIteration documentIteration =new DocumentIteration(documentRevision, user);
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
        assertNotNull(response);
        assertEquals(response.getStatus(), 206);
        assertEquals(response.getStatusInfo(), Response.Status.PARTIAL_CONTENT);


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
        assertNotNull(response);
        assertEquals(response.getStatus(), 206);
        assertEquals(response.getStatusInfo(), Response.Status.PARTIAL_CONTENT);


    }
}