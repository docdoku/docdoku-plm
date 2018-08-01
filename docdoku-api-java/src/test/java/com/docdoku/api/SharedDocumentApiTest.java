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

import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RunWith(JUnit4.class)
public class SharedDocumentApiTest {

    private static WorkspaceDTO workspace;

    @BeforeClass
    public static void initWorkspace() throws ApiException {
        workspace = TestUtils.createWorkspace(SharedDocumentApiTest.class.getName());
    }

    @AfterClass
    public static void deleteWorkspace() throws ApiException {
        TestUtils.deleteWorkspace(workspace);
    }

    @Test
    public void privateDocumentShareTests() throws ApiException {

        FoldersApi foldersApi = new FoldersApi(TestConfig.REGULAR_USER_CLIENT);
        DocumentsApi documentsApi = new DocumentsApi(TestConfig.REGULAR_USER_CLIENT);
        SharedApi sharedApi = new SharedApi(TestConfig.GUEST_CLIENT);

        // Create a document
        DocumentCreationDTO documentCreationDTO = new DocumentCreationDTO();
        documentCreationDTO.setReference(TestUtils.randomString());
        documentCreationDTO.setTitle("PublicDoc");

        DocumentRevisionDTO document = foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreationDTO, workspace.getId());
        document = documentsApi.checkInDocument(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion());

        // Try guest access (should fail)
        try {
            sharedApi.getPublicSharedDocumentRevision(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion());
            Assert.fail("Should have throw an exception with forbidden http response");
        } catch (ApiException e) {
            Assert.assertEquals(403, e.getCode());
        }

        // Create a private share
        SharedDocumentDTO sharedDocumentDTO = new SharedDocumentDTO();
        SharedDocumentDTO sharedDocument =
                documentsApi.createSharedDocument(document.getWorkspaceId(), document.getDocumentMasterId(),
                        document.getVersion(), sharedDocumentDTO);


        // Try guest access with shared entity uuid
        DocumentRevisionDTO documentWithSharedEntity = sharedApi.getDocumentWithSharedEntity(sharedDocument.getUuid(), null);
        Assert.assertEquals(document.getDocumentMasterId(), documentWithSharedEntity.getDocumentMasterId());

    }

    @Test
    public void publicDocumentShareTests() throws ApiException {

        FoldersApi foldersApi = new FoldersApi(TestConfig.REGULAR_USER_CLIENT);
        DocumentApi documentApi = new DocumentApi(TestConfig.REGULAR_USER_CLIENT);
        DocumentsApi documentsApi = new DocumentsApi(TestConfig.REGULAR_USER_CLIENT);
        SharedApi sharedApi = new SharedApi(TestConfig.GUEST_CLIENT);

        // Create a document
        DocumentCreationDTO documentCreationDTO = new DocumentCreationDTO();
        documentCreationDTO.setReference(TestUtils.randomString());
        documentCreationDTO.setTitle("PublicDoc");

        DocumentRevisionDTO document = foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreationDTO, workspace.getId());
        document = documentsApi.checkInDocument(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion());

        // Try guest access (should fail)
        try {
            sharedApi.getPublicSharedDocumentRevision(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion());
            Assert.fail("Should have throw an exception with forbidden http response");
        } catch (ApiException e) {
            Assert.assertEquals(403, e.getCode());
        }

        // publish
        documentApi.publishDocumentRevision(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion());

        // Try guest access with document key
        DocumentRevisionDTO documentWithSharedEntity = sharedApi.getPublicSharedDocumentRevision(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion());
        Assert.assertEquals(document.getDocumentMasterId(), documentWithSharedEntity.getDocumentMasterId());

        // un publish
        documentApi.unPublishDocumentRevision(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion());

        // Try guest access (should fail)
        try {
            sharedApi.getPublicSharedDocumentRevision(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion());
            Assert.fail("Should have throw an exception with forbidden http response");
        } catch (ApiException e) {
            Assert.assertEquals(403, e.getCode());
        }
    }


    @Test
    public void passwordProtectedSharedDocumentFilesDownloadTest() throws ApiException {

        FoldersApi foldersApi = new FoldersApi(TestConfig.REGULAR_USER_CLIENT);
        DocumentApi documentApi = new DocumentApi(TestConfig.REGULAR_USER_CLIENT);
        DocumentsApi documentsApi = new DocumentsApi(TestConfig.REGULAR_USER_CLIENT);
        DocumentBinaryApi documentBinaryApi = new DocumentBinaryApi(TestConfig.REGULAR_USER_CLIENT);
        SharedApi sharedApi = new SharedApi(TestConfig.GUEST_CLIENT);
        URL fileURL = DocumentApiTest.class.getClassLoader().getResource("com/docdoku/api/attached-file.md");
        Assert.assertNotNull(fileURL);
        File file = new File(fileURL.getPath());

        // Create a document
        DocumentCreationDTO documentCreationDTO = new DocumentCreationDTO();
        documentCreationDTO.setReference(TestUtils.randomString());
        documentCreationDTO.setTitle("PublicDoc");
        DocumentRevisionDTO document = foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreationDTO, workspace.getId());
        documentBinaryApi.uploadDocumentFiles(workspace.getId(), documentCreationDTO.getReference(), "A", 1, file);
        document = documentsApi.checkInDocument(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion());

        // Create a private share
        SharedDocumentDTO sharedDocumentDTO = new SharedDocumentDTO();
        sharedDocumentDTO.setPassword("password");
        SharedDocumentDTO sharedDocument =
                documentsApi.createSharedDocument(document.getWorkspaceId(), document.getDocumentMasterId(),
                        document.getVersion(), sharedDocumentDTO);

        // Try guest access with shared entity uuid
        // should fail without password
        try {
            sharedApi.getDocumentWithSharedEntity(sharedDocument.getUuid(), null);
            Assert.fail("Should have throw an exception with forbidden http response");
        } catch (ApiException e) {
            Assert.assertEquals(403, e.getCode());
        }

        // Try guest access with shared entity uuid
        // should fail with wrong password
        try {
            sharedApi.getDocumentWithSharedEntity(sharedDocument.getUuid(), "wrong-password");
            Assert.fail("Should have throw an exception with forbidden http response");
        } catch (ApiException e) {
            Assert.assertEquals(403, e.getCode());
        }

        DocumentRevisionDTO privateDocument = sharedApi.getDocumentWithSharedEntity(sharedDocument.getUuid(), "password");
        Assert.assertNotNull(privateDocument);

        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(privateDocument);
        List<BinaryResourceDTO> attachedFiles = lastIteration.getAttachedFiles();

        for (BinaryResourceDTO binaryResourceDTO : attachedFiles) {
            String name = binaryResourceDTO.getName();

            try {
                documentBinaryApi.downloadDocumentFile(workspace.getId(), privateDocument.getDocumentMasterId(),
                        privateDocument.getVersion(), lastIteration.getIteration(), name, null, null, null, sharedDocument.getUuid(), null, null);
                Assert.fail("Should have throw an exception with bad request http response");
            } catch (ApiException e) {
                Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), e.getCode());
            }

            try {
                documentBinaryApi.downloadDocumentFile(workspace.getId(), privateDocument.getDocumentMasterId(),
                        privateDocument.getVersion(), lastIteration.getIteration(), name, null, null, null, sharedDocument.getUuid(), "wrong-password", null);
                Assert.fail("Should have throw an exception with bad request http response");
            } catch (ApiException e) {
                Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), e.getCode());
            }

            File downloadedFile = documentBinaryApi.downloadDocumentFile(workspace.getId(), privateDocument.getDocumentMasterId(),
                    privateDocument.getVersion(), lastIteration.getIteration(), name, null, null, null, sharedDocument.getUuid(), "password", null);

            Assert.assertNotNull(downloadedFile);

        }

    }

    @Test
    public void expiredSharedDocumentFilesDownloadTest() throws ApiException {

        FoldersApi foldersApi = new FoldersApi(TestConfig.REGULAR_USER_CLIENT);
        DocumentApi documentApi = new DocumentApi(TestConfig.REGULAR_USER_CLIENT);
        DocumentsApi documentsApi = new DocumentsApi(TestConfig.REGULAR_USER_CLIENT);
        DocumentBinaryApi documentBinaryApi = new DocumentBinaryApi(TestConfig.REGULAR_USER_CLIENT);
        SharedApi sharedApi = new SharedApi(TestConfig.GUEST_CLIENT);
        URL fileURL = DocumentApiTest.class.getClassLoader().getResource("com/docdoku/api/attached-file.md");
        Assert.assertNotNull(fileURL);
        File file = new File(fileURL.getPath());

        // Create a document
        DocumentCreationDTO documentCreationDTO = new DocumentCreationDTO();
        documentCreationDTO.setReference(TestUtils.randomString());
        documentCreationDTO.setTitle("PublicDoc");
        DocumentRevisionDTO document = foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreationDTO, workspace.getId());
        documentBinaryApi.uploadDocumentFiles(workspace.getId(), documentCreationDTO.getReference(), "A", 1, file);
        document = documentsApi.checkInDocument(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion());

        // Calculate dates
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();
        cal.add(Calendar.DATE, 2);
        Date tomorrow = cal.getTime();

        // Create a private share for tomorrow
        SharedDocumentDTO sharedDocumentDTO = new SharedDocumentDTO();
        sharedDocumentDTO.setExpireDate(tomorrow);
        SharedDocumentDTO sharedDocument =
                documentsApi.createSharedDocument(document.getWorkspaceId(), document.getDocumentMasterId(),
                        document.getVersion(), sharedDocumentDTO);

        DocumentRevisionDTO privateDocument = sharedApi.getDocumentWithSharedEntity(sharedDocument.getUuid(), null);
        Assert.assertNotNull(privateDocument);

        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(privateDocument);
        List<BinaryResourceDTO> attachedFiles = lastIteration.getAttachedFiles();

        for (BinaryResourceDTO binaryResourceDTO : attachedFiles) {
            String name = binaryResourceDTO.getName();
            File downloadedFile = documentBinaryApi.downloadDocumentFile(workspace.getId(), privateDocument.getDocumentMasterId(),
                    privateDocument.getVersion(), lastIteration.getIteration(), name, null, null, null, null, sharedDocument.getUuid(), null);
            Assert.assertNotNull(downloadedFile);
        }


        // Create a private share for yesterday
        sharedDocumentDTO.setExpireDate(yesterday);
        sharedDocument =
                documentsApi.createSharedDocument(document.getWorkspaceId(), document.getDocumentMasterId(),
                        document.getVersion(), sharedDocumentDTO);

        // should get a forbidden exception
        try {
            sharedApi.getDocumentWithSharedEntity(sharedDocument.getUuid(), null);
            Assert.fail("Should have throw an exception with forbidden http response");
        } catch (ApiException e) {
            Assert.assertEquals(404, e.getCode());
        }

        for (BinaryResourceDTO binaryResourceDTO : attachedFiles) {
            try {
                String name = binaryResourceDTO.getName();
                documentBinaryApi.downloadDocumentFile(workspace.getId(), privateDocument.getDocumentMasterId(),
                        privateDocument.getVersion(), lastIteration.getIteration(), name, null, null, null, sharedDocument.getUuid(), null, null);
                Assert.fail("Should have throw an exception with forbidden http response");
            } catch (ApiException e) {
                Assert.assertEquals(404, e.getCode());
            }
        }

    }

}
