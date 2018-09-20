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
import com.docdoku.api.services.PartApi;
import com.docdoku.api.services.ProductBaselineApi;
import com.docdoku.api.services.ProductsApi;
import com.docdoku.api.services.WorkspacesApi;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(JUnit4.class)
public class ProductBaselineApiTest {

    private static final WorkspacesApi workspacesApi = new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT);
    private static final PartApi partApi = new PartApi(TestConfig.REGULAR_USER_CLIENT);
    private static final ProductsApi productsApi = new ProductsApi(TestConfig.REGULAR_USER_CLIENT);
    private static final ProductBaselineApi productBaselineApi = new ProductBaselineApi(TestConfig.REGULAR_USER_CLIENT);

    private static WorkspaceDTO workspace;
    private static PartRevisionDTO rootPart;
    private static ConfigurationItemDTO product;

    private static PartRevisionDTO P1;
    private static PartRevisionDTO P1C;
    private static PartRevisionDTO P2;
    private static PartRevisionDTO P2C;
    private static PartRevisionDTO P3;
    private static PartRevisionDTO P3S;
    private static PartRevisionDTO P4;
    private static PartRevisionDTO P4C;
    private static PartRevisionDTO P4S;

    /**
     * Product HEAD view
     * <p>
     * - rootPart
     * -- P1
     * ---  P1C (opt)
     * -- P2 (opt)
     * ---  P2C
     * -- P3 (sub P3S)
     * -- P4 (opt) (sub P4S)
     * ---  P4C
     */

    @BeforeClass
    public static void initStructure() throws ApiException {
        workspace = TestUtils.createWorkspace(ProductBaselineApiTest.class.getName());
        createParts();
        createLinks();
        checkinParts();
        createProduct();
        createBaseline();
    }

    @Test
    public void testHeadStructure() throws ApiException {

        ComponentDTO latestView = productsApi.filterProductStructure(workspace.getId(), product.getId(), "latest", "-1", -1, null, false);

        Assert.assertEquals(rootPart.getNumber(), latestView.getNumber());

        Assert.assertTrue(latestView.getAssembly());
        Assert.assertNotNull(latestView.getComponents());
        Assert.assertFalse(latestView.getComponents().isEmpty());

        ComponentDTO linkToP1 = latestView.getComponents().stream()
                .filter(componentDTO -> P1.getNumber().equals(componentDTO.getNumber()))
                .findFirst().orElse(null);

        ComponentDTO linkToP2 = latestView.getComponents().stream()
                .filter(componentDTO -> P2.getNumber().equals(componentDTO.getNumber()))
                .findFirst().orElse(null);

        ComponentDTO linkToP3 = latestView.getComponents().stream()
                .filter(componentDTO -> P3.getNumber().equals(componentDTO.getNumber()))
                .findFirst().orElse(null);

        ComponentDTO linkToP4 = latestView.getComponents().stream()
                .filter(componentDTO -> P4.getNumber().equals(componentDTO.getNumber()))
                .findFirst().orElse(null);

        Assert.assertNotNull(linkToP1);
        Assert.assertNotNull(linkToP2);
        Assert.assertNotNull(linkToP3);
        Assert.assertNotNull(linkToP4);

        Assert.assertNotNull(linkToP1.getComponents());
        Assert.assertFalse(linkToP1.getComponents().isEmpty());
        Assert.assertEquals(P1C.getNumber(), linkToP1.getComponents().get(0).getNumber());
        Assert.assertTrue(linkToP1.getComponents().get(0).getOptional());


    }


    @Test
    public void testPathChoices() throws ApiException {
        List<PathChoiceDTO> choices = productsApi.getBaselineCreationPathChoices(workspace.getId(), product.getId(), String.valueOf(ProductBaselineDTO.TypeEnum.LATEST));

        Assert.assertNotNull(choices);
        Assert.assertEquals(5, choices.size());

        PartUsageLinkDTO choice1 = choices.get(0).getPartUsageLink();
        Assert.assertEquals(P1C.getNumber(), choice1.getComponent().getNumber());
        Assert.assertTrue(choice1.getOptional());

        PartUsageLinkDTO choice2 = choices.get(1).getPartUsageLink();
        Assert.assertEquals(P2.getNumber(), choice2.getComponent().getNumber());
        Assert.assertTrue(choice2.getOptional());

        PartUsageLinkDTO choice3 = choices.get(2).getPartUsageLink();
        Assert.assertEquals(P2C.getNumber(), choice3.getComponent().getNumber());
        Assert.assertTrue(choice3.getOptional());

        PartUsageLinkDTO choice4 = choices.get(3).getPartUsageLink();
        Assert.assertEquals(P3.getNumber(), choice4.getComponent().getNumber());
        Assert.assertNotNull(choice4.getSubstitutes());
        Assert.assertFalse(choice4.getSubstitutes().isEmpty());
        Assert.assertFalse(choice4.getOptional());
        Assert.assertEquals(1, choice4.getSubstitutes().size());
        Assert.assertEquals(P3S.getNumber(), choice4.getSubstitutes().get(0).getSubstitute().getNumber());

        PartUsageLinkDTO choice5 = choices.get(4).getPartUsageLink();
        Assert.assertEquals(P4.getNumber(), choice5.getComponent().getNumber());
        Assert.assertNotNull(choice5.getSubstitutes());
        Assert.assertFalse(choice5.getSubstitutes().isEmpty());
        Assert.assertEquals(P4S.getNumber(), choice5.getSubstitutes().get(0).getSubstitute().getNumber());
        Assert.assertTrue(choice5.getOptional());

    }


    @AfterClass
    public static void deleteWorkspace() throws ApiException {
        workspacesApi.deleteWorkspace(workspace.getId());
    }


    private static void checkinParts() throws ApiException {
        rootPart = checkInPart(rootPart);
        P1 = checkInPart(P1);
        P1C = checkInPart(P1C);
        P2 = checkInPart(P2);
        P2C = checkInPart(P2C);
        P3 = checkInPart(P3);
        P3S = checkInPart(P3S);
        P4 = checkInPart(P4);
        P4C = checkInPart(P4C);
        P4S = checkInPart(P4S);
    }

    private static PartRevisionDTO checkInPart(PartRevisionDTO part) throws ApiException {
        return partApi.checkIn(part.getWorkspaceId(), part.getNumber(), part.getVersion());
    }

    private static void createParts() throws ApiException {
        rootPart = TestUtils.createPart(workspace.getId(), "root");
        P1 = TestUtils.createPart(workspace.getId(), "P1");
        P1C = TestUtils.createPart(workspace.getId(), "P1C");
        P2 = TestUtils.createPart(workspace.getId(), "P2");
        P2C = TestUtils.createPart(workspace.getId(), "P2C");
        P3 = TestUtils.createPart(workspace.getId(), "P3");
        P3S = TestUtils.createPart(workspace.getId(), "P3S");
        P4 = TestUtils.createPart(workspace.getId(), "P4");
        P4S = TestUtils.createPart(workspace.getId(), "P4S");
        P4C = TestUtils.createPart(workspace.getId(), "P4C");
    }

    private static void createLinks() throws ApiException {

        // From top to bottom
        rootPart = link(Arrays.asList(
                createLink(P1, false, null),
                createLink(P2, true, null),
                createLink(P3, false, P3S),
                createLink(P4, true, P4S)
        ), rootPart);

        P1 = link(Collections.singletonList(createLink(P1C, true, null)), P1);
        P2 = link(Collections.singletonList(createLink(P2C, true, null)), P2);
        P4 = link(Collections.singletonList(createLink(P4C, false, null)), P4);
    }

    private static PartRevisionDTO link(List<PartUsageLinkDTO> partUsageLinkDTOs, PartRevisionDTO linkHolder) throws ApiException {
        PartIterationDTO lastIteration = LastIterationHelper.getLastIteration(linkHolder);
        lastIteration.getComponents().addAll(partUsageLinkDTOs);

        return partApi.updatePartIteration(linkHolder.getWorkspaceId(), linkHolder.getNumber(), linkHolder.getVersion(),
                lastIteration.getIteration(), lastIteration);
    }

    private static PartUsageLinkDTO createLink(PartRevisionDTO child, boolean opt, PartRevisionDTO substitutePart) {

        PartUsageLinkDTO link = new PartUsageLinkDTO();

        List<CADInstanceDTO> instances = new ArrayList<>();
        CADInstanceDTO instance = new CADInstanceDTO();
        ComponentDTO component = new ComponentDTO();

        instance.setRotationType(CADInstanceDTO.RotationTypeEnum.ANGLE);
        instance.setRx(TestUtils.randomRotation());
        instance.setRy(TestUtils.randomRotation());
        instance.setRz(TestUtils.randomRotation());
        instance.setTx(TestUtils.randomTranslation());
        instance.setTy(TestUtils.randomTranslation());
        instance.setTz(TestUtils.randomTranslation());
        instances.add(instance);

        component.setNumber(child.getNumber());
        component.setAmount(1.0);
        component.setPartUsageLinkReferenceDescription("Link to " + child.getNumber());

        link.setComponent(component);
        link.setCadInstances(instances);
        link.setOptional(opt);

        if (substitutePart != null) {
            // create substitute link
            PartSubstituteLinkDTO partSubstituteLinkDTO = new PartSubstituteLinkDTO();
            ComponentDTO substitute = new ComponentDTO();
            List<PartSubstituteLinkDTO> substitutes = new ArrayList<>();
            substitute.setNumber(substitutePart.getNumber());
            partSubstituteLinkDTO.setSubstitute(substitute);
            partSubstituteLinkDTO.setCadInstances(instances);
            partSubstituteLinkDTO.setReferenceDescription("Substitute link to " + substitutePart.getNumber());
            substitutes.add(partSubstituteLinkDTO);
            link.setSubstitutes(substitutes);
        }

        return link;
    }


    private static void createProduct() throws ApiException {
        ConfigurationItemDTO productCreation = new ConfigurationItemDTO();
        productCreation.setId(TestUtils.randomString());
        productCreation.setDesignItemNumber(rootPart.getNumber());
        productCreation.setWorkspaceId(workspace.getId());
        productCreation.setDescription("Root part in generated assembly");
        product = productsApi.createConfigurationItem(workspace.getId(), productCreation);
    }

    private static void createBaseline() throws ApiException {
        ProductBaselineCreationDTO baselineCreation = new ProductBaselineCreationDTO();
        baselineCreation.setConfigurationItemId(product.getId());
        baselineCreation.setName("B1");
        baselineCreation.setType(ProductBaselineCreationDTO.TypeEnum.LATEST);
        productBaselineApi.createProductBaseline(workspace.getId(), baselineCreation, false);
    }


}
