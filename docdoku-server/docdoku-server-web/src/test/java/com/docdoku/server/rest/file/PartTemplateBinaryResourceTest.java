package com.docdoku.server.rest.file;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.product.PartMasterTemplate;
import com.docdoku.core.product.PartMasterTemplateKey;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.file.util.BinaryResourceBinaryStreamingOutput;
import com.docdoku.server.util.PartImp;
import com.docdoku.server.util.ResourceUtil;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import sun.misc.IOUtils;

import javax.ejb.EJB;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;

import static org.mockito.MockitoAnnotations.initMocks;

public class PartTemplateBinaryResourceTest {

    @InjectMocks
    PartTemplateBinaryResource partTemplateBinaryResource = new PartTemplateBinaryResource();
    @Mock
    private IDataManagerLocal dataManager;
    @Mock
    private IProductManagerLocal productService;
    @Spy
    BinaryResource binaryResource;

    @Before
    public void setup() throws Exception {
        initMocks(this);
    }

    /**
     * test the upload of a simple file in parts templates
     * @throws Exception
     */
    @Test
    public void testUploadPartTemplateFiles() throws Exception {
        //Given

        final File fileToUpload  = new File(ResourceUtil.SOURCE_PART_STORAGE+ResourceUtil.TEST_PART_FILENAME1);
        File fuploadedFile  = new File(ResourceUtil.TARGET_PART_STORAGE+ResourceUtil.FILENAME_TARGET_PART);
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> parts = new ArrayList<Part>();
        parts.add(new PartImp(fileToUpload));
        Mockito.when(request.getParts()).thenReturn(parts);
        binaryResource = new BinaryResource(ResourceUtil.FILENAME1,ResourceUtil.DOCUMENT_SIZE,new Date());
        OutputStream outputStream= new FileOutputStream(fuploadedFile);
        Mockito.when(productService.saveFileInTemplate(Matchers.any(PartMasterTemplateKey.class),Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceOutputStream(binaryResource)).thenReturn(outputStream);
        Mockito.when(request.getRequestURI()).thenReturn(ResourceUtil.WORKSPACE_ID+"/parts-templates/"+ResourceUtil.PART_TEMPLATE_ID+"/"+ResourceUtil.FILENAME_TARGET_PART);
        if (!fuploadedFile.getParentFile().exists()){
            fuploadedFile.getParentFile().mkdirs();
        }
        if (!fuploadedFile.exists()){
            fuploadedFile.createNewFile();
        }

        //When
        Response response = partTemplateBinaryResource.uploadPartTemplateFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_TEMPLATE_ID);
        //Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 201);
        Assert.assertEquals(response.getStatusInfo(), Response.Status.CREATED);


    }

    /**
     * test the upload of a file (under name that contains special characters) in parts templates
     * @throws Exception
     */
    @Test
    public void testUploadPartTemplateFilesUnderANameContainingSpecialCharacters() throws Exception {
        //Given

        final File fileToUpload  = new File(ResourceUtil.SOURCE_PART_STORAGE+ResourceUtil.TEST_PART_FILENAME1);
        File uploadedFile  = new File(ResourceUtil.TARGET_PART_STORAGE+ResourceUtil.FILENAME_TARGET_PART_SPECIAL_CHARACTERS);
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> parts = new ArrayList<Part>();
        parts.add(new PartImp(fileToUpload) );
        Mockito.when(request.getParts()).thenReturn(parts);
        binaryResource = new BinaryResource(ResourceUtil.FILENAME1,ResourceUtil.DOCUMENT_SIZE,new Date());
        OutputStream outputStream= new FileOutputStream(uploadedFile);
        Mockito.when(productService.saveFileInTemplate(Matchers.any(PartMasterTemplateKey.class),Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceOutputStream(binaryResource)).thenReturn(outputStream);
        Mockito.when(request.getRequestURI()).thenReturn(ResourceUtil.WORKSPACE_ID+"/parts-templates/"+ResourceUtil.PART_TEMPLATE_ID+"/"+ResourceUtil.FILENAME_TARGET_PART);
        if (!uploadedFile.getParentFile().exists()){
            uploadedFile.getParentFile().mkdirs();
        }
        if (!uploadedFile.exists()){
            uploadedFile.createNewFile();
        }

        //When
        Response response = partTemplateBinaryResource.uploadPartTemplateFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_TEMPLATE_ID);
        //Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 201);
        Assert.assertEquals(response.getStatusInfo(), Response.Status.CREATED);
    }
    /**
     * test the upload of a file (that contains special characters) in parts templates
     * @throws Exception
     */
    @Test
    public void testUploadPartTemplateFilesNameContainingSpecialCharacters() throws Exception {
        //Given

        final File fileToUpload  = new File(ResourceUtil.SOURCE_PART_STORAGE+ResourceUtil.FILENAME_TO_UPLOAD_PART_SPECIAL_CHARACTER);
        File uploadedFile  = new File(ResourceUtil.TARGET_PART_STORAGE+"new_"+ResourceUtil.FILENAME_TO_UPLOAD_PART_SPECIAL_CHARACTER);
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> parts = new ArrayList<Part>();
        parts.add(new PartImp(fileToUpload) );
        Mockito.when(request.getParts()).thenReturn(parts);
        binaryResource = new BinaryResource(ResourceUtil.FILENAME1,ResourceUtil.DOCUMENT_SIZE,new Date());
        OutputStream outputStream= new FileOutputStream(uploadedFile);
        Mockito.when(productService.saveFileInTemplate(Matchers.any(PartMasterTemplateKey.class),Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource);

        Mockito.when(dataManager.getBinaryResourceOutputStream(binaryResource)).thenReturn(outputStream);
        Mockito.when(request.getRequestURI()).thenReturn(ResourceUtil.WORKSPACE_ID+"/parts-templates/"+ResourceUtil.PART_TEMPLATE_ID+"/"+ResourceUtil.FILENAME_TARGET_PART);
        if (!uploadedFile.getParentFile().exists()){
            uploadedFile.getParentFile().mkdirs();
        }
        if (!uploadedFile.exists()){
            uploadedFile.createNewFile();
        }

        //When
        Response response = partTemplateBinaryResource.uploadPartTemplateFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_TEMPLATE_ID);
        //Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 201);
        Assert.assertEquals(response.getStatusInfo(), Response.Status.CREATED);
    }


    /**
     * test the upload of several files to parts templates
     * @throws Exception
     */
    @Test
    public void testUploadPartTemplateSeveralFiles() throws Exception {
        //Given
        final File fileToUpload1  = new File(ResourceUtil.SOURCE_PART_STORAGE+ResourceUtil.FILENAME_TO_UPLOAD_PART_SPECIAL_CHARACTER);
        final File fileToUpload2  = new File(ResourceUtil.SOURCE_PART_STORAGE+ResourceUtil.TEST_PART_FILENAME1);
        final File fileToUpload3  = new File(ResourceUtil.SOURCE_PART_STORAGE+ResourceUtil.TEST_PART_FILENAME2);
        File uploadedFile  = new File(ResourceUtil.TARGET_PART_STORAGE+"new_"+ResourceUtil.FILENAME_TO_UPLOAD_PART_SPECIAL_CHARACTER);
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> parts = new ArrayList<Part>();
        parts.add(new PartImp(fileToUpload1) );
        parts.add(new PartImp(fileToUpload2) );
        parts.add(new PartImp(fileToUpload3) );
        Mockito.when(request.getParts()).thenReturn(parts);
        binaryResource = new BinaryResource(ResourceUtil.FILENAME1,ResourceUtil.DOCUMENT_SIZE,new Date());
        OutputStream outputStream= new FileOutputStream(uploadedFile);
        Mockito.when(productService.saveFileInTemplate(Matchers.any(PartMasterTemplateKey.class),Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource);

        Mockito.when(dataManager.getBinaryResourceOutputStream(binaryResource)).thenReturn(outputStream);
        Mockito.when(request.getRequestURI()).thenReturn(ResourceUtil.WORKSPACE_ID+"/parts-templates/"+ResourceUtil.PART_TEMPLATE_ID+"/"+ResourceUtil.FILENAME_TARGET_PART);
        if (!uploadedFile.getParentFile().exists()){
            uploadedFile.getParentFile().mkdirs();
        }
        if (!uploadedFile.exists()){
            uploadedFile.createNewFile();
        }
        //When
        Response response = partTemplateBinaryResource.uploadPartTemplateFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_TEMPLATE_ID);
        //Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(response.getStatusInfo(), Response.Status.OK);
    }

    /**
     * test the download of file from part templates with non null range
     * @throws Exception
     */
    @Test
    public void testDownloadPartTemplateFileNonNullRange() throws Exception {

        //Given
        Request request = Mockito.mock(Request.class);
        binaryResource = new BinaryResource(ResourceUtil.TEST_PART_FILENAME1,ResourceUtil.PART_SIZE,new Date());
        File file = new File(ResourceUtil.SOURCE_PART_STORAGE+ResourceUtil.TEST_PART_FILENAME1);
        FileInputStream fileInputStream = new FileInputStream(file);
        Mockito.when(productService.getTemplateBinaryResource(ResourceUtil.WORKSPACE_ID+"/part-templates/" + ResourceUtil.PART_TEMPLATE_ID + "/" + ResourceUtil.TEST_PART_FILENAME1)).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(fileInputStream);
        //When
        Response response = partTemplateBinaryResource.downloadPartTemplateFile(request,ResourceUtil.RANGE, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_TEMPLATE_ID,ResourceUtil.TEST_PART_FILENAME1);
        //Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatusInfo(), Response.Status.PARTIAL_CONTENT);
        Assert.assertEquals(response.getStatus(),206);
        Assert.assertTrue(response.hasEntity());
        Assert.assertTrue(response.getEntity() instanceof BinaryResourceBinaryStreamingOutput);


    }

    /**
     * test the download of file from part templates with null range
     * @throws Exception
     */
    @Test
    public void testDownloadPartTemplateFileNullRange() throws Exception {

        //Given
        Request request = Mockito.mock(Request.class);
        binaryResource = new BinaryResource(ResourceUtil.TEST_PART_FILENAME1,ResourceUtil.PART_SIZE,new Date());
        File file = new File(ResourceUtil.SOURCE_PART_STORAGE+ResourceUtil.TEST_PART_FILENAME1);
        FileInputStream fileInputStream = new FileInputStream(file);
        Mockito.when(productService.getTemplateBinaryResource(ResourceUtil.WORKSPACE_ID+"/part-templates/" + ResourceUtil.PART_TEMPLATE_ID + "/" + ResourceUtil.TEST_PART_FILENAME1)).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(fileInputStream);
        //When
        Response response = partTemplateBinaryResource.downloadPartTemplateFile(request,null, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_TEMPLATE_ID,ResourceUtil.TEST_PART_FILENAME1);
        //Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatusInfo(), Response.Status.OK);
        Assert.assertTrue(response.hasEntity());
        Assert.assertTrue(response.getEntity() instanceof BinaryResourceBinaryStreamingOutput);
        Assert.assertNotNull(response.getHeaders().getFirst("Content-Disposition"));
        Assert.assertNotNull(response.getHeaders().getFirst("Content-Disposition").equals("attachement;filename=\""+ResourceUtil.TEST_PART_FILENAME1+"\""));


    }


}