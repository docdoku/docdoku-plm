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
import com.docdoku.api.models.*;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.net.URL;

@RunWith(JUnit4.class)
public class HTMLViewerApiTest {

    private static WorkspaceDTO workspace;
    private static DocumentRevisionDTO document;
    private ViewerApi viewerApi = new ViewerApi(TestConfig.REGULAR_USER_CLIENT);
    private static final WorkspacesApi workspacesApi = new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT);
    private static final DocumentBinaryApi documentBinaryApi = new DocumentBinaryApi(TestConfig.REGULAR_USER_CLIENT);

    @BeforeClass
    public static void init() throws ApiException {

        workspace = TestUtils.createWorkspace();
        String folder = workspace.getId();
        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(TestUtils.randomString());

        document = new FoldersApi(TestConfig.REGULAR_USER_CLIENT)
                .createDocumentMasterInFolder(workspace.getId(), documentCreation, folder);


        URL fileURL = DocumentApiTest.class.getClassLoader().getResource("com/docdoku/api/office-document.odt");
        File docFile = new File(fileURL.getPath());

        documentBinaryApi.uploadDocumentFiles(workspace.getId(),
                document.getDocumentMasterId(), document.getVersion(), 1, docFile);

        document = new DocumentApi(TestConfig.REGULAR_USER_CLIENT)
                .getDocumentRevision(workspace.getId(), document.getDocumentMasterId(), document.getVersion());
    }

    @Test
    public void convertOfficeDocument() throws ApiException {

        // Validate template
        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(document);
        BinaryResourceDTO binaryResourceDTO = lastIteration.getAttachedFiles().get(0);
        String htmlViewerForFile = viewerApi.getHtmlViewerForFile(binaryResourceDTO.getFullName(), null);
        Assert.assertNotNull(htmlViewerForFile);
        Assert.assertFalse(htmlViewerForFile.isEmpty());

        // Validate pdf output
        File pdf = documentBinaryApi.downloadDocumentFile(document.getWorkspaceId(), document.getDocumentMasterId(),
                document.getVersion(), 1, "office-document.odt", "", null, null, null, "pdf");

        Assert.assertTrue(pdf.getName().endsWith(".pdf"));
    }

    @AfterClass
    public static void destroy() throws ApiException {
        workspacesApi.deleteWorkspace(workspace.getId());
    }

}
