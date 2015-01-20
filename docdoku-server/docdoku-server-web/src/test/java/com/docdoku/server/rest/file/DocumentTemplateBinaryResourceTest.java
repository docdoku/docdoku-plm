package com.docdoku.server.rest.file;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentMasterTemplateKey;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentResourceGetterManagerLocal;
import com.docdoku.server.util.PartImp;
import com.docdoku.server.util.ResourceUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import javax.servlet.http.HttpServletRequest;
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

public class DocumentTemplateBinaryResourceTest {
    @InjectMocks
    DocumentTemplateBinaryResource documentTemplateBinaryResource = new DocumentTemplateBinaryResource();
    @Mock
    private IDataManagerLocal dataManager;
    @Mock
    private IDocumentManagerLocal documentService;
    @Mock
    private IDocumentResourceGetterManagerLocal documentResourceGetterService;
    @Spy
    BinaryResource binaryResource;
    @Before
    public void setup() throws Exception {
        initMocks(this);
    }

    @Test
    public void downloadDocumentTemplateFile() throws Exception {
        Request request = Mockito.mock(Request.class);

        binaryResource = Mockito.spy(new BinaryResource(ResourceUtil.FILENAME1,ResourceUtil.DOCUMENT_SIZE,new Date()));


        String fullName = ResourceUtil.WORKSPACE_ID + "/document-templates/" + ResourceUtil.DOC_TEMPLATE_ID + "/" + ResourceUtil.FILENAME1;
        Mockito.when(documentService.getTemplateBinaryResource(fullName)).thenReturn(binaryResource);
        File input = new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE+ResourceUtil.FILENAME1).getFile());
        FileInputStream fileInputStream = new FileInputStream(input);
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(fileInputStream);
        //When
        Response response =documentTemplateBinaryResource.downloadDocumentTemplateFile(request,ResourceUtil.RANGE, ResourceUtil.WORKSPACE_ID, ResourceUtil.DOC_TEMPLATE_ID,ResourceUtil.FILENAME1,ResourceUtil.FILE_TYPE,null);

        //Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(),206);
        Assert.assertEquals(response.getStatusInfo(), Response.Status.PARTIAL_CONTENT);
        Assert.assertNotNull(response.getEntity());

    }

    @Test
    public void downloadDocumentTemplateFilementTemplateFiles() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        binaryResource = Mockito.spy(new BinaryResource(ResourceUtil.FILENAME1,ResourceUtil.DOCUMENT_SIZE,new Date()));
        Collection<Part> filesParts = new ArrayList<Part>();
        filesParts.add(new PartImp(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1).getFile())));
        File uploadedFile1 = new File(getClass().getResource(ResourceUtil.TARGET_FILE_STORAGE + "new_" + ResourceUtil.FILENAME1).getFile());

        OutputStream outputStream = new FileOutputStream(uploadedFile1);
        Mockito.when(documentService.saveFileInTemplate(Matchers.any(DocumentMasterTemplateKey.class),Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceOutputStream(binaryResource)).thenReturn(outputStream);
        Mockito.when(request.getRequestURI()).thenReturn(ResourceUtil.WORKSPACE_ID + "/documents/" + ResourceUtil.DOCUMENT_ID + "/" + ResourceUtil.FILENAME1);

        //When
        Response response =documentTemplateBinaryResource.uploadDocumentTemplateFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.DOC_TEMPLATE_ID);

        //Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(),200);
        Assert.assertEquals(response.getStatusInfo(), Response.Status.OK);


    }
}