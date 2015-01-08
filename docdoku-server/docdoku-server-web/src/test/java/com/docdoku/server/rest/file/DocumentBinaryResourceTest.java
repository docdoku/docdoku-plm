package com.docdoku.server.rest.file;


import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentPostUploaderManagerLocal;
import com.docdoku.core.services.IDocumentResourceGetterManagerLocal;
import com.docdoku.server.util.ResourceUtil;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
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

    @Before
    public void setup() throws Exception {
     initMocks(this);
    }
    /**
     * Test the upload of file to a document
     * @throws Exception
     */
    @Test
    public void testUploadDocumentFiles1() throws Exception {
        //Given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        request.setAttribute("URI", "");
        Part part = Mockito.mock(Part.class);

        Collection<Part> parts = new ArrayList(1);
        Mockito.when(part.getSubmittedFileName()).thenReturn(ResourceUtil.FILENAME1);
        File sourceFile = new File(ResourceUtil.SOURCE_FILE_STORAGE+ResourceUtil.FILENAME1);
        File targetFile = new File(ResourceUtil.TARGET_FILE_STORAGE+ResourceUtil.FILENAME1);
        FileInputStream fileInputStream = Mockito.spy(new FileInputStream(getClass().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1).getFile()));
        Mockito.when(part.getInputStream()).thenReturn(fileInputStream);

        BinaryResource binaryResource = Mockito.spy(new BinaryResource(ResourceUtil.SOURCE_FILE_STORAGE+ResourceUtil.FILENAME1,12, new Date()));
        OutputStream outputstream = Mockito.spy(new FileOutputStream(getClass().getResource(ResourceUtil.TARGET_FILE_STORAGE+ResourceUtil.FILENAME1).getFile()));
        Mockito.when(documentService.saveFileInDocument(Matchers.any(DocumentIterationKey.class),Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceOutputStream(binaryResource)).thenReturn(outputstream);
        Mockito.when(request.getRequestURI()).thenReturn("/home/asmae/projects/plm/docdoku-plm/docdoku-server/docdoku-server-web/src/test/resources/com/docdoku/server/rest/file/toUpload/");
        parts.add(part);

        Mockito.when(request.getParts()).thenReturn(parts);
        //When
        Response response = documentBinaryResource.uploadDocumentFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.DOCUMENT_ID, ResourceUtil.VERSION, ResourceUtil.ITERATION);
        //Then
        Assert.assertTrue(response.getStatus() == 201);



            //User Case1
            //workspaceId = WORKSPACE_ID
            //documentId = DOCUMENT_ID
            //version = VERSION
            //iteration = ITERATION
            //formParts = [{fileName = FILENAME1,
            //              repository= SOURCE_FILE_STORAGE,
            //              tempFile= SOURCE_FILE_STORAGE+"/"+SOURCE_FILENAME1}]
            //binaryResource = {
            //                    fullName= WORKSPACE_ID+"/"+DOCUMENT_ID+"/"+VERSION+"/"+ITERATION+"/"+FILENAME1
            //                    contentLenght=O
            //                    lastModified = new Date();
            //                  }

            // assert uploaded file exist in TARGET_FILE_STORAGE+"/"+FILENAME1 et length > 0
            // assert response.status.code = "201"
            // assert response.getLocation().toString() = "/api/files/"+WORKSPACE_ID+"/"+DOCUMENT_ID+"/"+VERSION+"/"+ITERATION+"/"+FILENAME1
    }

    /**
     * Test to upload a file to a document with special characters
     * @throws Exception
     */
    @Test
    public void testUploadDocumentFiles2() throws Exception {
        //User Case2
            //workspaceId = WORKSPACE_ID
            //documentId = DOCUMENT_ID
            //version = VERSION
            //iteration = ITERATION
            //formParts = [{fileName = FILENAME1,
            //              repository= SOURCE_FILE_STORAGE,
            //              tempFile= SOURCE_FILE_STORAGE+"/"+SOURCE_FILENAME1}]
            //binaryResource = {
            //                    fullName= WORKSPACE_ID+"/"+DOCUMENT_ID+"/"+VERSION+"/"+ITERATION+"/"+FILENAME2
            //                    contentLenght=O
            //                    lastModified = new Date();
            //                  }

            // assert uploaded file exist in TARGET_FILE_STORAGE+"/"+FILENAME2 et length > 0
            // assert response.status.code = "201"
            // assert response.getLocation().toString() = "/api/files/"+WORKSPACE_ID+"/"+DOCUMENT_ID+"/"+VERSION+"/"+ITERATION+"/"+FILENAME2
    }

    /**
     * Test to upload several file to a document
     * @throws Exception
     */
    @Test
    public void testUploadDocumentFiles3() throws Exception {
        //User Case3
            //workspaceId = WORKSPACE_ID
            //documentId = DOCUMENT_ID
            //version = VERSION
            //iteration = ITERATION
            //formParts = [{
            //                  fileName = FILENAME3_1,
            //                  repository= SOURCE_FILE_STORAGE,
            //                  tempFile= SOURCE_FILE_STORAGE+"/"+SOURCE_FILENAME2_1
            //              },{
            //                  fileName = FILENAME3_2,
            //                  repository= SOURCE_FILE_STORAGE,
            //                  tempFile= SOURCE_FILE_STORAGE+"/"+SOURCE_FILENAME2_1
            //              }
            //            ]
            //getBinaryResource(pk,filename,length) = {
            //                    fullName= WORKSPACE_ID+"/"+DOCUMENT_ID+"/"+VERSION+"/"+ITERATION+"/"+filename
            //                    contentLength=length
            //                    lastModified = new Date();
            //                  }

            // assert uploaded file exist in TARGET_FILE_STORAGE+"/"+FILENAME3_1  et length > 0
            // assert uploaded file exist in TARGET_FILE_STORAGE+"/"+FILENAME3_2  et length > 0
            // assert response.status.code = "200"
    }

    /**
     * Test to download a document file as a guest and the document is public
     * @throws Exception
     */
    @Test
    public void testDownloadDocumentFile1() throws Exception {

    }

    /**
     * Test to download a document file as a guest but the document is not public
     * @throws Exception
     */
    @Test
    public void testDownloadDocumentFile2() throws Exception {

    }

    /**
     * Test to download a document file as a regular user who has read access on it
     * @throws Exception
     */
    @Test
    public void testDownloadDocumentFile3() throws Exception {

    }

    /**
     * Test to download a document file as a regular user who has no read access on it
     * @throws Exception
     */
    @Test
    public void testDownloadDocumentFile4() throws Exception {

    }

    /**
     * Test to download a document SCORM sub-resource
     * @throws Exception
     */
    @Test
    public void testDownloadDocumentFile5() throws Exception {

    }
}