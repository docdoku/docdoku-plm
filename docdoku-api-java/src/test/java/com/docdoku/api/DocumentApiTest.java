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
import com.docdoku.api.models.*;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.models.utils.UploadDownloadHelper;
import com.docdoku.api.services.DocumentApi;
import com.docdoku.api.services.DocumentsApi;
import com.docdoku.api.services.FoldersApi;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@RunWith(JUnit4.class)
public class DocumentApiTest {

    private DocumentApi documentApi = new DocumentApi(TestConfig.BASIC_CLIENT);
    private DocumentsApi documentsApi = new DocumentsApi(TestConfig.BASIC_CLIENT);
    private FoldersApi foldersApi = new FoldersApi(TestConfig.BASIC_CLIENT);

    @Test
    public void documentApiUsageTests() throws ApiException {

        // Create a document
        DocumentCreationDTO document = new DocumentCreationDTO();
        document.setReference(UUID.randomUUID().toString().substring(0, 8));
        document.setTitle("GeneratedDoc");

        DocumentRevisionDTO createdDocument = foldersApi.createDocumentMasterInFolder(TestConfig.WORKSPACE, document, TestConfig.WORKSPACE);
        Assert.assertEquals(createdDocument.getDocumentMasterId(), document.getReference());

        // Check in
        DocumentRevisionDTO checkedInDocument = documentApi.checkInDocument(TestConfig.WORKSPACE, createdDocument.getDocumentMasterId(), createdDocument.getVersion(), "");
        Assert.assertEquals(checkedInDocument.getDocumentMasterId(), document.getReference());
        Assert.assertEquals(LastIterationHelper.getLastIteration(checkedInDocument).getIteration(), Integer.valueOf("1"));

        // Check out
        DocumentRevisionDTO checkedOutDocument = documentApi.checkOutDocument(TestConfig.WORKSPACE, createdDocument.getDocumentMasterId(), createdDocument.getVersion(), "");
        Assert.assertEquals(checkedOutDocument.getDocumentMasterId(), document.getReference());
        Assert.assertEquals(LastIterationHelper.getLastIteration(checkedOutDocument).getIteration(), Integer.valueOf("2"));

        // Undo check out
        DocumentRevisionDTO undoCheckOutDocument = documentApi.undoCheckOutDocument(TestConfig.WORKSPACE, createdDocument.getDocumentMasterId(), createdDocument.getVersion(), "");
        Assert.assertEquals(undoCheckOutDocument, checkedInDocument);

        // Check out
        checkedOutDocument = documentApi.checkOutDocument(TestConfig.WORKSPACE, createdDocument.getDocumentMasterId(), createdDocument.getVersion(), "");

        // Edit
        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(checkedOutDocument);
        Assert.assertNull(lastIteration.getModificationDate());
        lastIteration.setRevisionNote("Something modified");

        DocumentIterationDTO updatedIteration = documentApi.updateDocumentIteration(TestConfig.WORKSPACE, checkedOutDocument.getDocumentMasterId(), checkedOutDocument.getVersion(), "2", lastIteration);

        Assert.assertNotNull(updatedIteration.getModificationDate());
        lastIteration.setModificationDate(updatedIteration.getModificationDate());
        Assert.assertEquals(lastIteration, updatedIteration);

        // Check in
        checkedInDocument = documentApi.checkInDocument(TestConfig.WORKSPACE, createdDocument.getDocumentMasterId(), createdDocument.getVersion(), "");
        Assert.assertNull(checkedInDocument.getCheckOutUser());

        // Release
        DocumentRevisionDTO releasedDocument = documentApi.releaseDocumentRevision(TestConfig.WORKSPACE, checkedInDocument.getDocumentMasterId(), checkedInDocument.getVersion(), "");
        Assert.assertEquals(releasedDocument.getStatus(), DocumentRevisionDTO.StatusEnum.RELEASED);

        // Mark as obsolete
        DocumentRevisionDTO obsoleteDocument = documentApi.markDocumentRevisionAsObsolete(TestConfig.WORKSPACE, releasedDocument.getDocumentMasterId(), releasedDocument.getVersion(), "");
        Assert.assertEquals(obsoleteDocument.getStatus(), DocumentRevisionDTO.StatusEnum.OBSOLETE);

    }

    @Test
    public void uploadDownloadAttachedFilesToDocumentTest() throws ApiException, IOException {

        // Create a document
        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(UUID.randomUUID().toString().substring(0, 8));
        documentCreation.setTitle("GeneratedDoc");

        DocumentRevisionDTO document = foldersApi.createDocumentMasterInFolder(TestConfig.WORKSPACE, documentCreation, TestConfig.WORKSPACE);

        URL fileURL = DocumentApiTest.class.getClassLoader().getResource("com/docdoku/api/attached-file.md");
        File file = new File(fileURL.getPath());

        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(document);
        UploadDownloadHelper.uploadAttachedFile(lastIteration, TestConfig.BASIC_CLIENT, file);

        document = documentApi.getDocumentRevision(TestConfig.WORKSPACE, document.getDocumentMasterId(), document.getVersion());

        lastIteration = LastIterationHelper.getLastIteration(document);
        Assert.assertFalse(lastIteration.getAttachedFiles().isEmpty());
        BinaryResourceDTO binaryResourceDTO = lastIteration.getAttachedFiles().get(0);
        File downloadedFile = UploadDownloadHelper.downloadFile(binaryResourceDTO.getFullName(), TestConfig.BASIC_CLIENT);
        Assert.assertTrue(FileUtils.contentEquals(file, downloadedFile));

    }

    @Test
    public void createDocumentAndSearch() throws ApiException, IOException, InterruptedException {

        // Create a document
        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(UUID.randomUUID().toString().substring(0, 8));
        documentCreation.setTitle(UUID.randomUUID().toString().substring(0, 8));

        DocumentRevisionDTO document = foldersApi.createDocumentMasterInFolder(TestConfig.WORKSPACE, documentCreation, TestConfig.WORKSPACE);

        String attrName = UUID.randomUUID().toString().substring(0, 8);
        String attrValue = UUID.randomUUID().toString().substring(0, 8);

        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(document);
        InstanceAttributeDTO attribute = new InstanceAttributeDTO();
        attribute.setName(attrName);
        attribute.setType(InstanceAttributeDTO.TypeEnum.TEXT);
        attribute.setValue(attrValue);
        lastIteration.getInstanceAttributes().add(attribute);
        documentApi.updateDocumentIteration(TestConfig.WORKSPACE, documentCreation.getReference(), "A", "1", lastIteration);
        documentApi.checkInDocument(TestConfig.WORKSPACE, documentCreation.getReference(), "A", "");

        String attributeSearchQuery = "TEXT:"+attrName+":"+attrValue;

        // Let some time to server for data indexing (asynchronous)
        Thread.sleep(2000);

        // search by attributes
        List<DocumentRevisionDTO> documentRevisions = documentsApi.searchDocumentRevision(TestConfig.WORKSPACE, null, null, null, null, null, null, null, null, null, null, null, null, attributeSearchQuery, null);
        Assert.assertEquals(1, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getReference().equals(documentRevisionDTO.getDocumentMasterId()))
                .count());


        // search by title
        documentRevisions = documentsApi.searchDocumentRevision(TestConfig.WORKSPACE, null, null, documentCreation.getTitle(), null, null, null, null, null, null, null, null, null, null, null);
        Assert.assertEquals(1, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getTitle().equals(documentRevisionDTO.getTitle()))
                .count());

        // search by author
        documentRevisions = documentsApi.searchDocumentRevision(TestConfig.WORKSPACE, null, null, null, null, null, TestConfig.LOGIN, null, null, null, null, null, null, null, null);
        Assert.assertFalse(documentRevisions.isEmpty());
        Assert.assertEquals(documentRevisions.size(), documentRevisions.stream()
                .filter(documentRevisionDTO -> TestConfig.LOGIN.equals(documentRevisionDTO.getAuthor().getLogin()))
                .count());
    }

    @Test
    public void attributesAdvancedSearchTests() throws ApiException, InterruptedException {

        String attributeValue1 = UUID.randomUUID().toString().substring(0, 8);
        String attributeValue2 = UUID.randomUUID().toString().substring(0, 8);
        String attributeValue3 = UUID.randomUUID().toString().substring(0, 8);
        String attrName1 = UUID.randomUUID().toString().substring(0, 8);
        String attrName2 = UUID.randomUUID().toString().substring(0, 8);

        DocumentRevisionDTO document1 = createDocumentWithAttributes(attrName1, attrName2, attributeValue1);
        DocumentRevisionDTO document2 = createDocumentWithAttributes(attrName1, attrName2, attributeValue2);
        DocumentRevisionDTO document3 = createDocumentWithAttributes(attrName1, attrName2, attributeValue3);

        Thread.sleep(2000);

        List<DocumentRevisionDTO> documentRevisions;
        String attributeQuery;

        // Simple attribute query
        attributeQuery = "TEXT:" + attrName1 + ":" + attributeValue1;
        documentRevisions = documentsApi.searchDocumentRevision(TestConfig.WORKSPACE, null, null, null, null, null, null, null, null, null, null, null, null, attributeQuery, null);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document1.getId())).count());
        Assert.assertEquals(1, documentRevisions.size());

        // Simple attribute query
        attributeQuery = "TEXT:" + attrName1 + ":" + attributeValue2;
        documentRevisions = documentsApi.searchDocumentRevision(TestConfig.WORKSPACE, null, null, null, null, null, null, null, null, null, null, null, null, attributeQuery, null);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document2.getId())).count());
        Assert.assertEquals(1, documentRevisions.size());

        // Simple attribute query
        attributeQuery = "TEXT:" + attrName1 + ":" + attributeValue3;
        documentRevisions = documentsApi.searchDocumentRevision(TestConfig.WORKSPACE, null, null, null, null, null, null, null, null, null, null, null, null, attributeQuery, null);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document3.getId())).count());
        Assert.assertEquals(1, documentRevisions.size());

        // "Or" query on same attribute
        attributeQuery = "TEXT:" + attrName1 + ":" + attributeValue1 + ";" + "TEXT:" + attrName1 + ":" + attributeValue2;
        documentRevisions = documentsApi.searchDocumentRevision(TestConfig.WORKSPACE, null, null, null, null, null, null, null, null, null, null, null, null, attributeQuery, null);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document1.getId())).count());
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document2.getId())).count());
        Assert.assertEquals(2, documentRevisions.size());

        // Something not existing
        attributeQuery = "TEXT:" + attrName1 + ":something that doesnt exist";
        documentRevisions = documentsApi.searchDocumentRevision(TestConfig.WORKSPACE, null, null, null, null, null, null, null, null, null, null, null, null, attributeQuery, null);
        Assert.assertTrue(documentRevisions.isEmpty());

        // Simple "AND" on 2 different attributes
        attributeQuery = "TEXT:" + attrName1 + ":" + attributeValue1 + ";TEXT:" + attrName2 + ":" + attributeValue1;
        documentRevisions = documentsApi.searchDocumentRevision(TestConfig.WORKSPACE, null, null, null, null, null, null, null, null, null, null, null, null, attributeQuery, null);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document1.getId())).count());
        Assert.assertEquals(1, documentRevisions.size());


        // Mix "AND" and "OR" on 2 same attribute
        attributeQuery =
                InstanceAttributeDTO.TypeEnum.TEXT + ":" + attrName1 + ":" + attributeValue1 + ";" +
                        InstanceAttributeDTO.TypeEnum.TEXT + ":" + attrName1 + ":" + attributeValue2 + ";" +
                        InstanceAttributeDTO.TypeEnum.TEXT + ":" + attrName2 + ":" + attributeValue1;

        documentRevisions = documentsApi.searchDocumentRevision(TestConfig.WORKSPACE, null, null, null, null, null, null, null, null, null, null, null, null, attributeQuery, null);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document1.getId())).count());
        Assert.assertEquals(1, documentRevisions.size());


        // Mix 2 "OR" in a "AND"
        attributeQuery = InstanceAttributeDTO.TypeEnum.TEXT + ":" + attrName1 + ":" + attributeValue1 + ";" +
                InstanceAttributeDTO.TypeEnum.TEXT + ":" + attrName1 + ":" + attributeValue2 + ";" +
                InstanceAttributeDTO.TypeEnum.TEXT + ":" + attrName2 + ":" + attributeValue1 + ";" +
                InstanceAttributeDTO.TypeEnum.TEXT + ":" + attrName2 + ":" + attributeValue2;

        documentRevisions = documentsApi.searchDocumentRevision(TestConfig.WORKSPACE, null, null, null, null, null, null, null, null, null, null, null, null, attributeQuery, null);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document1.getId())).count());
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document2.getId())).count());
        Assert.assertEquals(2, documentRevisions.size());


    }

    @Test
    public void folderAdvancedSearch() throws ApiException, InterruptedException {

        String folder1 = UUID.randomUUID().toString().substring(0, 8);
        String folder2 = UUID.randomUUID().toString().substring(0, 8);
        DocumentRevisionDTO document1 = createDocumentsInFolder(folder1);
        DocumentRevisionDTO document2 = createDocumentsInFolder(folder2);

        Thread.sleep(2000);

        List<DocumentRevisionDTO> documentRevisions;

        documentRevisions = documentsApi.searchDocumentRevision(TestConfig.WORKSPACE, null, null, null, null, null, null, null, null, null, null, null, null, null, folder1);
        Assert.assertEquals(1, documentRevisions
                .stream().filter(d -> d.getId().equals(document1.getId())).count());
        Assert.assertEquals(1, documentRevisions.size());

        documentRevisions = documentsApi.searchDocumentRevision(TestConfig.WORKSPACE, null, null, null, null, null, null, null, null, null, null, null, null, null, folder2);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document2.getId())).count());
        Assert.assertEquals(1, documentRevisions.size());

        documentRevisions = documentsApi.searchDocumentRevision(TestConfig.WORKSPACE, null, null, null, null, null, null, null, null, null, null, null, null, null, "NonExistingFolder");
        Assert.assertTrue(documentRevisions.isEmpty());

    }

    private DocumentRevisionDTO createDocumentsInFolder(String folderName) throws ApiException {

        FolderDTO folderDTO = new FolderDTO();
        folderDTO.setName(folderName);
        foldersApi.createSubFolder(TestConfig.WORKSPACE, TestConfig.WORKSPACE, folderDTO);

        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(UUID.randomUUID().toString().substring(0, 8));
        foldersApi.createDocumentMasterInFolder(TestConfig.WORKSPACE, documentCreation, TestConfig.WORKSPACE+":"+folderName);
        return documentApi.checkInDocument(TestConfig.WORKSPACE, documentCreation.getReference(), "A", "");
    }

    private DocumentRevisionDTO createDocumentWithAttributes(String attrName1, String attrName2, String attrValue) throws ApiException {

        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(UUID.randomUUID().toString().substring(0, 8));
        DocumentRevisionDTO document = foldersApi.createDocumentMasterInFolder(TestConfig.WORKSPACE, documentCreation, TestConfig.WORKSPACE);
        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(document);

        InstanceAttributeDTO attribute1 = new InstanceAttributeDTO();
        attribute1.setName(attrName1);
        attribute1.setType(InstanceAttributeDTO.TypeEnum.TEXT);
        attribute1.setValue(attrValue);

        InstanceAttributeDTO attribute2 = new InstanceAttributeDTO();
        attribute2.setName(attrName2);
        attribute2.setType(InstanceAttributeDTO.TypeEnum.TEXT);
        attribute2.setValue(attrValue);

        List<InstanceAttributeDTO> instanceAttributes = lastIteration.getInstanceAttributes();
        instanceAttributes.add(attribute1);
        instanceAttributes.add(attribute2);

        documentApi.updateDocumentIteration(TestConfig.WORKSPACE, documentCreation.getReference(), "A", "1", lastIteration);
        return documentApi.checkInDocument(TestConfig.WORKSPACE, documentCreation.getReference(), "A", "");
    }


}
