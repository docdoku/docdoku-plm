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
    private static final String FILENAME1="TestFile_With_éàè.odt";
    private static final String FILENAME2_1="TestFile2_With_éàè.odt";
    private static final String FILENAME2_3="TestFile3_With_éàè.odt";

    @Test
    public void testUploadDocumentFiles() throws Exception {
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


        //User Case2
            //workspaceId = WORKSPACE_ID
            //documentId = DOCUMENT_ID
            //version = VERSION
            //iteration = ITERATION
            //formParts = [{
            //                  fileName = FILENAME2_1,
            //                  repository= SOURCE_FILE_STORAGE,
            //                  tempFile= SOURCE_FILE_STORAGE+"/"+SOURCE_FILENAME2_1
            //              },{
            //                  fileName = FILENAME2_2,
            //                  repository= SOURCE_FILE_STORAGE,
            //                  tempFile= SOURCE_FILE_STORAGE+"/"+SOURCE_FILENAME2_1
            //              }
            //            ]
            //getBinaryResource(pk,filename,length) = {
            //                    fullName= WORKSPACE_ID+"/"+DOCUMENT_ID+"/"+VERSION+"/"+ITERATION+"/"+filename
            //                    contentLength=length
            //                    lastModified = new Date();
            //                  }

            // assert uploaded file exist in FILE_STORAGE+"/"+FILENAME2_1  et length > 0
            // assert uploaded file exist in FILE_STORAGE+"/"+FILENAME2_2  et length > 0
            // assert response.status.code = "201"
            // assert response.getLocation().toString() = "/api/files/"+WORKSPACE_ID+"/"+DOCUMENT_ID+"/"+VERSION+"/"+ITERATION+"/"+FILENAME1
    }

    @Test
    public void testDownloadDocumentFile() throws Exception {

    }
}