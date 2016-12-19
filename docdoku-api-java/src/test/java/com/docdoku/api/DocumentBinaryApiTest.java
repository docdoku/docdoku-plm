/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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
import com.docdoku.api.models.DocumentCreationDTO;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.services.DocumentBinaryApi;
import com.docdoku.api.services.FoldersApi;
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
public class DocumentBinaryApiTest {

    private DocumentBinaryApi documentBinaryApi = new DocumentBinaryApi(TestConfig.REGULAR_USER_CLIENT);
    private static FoldersApi foldersApi = new FoldersApi(TestConfig.REGULAR_USER_CLIENT);
    private static DocumentRevisionDTO createdDocument;

    @BeforeClass
    public static void initDocument() throws ApiException {

        DocumentCreationDTO document = new DocumentCreationDTO();
        document.setReference(TestUtils.randomString());
        document.setTitle("GeneratedDoc");
        createdDocument =
                foldersApi.createDocumentMasterInFolder(TestConfig.WORKSPACE, document, TestConfig.WORKSPACE);

    }

    @Test
    public void testSuite() throws ApiException, IOException {
        File original = documentUpload();
        File downloaded = documentDownload();
        Assert.assertTrue(FileUtils.contentEquals(original, downloaded));
    }

    private File documentUpload() throws ApiException {

        URL fileURL = DocumentApiTest.class.getClassLoader().getResource("com/docdoku/api/attached-file.md");
        File file = new File(fileURL.getPath());

        ApiResponse<Void> response =
                documentBinaryApi.uploadDocumentFilesWithHttpInfo(createdDocument.getWorkspaceId(),
                        createdDocument.getDocumentMasterId(), createdDocument.getVersion(), 1, file);

        String location = response.getHeaders().get("Location").get(0);
        Assert.assertNotNull(location);
        return file;

    }

    private File documentDownload() throws ApiException {
        return documentBinaryApi.downloadDocumentFile(createdDocument.getWorkspaceId(), createdDocument.getDocumentMasterId(),
                createdDocument.getVersion(), 1, "attached-file.md", "", null, null, null, null);
    }

}
