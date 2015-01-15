package com.docdoku.server.rest.file;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartMasterTemplateKey;
import com.docdoku.core.services.IConverterManagerLocal;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IShareManagerLocal;
import com.docdoku.server.filters.GuestProxy;
import com.docdoku.server.util.PartImp;
import com.docdoku.server.util.ResourceUtil;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class PartBinaryResourceTest {

    @InjectMocks
    private PartBinaryResource partBinaryResource;
    @Mock
    private IDataManagerLocal dataManager;
    @Mock
    private IProductManagerLocal productService;
    @Mock
    private IConverterManagerLocal converterService;
    @Mock
    private IShareManagerLocal shareService;
    @Mock
    private GuestProxy guestProxy;

    /**
     * Test to upload a file to a part
     * @throws Exception
     */
    @Test
    public void uploadPartFiles() throws Exception {
        //User Case1
        //workspaceId = WORKSPACE_ID
        //partId = PART_ID
        //version = VERSION
        //iteration = ITERATION
        //formParts = [{fileName = FILENAME1,
        //              repository= SOURCE_FILE_STORAGE,
        //              tempFile= SOURCE_FILE_STORAGE+"/"+SOURCE_FILENAME1}]
        //binaryResource = {
        //                    fullName= WORKSPACE_ID+"/"+PART_ID+"/"+VERSION+"/"+ITERATION+"/"+FILENAME1
        //                    contentLenght=O
        //                    lastModified = new Date();
        //                  }

        // assert uploaded file exist in TARGET_FILE_STORAGE+"/"+FILENAME1 et length > 0
        // assert response.status.code = "201"
        // assert response.getLocation().toString() = "/api/files/"+WORKSPACE_ID+"/"+PART_ID+"/"+VERSION+"/"+ITERATION+"/"+FILENAME1

        //Given
        final File fileToUpload  = new File(ResourceUtil.SOURCE_PART_STORAGE+ResourceUtil.TEST_PART_FILENAME1);
        File file  = new File(ResourceUtil.TARGET_PART_STORAGE+ResourceUtil.FILENAME_TARGET_PART);
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> parts = new ArrayList<Part>();
        parts.add(new PartImp(fileToUpload));
        Mockito.when(request.getParts()).thenReturn(parts);
        BinaryResource binaryResource = new BinaryResource(ResourceUtil.FILENAME1,ResourceUtil.DOCUMENT_SIZE,new Date());
        OutputStream outputStream= new FileOutputStream(file);
        Mockito.when(productService.saveFileInTemplate(Matchers.any(PartMasterTemplateKey.class),Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceOutputStream(binaryResource)).thenReturn(outputStream);
        Mockito.when(request.getRequestURI()).thenReturn(ResourceUtil.WORKSPACE_ID+"/parts-templates/"+ResourceUtil.PART_TEMPLATE_ID+"/"+ResourceUtil.FILENAME_TARGET_PART);
        Mockito.when(productService.saveNativeCADInPartIteration(Matchers.any(PartIterationKey.class),Matchers.anyString(),Matchers.anyInt()));
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        if (!file.exists()){
            file.createNewFile();
        }

        //When
        Response response = partBinaryResource.uploadPartFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_NUMBER,ResourceUtil.VERSION,ResourceUtil.ITERATION,ResourceUtil.SUBTYPE);
        //Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 201);
        Assert.assertEquals(response.getStatusInfo(), Response.Status.CREATED);


    }

    /**
     * Test to upload a file to a part with special characters
     * @throws Exception
     */
    @Test
    public void uploadPartFiles2() throws Exception {
        //User Case2
        //workspaceId = WORKSPACE_ID
        //partId = PART_ID
        //version = VERSION
        //iteration = ITERATION
        //formParts = [{fileName = FILENAME1,
        //              repository= SOURCE_FILE_STORAGE,
        //              tempFile= SOURCE_FILE_STORAGE+"/"+SOURCE_FILENAME1}]
        //binaryResource = {
        //                    fullName= WORKSPACE_ID+"/"+PART_ID+"/"+VERSION+"/"+ITERATION+"/"+FILENAME2
        //                    contentLenght=O
        //                    lastModified = new Date();
        //                  }

        // assert uploaded file exist in TARGET_FILE_STORAGE+"/"+FILENAME2 et length > 0
        // assert response.status.code = "201"
        // assert response.getLocation().toString() = "/api/files/"+WORKSPACE_ID+"/"+PART_ID+"/"+VERSION+"/"+ITERATION+"/"+FILENAME2
    }

    /**
     * Test to upload several file to a part
     * @throws Exception
     */
    @Test
    public void uploadPartFiles3() throws Exception {
        //User Case3
        //workspaceId = WORKSPACE_ID
        //partId = PART_ID
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
        //                    fullName= WORKSPACE_ID+"/"+PART_ID+"/"+VERSION+"/"+ITERATION+"/"+filename
        //                    contentLength=length
        //                    lastModified = new Date();
        //                  }

        // assert uploaded file exist in TARGET_FILE_STORAGE+"/"+FILENAME3_1  et length > 0
        // assert uploaded file exist in TARGET_FILE_STORAGE+"/"+FILENAME3_2  et length > 0
        // assert response.status.code = "200"
    }

    /**
     * Test to upload a native cad to a part
     */
    @Test
    public void uploadPartFiles4() throws Exception {
    }

    /**
     * Test to download a part file as a guest and the part is public
     * @throws Exception
     */
    @Test
    public void testDownloadPartFile1() throws Exception {

    }

    /**
     * Test to download a part file as a guest but the part is not public
     * @throws Exception
     */
    @Test
    public void testDownloadPartFile2() throws Exception {

    }

    /**
     * Test to download a part file as a regular user who has read access on it
     * @throws Exception
     */
    @Test
    public void testDownloadPartFile3() throws Exception {

    }

    /**
     * Test to download a part file as a regular user who has no read access on it
     * @throws Exception
     */
    @Test
    public void testDownloadPartFile4() throws Exception {

    }
}