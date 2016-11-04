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
import com.docdoku.api.models.DocumentCreationDTO;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.models.SharedDocumentDTO;
import com.docdoku.api.services.DocumentApi;
import com.docdoku.api.services.DocumentsApi;
import com.docdoku.api.services.FoldersApi;
import com.docdoku.api.services.SharedApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.UUID;

@RunWith(JUnit4.class)
public class SharedDocumentApiTest {


    @Test
    public void privateDocumentShareTests() throws ApiException {

        FoldersApi foldersApi = new FoldersApi(TestConfig.REGULAR_USER_CLIENT);
        DocumentsApi documentsApi = new DocumentsApi(TestConfig.REGULAR_USER_CLIENT);
        SharedApi sharedApi = new SharedApi(TestConfig.GUEST_CLIENT);

        // Create a document
        DocumentCreationDTO documentCreationDTO = new DocumentCreationDTO();
        documentCreationDTO.setReference(UUID.randomUUID().toString().substring(0, 8));
        documentCreationDTO.setTitle("PublicDoc");

        DocumentRevisionDTO document = foldersApi.createDocumentMasterInFolder(TestConfig.WORKSPACE, documentCreationDTO, TestConfig.WORKSPACE);
        document = documentsApi.checkInDocument(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion(), "");

        // Try guest access (should fail)
        try {
            sharedApi.getPublicSharedDocumentRevision(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion());
        } catch (ApiException e) {
            int statusCode = TestConfig.GUEST_CLIENT.getStatusCode();
            Assert.assertEquals(403, statusCode);
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
        documentCreationDTO.setReference(UUID.randomUUID().toString().substring(0, 8));
        documentCreationDTO.setTitle("PublicDoc");

        DocumentRevisionDTO document = foldersApi.createDocumentMasterInFolder(TestConfig.WORKSPACE, documentCreationDTO, TestConfig.WORKSPACE);
        document = documentsApi.checkInDocument(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion(), "");

        // Try guest access (should fail)
        try{
            sharedApi.getPublicSharedDocumentRevision(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion());
        } catch (ApiException e){
            int statusCode = TestConfig.GUEST_CLIENT.getStatusCode();
            Assert.assertEquals(403, statusCode);
        }

        // publish
        documentApi.publishDocumentRevision(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion(),"");

        // Try guest access with document key
        DocumentRevisionDTO documentWithSharedEntity = sharedApi.getPublicSharedDocumentRevision(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion());
        Assert.assertEquals(document.getDocumentMasterId(), documentWithSharedEntity.getDocumentMasterId());

        // un publish
        documentApi.unPublishDocumentRevision(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion(), "");

        // Try guest access (should fail)
        try{
            sharedApi.getPublicSharedDocumentRevision(document.getWorkspaceId(), document.getDocumentMasterId(), document.getVersion());
        } catch (ApiException e){
            int statusCode = TestConfig.GUEST_CLIENT.getStatusCode();
            Assert.assertEquals(403, statusCode);
        }
    }



}
