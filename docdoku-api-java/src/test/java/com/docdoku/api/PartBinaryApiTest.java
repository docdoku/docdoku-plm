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

package com.docdoku.api;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.client.ApiResponse;
import com.docdoku.api.models.PartCreationDTO;
import com.docdoku.api.models.PartRevisionDTO;
import com.docdoku.api.services.PartBinaryApi;
import com.docdoku.api.services.PartsApi;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@RunWith(JUnit4.class)
public class PartBinaryApiTest {

    private PartBinaryApi partBinaryApi = new PartBinaryApi(TestConfig.REGULAR_USER_CLIENT);
    private static PartsApi partsApi = new PartsApi(TestConfig.REGULAR_USER_CLIENT);
    private static PartRevisionDTO createdPart;

    @BeforeClass
    public static void initPart() throws ApiException {
        PartCreationDTO part = new PartCreationDTO();
        part.setNumber(TestUtils.randomString());
        createdPart = partsApi.createNewPart(TestConfig.WORKSPACE, part);
    }

    @Test
    public void testSuite() throws ApiException, IOException {
        File originalAttached = partAttachedFileUpload();
        File downloadedAttached = partAttachedFileDownload();
        Assert.assertTrue(FileUtils.contentEquals(originalAttached, downloadedAttached));

        File originalCAD = partNativeCADUpload();
        File downloadedCAD = partNativeCADDownload();
        Assert.assertTrue(FileUtils.contentEquals(originalCAD, downloadedCAD));
    }

    private File partAttachedFileUpload() throws ApiException {

        URL fileURL = PartBinaryApiTest.class.getClassLoader().getResource("com/docdoku/api/attached-file.md");
        File file = new File(fileURL.getPath());

        ApiResponse<Void> response =
                partBinaryApi.uploadAttachedFilesWithHttpInfo(createdPart.getWorkspaceId(),
                        createdPart.getNumber(), createdPart.getVersion(), 1, file);

        String location = response.getHeaders().get("Location").get(0);
        Assert.assertNotNull(location);

        return file;

    }

    private File partAttachedFileDownload() throws ApiException {
        return partBinaryApi.downloadPartFileWithSubtype(createdPart.getWorkspaceId(), createdPart.getNumber(),
                createdPart.getVersion(), 1, "attachedfiles", "attached-file.md", null, null, null);
    }

    private File partNativeCADUpload() throws ApiException {

        URL fileURL = PartBinaryApiTest.class.getClassLoader().getResource("com/docdoku/api/attached-file.md");
        File file = new File(fileURL.getPath());

        ApiResponse<Void> response =
                partBinaryApi.uploadNativeCADFileWithHttpInfo(createdPart.getWorkspaceId(),
                        createdPart.getNumber(), createdPart.getVersion(), 1, file);

        String location = response.getHeaders().get("Location").get(0);
        Assert.assertNotNull(location);

        return file;

    }

    private File partNativeCADDownload() throws ApiException {
        return partBinaryApi.downloadPartFileWithSubtype(createdPart.getWorkspaceId(), createdPart.getNumber(),
                createdPart.getVersion(), 1, "nativecad", "attached-file.md", null, null, null);
    }
}
