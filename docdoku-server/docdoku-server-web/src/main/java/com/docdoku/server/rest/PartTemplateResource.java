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

import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.product.PartMasterTemplate;
import com.docdoku.core.product.PartMasterTemplateKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.InstanceAttributeTemplateDTO;
import com.docdoku.server.rest.dto.PartMasterTemplateDTO;
import com.docdoku.server.rest.dto.PartTemplateCreationDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Morgan Guimard
 */
@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class PartTemplateResource {

    @EJB
    private IProductManagerLocal productService;
    private Mapper mapper;

    public PartTemplateResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public PartMasterTemplateDTO[] getPartMasterTemplates(@PathParam("workspaceId") String workspaceId) {
        try {

            PartMasterTemplate[] partMsTemplates = productService.getPartMasterTemplates(workspaceId);
            PartMasterTemplateDTO[] dtos = new PartMasterTemplateDTO[partMsTemplates.length];

            for (int i = 0; i < partMsTemplates.length; i++) {
                dtos[i] = mapper.map(partMsTemplates[i], PartMasterTemplateDTO.class);
            }

            return dtos;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{templateId}")
    @Produces("application/json;charset=UTF-8")
    public PartMasterTemplateDTO getPartMasterTemplates(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId) {
        try {

            PartMasterTemplate partMsTemplate = productService.getPartMasterTemplate(new PartMasterTemplateKey(workspaceId, templateId));
            PartMasterTemplateDTO dto = mapper.map(partMsTemplate, PartMasterTemplateDTO.class);

            return dto;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
    
    @GET
    @Path("{templateId}/generate_id")
    @Produces("application/json;charset=UTF-8")
    public String generatePartMsId(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId) {
        try {

            return productService.generateId(workspaceId, templateId);

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }    

    @POST
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public PartMasterTemplateDTO createPartMasterTemplate(@PathParam("workspaceId") String workspaceId, PartTemplateCreationDTO templateCreationDTO) {

        try {      
            String id =templateCreationDTO.getReference();
            String partType = templateCreationDTO.getPartType();
            String mask = templateCreationDTO.getMask();
            boolean idGenerated = templateCreationDTO.isIdGenerated();

            Set<InstanceAttributeTemplateDTO> attributeTemplates = templateCreationDTO.getAttributeTemplates();
            List<InstanceAttributeTemplateDTO> attributeTemplatesList = new ArrayList<InstanceAttributeTemplateDTO>(attributeTemplates);
            InstanceAttributeTemplateDTO[] attributeTemplatesDtos = new InstanceAttributeTemplateDTO[attributeTemplatesList.size()];

            for (int i = 0; i < attributeTemplatesDtos.length; i++) {
                attributeTemplatesDtos[i] = attributeTemplatesList.get(i);
            }

            PartMasterTemplate template = productService.createPartMasterTemplate(workspaceId, id, partType, mask, createInstanceAttributeTemplateFromDto(attributeTemplatesDtos), idGenerated);
            PartMasterTemplateDTO templateDto = mapper.map(template, PartMasterTemplateDTO.class);

            return templateDto;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
    
    @PUT
    @Path("{templateId}") 
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")  
    public PartMasterTemplateDTO updatePartMsTemplate(@PathParam("workspaceId") String workspaceId,@PathParam("templateId") String templateId, PartMasterTemplateDTO partMsTemplateDTO) {

        try {
            String id = partMsTemplateDTO.getId();
            String partType = partMsTemplateDTO.getPartType();
            String mask = partMsTemplateDTO.getMask();
            boolean idGenerated = partMsTemplateDTO.isIdGenerated();

            Set<InstanceAttributeTemplateDTO> attributeTemplates = partMsTemplateDTO.getAttributeTemplates();
            List<InstanceAttributeTemplateDTO> attributeTemplatesList = new ArrayList<InstanceAttributeTemplateDTO>(attributeTemplates);
            InstanceAttributeTemplateDTO[] attributeTemplatesDtos = new InstanceAttributeTemplateDTO[attributeTemplatesList.size()];

            for (int i = 0; i < attributeTemplatesDtos.length; i++) {
                attributeTemplatesDtos[i] = attributeTemplatesList.get(i);
            }

            PartMasterTemplate template = productService.updatePartMasterTemplate(new PartMasterTemplateKey(workspaceId, templateId), partType, mask, createInstanceAttributeTemplateFromDto(attributeTemplatesDtos), idGenerated);
            PartMasterTemplateDTO templateDto = mapper.map(template, PartMasterTemplateDTO.class);

            return templateDto;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Path("{templateId}")
    public Response deletePartMasterTemplate(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId) {
        try {
            productService.deletePartMasterTemplate(new PartMasterTemplateKey(workspaceId, templateId));
            return Response.ok().build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Consumes("application/json;charset=UTF-8")
    @Path("{templateId}/files/{fileName}")
    public Response removeAttachedFile(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId, @PathParam("fileName") String fileName) {
        try {
            String fileFullName = workspaceId + "/templates/" + templateId + "/" + fileName;

            productService.removeFileFromTemplate(fileFullName);
            return Response.ok().build();

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private InstanceAttributeTemplate[] createInstanceAttributeTemplateFromDto(InstanceAttributeTemplateDTO[] dtos) {
        InstanceAttributeTemplate[] data = new InstanceAttributeTemplate[dtos.length];
        for (int i = 0; i < dtos.length; i++) {
            data[i] = createInstanceAttributeTemplateObject(dtos[i]);
        }
        return data;
    }

    private InstanceAttributeTemplate createInstanceAttributeTemplateObject(InstanceAttributeTemplateDTO instanceAttributeTemplateDTO) {
        InstanceAttributeTemplate data = new InstanceAttributeTemplate();
        data.setName(instanceAttributeTemplateDTO.getName());
        data.setAttributeType(InstanceAttributeTemplate.AttributeType.valueOf(instanceAttributeTemplateDTO.getAttributeType().name()));
        return data;
    }
}