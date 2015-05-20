package com.docdoku.server.rest.file;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IConverterManagerLocal;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IShareManagerLocal;
import com.docdoku.core.sharing.SharedPart;
import com.docdoku.server.filters.GuestProxy;
import com.docdoku.server.rest.file.util.BinaryResourceBinaryStreamingOutput;
import com.docdoku.server.util.PartImp;
import com.docdoku.server.util.ResourceUtil;
import org.junit.*;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;


import javax.ejb.SessionContext;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

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
    @Mock
    private SessionContext ctx;


    @Before
    public void setup() throws Exception {
        initMocks(this);
    }

    /**
     * Test to upload a file to a part
     *
     * @throws Exception
     */
    @Test
    public void uploadFileToPart() throws Exception {
        //Given
        final File fileToUpload = new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_PART_STORAGE + ResourceUtil.TEST_PART_FILENAME1).getFile());
        File uploadedFile = File.createTempFile(ResourceUtil.TARGET_PART_STORAGE + ResourceUtil.FILENAME_TARGET_PART,ResourceUtil.TEMP_SUFFIX);
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> parts = new ArrayList<Part>();
        parts.add(new PartImp(fileToUpload));
        Mockito.when(request.getParts()).thenReturn(parts);
        BinaryResource binaryResource = new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date());
        OutputStream outputStream = new FileOutputStream(uploadedFile);
        Mockito.when(request.getRequestURI()).thenReturn(ResourceUtil.WORKSPACE_ID + "/parts/" + ResourceUtil.PART_TEMPLATE_ID + "/");
        Mockito.when(productService.saveFileInPartIteration(Matchers.any(PartIterationKey.class), Matchers.anyString(), Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceOutputStream(binaryResource)).thenReturn(outputStream);

        //Whenb n
        Response response = partBinaryResource.uploadPartFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_NUMBER, ResourceUtil.VERSION, ResourceUtil.ITERATION, null);
        //Then
        assertNotNull(response);
        assertEquals(response.getStatus(), 201);
        assertEquals(response.getStatusInfo(), Response.Status.CREATED);
        //delete temp file
        uploadedFile.deleteOnExit();

    }

    /**
     * Test to upload a native cad to a part
     */
    @Test
    public void uploadNativeCADToPart() throws Exception {

        //Given
        final File fileToUpload = new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_PART_STORAGE + ResourceUtil.TEST_PART_FILENAME1).getFile());
        File uploadedFile = File.createTempFile(ResourceUtil.TARGET_PART_STORAGE + ResourceUtil.FILENAME_TARGET_PART,ResourceUtil.TEMP_SUFFIX);
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> parts = new ArrayList<Part>();
        parts.add(new PartImp(fileToUpload));
        Mockito.when(request.getParts()).thenReturn(parts);
        BinaryResource binaryResource = new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date());
        OutputStream outputStream = new FileOutputStream(uploadedFile);
        Mockito.when(request.getRequestURI()).thenReturn(ResourceUtil.WORKSPACE_ID + "/parts/" + ResourceUtil.PART_TEMPLATE_ID + "/");
        Mockito.when(productService.saveNativeCADInPartIteration(Matchers.any(PartIterationKey.class), Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceOutputStream(binaryResource)).thenReturn(outputStream);

        //When
        Response response = partBinaryResource.uploadPartFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_NUMBER, ResourceUtil.VERSION, ResourceUtil.ITERATION, ResourceUtil.FILE_TYPE);
        //Then
        assertNotNull(response);
        assertEquals(response.getStatus(), 201);
        assertEquals(response.getStatusInfo(), Response.Status.CREATED);

        //delete temp file
        uploadedFile.deleteOnExit();

    }

    /**
     * Test to upload a file to a part with special characters
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void uploadFileWithSpecialCharactersToPart() throws Exception {
        //Given
        File fileToUpload = new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_PART_STORAGE + ResourceUtil.FILENAME_TO_UPLOAD_PART_SPECIAL_CHARACTER).getFile());
        File uploadedFile = File.createTempFile(ResourceUtil.TARGET_PART_STORAGE + ResourceUtil.FILENAME_TO_UPLOAD_PART_SPECIAL_CHARACTER,ResourceUtil.TEMP_SUFFIX);
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> parts = new ArrayList<Part>();
        parts.add(new PartImp(fileToUpload));
        Mockito.when(request.getParts()).thenReturn(parts);
        BinaryResource binaryResource = new BinaryResource(ResourceUtil.FILENAME_TO_UPLOAD_PART_SPECIAL_CHARACTER, ResourceUtil.DOCUMENT_SIZE, new Date());

        OutputStream outputStream = new FileOutputStream(uploadedFile);
        Mockito.when(request.getRequestURI()).thenReturn(ResourceUtil.WORKSPACE_ID + "/parts/" + ResourceUtil.PART_TEMPLATE_ID + "/");
        Mockito.when(productService.saveFileInPartIteration(Matchers.any(PartIterationKey.class), Matchers.anyString(), Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource);
        Mockito.when(dataManager.getBinaryResourceOutputStream(binaryResource)).thenReturn(outputStream);

        //When
        Response response = partBinaryResource.uploadPartFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_NUMBER, ResourceUtil.VERSION, ResourceUtil.ITERATION, null);
        //Then
        assertNotNull(response);
        assertEquals(response.getStatus(), 201);
        assertEquals(response.getStatusInfo(), Response.Status.CREATED);
        assertEquals(response.getLocation().toString(), (ResourceUtil.WORKSPACE_ID + "/parts/" + ResourceUtil.PART_TEMPLATE_ID +"/"+ URLEncoder.encode(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_PART_STORAGE).getFile() + ResourceUtil.FILENAME_TO_UPLOAD_PART_SPECIAL_CHARACTER, "UTF-8")));
        //delete temp file
        uploadedFile.deleteOnExit();

    }

    /**
     * Test to upload several file to a part
     *
     * @throws Exception
     */

    @Test
    @Ignore
    public void uploadSeveralFilesToPart() throws Exception {
        //Given
        File fileToUpload1 = new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_PART_STORAGE + ResourceUtil.TEST_PART_FILENAME1).getFile());
        File fileToUpload2 = new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_PART_STORAGE + ResourceUtil.TEST_PART_FILENAME2).getFile());
        File fileToUpload3 = new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_PART_STORAGE + ResourceUtil.FILENAME_TO_UPLOAD_PART_SPECIAL_CHARACTER).getFile());
        File uploadedFile1 = File.createTempFile(ResourceUtil.TARGET_PART_STORAGE + ResourceUtil.TEST_PART_FILENAME1,ResourceUtil.TEMP_SUFFIX);
        File uploadedFile2 = File.createTempFile(ResourceUtil.TARGET_PART_STORAGE + ResourceUtil.TEST_PART_FILENAME2,ResourceUtil.TEMP_SUFFIX);
        File uploadedFile3 = File.createTempFile(ResourceUtil.TARGET_PART_STORAGE + ResourceUtil.FILENAME_TO_UPLOAD_PART_SPECIAL_CHARACTER,ResourceUtil.TEMP_SUFFIX);
        HttpServletRequestWrapper request = Mockito.mock(HttpServletRequestWrapper.class);
        Collection<Part> parts = new ArrayList<Part>();
        parts.add(new PartImp(fileToUpload1));
        parts.add(new PartImp(fileToUpload2));
        parts.add(new PartImp(fileToUpload3));
        Mockito.when(request.getParts()).thenReturn(parts);
        BinaryResource binaryResource1 = new BinaryResource(ResourceUtil.TEST_PART_FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date());
        BinaryResource binaryResource2 = new BinaryResource(ResourceUtil.TEST_PART_FILENAME2, ResourceUtil.DOCUMENT_SIZE, new Date());
        BinaryResource binaryResource3 = new BinaryResource(ResourceUtil.FILENAME_TO_UPLOAD_PART_SPECIAL_CHARACTER, ResourceUtil.DOCUMENT_SIZE, new Date());

        OutputStream outputStream1 = new FileOutputStream(uploadedFile1);
        OutputStream outputStream2 = new FileOutputStream(uploadedFile2);
        OutputStream outputStream3 = new FileOutputStream(uploadedFile3);
        Mockito.when(productService.saveFileInPartIteration(Matchers.any(PartIterationKey.class), Matchers.anyString(), Matchers.anyString(), Matchers.anyInt())).thenReturn(binaryResource1, binaryResource1, binaryResource2, binaryResource2, binaryResource3, binaryResource3);
        Mockito.when(dataManager.getBinaryResourceOutputStream(Mockito.any(BinaryResource.class))).thenReturn(outputStream1, outputStream2, outputStream3);

        //When
        Response response = partBinaryResource.uploadPartFiles(request, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_NUMBER, ResourceUtil.VERSION, ResourceUtil.ITERATION, null);
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
     * Test to download a part file as a guest and the part is public
     *
     * @throws Exception
     */
    @Test
    public void downloadPartFileAsGuestPartPublic() throws Exception {

        //Given
        Request request = Mockito.mock(Request.class);
        BinaryResource binaryResource = Mockito.spy(new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date()));
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(new FileInputStream(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1).getFile())));
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)).thenReturn(false);
        Mockito.when(guestProxy.canAccess(Mockito.any(PartIterationKey.class))).thenReturn(true);
        Mockito.when(productService.canAccess(Matchers.any(PartIterationKey.class))).thenReturn(false);
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(new FileInputStream(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_PART_STORAGE + ResourceUtil.TEST_PART_FILENAME1).getFile())));
        //When
        Mockito.when(guestProxy.getBinaryResourceForPart(Matchers.anyString())).thenReturn(binaryResource);
        Response response = partBinaryResource.downloadPartFile(request, ResourceUtil.RANGE, ResourceUtil.DOC_REFER, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_NUMBER, ResourceUtil.VERSION, ResourceUtil.ITERATION, ResourceUtil.FILE_TYPE, ResourceUtil.TEST_PART_FILENAME1, ResourceUtil.FILE_TYPE, null, null);
        //Then
        assertNotNull(response);
        assertEquals(response.getStatus(), 206);
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof BinaryResourceBinaryStreamingOutput);
    }

    /**
     * Test to download a part file as a guest, the part is shared private mode
     *
     * @throws Exception
     */
    @Test
    public void downloadPartFilePrivateShare() throws Exception {

        //Given
        Request request = Mockito.mock(Request.class);
        Account account = Mockito.spy(new Account("user2", "user2", "user2@docdoku.com", "en", new Date(), null));
        Workspace workspace = new Workspace(ResourceUtil.WORKSPACE_ID,account, "pDescription", false);
        User user = new User(workspace, "user1" , "user1", "user1@docdoku.com", "en");
        PartMaster partMaster = new PartMaster(workspace,ResourceUtil.PART_NUMBER,user);
        PartRevision partRevision = new PartRevision(partMaster,ResourceUtil.VERSION,user);
        List<PartIteration> iterations = new ArrayList<>();
        PartIteration partIteration = new PartIteration(partRevision,ResourceUtil.ITERATION,user);
        PartIteration partIteration2 = new PartIteration(partRevision,2,user);
        iterations.add(partIteration);
        partRevision.setPartIterations(iterations);

        SharedPart sharedPart  = Mockito.spy(new SharedPart(workspace,user,new Date(2020,12,23),"password",partRevision));

        BinaryResource binaryResource = Mockito.spy(new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date()));
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(new FileInputStream(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1).getFile())));
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)).thenReturn(false);
        Mockito.when(guestProxy.canAccess(Mockito.any(PartIterationKey.class))).thenReturn(true);
        Mockito.when(productService.canAccess(Matchers.any(PartIterationKey.class))).thenReturn(false);
        File file = File.createTempFile(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_PART_STORAGE + ResourceUtil.TEST_PART_FILENAME1).getFile(), ResourceUtil.TEMP_SUFFIX);
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(new FileInputStream(file));
        Mockito.when(guestProxy.getBinaryResourceForPart(Matchers.anyString())).thenReturn(binaryResource);
        Mockito.when(shareService.findSharedEntityForGivenUUID(ResourceUtil.SHARED_PART_ENTITY_UUID.split("/")[2])).thenReturn(sharedPart);
        //When
        Response response = partBinaryResource.downloadPartFile(request, ResourceUtil.RANGE, "shares/"+sharedPart.getUuid(), ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_NUMBER, ResourceUtil.VERSION, ResourceUtil.ITERATION, ResourceUtil.FILE_TYPE, ResourceUtil.TEST_PART_FILENAME1, ResourceUtil.FILE_TYPE, null, ResourceUtil.SHARED_PART_ENTITY_UUID);
        //Then
        assertNotNull(response);
        assertEquals(response.getStatus(), 206);
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof BinaryResourceBinaryStreamingOutput);

        //Delete temp file
        file.deleteOnExit();

    }

    /**
     * Test to download a part file as a regular user who has read access 
     *
     * @throws Exception
     */
    @Test
    public void downloadPartFileAsRegularUserReadAccess() throws Exception {
        //Given
        Request request = Mockito.mock(Request.class);
        BinaryResource binaryResource = Mockito.spy(new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date()));
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(new FileInputStream(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1).getFile())));
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)).thenReturn(true);
        Mockito.when(productService.getBinaryResource(Matchers.anyString())).thenReturn(binaryResource);
        Mockito.when(productService.canAccess(Matchers.any(PartIterationKey.class))).thenReturn(true);
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(new FileInputStream(new File(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_PART_STORAGE + ResourceUtil.TEST_PART_FILENAME1).getFile())));
        Mockito.when(guestProxy.getBinaryResourceForPart(Matchers.anyString())).thenReturn(binaryResource);
        //When
        Response response = partBinaryResource.downloadPartFile(request, ResourceUtil.RANGE, ResourceUtil.DOC_REFER, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_NUMBER, ResourceUtil.VERSION, ResourceUtil.ITERATION, ResourceUtil.FILE_TYPE, ResourceUtil.TEST_PART_FILENAME1, ResourceUtil.FILE_TYPE, null, null);
        //Then
        assertNotNull(response);
        assertEquals(response.getStatus(), 206);
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof BinaryResourceBinaryStreamingOutput);

    }

    /**
     * Test to download a part file as a regular user who has no access
     *
     * @throws Exception
     */
    @Test
    public void downloadPartFileAsRegularUserNoReadAccess() throws Exception {

        //Given
        Request request = Mockito.mock(Request.class);
        BinaryResource binaryResource = Mockito.spy(new BinaryResource(ResourceUtil.FILENAME1, ResourceUtil.DOCUMENT_SIZE, new Date()));
        File file = File.createTempFile(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_FILE_STORAGE + ResourceUtil.FILENAME1).getFile(),ResourceUtil.TEMP_SUFFIX);
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(new FileInputStream(file));
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)).thenReturn(true);
        Mockito.when(productService.getBinaryResource(Matchers.anyString())).thenReturn(binaryResource);
        Mockito.when(productService.canAccess(Matchers.any(PartIterationKey.class))).thenReturn(false);
        File file1 = File.createTempFile(getClass().getClassLoader().getResource(ResourceUtil.SOURCE_PART_STORAGE + ResourceUtil.TEST_PART_FILENAME1).getFile(),ResourceUtil.TEMP_SUFFIX);
        Mockito.when(dataManager.getBinaryResourceInputStream(binaryResource)).thenReturn(new FileInputStream(file1));
        Mockito.when(guestProxy.getBinaryResourceForPart(Matchers.anyString())).thenReturn(binaryResource);
        //When
        Response response= partBinaryResource.downloadPartFile(request, ResourceUtil.RANGE, ResourceUtil.DOC_REFER, ResourceUtil.WORKSPACE_ID, ResourceUtil.PART_NUMBER, ResourceUtil.VERSION, ResourceUtil.ITERATION, ResourceUtil.FILE_TYPE, ResourceUtil.TEST_PART_FILENAME1, ResourceUtil.FILE_TYPE, null, null);

        assertNotNull(response);
        assertEquals(response.getStatus(), 401);
        assertEquals(response.getStatusInfo(), Response.Status.UNAUTHORIZED);
        //delete tem files
        file.deleteOnExit();
        file1.deleteOnExit();

    }

}