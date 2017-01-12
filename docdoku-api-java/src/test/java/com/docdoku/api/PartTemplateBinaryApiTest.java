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
import com.docdoku.api.models.PartMasterTemplateDTO;
import com.docdoku.api.models.PartTemplateCreationDTO;
import com.docdoku.api.models.WorkspaceDTO;
import com.docdoku.api.services.PartTemplateBinaryApi;
import com.docdoku.api.services.PartTemplatesApi;
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

@RunWith(JUnit4.class)
public class PartTemplateBinaryApiTest {

    private PartTemplateBinaryApi partTemplateBinaryApi =
            new PartTemplateBinaryApi(TestConfig.REGULAR_USER_CLIENT);

    private static PartTemplatesApi partTemplatesApi = new PartTemplatesApi(TestConfig.REGULAR_USER_CLIENT);
    private static PartMasterTemplateDTO template;
    private static WorkspaceDTO workspace;

    @BeforeClass
    public static void initWorkspace() throws ApiException {
        workspace = TestUtils.createWorkspace();
        PartTemplateCreationDTO templateCreation = new PartTemplateCreationDTO();
        templateCreation.setReference(TestUtils.randomString());
        template = partTemplatesApi.createPartMasterTemplate(workspace.getId(), templateCreation);
    }

    @AfterClass
    public static void deleteWorkspace() throws ApiException {
        TestUtils.deleteWorkspace(workspace);
    }

    @Test
    public void testSuite() throws ApiException, IOException {
        File original = partTemplateUpload();
        File downloaded = partTemplateDownload();
        Assert.assertTrue(FileUtils.contentEquals(original, downloaded));
    }

    private File partTemplateUpload() throws ApiException {

        URL fileURL = PartTemplateBinaryApiTest.class.getClassLoader().getResource("com/docdoku/api/attached-file.md");
        Assert.assertNotNull(fileURL);

        File file = new File(fileURL.getPath());

        ApiResponse<Void> response =
                partTemplateBinaryApi.uploadPartTemplateFilesWithHttpInfo(template.getWorkspaceId(),
                        template.getId(), file);

        String location = response.getHeaders().get("Location").get(0);
        Assert.assertNotNull(location);
        return file;

    }

    private File partTemplateDownload() throws ApiException {
        return partTemplateBinaryApi.downloadPartTemplateFile(template.getWorkspaceId(), template.getId(),
                "attached-file.md", null);
    }

}
