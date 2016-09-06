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
import com.docdoku.api.models.DocumentIterationDTO;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.models.utils.UploadDownloadHelper;
import com.docdoku.api.services.DocumentApi;
import com.docdoku.api.services.FoldersApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@RunWith(JUnit4.class)
public class UploadDownloadConsistencyTest {

    private DocumentApi documentApi = new DocumentApi(TestConfig.BASIC_CLIENT);
    private FoldersApi foldersApi = new FoldersApi(TestConfig.BASIC_CLIENT);


    @Test
    public void uploadZipTest() throws ApiException, IOException {

        // Create a document
        DocumentCreationDTO documentCreation = new DocumentCreationDTO();
        documentCreation.setReference(UUID.randomUUID().toString().substring(0, 6));
        documentCreation.setTitle("GeneratedDoc");

        DocumentRevisionDTO document = foldersApi.createDocumentMasterInFolder(TestConfig.WORKSPACE, documentCreation, TestConfig.WORKSPACE);

        URL fileURL = UploadDownloadConsistencyTest.class.getClassLoader().getResource("com/docdoku/api/attached-file.zip");
        File file = new File(fileURL.getPath());

        List<String> originalEntries = getEntries(file);
        Assert.assertEquals(1,originalEntries.size());

        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(document);
        UploadDownloadHelper.uploadAttachedFile(lastIteration, TestConfig.BASIC_CLIENT, file);
        document = documentApi.getDocumentRevision(TestConfig.WORKSPACE, document.getDocumentMasterId(), document.getVersion(), null);
        lastIteration = LastIterationHelper.getLastIteration(document);
        File downloadedFile = UploadDownloadHelper.downloadFile(lastIteration.getAttachedFiles().get(0).getFullName(), TestConfig.BASIC_CLIENT);

        List<String> downloadedEntries = getEntries(downloadedFile);
        Assert.assertTrue(new HashSet<>(originalEntries).equals(new HashSet<>(downloadedEntries)));


    }

    private List<String> getEntries(File file){
        List<String> fileNames = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                String fileName = ((ZipEntry) zipEntries.nextElement()).getName();
                fileNames.add(fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileNames;
    }


}
