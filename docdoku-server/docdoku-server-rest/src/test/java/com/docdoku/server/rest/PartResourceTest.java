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

package com.docdoku.server.rest;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.product.PartUsageLink;
import com.docdoku.core.services.IBinaryStorageManagerLocal;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.util.ResourceUtil;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class PartResourceTest {

    @InjectMocks
    PartResource partResource = new PartResource();
    @Mock
    private IProductManagerLocal productService;
    @Mock
    private IBinaryStorageManagerLocal storageManager;
    @Mock
    private EntityManager em;
    @Mock
    private IMailerLocal mailer;
    @Mock
    private IUserManagerLocal userManager;
    @Spy
    private Workspace workspace = new Workspace();
    @Spy
    private User user = new User(workspace, new Account("login", "user", "test@docdoku.com", "en", new Date(), null));
    @Spy
    private PartMaster partMaster = new PartMaster(workspace, "partNumber", user);
    @Spy
    private PartMaster subPartMaster = new PartMaster(workspace, "SubPartNumber", user);
    @Spy
    private Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();

    @Before
    public void setup() throws Exception {
        initMocks(this);

    }

    @Test
    public void createComponents() {
        //Given
        PartIterationDTO data = new PartIterationDTO(ResourceUtil.WORKSPACE_ID, "partName", "partNumber", "A", 1);
        List<PartUsageLinkDTO> partUsageLinkDTOs = new ArrayList<>();
        PartUsageLinkDTO partUsageLinkDTO = new PartUsageLinkDTO();
        partUsageLinkDTO.setAmount(2);
        partUsageLinkDTO.setUnit("");
        partUsageLinkDTO.setOptional(true);
        partUsageLinkDTO.setComment("comment part usage link");
        partUsageLinkDTO.setReferenceDescription("description part usage link");
        ComponentDTO componentDTO = new ComponentDTO("component01");
        componentDTO.setStandardPart(false);
        partUsageLinkDTO.setComponent(componentDTO);
        List<PartSubstituteLinkDTO> substituteDTOs = new ArrayList<>();
        PartSubstituteLinkDTO substituteLinkDTO = new PartSubstituteLinkDTO();
        substituteLinkDTO.setAmount(3);
        substituteLinkDTO.setUnit("Kg");
        ComponentDTO subComponentDTO = new ComponentDTO("subComponent01");
        substituteLinkDTO.setSubstitute(subComponentDTO);
        List<CADInstanceDTO> cadInstanceDTOs = new ArrayList<>();
        List<CADInstanceDTO> subCadInstanceDTOs = new ArrayList<>();
        cadInstanceDTOs.add(new CADInstanceDTO(12.0, 12.0, 12.0, 62.0, 24.0, 95.0));
        cadInstanceDTOs.add(new CADInstanceDTO(22.0, 12.0, 72.0, 52.0, 14.0, 45.0));
        subCadInstanceDTOs.add(new CADInstanceDTO(10.0, 11.0, 12.0, 13.0, 14.0, 15.0));
        subCadInstanceDTOs.add(new CADInstanceDTO(110.0, 10.0, 10.0, 52.0, 14.0, 45.0));
        subCadInstanceDTOs.add(new CADInstanceDTO(120.0, 10.0, 10.0, 52.0, 14.0, 45.0));

        substituteLinkDTO.setCadInstances(subCadInstanceDTOs);
        substituteDTOs.add(substituteLinkDTO);
        partUsageLinkDTO.setSubstitutes(substituteDTOs);
        partUsageLinkDTO.setCadInstances(cadInstanceDTOs);
        partUsageLinkDTOs.add(partUsageLinkDTO);
        data.setComponents(partUsageLinkDTOs);
        List<PartUsageLink> newComponents = new ArrayList<>();

        //when
        try {
            Mockito.when(productService.partMasterExists(Matchers.any(PartMasterKey.class))).thenReturn(false);
            Mockito.when(userManager.checkWorkspaceWriteAccess(ResourceUtil.WORKSPACE_ID)).thenReturn(user);
            Mockito.when(partResource.findOrCreatePartMaster(ResourceUtil.WORKSPACE_ID, componentDTO)).thenReturn(partMaster);
            Mockito.when(partResource.findOrCreatePartMaster(ResourceUtil.WORKSPACE_ID, subComponentDTO)).thenReturn(subPartMaster);

            newComponents = partResource.createComponents(ResourceUtil.WORKSPACE_ID, partUsageLinkDTOs);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            fail("Part Creation failed");
        }

        //Then
        assertNotNull(newComponents);
        assertTrue(newComponents.size() == 1);
        assertTrue("description part usage link".equals(newComponents.get(0).getReferenceDescription()));
        assertTrue("partNumber".equals(newComponents.get(0).getComponent().getNumber()));
        assertTrue(newComponents.get(0).getAmount() == 2);
        assertTrue(newComponents.get(0).getUnit().isEmpty());
        //check that the component is optional
        assertTrue(newComponents.get(0).isOptional());
        //check the amount of CADInstances
        assertTrue(newComponents.get(0).getCadInstances().size() == 2);

        // check if the cad instances mapping of the part usage link is correct
        assertTrue(newComponents.get(0).getCadInstances().get(0).getRx() == 12.0);
        assertTrue(newComponents.get(0).getCadInstances().get(0).getRy() == 12.0);
        assertTrue(newComponents.get(0).getCadInstances().get(0).getRz() == 12.0);
        assertTrue(newComponents.get(0).getCadInstances().get(0).getTx() == 62.0);
        assertTrue(newComponents.get(0).getCadInstances().get(0).getTy() == 24.0);
        assertTrue(newComponents.get(0).getCadInstances().get(0).getTz() == 95.0);

        assertTrue(newComponents.get(0).getCadInstances().get(1).getRx() == 22.0);
        assertTrue(newComponents.get(0).getCadInstances().get(1).getRy() == 12.0);
        assertTrue(newComponents.get(0).getCadInstances().get(1).getRz() == 72.0);
        assertTrue(newComponents.get(0).getCadInstances().get(1).getTx() == 52.0);
        assertTrue(newComponents.get(0).getCadInstances().get(1).getTy() == 14.0);
        assertTrue(newComponents.get(0).getCadInstances().get(1).getTz() == 45.0);
        // check if the cad instances mapping of the substitute part usage link is correct
        assertTrue(newComponents.get(0).getSubstitutes().get(0).getCadInstances().size() == 3);
        assertTrue(newComponents.get(0).getSubstitutes().get(0).getCadInstances().get(0).getRx() == 10);
        assertTrue(newComponents.get(0).getSubstitutes().get(0).getCadInstances().get(0).getRy() == 11);
        assertTrue(newComponents.get(0).getSubstitutes().get(0).getCadInstances().get(0).getRz() == 12);
        assertTrue(newComponents.get(0).getSubstitutes().get(0).getCadInstances().get(0).getTx() == 13.0);
        assertTrue(newComponents.get(0).getSubstitutes().get(0).getCadInstances().get(0).getTy() == 14.0);
        assertTrue(newComponents.get(0).getSubstitutes().get(0).getCadInstances().get(0).getTz() == 15.0);


    }
}