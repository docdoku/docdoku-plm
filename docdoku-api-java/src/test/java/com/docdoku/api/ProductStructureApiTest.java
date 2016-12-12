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
import com.docdoku.api.services.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class ProductStructureApiTest {

    private static final PartsApi partsApi = new PartsApi(TestConfig.REGULAR_USER_CLIENT);
    private static final WorkspacesApi workspacesApi = new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT);
    private static final PartApi partApi = new PartApi(TestConfig.REGULAR_USER_CLIENT);
    private static final ProductsApi productsApi = new ProductsApi(TestConfig.REGULAR_USER_CLIENT);
    private static final PartBinaryApi partBinaryApi = new PartBinaryApi(TestConfig.REGULAR_USER_CLIENT);

    private final Integer LEVEL_MAX = 4;
    private final Integer MAX_PER_LEVEL = 3;
    private final Integer MIN_PER_LEVEL = 1;
    private final String ITERATION_NOTE = "Init structure";
    private File cadFile = new File(ProductStructureApiTest.class.getClassLoader().getResource("com/docdoku/api/cube.obj").getPath());

    private static WorkspaceDTO workspace;
    private static PartRevisionDTO rootPart;
    private static ConfigurationItemDTO product;
    private List<PartRevisionDTO> partsInStructure = new ArrayList<>();
    private List<PartIterationDTO> partsAsLeaves = new ArrayList<>();

    @BeforeClass
    public static void initProductStructure() throws ApiException {
        workspace = TestUtils.createWorkspace();
        PartCreationDTO part = new PartCreationDTO();
        part.setNumber(TestUtils.randomString());
        rootPart = partsApi.createNewPart(workspace.getId(), part);
        ConfigurationItemDTO productCreation = new ConfigurationItemDTO();
        productCreation.setId(TestUtils.randomString());
        productCreation.setDesignItemNumber(rootPart.getNumber());
        productCreation.setWorkspaceId(workspace.getId());
        productCreation.setDescription("Root part in generated assembly");
        product = productsApi.createConfigurationItem(workspace.getId(), productCreation);
    }

    @Test
    public void testProductStructure() throws ApiException {

        generateStructure();

        Assert.assertTrue(partsInStructure.contains(rootPart));
        Assert.assertFalse(partsInStructure.isEmpty());

        ConfigurationItemDTO fetchedProduct = productsApi.getConfigurationItem(workspace.getId(), product.getId());
        Assert.assertEquals(fetchedProduct.getDesignItemNumber(), rootPart.getNumber());
        List<LeafDTO> leaves = productsApi.getFilteredInstances(product.getWorkspaceId(), product.getId(), "latest", "-1", false);
        Assert.assertFalse(leaves.isEmpty());
        Assert.assertEquals(partsAsLeaves.size(), leaves.size());
    }

    private void generateStructure() throws ApiException {
        createParts(rootPart, 0);
    }

    /**
     * Recursive method for structure generation
     */
    private void createParts(PartRevisionDTO parent, int currentLevel) throws ApiException {

        PartIterationDTO lastIteration = LastIterationHelper.getLastIteration(parent);
        List<PartRevisionDTO> currentLevelParts = new ArrayList<>();

        if (++currentLevel <= LEVEL_MAX) {

            Integer perLevel = TestUtils.randomInt(MIN_PER_LEVEL, MAX_PER_LEVEL);
            PartCreationDTO partCreation = new PartCreationDTO();

            for (int i = 0; i < perLevel; i++) {
                partCreation.setNumber(TestUtils.randomString());
                PartRevisionDTO part = partsApi.createNewPart(workspace.getId(), partCreation);
                currentLevelParts.add(part);
                createParts(part, currentLevel);
            }

        } else {
            partsAsLeaves.add(lastIteration);
            partBinaryApi.uploadNativeCADFile(lastIteration.getWorkspaceId(), lastIteration.getNumber(),
                    lastIteration.getVersion(), lastIteration.getIteration(), cadFile);
        }

        lastIteration.setComponents(createLinks(currentLevelParts));
        lastIteration.setIterationNote(ITERATION_NOTE);
        partApi.updatePartIteration(parent.getWorkspaceId(), parent.getNumber(), parent.getVersion(), lastIteration.getIteration(), lastIteration);
        partApi.checkIn(parent.getWorkspaceId(), parent.getNumber(), parent.getVersion());
        partsInStructure.add(parent);

    }

    private List<PartUsageLinkDTO> createLinks(List<PartRevisionDTO> partRevisions) {
        List<PartUsageLinkDTO> components = new ArrayList<>();
        for (PartRevisionDTO part : partRevisions) {

            List<CADInstanceDTO> instances = new ArrayList<>();
            CADInstanceDTO instance = new CADInstanceDTO();
            ComponentDTO component = new ComponentDTO();
            PartUsageLinkDTO link = new PartUsageLinkDTO();

            instance.setRotationType(CADInstanceDTO.RotationTypeEnum.ANGLE);

            instance.setRx(TestUtils.randomRotation());
            instance.setRy(TestUtils.randomRotation());
            instance.setRz(TestUtils.randomRotation());

            instance.setTx(TestUtils.randomTranslation());
            instance.setTy(TestUtils.randomTranslation());
            instance.setTz(TestUtils.randomTranslation());

            instances.add(instance);
            link.setCadInstances(instances);

            component.setNumber(part.getNumber());
            component.setAmount(1.0);
            component.setPartUsageLinkReferenceDescription("Link to " + part.getNumber());
            link.setComponent(component);

            components.add(link);

        }
        return components;
    }

    @AfterClass
    public static void deleteWorkspace() throws ApiException {
        workspacesApi.deleteWorkspace(workspace.getId());
    }


}
