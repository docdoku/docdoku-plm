package com.docdoku.server.rest.file;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentPostUploaderManagerLocal;
import com.docdoku.core.services.IDocumentResourceGetterManagerLocal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.mockito.Mock;

import javax.ejb.EJB;

import static org.junit.Assert.*;

public class DocumentBinaryResourceTest {

    @Mock
    private IDataManagerLocal dataManager;
    @Mock
    private IDocumentManagerLocal documentService;
    @Mock
    private IDocumentResourceGetterManagerLocal documentResourceGetterService;
    @Mock
    private IDocumentPostUploaderManagerLocal documentPostUploaderService;


    private String workspaceId;
    private  String documentId;
    private String version;
    private String iteration;
    private  String fileName;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testUploadDocumentFile() throws Exception {

    }

    @Test
    @Parameterized.Parameters
    public void testDownloadDocumentFile() throws Exception {

        String fullName = workspaceId + "/documents/" + documentId + "/" + version + "/" + iteration + "/" + fileName;
        BinaryResource binaryResource = documentService.getBinaryResource(fullName);

    }
}