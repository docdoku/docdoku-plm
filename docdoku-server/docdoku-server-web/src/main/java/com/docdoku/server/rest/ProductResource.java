/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */
package com.docdoku.server.rest;

import com.docdoku.core.product.CADInstance;
import com.docdoku.core.product.ConfigSpec;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.Geometry;
import com.docdoku.core.product.LatestConfigSpec;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartUsageLink;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.ConfigurationItemNotFoundException;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.NotAllowedException;
import com.docdoku.core.services.WorkspaceNotFoundException;
import com.docdoku.server.rest.dto.CADInstanceDTO;
import com.docdoku.server.rest.dto.ConfigurationItemDTO;
import com.docdoku.server.rest.dto.PartDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

/**
 *
 * @author Florent Garin
 */
@Stateless
@Path("workspaces/{workspaceId}/products")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ProductResource {

    @EJB
    private IProductManagerLocal productService;
    private Mapper mapper;

    public ProductResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public ConfigurationItemDTO[] getRootProducts(@PathParam("workspaceId") String workspaceId) {
        try {

            String wksId = Tools.stripTrailingSlash(workspaceId);
            ConfigurationItem[] cis = productService.getConfigurationItems(wksId);
            ConfigurationItemDTO[] dtos = new ConfigurationItemDTO[cis.length];

            for (int i = 0; i < cis.length; i++) {
                dtos[i] = new ConfigurationItemDTO(cis[i].getId(), cis[i].getWorkspaceId(), cis[i].getDescription());
            }

            return dtos;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @GET
    @Path("{ciId}")
    @Produces("application/json;charset=UTF-8")
    public PartDTO filterProductStructure(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType) {
        try {
            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            ConfigSpec cs = new LatestConfigSpec();
            PartMaster root = productService.filterProductStructure(ciKey, cs);

            PartUsageLink rootFakeUsageLink = new PartUsageLink();
            rootFakeUsageLink.setComponent(root);
            //TODO REMOVE STUB
//            PartDTO rootDTO = new PartDTO("Airbus", "A400M");
//            rootDTO.setVersion("B");
//            rootDTO.setIteration(3);
//            List<PartDTO> lstComp = new ArrayList<PartDTO>();
//            rootDTO.setComponents(lstComp);
//            PartDTO dto1 = new PartDTO("Airbus", "M5391106020000");
//            dto1.setVersion("A");
//            dto1.setIteration(1);
//            PartDTO dto2 = new PartDTO("Airbus", "M53S1030020000");
//            dto2.setStandardPart(true);
//            dto2.setVersion("A");
//            dto2.setIteration(1);
//            PartDTO dto3 = new PartDTO("Airbus", "M53S1030020000");
//            dto3.setStandardPart(true);
//            dto3.setVersion("A");
//            dto3.setIteration(1);
//            PartDTO dto4 = new PartDTO("Airbus", "M5311173120000");
//            dto4.setVersion("A");
//            dto4.setIteration(1);
//            PartDTO dto5 = new PartDTO("Airbus", "M5221123720000");
//            dto5.setVersion("A");
//            dto5.setIteration(1);
//            PartDTO dto6 = new PartDTO("Airbus", "M5311002120100");
//            List<PartDTO> lstComp2 = new ArrayList<PartDTO>();
//            dto6.setComponents(lstComp2);
//            PartDTO dto61 = new PartDTO("Airbus", "M531102622000050");
//            dto61.setVersion("C");
//            dto61.setIteration(1);
//            PartDTO dto62 = new PartDTO("Airbus", "M531104832000050");
//            dto62.setStandardPart(true);
//            dto62.setVersion("A");
//            dto62.setIteration(4);
//            PartDTO dto63 = new PartDTO("Airbus", "M53S104082000050");
//            dto63.setStandardPart(true);
//            dto63.setVersion("A");
//            dto63.setIteration(6);
//            lstComp2.add(dto61);
//            lstComp2.add(dto62);
//            lstComp2.add(dto63);
//            
//            dto6.setVersion("A");
//            dto6.setIteration(1);
//            PartDTO dto7 = new PartDTO("Airbus", "M5391021100000");
//            dto7.setVersion("A");
//            dto7.setIteration(1);
//            lstComp.add(dto1);
//            lstComp.add(dto2);
//            lstComp.add(dto3);
//            lstComp.add(dto4);
//            lstComp.add(dto5);
//            lstComp.add(dto6);
//            lstComp.add(dto7);
//            return rootDTO;        
            //STUB
            return createDTO(rootFakeUsageLink);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private PartDTO createDTO(PartUsageLink usageLink) {
        PartMaster pm = usageLink.getComponent();
        PartDTO dto = new PartDTO(pm.getWorkspaceId(), pm.getNumber());
        dto.setDescription(pm.getDescription());
        dto.setName(pm.getName());
        dto.setStandardPart(pm.isStandardPart());

        List<String> lstFiles = new ArrayList<String>();
        List<CADInstanceDTO> lstInstances = new ArrayList<CADInstanceDTO>();

        for (CADInstance cadInstance : usageLink.getCadInstances()) {
            lstInstances.add(mapper.map(cadInstance, CADInstanceDTO.class));
        }
        PartIteration partIte = pm.getPartRevisions().get(0).getIteration(1);
        for (Geometry cadFiles : partIte.getGeometries()) {
            lstFiles.add((cadFiles.getFullName()));
        }
        dto.setInstances(lstInstances);
        dto.setFiles(lstFiles);
        dto.setVersion(partIte.getPartVersion());
        dto.setIteration(partIte.getIteration());

        List<PartDTO> components = new ArrayList<PartDTO>();
        for (PartUsageLink component : partIte.getComponents()) {
            components.add(createDTO(component));
        }
        dto.setComponents(components);
        return dto;
    }
}
