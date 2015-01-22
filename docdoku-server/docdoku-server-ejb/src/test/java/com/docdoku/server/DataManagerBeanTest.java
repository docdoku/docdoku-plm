package com.docdoku.server;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.server.storage.StorageProvider;
import com.docdoku.server.storage.filesystem.FileStorageProvider;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.Mockito;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;


public class DataManagerBeanTest {

    public static final String TARGET_FILE_STORAGE="docdoku-server/docdoku-server-ejb/src/test/resources/com/docdoku/server/file/uploaded";

    public static final String FULLNAME= "TestFile.txt";
    private StorageProvider defaultStorageProvider;
    private FileStorageProvider fileStorageProvider;

    @Before
    public void setUp() throws Exception {

        defaultStorageProvider = new FileStorageProvider(TARGET_FILE_STORAGE);
    }

    @Test
    public void testGetBinaryResourceOutputStream() throws Exception {
        //Given
        BinaryResource binaryResource = Mockito.spy(new BinaryResource( FULLNAME, 22,new Date()));
        //When
        BufferedOutputStream outputStream = (BufferedOutputStream)defaultStorageProvider.getBinaryResourceOutputStream(binaryResource);
        //Then
        Assert.assertTrue(outputStream != null);
    }





}