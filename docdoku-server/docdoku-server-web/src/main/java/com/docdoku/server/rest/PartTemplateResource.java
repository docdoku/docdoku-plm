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

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.PartMasterTemplate;
import com.docdoku.core.product.PartMasterTemplateKey;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.util.InstanceAttributeFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Morgan Guimard
 */
@RequestScoped
@Api(hidden = true, value = "part-templates", description = "Operations about part templates")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class PartTemplateResource {

    @Inject
    private IProductManagerLocal productService;

    private Mapper mapper;

    public PartTemplateResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get part master templates", response = PartMasterTemplateDTO.class, responseContainer = "List")
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public PartMasterTemplateDTO[] getPartMasterTemplates(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {


        PartMasterTemplate[] partMasterTemplates = productService.getPartMasterTemplates(workspaceId);
        PartMasterTemplateDTO[] partMasterTemplateDTOs = new PartMasterTemplateDTO[partMasterTemplates.length];

        for (int i = 0; i < partMasterTemplates.length; i++) {
            partMasterTemplateDTOs[i] = mapper.map(partMasterTemplates[i], PartMasterTemplateDTO.class);
        }

        return partMasterTemplateDTOs;
    }

    @GET
    @ApiOperation(value = "Get part master template", response = PartMasterTemplateDTO.class)
    @Path("{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PartMasterTemplateDTO getPartMasterTemplate(@PathParam("workspaceId") String workspaceId,
                                                       @PathParam("templateId") String templateId)
            throws EntityNotFoundException, UserNotActiveException {

        PartMasterTemplate partMasterTemplate = productService.getPartMasterTemplate(new PartMasterTemplateKey(workspaceId, templateId));
        return mapper.map(partMasterTemplate, PartMasterTemplateDTO.class);
    }

    @GET
    @ApiOperation(value = "Generate part master template id", response = TemplateGeneratedIdDTO.class)
    @Path("{templateId}/generate_id")
    @Produces(MediaType.APPLICATION_JSON)
    public TemplateGeneratedIdDTO generatePartMasterTemplateId(@PathParam("workspaceId") String workspaceId,
                                                               @PathParam("templateId") String templateId)
            throws EntityNotFoundException, UserNotActiveException {

        String generatedId = productService.generateId(workspaceId, templateId);
        return new TemplateGeneratedIdDTO(generatedId);
    }

    @POST
    @ApiOperation(value = "Crate part master template", response = PartMasterTemplateDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartMasterTemplateDTO createPartMasterTemplate(@PathParam("workspaceId") String workspaceId,
                                                          @ApiParam(required = true, value = "Part master template to create") PartTemplateCreationDTO templateCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, CreationException, AccessRightException, NotAllowedException {

        String id = templateCreationDTO.getReference();
        String partType = templateCreationDTO.getPartType();
        String workflowModelId = templateCreationDTO.getWorkflowModelId();
        String mask = templateCreationDTO.getMask();
        boolean idGenerated = templateCreationDTO.isIdGenerated();
        boolean attributesLocked = templateCreationDTO.isAttributesLocked();

        List<InstanceAttributeTemplateDTO> attributeTemplates = templateCreationDTO.getAttributeTemplates();
        String[] lovNames = new String[attributeTemplates.size()];
        for (int i = 0; i < attributeTemplates.size(); i++) {
            lovNames[i] = attributeTemplates.get(i).getLovName();
        }

        List<InstanceAttributeTemplateDTO> attributeInstanceTemplates = templateCreationDTO.getAttributeInstanceTemplates();
        String[] instanceLovNames = new String[attributeInstanceTemplates.size()];
        for (int i = 0; i < attributeInstanceTemplates.size(); i++) {
            instanceLovNames[i] = attributeInstanceTemplates.get(i).getLovName();
        }
        InstanceAttributeFactory attributeFactory = new InstanceAttributeFactory();
        PartMasterTemplate template = productService.createPartMasterTemplate(workspaceId, id, partType, workflowModelId, mask, attributeFactory.createInstanceAttributeTemplateFromDTO(attributeTemplates), lovNames, attributeFactory.createInstanceAttributeTemplateFromDTO(attributeInstanceTemplates), instanceLovNames, idGenerated, attributesLocked);
        return mapper.map(template, PartMasterTemplateDTO.class);
    }

    @PUT
    @ApiOperation(value = "Update part master template", response = PartMasterTemplateDTO.class)
    @Path("{templateId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartMasterTemplateDTO updatePartMasterTemplate(@PathParam("workspaceId") String workspaceId,
                                                          @PathParam("templateId") String templateId,
                                                          @ApiParam(required = true, value = "Part master template to update") PartMasterTemplateDTO partMasterTemplateDTO)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException, NotAllowedException {

        String partType = partMasterTemplateDTO.getPartType();
        String mask = partMasterTemplateDTO.getMask();
        String workflowModelId = partMasterTemplateDTO.getWorkflowModelId();
        boolean idGenerated = partMasterTemplateDTO.isIdGenerated();
        boolean attributesLocked = partMasterTemplateDTO.isAttributesLocked();

        List<InstanceAttributeTemplateDTO> attributeTemplates = partMasterTemplateDTO.getAttributeTemplates();
        String[] lovNames = new String[attributeTemplates.size()];
        for (int i = 0; i < attributeTemplates.size(); i++) {
            lovNames[i] = attributeTemplates.get(i).getLovName();
        }


        List<InstanceAttributeTemplateDTO> attributeInstanceTemplates = partMasterTemplateDTO.getAttributeInstanceTemplates();
        String[] instanceLovNames = new String[attributeInstanceTemplates.size()];
        for (int i = 0; i < attributeInstanceTemplates.size(); i++) {
            instanceLovNames[i] = attributeInstanceTemplates.get(i).getLovName();
        }
        InstanceAttributeFactory attributeFactory = new InstanceAttributeFactory();
        PartMasterTemplate template = productService.updatePartMasterTemplate(new PartMasterTemplateKey(workspaceId, templateId), partType, workflowModelId, mask, attributeFactory.createInstanceAttributeTemplateFromDTO(attributeTemplates), lovNames, attributeFactory.createInstanceAttributeTemplateFromDTO(attributeInstanceTemplates), instanceLovNames, idGenerated, attributesLocked);
        return mapper.map(template, PartMasterTemplateDTO.class);
    }

    @PUT
    @ApiOperation(value = "Update part master template ACL", response = Response.class)
    @Path("{templateId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePartMasterTemplateACL(@PathParam("workspaceId") String workspaceId,
                                                @PathParam("templateId") String templateId,
                                                @ApiParam(required = true, value = "ACL rules to set") ACLDTO acl)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException, NotAllowedException {

        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {

            Map<String, String> userEntries = new HashMap<>();
            Map<String, String> groupEntries = new HashMap<>();

            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            productService.updateACLForPartMasterTemplate(workspaceId, templateId, userEntries, groupEntries);
        } else {
            productService.removeACLFromPartMasterTemplate(workspaceId, templateId);
        }

        return Response.ok().build();
    }

    @DELETE
    @ApiOperation(value = "Delete part master template", response = Response.class)
    @Path("{templateId}")
    public Response deletePartMasterTemplate(@PathParam("workspaceId") String workspaceId,
                                             @PathParam("templateId") String templateId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        productService.deletePartMasterTemplate(new PartMasterTemplateKey(workspaceId, templateId));
        return Response.ok().build();
    }

    @DELETE
    @ApiOperation(value = "Remove attached file from part master template", response = Response.class)
    @Path("{templateId}/files/{fileName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeAttachedFile(@PathParam("workspaceId") String workspaceId,
                                       @PathParam("templateId") String templateId,
                                       @PathParam("fileName") String fileName)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        String fileFullName = workspaceId + "/part-templates/" + templateId + "/" + fileName;
        productService.removeFileFromTemplate(fileFullName);
        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Rename attached file in part master template", response = Response.class)
    @Path("{templateId}/files/{fileName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FileDTO renameAttachedFile(@PathParam("workspaceId") String workspaceId,
                                      @PathParam("templateId") String templateId,
                                      @PathParam("fileName") String fileName,
                                      @ApiParam(required = true, value = "File to rename") FileDTO fileDTO)
            throws UserNotActiveException, WorkspaceNotFoundException, CreationException, UserNotFoundException, FileNotFoundException, AccessRightException, FileAlreadyExistsException, StorageException, NotAllowedException {

        String fileFullName = workspaceId + "/part-templates/" + templateId + "/" + fileName;
        BinaryResource binaryResource = productService.renameFileInTemplate(fileFullName, fileDTO.getShortName());
        return new FileDTO(true, binaryResource.getFullName(), binaryResource.getName());
    }

}
