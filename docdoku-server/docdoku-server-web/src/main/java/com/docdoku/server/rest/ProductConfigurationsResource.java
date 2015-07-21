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
package com.docdoku.server.rest;

import com.docdoku.core.configuration.ProductConfiguration;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.PartLink;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductBaselineManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.ACLDTO;
import com.docdoku.server.rest.dto.LightPartLinkDTO;
import com.docdoku.server.rest.dto.LightPartLinkListDTO;
import com.docdoku.server.rest.dto.baseline.ProductConfigurationDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Morgan Guimard
 */
@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ProductConfigurationsResource {

    @EJB
    private IProductBaselineManagerLocal productBaselineService;

    @EJB
    private IProductManagerLocal productService;

    private Mapper mapper;

    public ProductConfigurationsResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProductConfigurationDTO> getAllConfiguration(@PathParam("workspaceId") String workspaceId,  @PathParam("ciId") String ciId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException {
        List<ProductConfiguration> allProductConfigurations;
        if(ciId != null) {
            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,ciId);
            allProductConfigurations = productBaselineService.getAllProductConfigurationsByConfigurationItemId(ciKey);
        }else{
            allProductConfigurations = productBaselineService.getAllProductConfigurations(workspaceId);
        }
        List<ProductConfigurationDTO> configurationDTOs = new ArrayList<>();
        for(ProductConfiguration productConfiguration :allProductConfigurations){
            ProductConfigurationDTO productConfigurationDTO = mapper.map(productConfiguration, ProductConfigurationDTO.class);
            productConfigurationDTO.setConfigurationItemId(productConfiguration.getConfigurationItem().getId());
            configurationDTOs.add(productConfigurationDTO);
        }
        return configurationDTOs;
    }

    @GET
    @Path("{productConfigurationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductConfigurationDTO getConfiguration(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("productConfigurationId") int productConfigurationId) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ProductConfigurationNotFoundException, EntityConstraintException, NotAllowedException, AccessRightException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, PartMasterNotFoundException {
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,ciId);
        ProductConfiguration productConfiguration = productBaselineService.getProductConfiguration(ciKey, productConfigurationId);
        ProductConfigurationDTO productConfigurationDTO = mapper.map(productConfiguration, ProductConfigurationDTO.class);
        productConfigurationDTO.setConfigurationItemId(productConfiguration.getConfigurationItem().getId());

        List<LightPartLinkListDTO> substitutesParts = new ArrayList<>();
        List<LightPartLinkListDTO> optionalParts = new ArrayList<>();

        for(String path:productConfiguration.getSubstituteLinks()){
            LightPartLinkListDTO partDTOs = new LightPartLinkListDTO();
            for(PartLink partLink : productService.decodePath(ciKey, path)){
                partDTOs.getPartLinks().add(new LightPartLinkDTO(partLink));
            }
            substitutesParts.add(partDTOs);
        }

        for(String path:productConfiguration.getOptionalUsageLinks()){
            LightPartLinkListDTO partDTOs = new LightPartLinkListDTO();
            for(PartLink partLink : productService.decodePath(ciKey, path)){
                partDTOs.getPartLinks().add(new LightPartLinkDTO(partLink));
            }
            optionalParts.add(partDTOs);
        }

        productConfigurationDTO.setSubstitutesParts(substitutesParts);
        productConfigurationDTO.setOptionalsParts(optionalParts);

        return productConfigurationDTO;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductConfigurationDTO createConfiguration(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String pCiId, ProductConfigurationDTO pProductConfigurationDTO) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ConfigurationItemNotFoundException, CreationException, AccessRightException {
        String ciId = (pCiId != null) ? pCiId : pProductConfigurationDTO.getConfigurationItemId();
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,ciId);
        String description = pProductConfigurationDTO.getDescription();
        String name = pProductConfigurationDTO.getName();

        ACLDTO acldto = pProductConfigurationDTO.getAcl();
        Map<String,ACL.Permission> userEntries=new HashMap<>();
        Map<String,ACL.Permission> grpEntries=new HashMap<>();
        if(acldto != null){
            userEntries = acldto.getUserEntries();
            grpEntries= acldto.getGroupEntries();
        }

        ProductConfiguration productConfiguration = productBaselineService.createProductConfiguration(ciKey, name, description, pProductConfigurationDTO.getSubstituteLinks(), pProductConfigurationDTO.getOptionalUsageLinks(),userEntries,grpEntries);
        ProductConfigurationDTO productConfigurationDTO = mapper.map(productConfiguration, ProductConfigurationDTO.class);
        productConfigurationDTO.setConfigurationItemId(productConfiguration.getConfigurationItem().getId());
        return productConfigurationDTO;
    }


    @PUT
    @Path("{productConfigurationId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateConfigurationACL(@PathParam("workspaceId") String workspaceId,@PathParam("ciId") String pCiId, @PathParam("productConfigurationId") int productConfigurationId, ACLDTO acl) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ProductConfigurationNotFoundException, AccessRightException {
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,pCiId);

        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {

            Map<String,String> userEntries = new HashMap<>();
            Map<String,String> groupEntries = new HashMap<>();

            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            productBaselineService.updateACLForConfiguration(ciKey, productConfigurationId, userEntries, groupEntries);
        }else{
            productBaselineService.removeACLFromConfiguration(ciKey, productConfigurationId);
        }

        return Response.ok().build();
    }

    @DELETE
    @Path("{productConfigurationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProductConfiguration(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("productConfigurationId") int productConfigurationId) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ProductConfigurationNotFoundException, AccessRightException {
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,ciId);
        productBaselineService.deleteProductConfiguration(ciKey, productConfigurationId);
        return Response.ok().build();
    }
}