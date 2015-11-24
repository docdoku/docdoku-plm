/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.server;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.server.storage.StorageProvider;
import com.docdoku.server.storage.filesystem.FileStorageProvider;
import com.docdoku.server.util.DocumentUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.util.Date;


public class DataManagerBeanTest {

    public static final String TARGET_FILE_STORAGE="";

    private StorageProvider defaultStorageProvider;
    private BinaryResource binaryResource;

    @Before
    public void setUp() throws Exception {
        //Given
        defaultStorageProvider = new FileStorageProvider(System.getProperty("java.io.tmpdir")+TARGET_FILE_STORAGE);
        binaryResource = new BinaryResource(DocumentUtil.FULL_NAME4,DocumentUtil.DOCUMENT_SIZE,new Date());
    }

    @Test
    public void testGetBinaryResourceOutputStream() throws Exception {
        //When
        BufferedOutputStream outputStream = (BufferedOutputStream)defaultStorageProvider.getBinaryResourceOutputStream(binaryResource);
        //Then
        Assert.assertTrue(outputStream != null);
    }

}