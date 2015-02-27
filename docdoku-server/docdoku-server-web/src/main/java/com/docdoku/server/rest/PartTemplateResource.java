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

import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.product.PartMasterTemplate;
import com.docdoku.core.product.PartMasterTemplateKey;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.*;
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
import java.util.*;

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
    @Produces(MediaType.APPLICATION_JSON)
    public PartMasterTemplateDTO[] getPartMasterTemplates(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {


        PartMasterTemplate[] partMsTemplates = productService.getPartMasterTemplates(workspaceId);
        PartMasterTemplateDTO[] dtos = new PartMasterTemplateDTO[partMsTemplates.length];

        for (int i = 0; i < partMsTemplates.length; i++) {
            dtos[i] = mapper.map(partMsTemplates[i], PartMasterTemplateDTO.class);
        }

        return dtos;
    }

    @GET
    @Path("{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PartMasterTemplateDTO getPartMasterTemplates(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId)
            throws EntityNotFoundException, UserNotActiveException {

        PartMasterTemplate partMsTemplate = productService.getPartMasterTemplate(new PartMasterTemplateKey(workspaceId, templateId));
        return mapper.map(partMsTemplate, PartMasterTemplateDTO.class);
    }
    
    @GET
    @Path("{templateId}/generate_id")
    @Produces(MediaType.APPLICATION_JSON)
    public TemplateGeneratedIdDTO generatePartMsId(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId)
            throws EntityNotFoundException, UserNotActiveException {

        String generatedId = productService.generateId(workspaceId, templateId);
        return new TemplateGeneratedIdDTO(generatedId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartMasterTemplateDTO createPartMasterTemplate(@PathParam("workspaceId") String workspaceId, PartTemplateCreationDTO templateCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, CreationException, AccessRightException, NotAllowedException {

        String id =templateCreationDTO.getReference();
        String partType = templateCreationDTO.getPartType();
        String mask = templateCreationDTO.getMask();
        boolean idGenerated = templateCreationDTO.isIdGenerated();
        boolean attributesLocked = templateCreationDTO.isAttributesLocked();

        Set<InstanceAttributeTemplateDTO> attributeTemplates = templateCreationDTO.getAttributeTemplates();
        List<InstanceAttributeTemplateDTO> attributeTemplatesList = new ArrayList<>(attributeTemplates);
        InstanceAttributeTemplateDTO[] attributeTemplatesDtos = new InstanceAttributeTemplateDTO[attributeTemplatesList.size()];

        for (int i = 0; i < attributeTemplatesDtos.length; i++) {
            attributeTemplatesDtos[i] = attributeTemplatesList.get(i);
        }

        PartMasterTemplate template = productService.createPartMasterTemplate(workspaceId, id, partType, mask, createInstanceAttributeTemplateFromDto(attributeTemplatesDtos), idGenerated, attributesLocked);
        return mapper.map(template, PartMasterTemplateDTO.class);
    }
    
    @PUT
    @Path("{templateId}") 
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartMasterTemplateDTO updatePartMsTemplate(@PathParam("workspaceId") String workspaceId,@PathParam("templateId") String templateId, PartMasterTemplateDTO partMsTemplateDTO)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        String partType = partMsTemplateDTO.getPartType();
        String mask = partMsTemplateDTO.getMask();
        boolean idGenerated = partMsTemplateDTO.isIdGenerated();
        boolean attributesLocked = partMsTemplateDTO.isAttributesLocked();

        Set<InstanceAttributeTemplateDTO> attributeTemplates = partMsTemplateDTO.getAttributeTemplates();
        List<InstanceAttributeTemplateDTO> attributeTemplatesList = new ArrayList<>(attributeTemplates);
        InstanceAttributeTemplateDTO[] attributeTemplatesDtos = new InstanceAttributeTemplateDTO[attributeTemplatesList.size()];

        for (int i = 0; i < attributeTemplatesDtos.length; i++) {
            attributeTemplatesDtos[i] = attributeTemplatesList.get(i);
        }

        PartMasterTemplate template = productService.updatePartMasterTemplate(new PartMasterTemplateKey(workspaceId, templateId), partType, mask, createInstanceAttributeTemplateFromDto(attributeTemplatesDtos), idGenerated, attributesLocked);
        return mapper.map(template, PartMasterTemplateDTO.class);
    }

    @PUT
    @Path("{templateId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePartMsTemplateACL(@PathParam("workspaceId") String workspaceId,@PathParam("templateId") String templateId, ACLDTO acl)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException, NotAllowedException {

        if (acl.getGroupEntries().size() > 0 || acl.getUserEntries().size() > 0) {

            Map<String,String> userEntries = new HashMap<>();
            Map<String,String> groupEntries = new HashMap<>();

            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            productService.updateACLForPartMasterTemplate(workspaceId, templateId, userEntries, groupEntries);
        }else{
            productService.removeACLFromPartMasterTemplate(workspaceId, templateId);
        }

        return Response.ok().build();
    }

    @DELETE
    @Path("{templateId}")
    public Response deletePartMasterTemplate(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        productService.deletePartMasterTemplate(new PartMasterTemplateKey(workspaceId, templateId));
        return Response.ok().build();
    }

    @DELETE
    @Path("{templateId}/files/{fileName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeAttachedFile(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId, @PathParam("fileName") String fileName)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        String fileFullName = workspaceId + "/part-templates/" + templateId + "/" + fileName;
        productService.removeFileFromTemplate(fileFullName);
        return Response.ok().build();
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
        data.setMandatory(instanceAttributeTemplateDTO.isMandatory());
        return data;
    }
}