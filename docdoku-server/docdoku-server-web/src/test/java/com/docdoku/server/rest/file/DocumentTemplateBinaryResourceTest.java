package com.docdoku.server.rest.file;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentResourceGetterManagerLocal;
import com.docdoku.server.util.ResourceUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import java.io.File;
import java.io.FileInputStream;
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
    public void testUploadDocumentTemplateFiles() throws Exception {


    }

    @Test
    public void testDownloadDocumentTemplateFile() throws Exception {
        Request request = Mockito.mock(Request.class);
        binaryResource = Mockito.spy(new BinaryResource(ResourceUtil.FILENAME1,ResourceUtil.DOCUMENT_SIZE,new Date()));
        File file = new File(getClass().getResource(ResourceUtil.SOURCE_FILE_STORAGE+"/"+ResourceUtil.FILENAME1).getFile());
        FileInputStream fileInputStream = new FileInputStream(file);
        Mockito.when(documentService.getTemplateBinaryResource(ResourceUtil.WORKSPACE_ID+"/document-templates/" +ResourceUtil.PART_TEMPLATE_ID + "/" +ResourceUtil.FILENAME1)).thenReturn(binaryResource);
        Mockito.when(documentResourceGetterService.getConvertedResource(ResourceUtil.TARGET_FILE_STORAGE, binaryResource)).thenReturn(fileInputStream);
        Response response =documentTemplateBinaryResource.downloadDocumentTemplateFile(request, ResourceUtil.RANGE, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_TEMPLATE_ID, ResourceUtil.FILENAME1,
                ResourceUtil.FILE_TYPE, ResourceUtil.TARGET_FILE_STORAGE);

        //Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(),206);
        Assert.assertEquals(response.getLength(),ResourceUtil.DOCUMENT_SIZE);
        Assert.assertEquals(response.getStatusInfo(), Response.Status.PARTIAL_CONTENT);
        Assert.assertEquals(response.getHeaders().get("Content-Length").get(0), ResourceUtil.DOCUMENT_SIZE);

    }
}