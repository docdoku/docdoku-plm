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
import com.docdoku.api.models.*;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@RunWith(JUnit4.class)
public class ProductApiTest {

    private PartApi partApi = new PartApi(TestConfig.REGULAR_USER_CLIENT);
    private PartsApi partsApi = new PartsApi(TestConfig.REGULAR_USER_CLIENT);
    private PartBinaryApi partBinaryApi = new PartBinaryApi(TestConfig.REGULAR_USER_CLIENT);
    private ProductsApi productsApi = new ProductsApi(TestConfig.REGULAR_USER_CLIENT);
    private ProductBaselineApi productBaselineApi = new ProductBaselineApi(TestConfig.REGULAR_USER_CLIENT);
    private static WorkspaceDTO workspace;

    @BeforeClass
    public static void initWorkspace() throws ApiException {
        workspace = TestUtils.createWorkspace(ProductApiTest.class.getName());
    }

    @AfterClass
    public static void deleteWorkspace() throws ApiException {
        TestUtils.deleteWorkspace(workspace);
    }

    @Test
    public void createProductTest() throws ApiException {

        String p1Number = TestUtils.randomString();
        String p2Number = TestUtils.randomString();
        String p3Number = TestUtils.randomString();

        String[] partNumbers = {p1Number, p2Number, p3Number};
        PartCreationDTO part = new PartCreationDTO();


        URL fileURL = PartBinaryApiTest.class.getClassLoader().getResource("com/docdoku/api/cube.obj");
        File file = new File(fileURL.getPath());

        for (String partNumber : partNumbers) {
            part.setNumber(partNumber);
            PartRevisionDTO createdPart = partsApi.createNewPart(workspace.getId(), part);
            partBinaryApi.uploadNativeCADFileWithHttpInfo(createdPart.getWorkspaceId(),
                    createdPart.getNumber(), createdPart.getVersion(), 1, file);
        }

        // Create a structure
        //  - P1
        //    - P2
        //    - P3

        PartRevisionDTO p1 = partApi.getPartRevision(workspace.getId(), p1Number, "A");
        PartIterationDTO i1 = LastIterationHelper.getLastIteration(p1);
        List<PartUsageLinkDTO> components = new ArrayList<>();

        PartUsageLinkDTO link1 = new PartUsageLinkDTO();
        ComponentDTO component = new ComponentDTO();
        component.setNumber(p2Number);
        link1.setComponent(component);
        component.setAmount(1.0);
        component.setVersion("A");
        component.setPartUsageLinkReferenceDescription("Link to P2");
        components.add(link1);

        PartUsageLinkDTO link2 = new PartUsageLinkDTO();
        ComponentDTO component2 = new ComponentDTO();
        component2.setNumber(p3Number);
        component2.setAmount(1.0);
        component2.setVersion("A");
        component2.setPartUsageLinkReferenceDescription("Link to P3");
        link2.setComponent(component2);

        components.add(link2);

        i1.setComponents(components);

        partApi.updatePartIteration(workspace.getId(), p1Number, "A", 1, i1);

        for (String partNumber : partNumbers) {
            partApi.checkIn(workspace.getId(), partNumber, "A");
        }


        ConfigurationItemDTO product = new ConfigurationItemDTO();
        product.setId(TestUtils.randomString());
        product.setDesignItemNumber(p1Number);
        product.setDescription("Generated product");
        product.setWorkspaceId(workspace.getId());

        productsApi.createConfigurationItem(workspace.getId(), product);

        ProductBaselineCreationDTO baseline = new ProductBaselineCreationDTO();
        baseline.setType(ProductBaselineCreationDTO.TypeEnum.LATEST);
        baseline.setName("Generated baseline");
        baseline.setConfigurationItemId(product.getId());

        ProductBaselineDTO productBaseline = productBaselineApi.createProductBaseline(workspace.getId(), baseline, false);
        List<ProductBaselineDTO> productBaselines = productBaselineApi.getProductBaselinesForProduct(workspace.getId(), product.getId());
        Assert.assertEquals(1, productBaselines.stream()
                .filter(productBaselineDTO -> productBaseline.getId().equals(productBaselineDTO.getId()))
                .count());


        // Structure tests
        List<LeafDTO> leaves = productsApi.getFilteredInstances(workspace.getId(), product.getId(), "latest", "-1", true);
        Assert.assertNotNull(leaves);

        // Ask for full structure
        ComponentDTO structure = productsApi.filterProductStructure(workspace.getId(),
                product.getId(), "wip", "-1", -1, null, true);

        Assert.assertNotNull(structure);
        Assert.assertTrue(structure.getAssembly());

        List<ComponentDTO> structureComponents = structure.getComponents();
        Assert.assertEquals(2, structureComponents.size());

        // Typed links
        LightPathToPathLinkDTO link = new LightPathToPathLinkDTO();
        link.setType("myType");
        link.setDescription("myType");
        link.setSourcePath(structureComponents.get(0).getPath());
        link.setTargetPath(structureComponents.get(1).getPath());
        productsApi.createPathToPathLink(workspace.getId(), product.getId(), link);

        structure = productsApi.filterProductStructure(workspace.getId(),
                product.getId(), "wip", null, -1, "myType", false);

        Assert.assertNotNull(structure);
        Assert.assertEquals("myType", structure.getNumber());
        Assert.assertTrue(structure.getVirtual());
        structureComponents = structure.getComponents();
        Assert.assertEquals(1, structureComponents.size());
        component = structureComponents.get(0);
        Assert.assertEquals(p2Number, component.getNumber());

        // next level
        structure = productsApi.filterProductStructure(workspace.getId(),
                product.getId(), "wip", component.getPath(), -1, "myType", false);

        Assert.assertNotNull(structure);
        Assert.assertFalse(structure.getVirtual());
        Assert.assertEquals(p2Number, structure.getNumber());

        structureComponents = structure.getComponents();
        Assert.assertEquals(1, structureComponents.size());
        Assert.assertEquals(p3Number, structureComponents.get(0).getNumber());

        // Export test
        File export = productsApi.exportProductFiles(workspace.getId(), product.getId(), "wip", true, true);
        Assert.assertTrue(export.exists());
        Assert.assertTrue(export.getName().startsWith(product.getId()));
        Assert.assertTrue(export.getName().endsWith(".zip"));

        ZipFile zipFile;

        try {
            zipFile = new ZipFile(export.getAbsolutePath());
        } catch (IOException e) {
            Assert.fail("Cannot open zip file\n" + e.getMessage());
            return;
        }

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            Assert.assertTrue(entry.getName().endsWith("cube.obj"));
        }

    }

}
