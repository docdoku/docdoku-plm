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
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductBaselineManagerLocal;
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
import java.util.List;

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

    private Mapper mapper;

    public ProductConfigurationsResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProductConfigurationDTO> getAllConfiguration(@PathParam("workspaceId") String workspaceId,  @PathParam("ciId") String ciId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        List<ProductConfiguration> allProductConfigurations;
        if(ciId != null) {
            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,ciId);
            allProductConfigurations = productBaselineService.getAllProductConfigurationsByConfigurationItemId(ciKey);
        }else{
            allProductConfigurations = productBaselineService.getAllProductConfigurations(workspaceId);
        }
        List<ProductConfigurationDTO> configurationDTOs = new ArrayList<>();
        for(ProductConfiguration productConfiguration :allProductConfigurations){
            configurationDTOs.add(mapper.map(productConfiguration,ProductConfigurationDTO.class));
        }
        return configurationDTOs;
    }

    @PUT
    @Path("{productConfigurationId}}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductConfigurationDTO getConfiguration(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("productConfigurationId") int productConfigurationId) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ProductConfigurationNotFoundException {
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,ciId);
        ProductConfiguration productConfiguration = productBaselineService.getProductConfiguration(ciKey, productConfigurationId);
        return mapper.map(productConfiguration,ProductConfigurationDTO.class);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductConfigurationDTO createConfiguration(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String pCiId, ProductConfigurationDTO productConfigurationDTO) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ConfigurationItemNotFoundException, CreationException {
        String ciId = (pCiId != null) ? pCiId : productConfigurationDTO.getConfigurationItemId();
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,ciId);
        String description = productConfigurationDTO.getDescription();
        String name = productConfigurationDTO.getName();
        ProductConfiguration productConfiguration = productBaselineService.createProductConfiguration(ciKey, name, description, productConfigurationDTO.getSubstituteLinks(), productConfigurationDTO.getOptionalUsageLinks());
        return mapper.map(productConfiguration,ProductConfigurationDTO.class);
    }


    @PUT
    @Path("{productConfigurationId}}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductConfigurationDTO updateConfiguration(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String pCiId, @PathParam("productConfigurationId") int productConfigurationId, ProductConfigurationDTO productConfigurationDTO) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ProductConfigurationNotFoundException {
        String ciId = (pCiId != null) ? pCiId : productConfigurationDTO.getConfigurationItemId();
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,ciId);
        String description = productConfigurationDTO.getDescription();
        String name = productConfigurationDTO.getName();
        ProductConfiguration productConfiguration = productBaselineService.updateProductConfiguration(ciKey, productConfigurationId, name, description, productConfigurationDTO.getSubstituteLinks(), productConfigurationDTO.getOptionalUsageLinks());
        return mapper.map(productConfiguration,ProductConfigurationDTO.class);
    }


    @DELETE
    @Path("{productConfigurationId}}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProductConfiguration(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("productConfigurationId") int productConfigurationId) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ProductConfigurationNotFoundException {
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,ciId);
        productBaselineService.deleteProductConfiguration(ciKey, productConfigurationId);
        return Response.ok().build();
    }
}