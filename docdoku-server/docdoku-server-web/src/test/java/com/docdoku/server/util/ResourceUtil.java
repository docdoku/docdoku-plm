package com.docdoku.server.util;

/**
 * Created by asmae on 07/01/15.
 */
public class ResourceUtil {
    public static final String WORKSPACE_ID="TestWorkspace";
    public static final String DOCUMENT_ID="TestDocument";
    public static final String VERSION="A";
    public static final int ITERATION=1;
    public static final String SOURCE_FILE_STORAGE="docdoku-server/docdoku-server-web/src/test/resources/com/docdoku/server/rest/file/toUpload/";
    public static final String TARGET_FILE_STORAGE="docdoku-server/docdoku-server-web/src/test/resources/com/docdoku/server/rest/file/uploaded/";
    public static final String SOURCE_FILENAME1="/upload_0000000_0000000000__0000_00000001.tmp";
    public static final String SOURCE_FILENAME2_1="/upload_0000000_0000000000__0000_00000002.tmp";
    public static final String SOURCE_FILENAME2_3="/upload_0000000_0000000000__0000_00000003.tmp";
    public static final String FILENAME1= "TestFile.txt";
    public static final String FILENAME2="TestFile_With_éàè.txt";
    public static final String FILENAME3="TestFile_3.txt";
    public static final String FILENAME3_2="TestFile3.txt";
    public static final String DOC_TEMPLATE_ID ="temp_01" ;
    public static final String FILE_TYPE = "application/pdf";
    public static final String SHARED_ENTITY_UUID ="documents/share/shareuuid01";
    public static final String RANGE= "bytes=0-366828";
    public static final long DOCUMENT_SIZE = 26 ;
    public static final String PART_TEMPLATE_ID = "part_templ_01";
    public static final String TEST_PART_FILENAME1 = "part_file_test1.txt";
    public static final String TEST_PART_FILENAME2 = "part_file_test1.txt";
    public static final String VIRTUAL_SUB_RESOURCE = "scormFile.zip";
    public static final String REFER ="refers/073dd114-e13b-46a9-a348-2c61138aba20" ;
    public static final String PART_NUMBER = "PART01";
    public static final String SUBTYPE ="obj" ;

    public static String FILENAME_TO_UPLOAD_PART_SPECIAL_CHARACTER="part_file_to_upload-èé_spécial&.txt";
    public static final long PART_SIZE =363666 ;
    public static final String TARGET_PART_STORAGE = "docdoku-server/docdoku-server-web/src/test/resources/com/docdoku/server/rest/part/uploaded/";
    public static final String SOURCE_PART_STORAGE = "docdoku-server/docdoku-server-web/src/test/resources/com/docdoku/server/rest/part/toUpload/";
    public static String FILENAME_TARGET_PART ="new_part_file.txt";
    public static String FILENAME_TARGET_PART_SPECIAL_CHARACTERS ="new_part_fileÉ'' .txt";

}
