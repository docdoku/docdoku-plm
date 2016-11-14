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

    private ConfigurationItemDTO createConfigurationItemDTO() throws ApiException {
        UserDTO userDTO = new UserDTO();
        userDTO.setLogin(TestConfig.LOGIN);
        userDTO.setWorkspaceId(TestConfig.WORKSPACE);

        // Part creation
        String partNumber = UUID.randomUUID().toString().substring(0, 8);
        PartCreationDTO part = new PartCreationDTO();
        part.setNumber(partNumber);
        partsApi.createNewPart(TestConfig.WORKSPACE, part);

        PartRevisionDTO p1 = partApi.getPartRevision(TestConfig.WORKSPACE, partNumber, "A");
        PartIterationDTO i1 = LastIterationHelper.getLastIteration(p1);
        List<PartUsageLinkDTO> components = new ArrayList<>();
        i1.setComponents(components);

        partApi.updatePartIteration(TestConfig.WORKSPACE, partNumber, "A", 1, i1);

        // Configuration Item creation
        ConfigurationItemDTO product = new ConfigurationItemDTO();
        product.setId(UUID.randomUUID().toString().substring(0,8));
        product.setDesignItemNumber(partNumber);
        product.setDescription("Generated product by tests");
        product.setWorkspaceId(TestConfig.WORKSPACE);

        return productsApi.createConfigurationItem(TestConfig.WORKSPACE, product);
    }

    @Test
    public void createSerialNumberBasedEffectivityTest() throws ApiException {
        ConfigurationItemDTO configurationItemDTO = this.createConfigurationItemDTO();

        SerialNumberBasedEffectivityDTO serialNumberBasedEffectivityDTO = new SerialNumberBasedEffectivityDTO();
        serialNumberBasedEffectivityDTO.setName(UUID.randomUUID().toString().substring(0, 8));
        SerialNumberBasedEffectivityDTO serialNumberBasedEffectivity = new EffectivityApi(TestConfig.BASIC_CLIENT)
                .createSerialNumberBasedEffectivity(serialNumberBasedEffectivityDTO, TestConfig.WORKSPACE, configurationItemDTO.getId());
        Assert.assertEquals(serialNumberBasedEffectivity.getName(), serialNumberBasedEffectivityDTO.getName());
    }

    @Test
    public void createDateBasedEffectivityTest() throws ApiException {
        ConfigurationItemDTO configurationItemDTO = this.createConfigurationItemDTO();
        String generatedId = UUID.randomUUID().toString().substring(0, 8);

        DateBasedEffectivityDTO dateBasedEffectivityDTO = new DateBasedEffectivityDTO();
        dateBasedEffectivityDTO.setName(generatedId);
        DateBasedEffectivityDTO dateBasedEffectivity = effectivityApi.createDateBasedEffectivity(dateBasedEffectivityDTO,
                TestConfig.WORKSPACE, configurationItemDTO.getId());
        Assert.assertEquals(dateBasedEffectivity.getName(), dateBasedEffectivityDTO.getName());

        effectivityApi.deleteEffectivity(dateBasedEffectivity.getId());
    }

    @Test
    public void createLotBasedEffectivityTest() throws ApiException {
        ConfigurationItemDTO configurationItemDTO = this.createConfigurationItemDTO();
        String generatedId = UUID.randomUUID().toString().substring(0, 8);

        LotBasedEffectivityDTO lotBasedEffectivityDTO = new LotBasedEffectivityDTO();
        lotBasedEffectivityDTO.setName(generatedId);
        LotBasedEffectivityDTO lotBasedEffectivity = effectivityApi.createLotBasedEffectivity(lotBasedEffectivityDTO,
                TestConfig.WORKSPACE, configurationItemDTO.getId());
        Assert.assertEquals(lotBasedEffectivity.getName(), lotBasedEffectivityDTO.getName());

        effectivityApi.deleteEffectivity(lotBasedEffectivity.getId());
    }

    @Test
    public void getEffectivityTest() throws ApiException {
        ConfigurationItemDTO configurationItemDTO = this.createConfigurationItemDTO();
        String generatedId = UUID.randomUUID().toString().substring(0, 8);

        LotBasedEffectivityDTO lotBasedEffectivityDTO = new LotBasedEffectivityDTO();
        lotBasedEffectivityDTO.setName(generatedId);
        LotBasedEffectivityDTO lotBasedEffectivity = effectivityApi.createLotBasedEffectivity(lotBasedEffectivityDTO, TestConfig.WORKSPACE, configurationItemDTO.getId());

        EffectivityDTO effectivityDTO = effectivityApi.getEffectivity(lotBasedEffectivity.getId());

        Assert.assertEquals(lotBasedEffectivityDTO.getName(), effectivityDTO.getName());
        effectivityApi.deleteEffectivity(lotBasedEffectivity.getId());
    }

    @Test
    public void getEffectivitiesTest() throws ApiException {
        ConfigurationItemDTO configurationItemDTO = this.createConfigurationItemDTO();
        String generatedId = UUID.randomUUID().toString().substring(0, 8);

        LotBasedEffectivityDTO lotBasedEffectivityDTO = new LotBasedEffectivityDTO();
        lotBasedEffectivityDTO.setName(generatedId);
        LotBasedEffectivityDTO lotBasedEffectivity = effectivityApi.createLotBasedEffectivity(lotBasedEffectivityDTO, TestConfig.WORKSPACE, configurationItemDTO.getId());

        List<EffectivityDTO> effectivityDTOList = effectivityApi.getEffectivities();
        EffectivityDTO lastInsertedEffectivityDTO = effectivityDTOList.get(effectivityDTOList.size()-1);

        Assert.assertEquals(lotBasedEffectivityDTO.getName(), lastInsertedEffectivityDTO.getName());
        effectivityApi.deleteEffectivity(lotBasedEffectivity.getId());
    }

    @Test
    public void updateEffectivityTest() throws ApiException {
        ConfigurationItemDTO configurationItemDTO = this.createConfigurationItemDTO();
        String updatedName = UUID.randomUUID().toString().substring(0, 8);

        LotBasedEffectivityDTO lotBasedEffectivityDTO = new LotBasedEffectivityDTO();
        lotBasedEffectivityDTO.setName(UUID.randomUUID().toString().substring(0, 8));
        LotBasedEffectivityDTO lotBasedEffectivity = effectivityApi.createLotBasedEffectivity(lotBasedEffectivityDTO, TestConfig.WORKSPACE, configurationItemDTO.getId());

        lotBasedEffectivityDTO.setName(updatedName);
        EffectivityDTO updatedEffectivity = effectivityApi.updateEffectivity(lotBasedEffectivity.getId(), lotBasedEffectivityDTO);
        EffectivityDTO effectivityDTO = effectivityApi.getEffectivity(lotBasedEffectivity.getId());

        Assert.assertEquals(updatedEffectivity.getId(), effectivityDTO.getId());
        Assert.assertEquals(updatedEffectivity.getName(), effectivityDTO.getName());
        Assert.assertNotEquals(lotBasedEffectivity.getName(), effectivityDTO.getName());
        effectivityApi.deleteEffectivity(lotBasedEffectivity.getId());
    }

    @Test
    public void deleteEffectivityTest() throws ApiException {
        ConfigurationItemDTO configurationItemDTO = this.createConfigurationItemDTO();
        String updatedName = UUID.randomUUID().toString().substring(0, 8);

        LotBasedEffectivityDTO lotBasedEffectivityDTO = new LotBasedEffectivityDTO();
        lotBasedEffectivityDTO.setName(UUID.randomUUID().toString().substring(0, 8));
        LotBasedEffectivityDTO lotBasedEffectivity = effectivityApi.createLotBasedEffectivity(lotBasedEffectivityDTO, TestConfig.WORKSPACE, configurationItemDTO.getId());

        effectivityApi.deleteEffectivity(lotBasedEffectivity.getId());
        List<EffectivityDTO> effectivities = effectivityApi.getEffectivities();
        Assert.assertFalse(effectivities.contains(lotBasedEffectivity));
    }
}
