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

import com.docdoku.core.configuration.Baseline;
import com.docdoku.core.configuration.BaselineCreation;
import com.docdoku.core.configuration.BaselinedPart;
import com.docdoku.core.exceptions.ApplicationException;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.BaselineCreationDTO;
import com.docdoku.server.rest.dto.BaselineDTO;
import com.docdoku.server.rest.dto.BaselinedPartDTO;
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Taylor LABEJOF
 */
@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class BaselinesResource {

    @EJB
    private IProductManagerLocal productService;

    private final static Logger LOGGER = Logger.getLogger(BaselinesResource.class.getName());
    private Mapper mapper;

    public BaselinesResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<BaselineDTO> getBaselines(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId){
            try {
                List<Baseline> baselines;
                if(ciId != null) {
                    ConfigurationItemKey configurationItemKey = new ConfigurationItemKey(workspaceId, ciId);
                    baselines = productService.getBaselines(configurationItemKey);
                }else{
                    baselines = productService.getAllBaselines(workspaceId);
                }
                List<BaselineDTO> baselinesDTO = new ArrayList<>();
                for(Baseline baseline:baselines){
                    BaselineDTO baselineDTO = mapper.map(baseline,BaselineDTO.class);
                    baselineDTO.setConfigurationItemId(baseline.getConfigurationItem().getId());
                    baselinesDTO.add(baselineDTO);
                }
                return baselinesDTO;
            } catch (ApplicationException ex) {
                LOGGER.log(Level.WARNING,null,ex);
                throw new RestApiException(ex.toString(), ex.getMessage());
            }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createBaseline(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, BaselineCreationDTO baselineCreationDTO){
        try {
            ciId = (ciId != null) ? ciId : baselineCreationDTO.getConfigurationItemId();
            BaselineCreation baselineCreation = productService.createBaseline(new ConfigurationItemKey(workspaceId,ciId),baselineCreationDTO.getName(),baselineCreationDTO.getType(),baselineCreationDTO.getDescription());
            BaselineDTO baselineDTO= mapper.map(baselineCreation.getBaseline(),BaselineDTO.class);
            if(baselineCreation.getConflit().size()>0){
                return Response.status(202).entity(baselineCreation.getMessage()).type("text/plain").build();
            }

            try {
                return Response.created(URI.create(URLEncoder.encode(String.valueOf(baselineDTO.getId()),"UTF-8"))).entity(baselineDTO).build();
            } catch (UnsupportedEncodingException ignored) {
                return Response.ok().build();
            }
        } catch(ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("{baselineId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateBaseline(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("baselineId") String baselineId, BaselineDTO baselineDTO){
        try {
            ciId = (ciId != null) ? ciId : baselineDTO.getConfigurationItemId();
            List<PartIterationKey> partIterationKeys = new ArrayList<>();
            for(BaselinedPartDTO baselinedPartDTO : baselineDTO.getBaselinedParts()){
                partIterationKeys.add(new PartIterationKey(workspaceId, baselinedPartDTO.getNumber(),baselinedPartDTO.getVersion(),baselinedPartDTO.getIteration()));
            }
            productService.updateBaseline(new ConfigurationItemKey(workspaceId,ciId),Integer.parseInt(baselineId),baselineDTO.getName(),baselineDTO.getType(),baselineDTO.getDescription(),partIterationKeys);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Path("{baselineId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteBaseline(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("baselineId") int baselineId){
        try {
            productService.deleteBaseline(baselineId);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{baselineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public BaselineDTO getBaseline(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("baselineId") int baselineId){
        try {
            Baseline baseline = productService.getBaseline(baselineId);
            BaselineDTO baselineDTO = mapper.map(baseline,BaselineDTO.class);
            baselineDTO.setConfigurationItemId(baseline.getConfigurationItem().getId());
            baselineDTO.setBaselinedParts(Tools.mapBaselinedPartsToBaselinedPartDTO(baseline));
            return baselineDTO;
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{baselineId}/parts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BaselinedPartDTO> getBaselineParts(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("baselineId") int baselineId, @QueryParam("q") String q){
        try {
            int maxResults = 8;
            List<BaselinedPart> baselinedPartList = productService.getBaselinedPartWithReference(baselineId, q, maxResults);

            List<BaselinedPartDTO> baselinedPartDTOList = new ArrayList<>();
            for(BaselinedPart baselinedPart:baselinedPartList){
                baselinedPartDTOList.add(Tools.mapBaselinedPartToBaselinedPartDTO(baselinedPart));
            }
            return baselinedPartDTOList;
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Path("{baselineId}/duplicate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public BaselineDTO duplicateBaseline(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("baselineId") int baselineId,  BaselineCreationDTO baselineCreationDTO){
        try {
            Baseline baseline = productService.duplicateBaseline(baselineId, baselineCreationDTO.getName(), baselineCreationDTO.getType(), baselineCreationDTO.getDescription());
            return mapper.map(baseline, BaselineDTO.class);
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
}