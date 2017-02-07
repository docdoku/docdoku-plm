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

import com.docdoku.api.client.ApiCallback;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RunWith(JUnit4.class)
public class DocumentApiTest {

    private DocumentApi documentApi = new DocumentApi(TestConfig.REGULAR_USER_CLIENT);
    private DocumentsApi documentsApi = new DocumentsApi(TestConfig.REGULAR_USER_CLIENT);
    private FoldersApi foldersApi = new FoldersApi(TestConfig.REGULAR_USER_CLIENT);
    private AccountsApi accountsApi = new AccountsApi(TestConfig.REGULAR_USER_CLIENT);
    private UsersApi usersApi = new UsersApi(TestConfig.REGULAR_USER_CLIENT);
    private WorkflowModelsApi workflowModelsApi = new WorkflowModelsApi(TestConfig.REGULAR_USER_CLIENT);
    private WorkspacesApi workspacesApi = new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT);
    private RolesApi rolesApi = new RolesApi(TestConfig.REGULAR_USER_CLIENT);

    private static WorkspaceDTO workspace;

    @BeforeClass
    public static void initWorkspace() throws ApiException {
        workspace = TestUtils.createWorkspace();
    }

    @AfterClass
    public static void deleteWorkspace() throws ApiException {
        TestUtils.deleteWorkspace(workspace);
    }

    @Test
    public void documentApiUsageTests() throws ApiException {

        // Create a document
        DocumentCreationDTO document = new DocumentCreationDTO();
        document.setReference(TestUtils.randomString());
        document.setTitle("GeneratedDoc");

        DocumentRevisionDTO createdDocument = foldersApi.createDocumentMasterInFolder(workspace.getId(), document, workspace.getId());
        Assert.assertEquals(createdDocument.getDocumentMasterId(), document.getReference());

        // Check in
        DocumentRevisionDTO checkedInDocument = documentApi.checkInDocument(workspace.getId(), createdDocument.getDocumentMasterId(), createdDocument.getVersion());
        Assert.assertEquals(checkedInDocument.getDocumentMasterId(), document.getReference());
        Assert.assertEquals(Integer.valueOf("1"), LastIterationHelper.getLastIteration(checkedInDocument).getIteration());

        // Check out
        DocumentRevisionDTO checkedOutDocument = documentApi.checkOutDocument(workspace.getId(), createdDocument.getDocumentMasterId(), createdDocument.getVersion());
        Assert.assertEquals(checkedOutDocument.getDocumentMasterId(), document.getReference());
        Assert.assertEquals(Integer.valueOf("2"), LastIterationHelper.getLastIteration(checkedOutDocument).getIteration());

        // Undo check out
        DocumentRevisionDTO undoCheckOutDocument = documentApi.undoCheckOutDocument(workspace.getId(), createdDocument.getDocumentMasterId(), createdDocument.getVersion());
        Assert.assertEquals(undoCheckOutDocument, checkedInDocument);

        // Check out
        checkedOutDocument = documentApi.checkOutDocument(workspace.getId(), createdDocument.getDocumentMasterId(), createdDocument.getVersion());

        // Edit
        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(checkedOutDocument);
        Assert.assertNull(lastIteration.getModificationDate());
        lastIteration.setRevisionNote("Something modified");

        DocumentIterationDTO updatedIteration = documentApi.updateDocumentIteration(workspace.getId(), checkedOutDocument.getDocumentMasterId(), checkedOutDocument.getVersion(), "2", lastIteration);

        Assert.assertNotNull(updatedIteration.getModificationDate());
        lastIteration.setModificationDate(updatedIteration.getModificationDate());
        Assert.assertEquals(lastIteration, updatedIteration);

        // Check in
        checkedInDocument = documentApi.checkInDocument(workspace.getId(), createdDocument.getDocumentMasterId(), createdDocument.getVersion());
        Assert.assertNull(checkedInDocument.getCheckOutUser());

        // Release
        DocumentRevisionDTO releasedDocument = documentApi.releaseDocumentRevision(workspace.getId(), checkedInDocument.getDocumentMasterId(), checkedInDocument.getVersion());
        Assert.assertEquals(releasedDocument.getStatus(), DocumentRevisionDTO.StatusEnum.RELEASED);

        // Mark as obsolete
        DocumentRevisionDTO obsoleteDocument = documentApi.markDocumentRevisionAsObsolete(workspace.getId(), releasedDocument.getDocumentMasterId(), releasedDocument.getVersion());
        Assert.assertEquals(obsoleteDocument.getStatus(), DocumentRevisionDTO.StatusEnum.OBSOLETE);

    }

    @Test
    public void searchDocumentById() throws ApiException, IOException, InterruptedException {
        // Create a document
        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(TestUtils.randomString());
        documentCreation.setTitle(TestUtils.randomString());

        foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreation, workspace.getId());
        documentApi.checkInDocument(workspace.getId(), documentCreation.getReference(), "A");

        // create a document that shouldn't be retrieved
        DocumentCreationDTO documentCreation2 = new DocumentCreationDTO();
        documentCreation2.setReference(TestUtils.randomString());
        documentCreation2.setTitle(TestUtils.randomString());

        foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreation2, workspace.getId());
        documentApi.checkInDocument(workspace.getId(), documentCreation2.getReference(), "A");

        // Let some time to server for data indexing (asynchronous)
        Thread.sleep(2000);

        // search by id
        List<DocumentRevisionDTO> documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null,
                documentCreation.getReference(), null, null, null, null, null, null, null, null, null, null, null, null, false);
        Assert.assertEquals(1, documentRevisions.size());
        Assert.assertEquals(1, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getReference().equals(documentRevisionDTO.getDocumentMasterId()))
                .count());
    }

    @Test
    public void searchDocumentByAuthor() throws ApiException, IOException, InterruptedException {

        AccountDTO account = accountsApi.getAccount();
        account.setName(TestUtils.randomString());
        accountsApi.updateAccount(account);

        // Create a document
        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(TestUtils.randomString());
        documentCreation.setTitle(TestUtils.randomString());

        foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreation, workspace.getId());
        documentApi.checkInDocument(workspace.getId(), documentCreation.getReference(), "A");

        // Let some time to server for data indexing (asynchronous)
        Thread.sleep(2000);

        // search by author login
        List<DocumentRevisionDTO> documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null,
                account.getLogin(), null, null, null, null, null, null, null, null, false);
        Assert.assertFalse(documentRevisions.isEmpty());
        Assert.assertEquals(documentRevisions.size(), documentRevisions.stream()
                .filter(documentRevisionDTO -> TestConfig.LOGIN.equals(documentRevisionDTO.getAuthor().getLogin()))
                .count());

        // search by author name
        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null,
                account.getName(), null, null, null, null, null, null, null, null, false);
        Assert.assertFalse(documentRevisions.isEmpty());
        Assert.assertEquals(documentRevisions.size(), documentRevisions.stream()
                .filter(documentRevisionDTO -> TestConfig.LOGIN.equals(documentRevisionDTO.getAuthor().getLogin()))
                .count());
    }

    @Test
    public void searchDocumentByTitle() throws ApiException, IOException, InterruptedException {
        // Create a document
        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(TestUtils.randomString());
        documentCreation.setTitle(TestUtils.randomString());

        foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreation, workspace.getId());
        documentApi.checkInDocument(workspace.getId(), documentCreation.getReference(), "A");

        // create a document that shouldn't be retrieved
        DocumentCreationDTO documentCreation2 = new DocumentCreationDTO();
        documentCreation2.setReference(TestUtils.randomString());
        documentCreation2.setTitle(TestUtils.randomString());

        foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreation2, workspace.getId());
        documentApi.checkInDocument(workspace.getId(), documentCreation2.getReference(), "A");

        // Let some time to server for data indexing (asynchronous)
        Thread.sleep(2000);

        // search by id
        List<DocumentRevisionDTO> documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null,
                null, documentCreation.getTitle(), null, null, null, null, null, null, null, null, null, null, null, false);
        Assert.assertEquals(1, documentRevisions.size());
        Assert.assertEquals(1, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getReference().equals(documentRevisionDTO.getDocumentMasterId()))
                .count());
    }

    @Test
    public void searchDocumentByVersion() throws ApiException, IOException, InterruptedException {
        // Create a document
        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(TestUtils.randomString());
        documentCreation.setTitle(TestUtils.randomString());

        foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreation, workspace.getId());
        documentApi.checkInDocument(workspace.getId(), documentCreation.getReference(), "A");

        documentsApi.createNewDocumentVersion(workspace.getId(), documentCreation.getReference(), "A", documentCreation);
        documentApi.checkInDocument(workspace.getId(), documentCreation.getReference(), "B");

        documentsApi.createNewDocumentVersion(workspace.getId(), documentCreation.getReference(), "B", documentCreation);
        documentApi.checkInDocument(workspace.getId(), documentCreation.getReference(), "C");

        // Let some time to server for data indexing (asynchronous)
        Thread.sleep(2000);

        List<DocumentRevisionDTO> documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null,
                null, null, "B", null, null, null, null, null, null, null, null, null, false);
        Assert.assertEquals(1, documentRevisions.size());
        Assert.assertEquals(0, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getReference().equals(documentRevisionDTO.getDocumentMasterId())
                        && documentRevisionDTO.getVersion().equals("A"))
                .count());
        Assert.assertEquals(1, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getReference().equals(documentRevisionDTO.getDocumentMasterId())
                        && documentRevisionDTO.getVersion().equals("B"))
                .count());
        Assert.assertEquals(0, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getReference().equals(documentRevisionDTO.getDocumentMasterId())
                        && documentRevisionDTO.getVersion().equals("C"))
                .count());

        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null,
                null, null, "C", null, null, null, null, null, null, null, null, null, false);
        Assert.assertEquals(1, documentRevisions.size());
        Assert.assertEquals(0, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getReference().equals(documentRevisionDTO.getDocumentMasterId())
                        && documentRevisionDTO.getVersion().equals("A"))
                .count());
        Assert.assertEquals(0, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getReference().equals(documentRevisionDTO.getDocumentMasterId())
                        && documentRevisionDTO.getVersion().equals("B"))
                .count());
        Assert.assertEquals(1, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getReference().equals(documentRevisionDTO.getDocumentMasterId())
                        && documentRevisionDTO.getVersion().equals("C"))
                .count());


    }

    @Test
    public void searchDocumentByMultipleCriteria() throws ApiException, IOException, InterruptedException {
        // Create a document
        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(TestUtils.randomString());
        documentCreation.setTitle(TestUtils.randomString());

        foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreation, workspace.getId());
        documentApi.checkInDocument(workspace.getId(), documentCreation.getReference(), "A");

        // Let some time to server for data indexing (asynchronous)
        Thread.sleep(2000);

        // search by id
        List<DocumentRevisionDTO> documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null,
                documentCreation.getReference(), documentCreation.getTitle(), null, "A", null, null, null, null,
                null, null, null, null, null, false);

        Assert.assertEquals(1, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getReference().equals(documentRevisionDTO.getDocumentMasterId()))
                .count());
    }

    @Test
    public void searchDocumentByTag() throws ApiException, IOException, InterruptedException {

        // Create a document
        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(TestUtils.randomString());

        DocumentRevisionDTO document = foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreation, workspace.getId());

        TagListDTO tagListDTO = new TagListDTO();

        TagDTO tag = new TagDTO();
        tag.setLabel(TestUtils.randomString());

        TagDTO anOtherTag = new TagDTO();
        anOtherTag.setLabel(TestUtils.randomString());

        List<TagDTO> tags = Arrays.asList(tag, anOtherTag);
        tagListDTO.setTags(tags);

        documentApi.addDocTag(workspace.getId(), document.getDocumentMasterId(), "A", tagListDTO);
        documentApi.checkInDocument(workspace.getId(), documentCreation.getReference(), "A");


        // create a document that shouldn't be retrieved
        DocumentCreationDTO documentCreation2 = new DocumentCreationDTO();
        documentCreation2.setReference(TestUtils.randomString());
        documentCreation2.setTitle(TestUtils.randomString());

        foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreation2, workspace.getId());
        DocumentRevisionDTO a = documentApi.checkInDocument(workspace.getId(), documentCreation2.getReference(), "A");

        // Let some time to server for data indexing (asynchronous)
        Thread.sleep(2000);

        // search by tag
        List<DocumentRevisionDTO> documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null,
                null, null, null, null, null, tag.getLabel(), null, null, null, null, null, null, null, false);

        Assert.assertEquals(1, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getReference().equals(documentRevisionDTO.getDocumentMasterId()))
                .count());

        Assert.assertEquals(1, documentRevisions.size());

        // search by tag
        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null,
                null, null, null, null, null, anOtherTag.getLabel(), null, null, null, null, null, null, null, false);

        Assert.assertEquals(1, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getReference().equals(documentRevisionDTO.getDocumentMasterId()))
                .count());

        Assert.assertEquals(1, documentRevisions.size());

        // search by tag
        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null,
                null, null, null, null, null, tag.getLabel() + "," + anOtherTag.getLabel(),
                null, null, null, null, null, null, null, false);

        Assert.assertEquals(1, documentRevisions.size());

        // search by tag
        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null,
                null, null, null, null, null, "whatever", null, null, null, null, null, null, null, false);

        Assert.assertEquals(0, documentRevisions.size());
    }

    @Test
    public void createDocumentAndSearch() throws ApiException, IOException, InterruptedException {

        // Create a document
        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(TestUtils.randomString());
        documentCreation.setTitle(TestUtils.randomString());

        DocumentRevisionDTO document = foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreation, workspace.getId());

        String attrName = TestUtils.randomString();
        String attrValue = TestUtils.randomString();

        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(document);
        InstanceAttributeDTO attribute = new InstanceAttributeDTO();
        attribute.setName(attrName);
        attribute.setType(InstanceAttributeDTO.TypeEnum.TEXT);
        attribute.setValue(attrValue);
        lastIteration.getInstanceAttributes().add(attribute);
        documentApi.updateDocumentIteration(workspace.getId(), documentCreation.getReference(), "A", "1", lastIteration);
        documentApi.checkInDocument(workspace.getId(), documentCreation.getReference(), "A");

        String attributeSearchQuery = "TEXT:" + attrName + ":" + attrValue;

        // Let some time to server for data indexing (asynchronous)
        Thread.sleep(2000);

        // search by attributes
        List<DocumentRevisionDTO> documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null,
                null, null, null, null, null, null, null, null, null, null, null, attributeSearchQuery, null, false);
        Assert.assertEquals(1, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getReference().equals(documentRevisionDTO.getDocumentMasterId()))
                .count());


        // search by title
        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null,
                documentCreation.getTitle(), null, null, null, null, null, null, null, null, null, null, null, false);
        Assert.assertEquals(1, documentRevisions.stream()
                .filter(documentRevisionDTO -> documentCreation.getTitle().equals(documentRevisionDTO.getTitle()))
                .count());

    }

    @Test
    public void attributesAdvancedSearchTests() throws ApiException, InterruptedException {

        String attributeValue1 = TestUtils.randomString();
        String attributeValue2 = TestUtils.randomString();
        String attributeValue3 = TestUtils.randomString();
        String attrName1 = TestUtils.randomString();
        String attrName2 = TestUtils.randomString();

        DocumentRevisionDTO document1 = createDocumentWithAttributes(attrName1, attrName2, attributeValue1);
        DocumentRevisionDTO document2 = createDocumentWithAttributes(attrName1, attrName2, attributeValue2);
        DocumentRevisionDTO document3 = createDocumentWithAttributes(attrName1, attrName2, attributeValue3);

        Thread.sleep(2000);

        List<DocumentRevisionDTO> documentRevisions;
        String attributeQuery;

        // Simple attribute query
        attributeQuery = "TEXT:" + attrName1 + ":" + attributeValue1;
        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null,
                null, null, null, null, null, null, null, attributeQuery, null, false);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document1.getId())).count());
        Assert.assertEquals(1, documentRevisions.size());

        // Simple attribute query
        attributeQuery = "TEXT:" + attrName1 + ":" + attributeValue2;
        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null,
                null, null, null, null, null, null, null, attributeQuery, null, false);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document2.getId())).count());
        Assert.assertEquals(1, documentRevisions.size());

        // Simple attribute query
        attributeQuery = "TEXT:" + attrName1 + ":" + attributeValue3;
        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null,
                null, null, null, null, null, null, null, attributeQuery, null, false);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document3.getId())).count());
        Assert.assertEquals(1, documentRevisions.size());

        // "Or" query on same attribute
        attributeQuery = "TEXT:" + attrName1 + ":" + attributeValue1 + ";" + "TEXT:" + attrName1 + ":" + attributeValue2;
        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null, null,
                null, null, null, null, null, null, attributeQuery, null, false);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document1.getId())).count());
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document2.getId())).count());
        Assert.assertEquals(2, documentRevisions.size());

        // Something not existing
        attributeQuery = "TEXT:" + attrName1 + ":something that doesnt exist";
        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null, null,
                null, null, null, null, null, null, attributeQuery, null, false);
        Assert.assertTrue(documentRevisions.isEmpty());

        // Simple "AND" on 2 different attributes
        attributeQuery = "TEXT:" + attrName1 + ":" + attributeValue1 + ";TEXT:" + attrName2 + ":" + attributeValue1;
        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null, null,
                null, null, null, null, null, null, attributeQuery, null, false);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document1.getId())).count());
        Assert.assertEquals(1, documentRevisions.size());


        // Mix "AND" and "OR" on 2 same attribute
        attributeQuery =
                InstanceAttributeDTO.TypeEnum.TEXT + ":" + attrName1 + ":" + attributeValue1 + ";" +
                        InstanceAttributeDTO.TypeEnum.TEXT + ":" + attrName1 + ":" + attributeValue2 + ";" +
                        InstanceAttributeDTO.TypeEnum.TEXT + ":" + attrName2 + ":" + attributeValue1;

        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null,
                null, null, null, null, null, null, null, attributeQuery, null, false);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document1.getId())).count());
        Assert.assertEquals(1, documentRevisions.size());


        // Mix 2 "OR" in a "AND"
        attributeQuery = InstanceAttributeDTO.TypeEnum.TEXT + ":" + attrName1 + ":" + attributeValue1 + ";" +
                InstanceAttributeDTO.TypeEnum.TEXT + ":" + attrName1 + ":" + attributeValue2 + ";" +
                InstanceAttributeDTO.TypeEnum.TEXT + ":" + attrName2 + ":" + attributeValue1 + ";" +
                InstanceAttributeDTO.TypeEnum.TEXT + ":" + attrName2 + ":" + attributeValue2;

        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null,
                null, null, null, null, null, null, null, attributeQuery, null, false);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document1.getId())).count());
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document2.getId())).count());
        Assert.assertEquals(2, documentRevisions.size());


    }

    @Test
    public void folderAdvancedSearch() throws ApiException, InterruptedException {

        String folder1 = TestUtils.randomString();
        String folder2 = TestUtils.randomString();
        DocumentRevisionDTO document1 = createDocumentsInFolder(folder1);
        DocumentRevisionDTO document2 = createDocumentsInFolder(folder2);

        Thread.sleep(2000);

        List<DocumentRevisionDTO> documentRevisions;

        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null,
                null, null, null, null, null, null, null, null, folder1, false);
        Assert.assertEquals(1, documentRevisions
                .stream().filter(d -> d.getId().equals(document1.getId())).count());
        Assert.assertEquals(1, documentRevisions.size());

        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null,
                null, null, null, null, null, null, null, null, null, folder2, false);
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(document2.getId())).count());
        Assert.assertEquals(1, documentRevisions.size());

        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null,
                null, null, null, null, null, null, null, null, null, "NonExistingFolder", false);
        Assert.assertTrue(documentRevisions.isEmpty());

    }

    @Test
    public void asyncDocumentCreationTest() throws ApiException, ExecutionException, InterruptedException {

        CompletableFuture<DocumentRevisionDTO> future = new CompletableFuture<>();
        DocumentCreationDTO document = new DocumentCreationDTO();
        document.setReference(TestUtils.randomString());
        foldersApi.createDocumentMasterInFolderAsync(workspace.getId(),
                document, workspace.getId(), new ApiCallback<DocumentRevisionDTO>() {

                    @Override
                    public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                        future.complete(null);
                    }

                    @Override
                    public void onSuccess(DocumentRevisionDTO result, int statusCode, Map<String, List<String>> responseHeaders) {
                        future.complete(result);
                    }

                    @Override
                    public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
                    }

                    @Override
                    public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
                    }
                });

        Assert.assertNotNull(future.get());

    }

    private DocumentRevisionDTO createDocumentsInFolder(String folderName) throws ApiException {

        FolderDTO folderDTO = new FolderDTO();
        folderDTO.setName(folderName);
        foldersApi.createSubFolder(workspace.getId(), workspace.getId(), folderDTO);

        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(TestUtils.randomString());
        foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreation, workspace.getId() + ":" + folderName);
        return documentApi.checkInDocument(workspace.getId(), documentCreation.getReference(), "A");
    }

    private DocumentRevisionDTO createDocumentWithAttributes(String attrName1, String attrName2, String attrValue) throws ApiException {

        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(TestUtils.randomString());
        DocumentRevisionDTO document = foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreation, workspace.getId());
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

        documentApi.updateDocumentIteration(workspace.getId(), documentCreation.getReference(), "A", "1", lastIteration);
        return documentApi.checkInDocument(workspace.getId(), documentCreation.getReference(), "A");
    }


    @Test
    public void searchDocumentHeadOnly() throws ApiException, IOException, InterruptedException {

        // Attributes
        String attrName = TestUtils.randomString();
        String attrValueOld = TestUtils.randomString();
        String attrValueNew = TestUtils.randomString();


        // Create a document
        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(TestUtils.randomString());
        documentCreation.setTitle(TestUtils.randomString());

        DocumentRevisionDTO document = foldersApi.createDocumentMasterInFolder(workspace.getId(), documentCreation, workspace.getId());
        String docId = document.getId();

        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(document);

        InstanceAttributeDTO attributeOld = new InstanceAttributeDTO();
        attributeOld.setName(attrName);
        attributeOld.setType(InstanceAttributeDTO.TypeEnum.TEXT);
        attributeOld.setValue(attrValueOld);

        InstanceAttributeDTO attributeNew = new InstanceAttributeDTO();
        attributeNew.setName(attrName);
        attributeNew.setType(InstanceAttributeDTO.TypeEnum.TEXT);
        attributeNew.setValue(attrValueNew);

        List<InstanceAttributeDTO> instanceAttributes = lastIteration.getInstanceAttributes();
        instanceAttributes.add(attributeOld);

        documentApi.updateDocumentIteration(workspace.getId(), documentCreation.getReference(), "A", "1", lastIteration);
        documentApi.checkInDocument(workspace.getId(), documentCreation.getReference(), "A");

        // Asynchronous indexation when checking in
        Thread.sleep(2000);

        // Update document with new attribute
        document = documentApi.checkOutDocument(workspace.getId(), documentCreation.getReference(), "A");
        lastIteration = LastIterationHelper.getLastIteration(document);
        instanceAttributes = lastIteration.getInstanceAttributes();
        instanceAttributes.remove(attributeOld);
        instanceAttributes.add(attributeNew);

        documentApi.updateDocumentIteration(workspace.getId(), documentCreation.getReference(), "A", "2", lastIteration);
        documentApi.checkInDocument(workspace.getId(), documentCreation.getReference(), "A");

        // Let some time to server for data indexing (asynchronous)
        Thread.sleep(2000);

        // Search document in history with old attribute: should retrieve the document
        String attributeQuery = "TEXT:" + attrName + ":" + attrValueOld;

        List<DocumentRevisionDTO> documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null,
                null, null, null, null, null, null, null, attributeQuery, null, false);
        Assert.assertEquals(1, documentRevisions.size());
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(docId)).count());

        // Search document in history with new attribute: should retrieve the document
        attributeQuery = "TEXT:" + attrName + ":" + attrValueNew;
        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null,
                null, null, null, null, null, null, null, attributeQuery, null, false);
        Assert.assertEquals(1, documentRevisions.size());
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(docId)).count());


        // Search document in head only with old attribute: should not retrieve the document
        attributeQuery = "TEXT:" + attrName + ":" + attrValueOld;
        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null,
                null, null, null, null, null, null, null, attributeQuery, null, true);
        Assert.assertEquals(0, documentRevisions.size());
        Assert.assertEquals(0, documentRevisions.stream().filter(d -> d.getId().equals(docId)).count());

        // Search document in head only with new attribute: should retrieve the document
        attributeQuery = "TEXT:" + attrName + ":" + attrValueNew;
        documentRevisions = documentsApi.searchDocumentRevision(workspace.getId(), null, null, null, null, null,
                null, null, null, null, null, null, null, attributeQuery, null, true);
        Assert.assertEquals(1, documentRevisions.size());
        Assert.assertEquals(1, documentRevisions.stream().filter(d -> d.getId().equals(docId)).count());

    }


}
