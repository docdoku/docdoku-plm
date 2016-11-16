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
import java.util.List;
import java.util.UUID;

public class EffectivityApiTest {

    private PartApi partApi = new PartApi(TestConfig.BASIC_CLIENT);
    private PartsApi partsApi = new PartsApi(TestConfig.BASIC_CLIENT);
    private ProductsApi productsApi = new ProductsApi(TestConfig.BASIC_CLIENT);
    private EffectivityApi effectivityApi = new EffectivityApi(TestConfig.BASIC_CLIENT);

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
    public void createSerialNumberBasedEffectivityTest() throws ApiException {
        this.createPartRevisionDTO();
        this.createConfigurationItemDTO();

        SerialNumberBasedEffectivityDTO serialNumberBasedEffectivityDTO = new SerialNumberBasedEffectivityDTO();
        serialNumberBasedEffectivityDTO.setName(UUID.randomUUID().toString().substring(0, 8));
        SerialNumberBasedEffectivityDTO serialNumberBasedEffectivity = effectivityApi.createSerialNumberBasedEffectivity(
                        serialNumberBasedEffectivityDTO, TestConfig.WORKSPACE, configurationItemDTO.getId(), partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        List<SerialNumberBasedEffectivityDTO> retreiveEffectivities = effectivityApi.getSerialNumberBasedEffectivities(
                TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        Assert.assertEquals(serialNumberBasedEffectivity.getName(), serialNumberBasedEffectivity.getName());
        Assert.assertTrue(retreiveEffectivities.contains(serialNumberBasedEffectivity));

        effectivityApi.deleteEffectivity(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), serialNumberBasedEffectivity.getId());
    }

    @Test
    public void createDateBasedEffectivityTest() throws ApiException {
        this.createPartRevisionDTO();
        this.createConfigurationItemDTO();
        String generatedId = UUID.randomUUID().toString().substring(0, 8);

        DateBasedEffectivityDTO dateBasedEffectivityDTO = new DateBasedEffectivityDTO();
        dateBasedEffectivityDTO.setName(generatedId);
        DateBasedEffectivityDTO dateBasedEffectivity = effectivityApi.createDateBasedEffectivity(
                dateBasedEffectivityDTO, TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        List<DateBasedEffectivityDTO> retreiveEffectivities = effectivityApi.getDateBasedEffectivities(
                TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        Assert.assertEquals(dateBasedEffectivityDTO.getName(), dateBasedEffectivity.getName());
        Assert.assertTrue(retreiveEffectivities.contains(dateBasedEffectivity));

        effectivityApi.deleteEffectivity(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), dateBasedEffectivity.getId());
    }

    @Test
    public void createLotBasedEffectivityTest() throws ApiException {
        this.createPartRevisionDTO();
        this.createConfigurationItemDTO();
        String generatedId = UUID.randomUUID().toString().substring(0, 8);

        LotBasedEffectivityDTO lotBasedEffectivityDTO = new LotBasedEffectivityDTO();
        lotBasedEffectivityDTO.setName(generatedId);
        LotBasedEffectivityDTO lotBasedEffectivity = effectivityApi.createLotBasedEffectivity(
                lotBasedEffectivityDTO, TestConfig.WORKSPACE, configurationItemDTO.getId(), partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        List<LotBasedEffectivityDTO> retreiveEffectivities = effectivityApi.getLotBasedEffectivities(
                TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        Assert.assertEquals(lotBasedEffectivityDTO.getName(), lotBasedEffectivity.getName());
        Assert.assertTrue(retreiveEffectivities.contains(lotBasedEffectivity));

        effectivityApi.deleteEffectivity(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), lotBasedEffectivity.getId());
    }

    @Test
    public void getSerialNumberBasedEffectivityTest() throws ApiException {
        this.createPartRevisionDTO();
        this.createConfigurationItemDTO();
        String generatedId = UUID.randomUUID().toString().substring(0, 8);

        SerialNumberBasedEffectivityDTO serialNumberBasedEffectivityDTO = new SerialNumberBasedEffectivityDTO();
        serialNumberBasedEffectivityDTO.setName(generatedId);
        SerialNumberBasedEffectivityDTO serialNumberBasedEffectivity = effectivityApi.createSerialNumberBasedEffectivity(
                serialNumberBasedEffectivityDTO, TestConfig.WORKSPACE, configurationItemDTO.getId(), partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        SerialNumberBasedEffectivityDTO effectivityDTO = effectivityApi.getSerialNumberBasedEffectivity(serialNumberBasedEffectivity.getId());

        Assert.assertEquals(serialNumberBasedEffectivityDTO.getName(), effectivityDTO.getName());
        effectivityApi.deleteEffectivity(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), serialNumberBasedEffectivity.getId());
    }

    @Test
    public void getDateBasedEffectivityTest() throws ApiException {
        this.createPartRevisionDTO();
        this.createConfigurationItemDTO();
        String generatedId = UUID.randomUUID().toString().substring(0, 8);

        DateBasedEffectivityDTO dateBasedEffectivityDTO = new DateBasedEffectivityDTO();
        dateBasedEffectivityDTO.setName(generatedId);
        DateBasedEffectivityDTO dateBasedEffectivity = effectivityApi.createDateBasedEffectivity(
                dateBasedEffectivityDTO, TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        DateBasedEffectivityDTO effectivityDTO = effectivityApi.getDateBasedEffectivity(dateBasedEffectivity.getId());

        Assert.assertEquals(dateBasedEffectivityDTO.getName(), effectivityDTO.getName());
        effectivityApi.deleteEffectivity(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), dateBasedEffectivity.getId());
    }

    @Test
    public void getLotBasedEffectivityTest() throws ApiException {
        this.createPartRevisionDTO();
        this.createConfigurationItemDTO();
        String generatedId = UUID.randomUUID().toString().substring(0, 8);

        LotBasedEffectivityDTO lotBasedEffectivityDTO = new LotBasedEffectivityDTO();
        lotBasedEffectivityDTO.setName(generatedId);
        LotBasedEffectivityDTO lotBasedEffectivity = effectivityApi.createLotBasedEffectivity(
                lotBasedEffectivityDTO, TestConfig.WORKSPACE, configurationItemDTO.getId(), partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        LotBasedEffectivityDTO effectivityDTO = effectivityApi.getLotBasedEffectivity(lotBasedEffectivity.getId());

        Assert.assertEquals(lotBasedEffectivityDTO.getName(), effectivityDTO.getName());
        effectivityApi.deleteEffectivity(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), lotBasedEffectivity.getId());
    }

    @Test
    public void getSerialNumberBasedEffectivitiesTest() throws ApiException {
        this.createPartRevisionDTO();
        this.createConfigurationItemDTO();
        String generatedId = UUID.randomUUID().toString().substring(0, 8);

        SerialNumberBasedEffectivityDTO serialNumberBasedEffectivityDTO = new SerialNumberBasedEffectivityDTO();
        serialNumberBasedEffectivityDTO.setName(generatedId);
        SerialNumberBasedEffectivityDTO serialNumberBasedEffectivity = effectivityApi.createSerialNumberBasedEffectivity(
                serialNumberBasedEffectivityDTO, TestConfig.WORKSPACE, configurationItemDTO.getId(), partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        List<SerialNumberBasedEffectivityDTO> effectivityDTOList = effectivityApi.getSerialNumberBasedEffectivities(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());
        System.out.println(effectivityDTOList.size());
        EffectivityDTO lastInsertedEffectivityDTO = effectivityDTOList.get(effectivityDTOList.size()-1);

        Assert.assertEquals(serialNumberBasedEffectivityDTO.getName(), lastInsertedEffectivityDTO.getName());
        effectivityApi.deleteEffectivity(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), serialNumberBasedEffectivity.getId());
    }

    @Test
    public void getDateBasedEffectivitiesTest() throws ApiException {
        this.createPartRevisionDTO();
        this.createConfigurationItemDTO();
        String generatedId = UUID.randomUUID().toString().substring(0, 8);

        DateBasedEffectivityDTO dateBasedEffectivityDTO = new DateBasedEffectivityDTO();
        dateBasedEffectivityDTO.setName(generatedId);
        DateBasedEffectivityDTO dateBasedEffectivity = effectivityApi.createDateBasedEffectivity(
                dateBasedEffectivityDTO, TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        List<DateBasedEffectivityDTO> effectivityDTOList = effectivityApi.getDateBasedEffectivities(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());
        EffectivityDTO lastInsertedEffectivityDTO = effectivityDTOList.get(effectivityDTOList.size()-1);

        Assert.assertEquals(dateBasedEffectivityDTO.getName(), lastInsertedEffectivityDTO.getName());
        effectivityApi.deleteEffectivity(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), dateBasedEffectivity.getId());
    }

    @Test
    public void getLotBasedEffectivitiesTest() throws ApiException {
        this.createPartRevisionDTO();
        this.createConfigurationItemDTO();
        String generatedId = UUID.randomUUID().toString().substring(0, 8);

        LotBasedEffectivityDTO lotBasedEffectivityDTO = new LotBasedEffectivityDTO();
        lotBasedEffectivityDTO.setName(generatedId);
        LotBasedEffectivityDTO lotBasedEffectivity = effectivityApi.createLotBasedEffectivity(
                lotBasedEffectivityDTO, TestConfig.WORKSPACE, configurationItemDTO.getId(), partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        List<LotBasedEffectivityDTO> effectivityDTOList = effectivityApi.getLotBasedEffectivities(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());
        EffectivityDTO lastInsertedEffectivityDTO = effectivityDTOList.get(effectivityDTOList.size()-1);

        Assert.assertEquals(lotBasedEffectivityDTO.getName(), lastInsertedEffectivityDTO.getName());
        effectivityApi.deleteEffectivity(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), lotBasedEffectivity.getId());
    }

    @Test
    public void updateEffectivityTest() throws ApiException {
        this.createPartRevisionDTO();
        this.createConfigurationItemDTO();
        String updatedName = UUID.randomUUID().toString().substring(0, 8);

        LotBasedEffectivityDTO lotBasedEffectivityDTO = new LotBasedEffectivityDTO();
        lotBasedEffectivityDTO.setName(UUID.randomUUID().toString().substring(0, 8));
        LotBasedEffectivityDTO lotBasedEffectivity = effectivityApi.createLotBasedEffectivity(
                lotBasedEffectivityDTO, TestConfig.WORKSPACE, configurationItemDTO.getId(), partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        lotBasedEffectivityDTO.setName(updatedName);
        EffectivityDTO updatedEffectivity = effectivityApi.updateEffectivity(lotBasedEffectivity.getId(), lotBasedEffectivityDTO);
        EffectivityDTO effectivityDTO = effectivityApi.getLotBasedEffectivity(lotBasedEffectivity.getId());

        Assert.assertEquals(updatedEffectivity.getId(), effectivityDTO.getId());
        Assert.assertEquals(updatedEffectivity.getName(), effectivityDTO.getName());
        Assert.assertNotEquals(lotBasedEffectivity.getName(), effectivityDTO.getName());
        effectivityApi.deleteEffectivity(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), lotBasedEffectivity.getId());
    }

    @Test
    public void deleteEffectivityTest() throws ApiException {
        this.createPartRevisionDTO();
        this.createConfigurationItemDTO();
        String updatedName = UUID.randomUUID().toString().substring(0, 8);

        LotBasedEffectivityDTO lotBasedEffectivityDTO = new LotBasedEffectivityDTO();
        lotBasedEffectivityDTO.setName(UUID.randomUUID().toString().substring(0, 8));
        LotBasedEffectivityDTO lotBasedEffectivity = effectivityApi.createLotBasedEffectivity(
                lotBasedEffectivityDTO, TestConfig.WORKSPACE, configurationItemDTO.getId(), partRevisionDTO.getNumber(), partRevisionDTO.getVersion());

        effectivityApi.deleteEffectivity(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion(), lotBasedEffectivity.getId());
        List<LotBasedEffectivityDTO> effectivities = effectivityApi.getLotBasedEffectivities(TestConfig.WORKSPACE, partRevisionDTO.getNumber(), partRevisionDTO.getVersion());
        Assert.assertFalse(effectivities.contains(lotBasedEffectivity));
    }
}
