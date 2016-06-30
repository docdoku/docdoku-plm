package com.docdoku.api;


import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.DocumentCreationDTO;
import com.docdoku.api.models.DocumentIterationDTO;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.DocumentApi;
import com.docdoku.api.services.FoldersApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.UUID;

@RunWith(JUnit4.class)
public class DocumentApiTest {

    private DocumentApi documentApi = new DocumentApi(TestConfig.BASIC_CLIENT);
    private FoldersApi foldersApi = new FoldersApi(TestConfig.BASIC_CLIENT);

    @Test
    public void documentApiUsageTests() throws ApiException {

        // Create a document
        DocumentCreationDTO document = new DocumentCreationDTO();
        document.setReference(UUID.randomUUID().toString().substring(0,6));
        document.setTitle("GeneratedDoc");

        DocumentRevisionDTO createdDocument = foldersApi.createDocumentMasterInFolder(TestConfig.WORKSPACE, document, TestConfig.WORKSPACE);
        Assert.assertEquals(createdDocument.getDocumentMasterId(),document.getReference());

        // Check in
        DocumentRevisionDTO checkedInDocument = documentApi.checkInDocument(TestConfig.WORKSPACE, createdDocument.getDocumentMasterId(), createdDocument.getVersion(), "");
        Assert.assertEquals(checkedInDocument.getDocumentMasterId(),document.getReference());
        Assert.assertEquals(LastIterationHelper.getLastIteration(checkedInDocument).getIteration(), Integer.valueOf("1"));

        // Check out
        DocumentRevisionDTO checkedOutDocument = documentApi.checkOutDocument(TestConfig.WORKSPACE, createdDocument.getDocumentMasterId(), createdDocument.getVersion(), "");
        Assert.assertEquals(checkedOutDocument.getDocumentMasterId(),document.getReference());
        Assert.assertEquals(LastIterationHelper.getLastIteration(checkedOutDocument).getIteration(),Integer.valueOf("2"));

        // Undo check out
        DocumentRevisionDTO undoCheckOutDocument= documentApi.undoCheckOutDocument(TestConfig.WORKSPACE, createdDocument.getDocumentMasterId(), createdDocument.getVersion(), "");
        Assert.assertEquals(undoCheckOutDocument,checkedInDocument);

        // Check out
        checkedOutDocument = documentApi.checkOutDocument(TestConfig.WORKSPACE, createdDocument.getDocumentMasterId(), createdDocument.getVersion(), "");

        // Edit
        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(checkedOutDocument);
        Assert.assertNull(lastIteration.getModificationDate());
        lastIteration.setRevisionNote("Something modified");

        DocumentIterationDTO updatedIteration = documentApi.updateDocumentIteration(TestConfig.WORKSPACE, checkedOutDocument.getDocumentMasterId(), checkedOutDocument.getVersion(), "2", lastIteration);

        Assert.assertNotNull(updatedIteration.getModificationDate());
        lastIteration.setModificationDate(updatedIteration.getModificationDate());
        Assert.assertEquals(lastIteration,updatedIteration);

        // Check in
        checkedInDocument = documentApi.checkInDocument(TestConfig.WORKSPACE, createdDocument.getDocumentMasterId(), createdDocument.getVersion(), "");
        Assert.assertEquals(checkedInDocument.getDocumentIterations().size(),2);

    }



}
