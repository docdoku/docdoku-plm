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
import com.docdoku.api.models.DocumentMasterTemplateDTO;
import com.docdoku.api.models.DocumentTemplateCreationDTO;
import com.docdoku.api.services.DocumentTemplateBinaryApi;
import com.docdoku.api.services.DocumentTemplatesApi;
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
public class DocumentTemplateBinaryApiTest {

    private DocumentTemplateBinaryApi documentTemplateBinaryApi =
            new DocumentTemplateBinaryApi(TestConfig.REGULAR_USER_CLIENT);

    private static DocumentTemplatesApi documentTemplatesApi = new DocumentTemplatesApi(TestConfig.REGULAR_USER_CLIENT);
    private static DocumentMasterTemplateDTO template;

    @BeforeClass
    public static void initDocument() throws ApiException {
        DocumentTemplateCreationDTO templateCreation = new DocumentTemplateCreationDTO();
        templateCreation.setReference(TestUtils.randomString());
        template = documentTemplatesApi.createDocumentMasterTemplate(TestConfig.WORKSPACE, templateCreation);
    }

    @Test
    public void testSuite() throws ApiException, IOException {
        File original = documentTemplateUpload();
        File downloaded = documentTemplateDownload();
        Assert.assertTrue(FileUtils.contentEquals(original, downloaded));
    }

    private File documentTemplateUpload() throws ApiException {

        URL fileURL = DocumentTemplateBinaryApiTest.class.getClassLoader().getResource("com/docdoku/api/attached-file.md");
        File file = new File(fileURL.getPath());

        ApiResponse<Void> response =
                documentTemplateBinaryApi.uploadDocumentTemplateFilesWithHttpInfo(template.getWorkspaceId(),
                        template.getId(), file);

        String location = response.getHeaders().get("Location").get(0);
        Assert.assertNotNull(location);
        return file;

    }

    private File documentTemplateDownload() throws ApiException {
        return documentTemplateBinaryApi.downloadDocumentTemplateFile(template.getWorkspaceId(), template.getId(),
                "attached-file.md", null, null, null);
    }

}
