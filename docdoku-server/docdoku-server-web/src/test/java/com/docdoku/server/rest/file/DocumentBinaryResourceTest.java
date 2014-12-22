package com.docdoku.server.rest.file;

import org.junit.Test;

public class DocumentBinaryResourceTest {
    private static final String WORKSPACE_ID="TestWorkspace";
    private static final String DOCUMENT_ID="TestDocument";
    private static final String VERSION="A";
    private static final int ITERATION=1;
    private static final String SOURCE_FILE_STORAGE="test/file/toUpload";
    private static final String FILE_STORAGE="test/file/uploaded";
    private static final String SOURCE_FILENAME1="/upload_0000000_0000000000__0000_00000001.tmp";
    private static final String SOURCE_FILENAME2_1="/upload_0000000_0000000000__0000_00000002.tmp";
    private static final String SOURCE_FILENAME2_3="/upload_0000000_0000000000__0000_00000003.tmp";
    private static final String FILENAME1="TestFile.txt";
    private static final String FILENAME2="TestFile_With_éàè.txt";
    private static final String FILENAME3_1="TestFile2.txt";
    private static final String FILENAME3_2="TestFile3.txt";

    /**
     * Test to upload a file to a document
     * @throws Exception
     */
    @Test
    public void testUploadDocumentFiles1() throws Exception {
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

            // assert uploaded file exist in FILE_STORAGE+"/"+FILENAME1 et length > 0
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

            // assert uploaded file exist in FILE_STORAGE+"/"+FILENAME2 et length > 0
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

            // assert uploaded file exist in FILE_STORAGE+"/"+FILENAME3_1  et length > 0
            // assert uploaded file exist in FILE_STORAGE+"/"+FILENAME3_2  et length > 0
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