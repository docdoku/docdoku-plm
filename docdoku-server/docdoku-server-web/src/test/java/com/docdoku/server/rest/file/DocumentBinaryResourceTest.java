package com.docdoku.server.rest.file;


import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.product.PartMasterTemplateKey;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentPostUploaderManagerLocal;
import com.docdoku.core.services.IDocumentResourceGetterManagerLocal;
import com.docdoku.server.util.PartImp;
import com.docdoku.server.util.ResourceUtil;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import javax.servlet.http.HttpServletRequest;
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
    public void testUploadDocumentFiles() throws Exception {
        //Given
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> filesParts = new ArrayList<Part>();
        filesParts.add(new PartImp(new File(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1)));

        BinaryResource binaryResource = new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date());

        File uploadedFile1 = new File(ResourceUtil.TARGET_FILE_STORAGE + "new_" + ResourceUtil.FILENAME1);
        if (!uploadedFile1.getParentFile().exists()){
            uploadedFile1.getParentFile().mkdirs();
        }
        else {
            for(File file:uploadedFile1.getParentFile().listFiles())
                file.delete();
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
        org.junit.Assert.assertEquals(response.getStatusInfo(), Response.Status.OK);
    }

    /**
     * Test to upload a file to a document with special characters
     *
     * @throws Exception
     */
    @Test
    public void testUploadFileWithSpecialCharactersToDocumentTemplates() throws Exception {

        //Given
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> filesParts = new ArrayList<Part>();
        filesParts.add(new PartImp(new File(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME2)));

        BinaryResource binaryResource = new BinaryResource(ResourceUtil.FILENAME2, ResourceUtil.DOCUMENT_SIZE, new Date());

        File uploadedFile1 = new File(ResourceUtil.TARGET_FILE_STORAGE + "new_" + ResourceUtil.FILENAME2);
        if (!uploadedFile1.getParentFile().exists()){
            uploadedFile1.getParentFile().mkdirs();
        }
        else {
            for(File file:uploadedFile1.getParentFile().listFiles())
                file.delete();
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
        org.junit.Assert.assertEquals(response.getStatus(), 200);
        org.junit.Assert.assertEquals(response.getStatusInfo(), Response.Status.OK);
    }

    /**
     * Test to upload several file to a document
     *
     * @throws Exception
     */
    @Test
    public void testUploadSeveralFilesToDocumentsTemplates() throws Exception {

        //Given
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> filesParts = new ArrayList<Part>();
        filesParts.add(new PartImp(new File(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1)));
        filesParts.add(new PartImp(new File(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME2)));
        filesParts.add(new PartImp(new File(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME3)));

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
    public void testDownloadDocumentFile1() throws Exception {

        //Given
        Request request = Mockito.mock(Request.class);


        String output = null;
        BinaryResource binaryResource = new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date());
        Mockito.when(documentService.canAccess(new DocumentIterationKey(ResourceUtil.WORKSPACE_ID, ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION,ResourceUtil.ITERATION))).thenReturn(false);
        Mockito.when(documentService.getBinaryResource( ResourceUtil.WORKSPACE_ID+"/documents/" +ResourceUtil.DOCUMENT_ID+"/"+ResourceUtil.VERSION  +"/" + ResourceUtil.ITERATION + "/"+ResourceUtil.FILENAME1)).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(new FileInputStream(new File(ResourceUtil.SOURCE_FILE_STORAGE+ResourceUtil.FILENAME1)));
        //When
        Response response = documentBinaryResource.downloadDocumentFile(request, ResourceUtil.RANGE, ResourceUtil.WORKSPACE_ID,ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION, ResourceUtil.ITERATION,ResourceUtil.FILENAME1,null,ResourceUtil.FILE_TYPE,output);

        //Then
        org.junit.Assert.assertNotNull(response);
        org.junit.Assert.assertEquals(response.getStatus(), 200);
        org.junit.Assert.assertEquals(response.getStatusInfo(), Response.Status.PARTIAL_CONTENT);
    }

    /**
     * Test to download a document file as a guest but the document is not public
     *
     * @throws Exception
     */
    @Test
    public void testDownloadDocumentFile2() throws Exception {

    }

    /**
     * Test to download a document file as a regular user who has read access on it
     *
     * @throws Exception
     */
    @Test
    public void testDownloadDocumentFile3() throws Exception {

    }

    /**
     * Test to download a document file as a regular user who has no read access on it
     *
     * @throws Exception
     */
    @Test
    public void testDownloadDocumentFile4() throws Exception {

    }

    /**
     * Test to download a document SCORM sub-resource
     *
     * @throws Exception
     */
    @Test
    public void testDownloadDocumentFile5() throws Exception {

    }
}