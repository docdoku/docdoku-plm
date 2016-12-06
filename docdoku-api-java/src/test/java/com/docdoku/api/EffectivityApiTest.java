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
import com.docdoku.api.services.EffectivityApi;
import com.docdoku.api.services.PartApi;
import com.docdoku.api.services.PartsApi;
import com.docdoku.api.services.ProductsApi;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class EffectivityApiTest {

    private PartApi partApi = new PartApi(TestConfig.REGULAR_USER_CLIENT);
    private PartsApi partsApi = new PartsApi(TestConfig.REGULAR_USER_CLIENT);
    private ProductsApi productsApi = new ProductsApi(TestConfig.REGULAR_USER_CLIENT);
    private EffectivityApi effectivityApi = new EffectivityApi(TestConfig.REGULAR_USER_CLIENT);

    private static String partNumber = UUID.randomUUID().toString().substring(0, 8);

    private PartRevisionDTO partRevisionDTO = null;
    private ConfigurationItemDTO configurationItemDTO = null;

    private PartRevisionDTO createPartRevisionDTO() throws ApiException {
        partNumber = UUID.randomUUID().toString().substring(0, 8);

        PartCreationDTO part = new PartCreationDTO();
        part.setNumber(partNumber);
        partsApi.createNewPart(TestConfig.WORKSPACE, part);

        partRevisionDTO = partApi.getPartRevision(TestConfig.WORKSPACE, partNumber, "A");
        PartIterationDTO i1 = LastIterationHelper.getLastIteration(partRevisionDTO);
        List<PartUsageLinkDTO> components = new ArrayList<>();
        i1.setComponents(components);

        partApi.updatePartIteration(TestConfig.WORKSPACE, partNumber, "A", 1, i1);
        return partRevisionDTO;
    }

    private ConfigurationItemDTO createConfigurationItemDTO() throws ApiException {
        // Configuration Item creation
        configurationItemDTO = new ConfigurationItemDTO();
        configurationItemDTO.setId(UUID.randomUUID().toString().substring(0, 8));
        configurationItemDTO.setDesignItemNumber(partNumber);
        configurationItemDTO.setDescription("Generated product by tests");
        configurationItemDTO.setWorkspaceId(TestConfig.WORKSPACE);

        productsApi.createConfigurationItem(TestConfig.WORKSPACE, configurationItemDTO);
        return configurationItemDTO;
    }

    @Test
    public void createEffectivitiesTest() throws ApiException {

        createPartRevisionDTO();
        createConfigurationItemDTO();

        String workspaceId = TestConfig.WORKSPACE;

        ConfigurationItemKey configurationItemKey = new ConfigurationItemKey();
        configurationItemKey.setId(configurationItemDTO.getId());
        configurationItemKey.setWorkspace(configurationItemDTO.getWorkspaceId());

        EffectivityDTO effectivityDTO;

        // Creation of Serial Number Based Effectivity
        effectivityDTO = new EffectivityDTO();
        effectivityDTO.setConfigurationItemKey(configurationItemKey);
        effectivityDTO.setDescription("Generated effectivity by tests");
        effectivityDTO.setTypeEffectivity(EffectivityDTO.TypeEffectivityEnum.SERIALNUMBERBASEDEFFECTIVITY);
        effectivityDTO.setStartNumber("1");
        effectivityDTO.setEndNumber("1");
        effectivityDTO.setName(UUID.randomUUID().toString().substring(0, 8));

        EffectivityDTO createdEffectivityDTO = partsApi.createEffectivity(effectivityDTO, workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());
        Assert.assertEquals(createdEffectivityDTO.getTypeEffectivity(), EffectivityDTO.TypeEffectivityEnum.SERIALNUMBERBASEDEFFECTIVITY);
        Assert.assertEquals(createdEffectivityDTO.getName(), effectivityDTO.getName());

        partsApi.deleteEffectivity(workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), createdEffectivityDTO.getId());

        // Creation of Date Based Effectivity
        effectivityDTO = new EffectivityDTO();
        effectivityDTO.setConfigurationItemKey(configurationItemKey);
        effectivityDTO.setDescription("Generated effectivity by tests");
        effectivityDTO.setTypeEffectivity(EffectivityDTO.TypeEffectivityEnum.DATEBASEDEFFECTIVITY);
        effectivityDTO.setStartDate(new Date());
        effectivityDTO.setEndDate(new Date());
        effectivityDTO.setName(UUID.randomUUID().toString().substring(0, 8));

        createdEffectivityDTO = partsApi.createEffectivity(effectivityDTO, workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());
        Assert.assertEquals(createdEffectivityDTO.getTypeEffectivity(), EffectivityDTO.TypeEffectivityEnum.DATEBASEDEFFECTIVITY);
        Assert.assertEquals(createdEffectivityDTO.getName(), effectivityDTO.getName());

        partsApi.deleteEffectivity(workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), createdEffectivityDTO.getId());

        // Creation of Lot Based Effectivity
        effectivityDTO = new EffectivityDTO();
        effectivityDTO.setConfigurationItemKey(configurationItemKey);
        effectivityDTO.setDescription("Generated effectivity by tests");
        effectivityDTO.setTypeEffectivity(EffectivityDTO.TypeEffectivityEnum.LOTBASEDEFFECTIVITY);
        effectivityDTO.setStartLotId("1");
        effectivityDTO.setEndLotId("1");
        effectivityDTO.setName(UUID.randomUUID().toString().substring(0, 8));

        createdEffectivityDTO = partsApi.createEffectivity(effectivityDTO, workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());
        Assert.assertEquals(createdEffectivityDTO.getTypeEffectivity(), EffectivityDTO.TypeEffectivityEnum.LOTBASEDEFFECTIVITY);
        Assert.assertEquals(createdEffectivityDTO.getName(), effectivityDTO.getName());

        partsApi.deleteEffectivity(workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), createdEffectivityDTO.getId());
        productsApi.deleteConfigurationItem(configurationItemKey.getWorkspace(), configurationItemKey.getId());

    }

    @Test
    public void getEffectivitiesTest() throws ApiException {

        createPartRevisionDTO();
        createConfigurationItemDTO();

        String workspaceId = TestConfig.WORKSPACE;

        ConfigurationItemKey configurationItemKey = new ConfigurationItemKey();
        configurationItemKey.setId(configurationItemDTO.getId());
        configurationItemKey.setWorkspace(configurationItemDTO.getWorkspaceId());

        EffectivityDTO effectivityDTO = new EffectivityDTO();
        effectivityDTO.setConfigurationItemKey(configurationItemKey);
        effectivityDTO.setDescription("Generated effectivity by tests");
        effectivityDTO.setTypeEffectivity(EffectivityDTO.TypeEffectivityEnum.SERIALNUMBERBASEDEFFECTIVITY);
        effectivityDTO.setStartNumber("1");
        effectivityDTO.setEndNumber("1");
        effectivityDTO.setName(UUID.randomUUID().toString().substring(0, 8));
        EffectivityDTO createdEffectivityDTO = partsApi.createEffectivity(effectivityDTO, workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        List<EffectivityDTO> effectivityDTOList = partsApi.getEffectivities(workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());
        Assert.assertTrue(effectivityDTOList.contains(createdEffectivityDTO));
        Assert.assertEquals(effectivityDTOList.get(effectivityDTOList.indexOf(createdEffectivityDTO)).getStartNumber(), "1");

        partsApi.deleteEffectivity(workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), createdEffectivityDTO.getId());
        productsApi.deleteConfigurationItem(configurationItemKey.getWorkspace(), configurationItemKey.getId());

    }

    @Test
    public void getEffectivityTest() throws ApiException {

        createPartRevisionDTO();
        createConfigurationItemDTO();

        String workspaceId = TestConfig.WORKSPACE;

        ConfigurationItemKey configurationItemKey = new ConfigurationItemKey();
        configurationItemKey.setId(configurationItemDTO.getId());
        configurationItemKey.setWorkspace(configurationItemDTO.getWorkspaceId());

        EffectivityDTO effectivityDTO = new EffectivityDTO();
        effectivityDTO.setConfigurationItemKey(configurationItemKey);
        effectivityDTO.setDescription("Generated effectivity by tests");
        effectivityDTO.setTypeEffectivity(EffectivityDTO.TypeEffectivityEnum.SERIALNUMBERBASEDEFFECTIVITY);
        effectivityDTO.setStartNumber("1");
        effectivityDTO.setEndNumber("1");
        effectivityDTO.setName(UUID.randomUUID().toString().substring(0, 8));
        EffectivityDTO createdEffectivityDTO = partsApi.createEffectivity(effectivityDTO, workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        EffectivityDTO retreivedEffectivityDTO = effectivityApi.getEffectivity(createdEffectivityDTO.getId());
        Assert.assertTrue(retreivedEffectivityDTO != null);
        Assert.assertEquals(retreivedEffectivityDTO.getName(), createdEffectivityDTO.getName());
        Assert.assertEquals(retreivedEffectivityDTO.getTypeEffectivity(), createdEffectivityDTO.getTypeEffectivity());

        partsApi.deleteEffectivity(workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), createdEffectivityDTO.getId());
        productsApi.deleteConfigurationItem(configurationItemKey.getWorkspace(), configurationItemKey.getId());
    }

    @Test
    public void updateEffectivityTest() throws ApiException {

        createPartRevisionDTO();
        createConfigurationItemDTO();

        String workspaceId = TestConfig.WORKSPACE;

        ConfigurationItemKey configurationItemKey = new ConfigurationItemKey();
        configurationItemKey.setId(configurationItemDTO.getId());
        configurationItemKey.setWorkspace(configurationItemDTO.getWorkspaceId());

        EffectivityDTO effectivityDTO = new EffectivityDTO();
        effectivityDTO.setConfigurationItemKey(configurationItemKey);
        effectivityDTO.setDescription("Generated effectivity by tests");
        effectivityDTO.setTypeEffectivity(EffectivityDTO.TypeEffectivityEnum.SERIALNUMBERBASEDEFFECTIVITY);
        effectivityDTO.setStartNumber("1");
        effectivityDTO.setEndNumber("1");
        effectivityDTO.setName(UUID.randomUUID().toString().substring(0, 8));
        EffectivityDTO createdEffectivityDTO = partsApi.createEffectivity(effectivityDTO, workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        effectivityDTO.setName(UUID.randomUUID().toString().substring(0, 8));
        effectivityDTO.setStartNumber("2");
        EffectivityDTO updatedEffectivityDTO = effectivityApi.updateEffectivity(createdEffectivityDTO.getId(), effectivityDTO);

        Assert.assertNotEquals(createdEffectivityDTO.getName(), updatedEffectivityDTO.getName());
        Assert.assertNotEquals(createdEffectivityDTO.getStartNumber(), updatedEffectivityDTO.getStartNumber());
        Assert.assertEquals(createdEffectivityDTO.getDescription(), updatedEffectivityDTO.getDescription());
        Assert.assertEquals(createdEffectivityDTO.getTypeEffectivity(), updatedEffectivityDTO.getTypeEffectivity());
        Assert.assertEquals(createdEffectivityDTO.getEndNumber(), updatedEffectivityDTO.getEndNumber());

        partsApi.deleteEffectivity(workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), createdEffectivityDTO.getId());
        productsApi.deleteConfigurationItem(configurationItemKey.getWorkspace(), configurationItemKey.getId());

    }

    @Test
    public void deleteEffectivityTest() throws ApiException {

        createPartRevisionDTO();
        createConfigurationItemDTO();

        String workspaceId = TestConfig.WORKSPACE;

        ConfigurationItemKey configurationItemKey = new ConfigurationItemKey();
        configurationItemKey.setId(configurationItemDTO.getId());
        configurationItemKey.setWorkspace(configurationItemDTO.getWorkspaceId());

        EffectivityDTO effectivityDTO = new EffectivityDTO();
        effectivityDTO.setConfigurationItemKey(configurationItemKey);
        effectivityDTO.setDescription("Generated effectivity by tests");
        effectivityDTO.setTypeEffectivity(EffectivityDTO.TypeEffectivityEnum.SERIALNUMBERBASEDEFFECTIVITY);
        effectivityDTO.setStartNumber("1");
        effectivityDTO.setEndNumber("1");
        effectivityDTO.setName(UUID.randomUUID().toString().substring(0, 8));
        EffectivityDTO createdEffectivityDTO = partsApi.createEffectivity(effectivityDTO, workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        List<EffectivityDTO> effectivityDTOList = partsApi.getEffectivities(workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());
        Assert.assertTrue(effectivityDTOList.contains(createdEffectivityDTO));

        partsApi.deleteEffectivity(workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), createdEffectivityDTO.getId());

        effectivityDTOList = partsApi.getEffectivities(workspaceId, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());
        Assert.assertFalse(effectivityDTOList.contains(createdEffectivityDTO));
    }

}
