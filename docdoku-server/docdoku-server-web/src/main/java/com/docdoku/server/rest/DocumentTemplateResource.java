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
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.document.DocumentMasterTemplateKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
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
 * @author Yassine Belouad
 */

@RequestScoped
@Api(hidden = true, value = "document-templates", description = "Operations about document templates")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class DocumentTemplateResource {

    @Inject
    private IDocumentManagerLocal documentService;

    private Mapper mapper;

    public DocumentTemplateResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get document templates", response = DocumentMasterTemplateDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentMasterTemplateDTO[] getDocumentMasterTemplates(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        DocumentMasterTemplate[] documentMasterTemplates = documentService.getDocumentMasterTemplates(workspaceId);
        DocumentMasterTemplateDTO[] dtos = new DocumentMasterTemplateDTO[documentMasterTemplates.length];

        for (int i = 0; i < documentMasterTemplates.length; i++) {
            dtos[i] = mapper.map(documentMasterTemplates[i], DocumentMasterTemplateDTO.class);
        }

        return dtos;
    }

    @GET
    @ApiOperation(value = "Get document template", response = DocumentMasterTemplateDTO.class)
    @Path("{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentMasterTemplateDTO getDocumentMasterTemplate(@PathParam("workspaceId") String workspaceId,
                                                               @PathParam("templateId") String templateId)
            throws EntityNotFoundException, UserNotActiveException {

        DocumentMasterTemplate documentMasterTemplate = documentService.getDocumentMasterTemplate(new DocumentMasterTemplateKey(workspaceId, templateId));
        return mapper.map(documentMasterTemplate, DocumentMasterTemplateDTO.class);
    }

    @GET
    @ApiOperation(value = "Generate document template id", response = TemplateGeneratedIdDTO.class)
    @Path("{templateId}/generate_id")
    @Produces(MediaType.APPLICATION_JSON)
    public TemplateGeneratedIdDTO generateDocumentMasterId(@PathParam("workspaceId") String workspaceId,
                                                           @PathParam("templateId") String templateId)
            throws EntityNotFoundException, UserNotActiveException {

        String generateId = documentService.generateId(workspaceId, templateId);
        return new TemplateGeneratedIdDTO(generateId);
    }

    @POST
    @ApiOperation(value = "Create document template", response = DocumentMasterTemplateDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentMasterTemplateDTO createDocumentMasterTemplate(@PathParam("workspaceId") String workspaceId,
                                                                  @ApiParam(required = true, value = "Document master template to create") DocumentTemplateCreationDTO templateCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, NotAllowedException, CreationException {

        String id = templateCreationDTO.getReference();
        String documentType = templateCreationDTO.getDocumentType();
        String workflowModelId = templateCreationDTO.getWorkflowModelId();
        String mask = templateCreationDTO.getMask();
        boolean idGenerated = templateCreationDTO.isIdGenerated();
        boolean attributesLocked = templateCreationDTO.isAttributesLocked();

        List<InstanceAttributeTemplateDTO> attributeTemplates = templateCreationDTO.getAttributeTemplates();
        String[] lovNames = new String[attributeTemplates.size()];
        for (int i = 0; i < attributeTemplates.size(); i++)
            lovNames[i] = attributeTemplates.get(i).getLovName();

        DocumentMasterTemplate template = documentService.createDocumentMasterTemplate(workspaceId, id, documentType, workflowModelId, mask, new InstanceAttributeFactory().createInstanceAttributeTemplateFromDTO(attributeTemplates), lovNames, idGenerated, attributesLocked);
        DocumentMasterTemplateDTO response = mapper.map(template, DocumentMasterTemplateDTO.class);
        return response;
    }

    @PUT
    @ApiOperation(value = "Update document template", response = DocumentMasterTemplateDTO.class)
    @Path("{templateId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentMasterTemplateDTO updateDocumentMasterTemplate(@PathParam("workspaceId") String workspaceId,
                                                                  @PathParam("templateId") String templateId,
                                                                  @ApiParam(required = true, value = "Document master template to update") DocumentMasterTemplateDTO documentMasterTemplateDTO)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException, NotAllowedException {

        String documentType = documentMasterTemplateDTO.getDocumentType();
        String workflowModelId = documentMasterTemplateDTO.getWorkflowModelId();
        String mask = documentMasterTemplateDTO.getMask();
        boolean idGenerated = documentMasterTemplateDTO.isIdGenerated();
        boolean attributesLocked = documentMasterTemplateDTO.isAttributesLocked();

        List<InstanceAttributeTemplateDTO> attributeTemplates = documentMasterTemplateDTO.getAttributeTemplates();
        String[] lovNames = new String[attributeTemplates.size()];
        for (int i = 0; i < attributeTemplates.size(); i++)
            lovNames[i] = attributeTemplates.get(i).getLovName();

        DocumentMasterTemplate template = documentService.updateDocumentMasterTemplate(new DocumentMasterTemplateKey(workspaceId, templateId), documentType, workflowModelId, mask, new InstanceAttributeFactory().createInstanceAttributeTemplateFromDTO(attributeTemplates), lovNames, idGenerated, attributesLocked);
        return mapper.map(template, DocumentMasterTemplateDTO.class);
    }

    @PUT
    @ApiOperation(value = "Update document template ACL", response = Response.class)
    @Path("{templateId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDocumentMasterTemplateACL(@PathParam("workspaceId") String workspaceId,
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

            documentService.updateACLForDocumentMasterTemplate(workspaceId, templateId, userEntries, groupEntries);
        } else {
            documentService.removeACLFromDocumentMasterTemplate(workspaceId, templateId);
        }

        return Response.ok().build();
    }

    @DELETE
    @ApiOperation(value = "Delete document template", response = Response.class)
    @Path("{templateId}")
    public Response deleteDocumentMasterTemplate(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        documentService.deleteDocumentMasterTemplate(new DocumentMasterTemplateKey(workspaceId, templateId));
        return Response.ok().build();
    }

    @DELETE
    @ApiOperation(value = "Remove attached file from document template", response = Response.class)
    @Path("{templateId}/files/{fileName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeAttachedFile(@PathParam("workspaceId") String workspaceId,
                                       @PathParam("templateId") String templateId,
                                       @PathParam("fileName") String fileName)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException, StorageException {

        String fileFullName = workspaceId + "/document-templates/" + templateId + "/" + fileName;

        documentService.removeFileFromTemplate(fileFullName);
        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Rename attached file in document template", response = Response.class)
    @Path("{templateId}/files/{fileName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FileDTO renameAttachedFile(@PathParam("workspaceId") String workspaceId,
                                      @PathParam("templateId") String templateId,
                                      @PathParam("fileName") String fileName,
                                      @ApiParam(required = true, value = "File to rename") FileDTO fileDTO)
            throws UserNotActiveException, WorkspaceNotFoundException, CreationException, UserNotFoundException, FileNotFoundException, NotAllowedException, AccessRightException, FileAlreadyExistsException, StorageException {
        String fileFullName = workspaceId + "/document-templates/" + templateId + "/" + fileName;
        BinaryResource binaryResource = documentService.renameFileInTemplate(fileFullName, fileDTO.getShortName());
        return new FileDTO(true, binaryResource.getFullName(), binaryResource.getName());
    }
}
