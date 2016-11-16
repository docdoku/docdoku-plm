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
import com.docdoku.api.services.PartApi;
import com.docdoku.api.services.PartsApi;
import com.docdoku.api.services.ProductBaselineApi;
import com.docdoku.api.services.ProductsApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class ProductApiTest {

    private PartApi partApi = new PartApi(TestConfig.REGULAR_USER_CLIENT);
    private PartsApi partsApi = new PartsApi(TestConfig.REGULAR_USER_CLIENT);
    private ProductsApi productsApi = new ProductsApi(TestConfig.REGULAR_USER_CLIENT);
    private ProductBaselineApi productBaselineApi = new ProductBaselineApi(TestConfig.REGULAR_USER_CLIENT);

    @Test
    public void createProductTest() throws ApiException {

        String p1Number = TestUtils.randomString();
        String p2Number = TestUtils.randomString();
        String p3Number = TestUtils.randomString();

        String[] partNumbers = {p1Number, p2Number, p3Number};
        PartCreationDTO part = new PartCreationDTO();

        for (String partNumber : partNumbers) {
            part.setNumber(partNumber);
            partsApi.createNewPart(TestConfig.WORKSPACE, part);
        }

        // Create a structure
        //  - P1
        //    - P2
        //    - P3

        PartRevisionDTO p1 = partApi.getPartRevision(TestConfig.WORKSPACE, p1Number, "A");
        PartIterationDTO i1 = LastIterationHelper.getLastIteration(p1);
        List<PartUsageLinkDTO> components = new ArrayList<>();

        PartUsageLinkDTO link1= new PartUsageLinkDTO();
        ComponentDTO component1 = new ComponentDTO();
        component1.setNumber(p2Number);
        link1.setComponent(component1);
        component1.setAmount(1.0);
        component1.setVersion("A");
        component1.setPartUsageLinkReferenceDescription("Link to P2");
        components.add(link1);

        PartUsageLinkDTO link2= new PartUsageLinkDTO();
        ComponentDTO component2 = new ComponentDTO();
        component2.setNumber(p3Number);
        component2.setAmount(1.0);
        component2.setVersion("A");
        component2.setPartUsageLinkReferenceDescription("Link to P3");
        link2.setComponent(component2);

        components.add(link2);

        i1.setComponents(components);

        partApi.updatePartIteration(TestConfig.WORKSPACE, p1Number, "A", 1, i1);

        for (String partNumber : partNumbers) {
            partApi.checkIn(TestConfig.WORKSPACE, partNumber, "A", "");
        }


        ConfigurationItemDTO product = new ConfigurationItemDTO();
        product.setId(TestUtils.randomString());
        product.setDesignItemNumber(p1Number);
        product.setDescription("Generated product");
        product.setWorkspaceId(TestConfig.WORKSPACE);

        productsApi.createConfigurationItem(TestConfig.WORKSPACE, product);

        ProductBaselineDTO baseline = new ProductBaselineDTO();
        baseline.setType(ProductBaselineDTO.TypeEnum.LATEST);
        baseline.setName("Generated baseline");
        baseline.setConfigurationItemId(product.getId());

        ProductBaselineDTO productBaseline = productBaselineApi.createProductBaseline(TestConfig.WORKSPACE, baseline);
        List<ProductBaselineDTO> productBaselines = productBaselineApi.getProductBaselinesForProduct(TestConfig.WORKSPACE, product.getId());
        Assert.assertEquals(1, productBaselines.stream()
                .filter(productBaselineDTO -> productBaseline.getId().equals(productBaselineDTO.getId()))
                .count());

        List<LeafDTO> leaves = productsApi.getInstances(TestConfig.WORKSPACE, product.getId(), "latest", "-1", true);

        Assert.assertNotNull(leaves);
        // No geometric data uploaded
        Assert.assertTrue(leaves.isEmpty());

    }

}
