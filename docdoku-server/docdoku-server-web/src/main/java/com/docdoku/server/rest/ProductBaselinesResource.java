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

import com.docdoku.core.configuration.BaselinedPart;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductBaselineManagerLocal;
import com.docdoku.server.rest.dto.baseline.BaselinedPartDTO;
import com.docdoku.server.rest.dto.baseline.ProductBaselineDTO;
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
public class ProductBaselinesResource {

    @EJB
    private IProductBaselineManagerLocal productBaselineService;

    private Mapper mapper;

    public ProductBaselinesResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProductBaselineDTO> getBaselines(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId)
            throws UserNotActiveException, EntityNotFoundException {
        List<ProductBaseline> productBaselines;
        if(ciId != null) {
            ConfigurationItemKey configurationItemKey = new ConfigurationItemKey(workspaceId, ciId);
            productBaselines = productBaselineService.getBaselines(configurationItemKey);
        }else{
            productBaselines = productBaselineService.getAllBaselines(workspaceId);
        }
        List<ProductBaselineDTO> baselinesDTO = new ArrayList<>();
        for(ProductBaseline productBaseline : productBaselines){
            ProductBaselineDTO productBaselineDTO = mapper.map(productBaseline,ProductBaselineDTO.class);
            productBaselineDTO.setConfigurationItemId(productBaseline.getConfigurationItem().getId());
            productBaselineDTO.setConfigurationItemLatestRevision(productBaseline.getConfigurationItem().getDesignItem().getLastRevision().getVersion());
            baselinesDTO.add(productBaselineDTO);
        }
        return baselinesDTO;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductBaselineDTO createBaseline(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String pCiId, ProductBaselineDTO productBaselineDTO)
            throws UserNotActiveException, EntityNotFoundException, NotAllowedException, AccessRightException, PartRevisionNotReleasedException, EntityConstraintException, CreationException {

        String ciId = (pCiId != null) ? pCiId : productBaselineDTO.getConfigurationItemId();
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,ciId);
        String description = productBaselineDTO.getDescription();
        String name = productBaselineDTO.getName();
        ProductBaseline.BaselineType type = productBaselineDTO.getType();

        List<BaselinedPartDTO> baselinedPartsDTO = productBaselineDTO.getBaselinedParts();
        List<PartIterationKey> partIterationKeys = new ArrayList<>();
        for(BaselinedPartDTO part:baselinedPartsDTO){
            partIterationKeys.add(new PartIterationKey(workspaceId,part.getNumber(),part.getVersion(),part.getIteration()));
        }

        ProductBaseline baseline = productBaselineService.createBaseline(ciKey,
                name, type, description, partIterationKeys, productBaselineDTO.getSubstituteLinks(),productBaselineDTO.getOptionalUsageLinks());
        ProductBaselineDTO dto = mapper.map(baseline, ProductBaselineDTO.class);
        dto.setConfigurationItemLatestRevision(baseline.getConfigurationItem().getDesignItem().getLastRevision().getVersion());
        return dto;
    }

    @DELETE
    @Path("{baselineId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteBaseline(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("baselineId") int baselineId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {
        productBaselineService.deleteBaseline(workspaceId,baselineId);
        return Response.ok().build();
    }

    @GET
    @Path("{baselineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductBaselineDTO getBaseline(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("baselineId") int baselineId)
            throws EntityNotFoundException, UserNotActiveException{
        ProductBaseline productBaseline = productBaselineService.getBaseline(baselineId);
        ProductBaselineDTO productBaselineDTO = mapper.map(productBaseline,ProductBaselineDTO.class);
        productBaselineDTO.setConfigurationItemId(productBaseline.getConfigurationItem().getId());
        productBaselineDTO.setConfigurationItemLatestRevision(productBaseline.getConfigurationItem().getDesignItem().getLastRevision().getVersion());
        productBaselineDTO.setBaselinedParts(Tools.mapBaselinedPartsToBaselinedPartDTO(productBaseline));
        return productBaselineDTO;
    }

    @GET
    @Path("{baselineId}/parts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BaselinedPartDTO> getBaselineParts(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("baselineId") int baselineId, @QueryParam("q") String q)
            throws EntityNotFoundException, UserNotActiveException{
        int maxResults = 8;
        List<BaselinedPart> baselinedPartList = productBaselineService.getBaselinedPartWithReference(baselineId, q, maxResults);

        List<BaselinedPartDTO> baselinedPartDTOList = new ArrayList<>();
        for(BaselinedPart baselinedPart:baselinedPartList){
            baselinedPartDTOList.add(Tools.mapBaselinedPartToBaselinedPartDTO(baselinedPart));
        }
        return baselinedPartDTOList;
    }
}