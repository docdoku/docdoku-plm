/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.configuration.*;
import com.docdoku.core.exceptions.ApplicationException;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.BaselinedPartDTO;
import com.docdoku.server.rest.dto.ProductInstanceCreationDTO;
import com.docdoku.server.rest.dto.ProductInstanceIterationDTO;
import com.docdoku.server.rest.dto.ProductInstanceMasterDTO;
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
 * @author Taylor LABEJOF
 */
@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ProductInstancesResource {

    @EJB
    private IProductManagerLocal productService;

    private Mapper mapper;

    public ProductInstancesResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProductInstanceMasterDTO> getProductInstances(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId){
        try {
            List<ProductInstanceMaster> productInstanceMasterList;
            if(ciId != null) {
                ConfigurationItemKey configurationItemKey = new ConfigurationItemKey(workspaceId, ciId);
                productInstanceMasterList = productService.getProductInstanceMasters(configurationItemKey);
            }else{
                productInstanceMasterList = productService.getProductInstanceMasters(workspaceId);
            }
            List<ProductInstanceMasterDTO> productInstanceMasterDTOList = new ArrayList<>();
            for(ProductInstanceMaster productInstanceMaster : productInstanceMasterList){
                ProductInstanceMasterDTO productInstanceMasterDTO = mapper.map(productInstanceMaster,ProductInstanceMasterDTO.class);
                productInstanceMasterDTO.setConfigurationItemId(productInstanceMaster.getInstanceOf().getId());
                productInstanceMasterDTOList.add(productInstanceMasterDTO);
            }
            return productInstanceMasterDTOList;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInstanceMasterDTO createProductInstanceMaster(@PathParam("workspaceId") String workspaceId, ProductInstanceCreationDTO productInstanceCreationDTO){
        try {
            ProductInstanceMaster productInstanceMaster = productService.createProductInstance(new ConfigurationItemKey(workspaceId, productInstanceCreationDTO.getConfigurationItemId()), productInstanceCreationDTO.getSerialNumber(), productInstanceCreationDTO.getBaselineId());
            return mapper.map(productInstanceMaster, ProductInstanceMasterDTO.class);
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{serialNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInstanceMasterDTO getProductInstance(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber){
        try {
            ProductInstanceMaster productInstanceMaster = productService.getProductInstanceMaster(new ProductInstanceMasterKey(serialNumber,workspaceId,configurationItemId));
            ProductInstanceMasterDTO productInstanceMasterDTO= mapper.map(productInstanceMaster,ProductInstanceMasterDTO.class);
            return productInstanceMasterDTO;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Path("{serialNumber}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteProductInstanceMaster(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber){
        try {
            productService.deleteProductInstance(workspaceId, configurationItemId, serialNumber);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{serialNumber}/iterations")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProductInstanceIterationDTO> getProductInstanceIterations(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber){
        try {
            List<ProductInstanceIteration> productInstanceIterationList = productService.getProductInstanceIterations(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
            List<ProductInstanceIterationDTO> productInstanceIterationDTOList = new ArrayList<>();
            for (ProductInstanceIteration productInstanceIteration : productInstanceIterationList) {
                ProductInstanceIterationDTO productInstanceIterationDTO = mapper.map(productInstanceIteration, ProductInstanceIterationDTO.class);
                productInstanceIterationDTOList.add(productInstanceIterationDTO);
            }
            return productInstanceIterationDTOList;
        }catch(ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Path("{serialNumber}/iterations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInstanceIterationDTO createProductInstanceIteration(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationId, @PathParam("serialNumber") String serialNumber, @PathParam("iteration") int iteration, ProductInstanceIterationDTO productInstanceIterationDTO){
        try {
            configurationId = (configurationId != null) ? configurationId : productInstanceIterationDTO.getConfigurationItemId();
            List<PartIterationKey> partIterationKeys = new ArrayList<>();
            for(BaselinedPartDTO baselinedPartDTO : productInstanceIterationDTO.getBaselinedPartsList()){
                partIterationKeys.add(new PartIterationKey(workspaceId, baselinedPartDTO.getNumber(),baselinedPartDTO.getVersion(),baselinedPartDTO.getIteration()));
            }
            ProductInstanceIteration productInstanceIteration = productService.updateProductInstance(new ConfigurationItemKey(workspaceId, configurationId), productInstanceIterationDTO.getSerialNumber(), productInstanceIterationDTO.getIterationNote(), partIterationKeys);
            return mapper.map(productInstanceIteration,ProductInstanceIterationDTO.class);
        }catch(ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{serialNumber}/iterations/{iteration}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInstanceIterationDTO getProductInstanceIteration(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, @PathParam("iteration") int iteration){
        try {
            ProductInstanceIteration productInstanceIteration = productService.getProductInstanceIteration(new ProductInstanceIterationKey(serialNumber, workspaceId, configurationItemId, iteration));
            ProductInstanceIterationDTO productInstanceIterationDTO= mapper.map(productInstanceIteration,ProductInstanceIterationDTO.class);
            return productInstanceIterationDTO;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{serialNumber}/iterations/{iteration}/baselined-parts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BaselinedPartDTO> getProductInstanceBaselinedPart(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, @PathParam("iteration") int iteration, @QueryParam("q") String q){
        try {
            List<BaselinedPart> baselinedParts;
            if(q!=null){
                int maxResults = 8;
                baselinedParts = productService.getProductInstanceIterationPartWithReference(new ProductInstanceIterationKey(serialNumber, workspaceId, configurationItemId, iteration), q, maxResults);
            }else{
                baselinedParts = productService.getProductInstanceIterationBaselinedPart(new ProductInstanceIterationKey(serialNumber, workspaceId, configurationItemId, iteration));
            }

            List<BaselinedPartDTO> baselinedPartDTOList = new ArrayList<>();
            for(BaselinedPart baselinedPart : baselinedParts){
                baselinedPartDTOList.add(Tools.mapBaselinedPartToBaselinedPartDTO(baselinedPart));
            }
            return baselinedPartDTOList;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
}
