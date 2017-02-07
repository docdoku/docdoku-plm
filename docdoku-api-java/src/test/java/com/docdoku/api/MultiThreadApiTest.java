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
import com.docdoku.api.models.DocumentIterationDTO;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.models.WorkspaceDTO;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.DocumentApi;
import com.docdoku.api.services.DocumentBinaryApi;
import com.docdoku.api.services.FoldersApi;
import com.docdoku.api.services.WorkspacesApi;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RunWith(JUnit4.class)
public class MultiThreadApiTest {

    private static final int MAX_THREADS = 100;
    private static final int TIME_PER_THREAD = 150;
    private static final long WAIT_TIME = MAX_THREADS * TIME_PER_THREAD;
    private static WorkspaceDTO workspace;


    @BeforeClass
    public static void initTestData() throws ApiException {
        workspace = TestUtils.createWorkspace(MultiThreadApiTest.class.getName());
    }

    @AfterClass
    public static void clearData() throws ApiException {
        new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT).deleteWorkspace(workspace.getId());
    }

    @Test
    public void concurrentOperationTests() throws ApiException, InterruptedException {

        final List<Integer> threadSucceed = new ArrayList<>();
        final List<Throwable> exceptions = new ArrayList<>();
        for (int i = 0; i < MAX_THREADS; i++) {
            final int finalI = i;

            Logger threadLogger = Logger.getLogger("THREAD[" + finalI + "]");

            Runnable runnable = () -> {
                try {
                    runInThread(threadLogger, finalI);
                    threadSucceed.add(finalI);
                } catch (ApiException | IOException e) {
                    threadLogger.log(Level.SEVERE, "Thread fail [" + finalI + "]", e);
                    exceptions.add(e);
                }
            };

            threadLogger.log(Level.INFO, "Starting thread " + finalI);
            Thread thread = new Thread(runnable);
            thread.start();

        }

        if (!exceptions.isEmpty()) {
            for (Throwable t : exceptions) {
                System.out.println(t.getMessage());
            }
        }
        Thread.sleep(WAIT_TIME);
        Assert.assertEquals(threadSucceed.size(), MAX_THREADS);

    }

    private void runInThread(Logger logger, int step) throws ApiException, IOException {

        DocumentApi documentApi = new DocumentApi(TestConfig.REGULAR_USER_CLIENT);
        DocumentBinaryApi documentBinaryApi = new DocumentBinaryApi(TestConfig.REGULAR_USER_CLIENT);
        FoldersApi foldersApi = new FoldersApi(TestConfig.REGULAR_USER_CLIENT);

        DocumentCreationDTO document = new DocumentCreationDTO();
        document.setReference(TestUtils.randomString());
        document.setTitle("GeneratedDoc");

        DocumentRevisionDTO documentRevisionDTO =
                foldersApi.createDocumentMasterInFolder(workspace.getId(), document, workspace.getId());

        URL fileURL = DocumentApiTest.class.getClassLoader().getResource("com/docdoku/api/attached-file.md");
        File original = new File(fileURL.getPath());
        ApiResponse<Void> response =
                documentBinaryApi.uploadDocumentFilesWithHttpInfo(documentRevisionDTO.getWorkspaceId(),
                        documentRevisionDTO.getDocumentMasterId(), documentRevisionDTO.getVersion(), 1, original);

        documentApi.checkInDocument(documentRevisionDTO.getWorkspaceId(), documentRevisionDTO.getDocumentMasterId(), documentRevisionDTO.getVersion());
        documentRevisionDTO = documentApi.checkOutDocument(documentRevisionDTO.getWorkspaceId(), documentRevisionDTO.getDocumentMasterId(), documentRevisionDTO.getVersion());

        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(documentRevisionDTO);

        lastIteration.setRevisionNote("Updated by thread  [" + step + "]");

        documentApi.updateDocumentIteration(documentRevisionDTO.getWorkspaceId(), documentRevisionDTO.getDocumentMasterId(), documentRevisionDTO.getVersion(),
                String.valueOf(lastIteration.getIteration()), lastIteration);
        documentApi.checkInDocument(documentRevisionDTO.getWorkspaceId(), documentRevisionDTO.getDocumentMasterId(), documentRevisionDTO.getVersion());

        File downloaded = documentBinaryApi.downloadDocumentFile(documentRevisionDTO.getWorkspaceId(), documentRevisionDTO.getDocumentMasterId(),
                documentRevisionDTO.getVersion(), 1, "attached-file.md", "", null, null, null, null);

        logger.log(Level.INFO, "Thread over [" + step + "]");
        Assert.assertTrue(FileUtils.contentEquals(original, downloaded));

    }

}
