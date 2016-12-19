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

import com.docdoku.api.client.ApiClient;
import com.docdoku.api.client.ApiException;
import com.docdoku.api.client.ApiResponse;
import com.docdoku.api.models.*;
import com.docdoku.api.services.*;
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
public class ProductInstanceBinaryApiTest {


    private static ProductInstanceMasterDTO productInstance;
    private static PathDataMasterDTO pathDataMaster;
    private ProductInstanceBinaryApi productInstanceBinaryApi =
            new ProductInstanceBinaryApi(TestConfig.REGULAR_USER_CLIENT);

    @BeforeClass
    public static void initProductInstance() throws ApiException {

        String workspaceId = TestConfig.WORKSPACE;
        ApiClient client = TestConfig.REGULAR_USER_CLIENT;

        PartRevisionDTO part = TestUtils.createPart(workspaceId, TestUtils.randomString());
        part = new PartApi(client).checkIn(part.getWorkspaceId(), part.getNumber(), part.getVersion());

        ConfigurationItemDTO productCreation = new ConfigurationItemDTO();
        productCreation.setDescription("Generated product");
        productCreation.setDesignItemNumber(part.getNumber());
        productCreation.setId(TestUtils.randomString());
        productCreation.setWorkspaceId(workspaceId);

        ConfigurationItemDTO product = new ProductsApi(client)
                .createConfigurationItem(workspaceId, productCreation);


        ProductBaselineDTO baselineCreation = new ProductBaselineDTO();
        baselineCreation.setConfigurationItemId(product.getId());
        baselineCreation.setName(TestUtils.randomString());
        baselineCreation.setType(ProductBaselineDTO.TypeEnum.LATEST);

        ProductBaselineDTO baseline =
                new ProductBaselineApi(client).createProductBaseline(workspaceId,
                        baselineCreation);

        ProductInstanceCreationDTO instance = new ProductInstanceCreationDTO();
        instance.setConfigurationItemId(product.getId());
        instance.setBaselineId(baseline.getId());
        instance.setSerialNumber(TestUtils.randomString());

        ProductInstancesApi productInstancesApi = new ProductInstancesApi(client);
        productInstance = productInstancesApi.createProductInstanceMaster(workspaceId, instance);


        PathDataIterationCreationDTO pathData = new PathDataIterationCreationDTO();

        pathDataMaster = productInstancesApi.createPathDataMaster(workspaceId, productInstance.getConfigurationItemId(),
                productInstance.getSerialNumber(), "-1", pathData);

    }


    @Test
    public void testSuite() throws ApiException, IOException {
        File originalAttached = uploadAttachedFile();
        File downloadedAttached = downloadAttachedFile();
        Assert.assertTrue(FileUtils.contentEquals(originalAttached, downloadedAttached));

        File originalPathData = uploadPathDataFile();
        File downloadedPathData = downloadPathDataFile();
        Assert.assertTrue(FileUtils.contentEquals(originalPathData, downloadedPathData));

    }

    private File uploadAttachedFile() throws ApiException {

        URL fileURL = DocumentApiTest.class.getClassLoader().getResource("com/docdoku/api/attached-file.md");
        File file = new File(fileURL.getPath());

        ApiResponse<Void> response =
                productInstanceBinaryApi.uploadFilesToProductInstanceIterationWithHttpInfo(TestConfig.WORKSPACE,
                        productInstance.getConfigurationItemId(), productInstance.getSerialNumber(), 1, file);

        String location = response.getHeaders().get("Location").get(0);
        Assert.assertNotNull(location);
        return file;

    }

    private File downloadAttachedFile() throws ApiException {
        return productInstanceBinaryApi.downloadFileFromProductInstance(TestConfig.WORKSPACE,
                productInstance.getConfigurationItemId(), productInstance.getSerialNumber(), 1, "attached-file.md",
                null, null, null);
    }

    private File uploadPathDataFile() throws ApiException {

        URL fileURL = DocumentApiTest.class.getClassLoader().getResource("com/docdoku/api/attached-file.md");
        File file = new File(fileURL.getPath());

        ApiResponse<Void> response =
                productInstanceBinaryApi.uploadFilesToPathDataIterationWithHttpInfo(TestConfig.WORKSPACE,
                        productInstance.getConfigurationItemId(), productInstance.getSerialNumber(), 1,
                        pathDataMaster.getId(), file);

        String location = response.getHeaders().get("Location").get(0);
        Assert.assertNotNull(location);
        return file;

    }

    private File downloadPathDataFile() throws ApiException {
        return productInstanceBinaryApi.downloadFileFromPathDataIteration(TestConfig.WORKSPACE,
                productInstance.getSerialNumber(), pathDataMaster.getId(), 1, "attached-file.md",
                null, null, null);
    }
}
