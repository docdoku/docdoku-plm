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
import com.docdoku.core.meta.DefaultAttributeTemplate;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.meta.ListOfValuesAttributeTemplate;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Yassine Belouad
 */
@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class DocumentTemplateResource {

    @EJB
    private IDocumentManagerLocal documentService;
    private Mapper mapper;

    public DocumentTemplateResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentMasterTemplateDTO[] getDocumentMasterTemplates(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        DocumentMasterTemplate[] docMsTemplates = documentService.getDocumentMasterTemplates(workspaceId);
        DocumentMasterTemplateDTO[] dtos = new DocumentMasterTemplateDTO[docMsTemplates.length];

        for (int i = 0; i < docMsTemplates.length; i++) {
            dtos[i] = mapper.map(docMsTemplates[i], DocumentMasterTemplateDTO.class);
        }

        return dtos;
    }

    @GET
    @Path("{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentMasterTemplateDTO getDocumentMasterTemplates(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId)
            throws EntityNotFoundException, UserNotActiveException {

        DocumentMasterTemplate docMsTemplate = documentService.getDocumentMasterTemplate(new DocumentMasterTemplateKey(workspaceId, templateId));
        return mapper.map(docMsTemplate, DocumentMasterTemplateDTO.class);
    }
    
    @GET
    @Path("{templateId}/generate_id")
    @Produces(MediaType.APPLICATION_JSON)
    public TemplateGeneratedIdDTO generateDocMsId(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId)
            throws EntityNotFoundException, UserNotActiveException {

        String generateId = documentService.generateId(workspaceId, templateId);
        return new TemplateGeneratedIdDTO(generateId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentMasterTemplateDTO createDocumentMasterTemplate(@PathParam("workspaceId") String workspaceId, DocumentTemplateCreationDTO templateCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, NotAllowedException, CreationException {

        String id =templateCreationDTO.getReference();
        String documentType = templateCreationDTO.getDocumentType();
        String workflowModelId=templateCreationDTO.getWorkflowModelId();
        String mask = templateCreationDTO.getMask();
        boolean idGenerated = templateCreationDTO.isIdGenerated();
        boolean attributesLocked = templateCreationDTO.isAttributesLocked();

        List<InstanceAttributeTemplateDTO> attributeTemplates = templateCreationDTO.getAttributeTemplates();
        String[] lovNames=new String[attributeTemplates.size()];
        for (int i=0;i<attributeTemplates.size();i++)
            lovNames[i]=attributeTemplates.get(i).getLovName();

        DocumentMasterTemplate template = documentService.createDocumentMasterTemplate(workspaceId, id, documentType, workflowModelId, mask, createInstanceAttributeTemplateFromDto(attributeTemplates), lovNames, idGenerated, attributesLocked);
        DocumentMasterTemplateDTO response = mapper.map(template, DocumentMasterTemplateDTO.class);
        return response;
    }
    
    @PUT
    @Path("{templateId}") 
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentMasterTemplateDTO updateDocMsTemplate(@PathParam("workspaceId") String workspaceId,@PathParam("templateId") String templateId, DocumentMasterTemplateDTO docMsTemplateDTO)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        String documentType = docMsTemplateDTO.getDocumentType();
        String workflowModelId = docMsTemplateDTO.getWorkflowModelId();
        String mask = docMsTemplateDTO.getMask();
        boolean idGenerated = docMsTemplateDTO.isIdGenerated();
        boolean attributesLocked = docMsTemplateDTO.isAttributesLocked();

        List<InstanceAttributeTemplateDTO> attributeTemplates = docMsTemplateDTO.getAttributeTemplates();
        String[] lovNames=new String[attributeTemplates.size()];
        for (int i=0;i<attributeTemplates.size();i++)
            lovNames[i]=attributeTemplates.get(i).getLovName();

        DocumentMasterTemplate template = documentService.updateDocumentMasterTemplate(new DocumentMasterTemplateKey(workspaceId, templateId), documentType, workflowModelId, mask, createInstanceAttributeTemplateFromDto(attributeTemplates), lovNames, idGenerated, attributesLocked);
        return mapper.map(template, DocumentMasterTemplateDTO.class);
    }

    @PUT
    @Path("{templateId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDocMsTemplateACL(@PathParam("workspaceId") String workspaceId,@PathParam("templateId") String templateId, ACLDTO acl)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException, NotAllowedException {

        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {

            Map<String,String> userEntries = new HashMap<>();
            Map<String,String> groupEntries = new HashMap<>();

            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            documentService.updateACLForDocumentMasterTemplate(workspaceId, templateId, userEntries, groupEntries);
        }else{
            documentService.removeACLFromDocumentMasterTemplate(workspaceId, templateId);
        }

        return Response.ok().build();
    }

    @DELETE
    @Path("{templateId}")
    public Response deleteDocumentMasterTemplate(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        documentService.deleteDocumentMasterTemplate(new DocumentMasterTemplateKey(workspaceId, templateId));
        return Response.ok().build();
    }

    @DELETE
    @Path("{templateId}/files/{fileName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeAttachedFile(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId, @PathParam("fileName") String fileName)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException, StorageException {

        String fileFullName = workspaceId + "/document-templates/" + templateId + "/" + fileName;

        documentService.removeFileFromTemplate(fileFullName);
        return Response.ok().build();
    }

    @PUT
    @Path("{templateId}/files/{fileName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FileDTO renameAttachedFile(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId, @PathParam("fileName") String fileName, FileDTO fileDTO)
            throws UserNotActiveException, WorkspaceNotFoundException, CreationException, UserNotFoundException, FileNotFoundException, NotAllowedException, AccessRightException, FileAlreadyExistsException, StorageException {
        String fileFullName = workspaceId + "/document-templates/" + templateId + "/" + fileName;
        BinaryResource binaryResource = documentService.renameFileInTemplate(fileFullName, fileDTO.getShortName());
        return new FileDTO(true,binaryResource.getFullName(),binaryResource.getName());
    }

    private InstanceAttributeTemplate[] createInstanceAttributeTemplateFromDto(List<InstanceAttributeTemplateDTO> dtos) {
        InstanceAttributeTemplate[] data = new InstanceAttributeTemplate[dtos.size()];

        for (int i = 0; i < dtos.size(); i++) {
            data[i] = createInstanceAttributeTemplateObject(dtos.get(i));
        }

        return data;
    }

    private InstanceAttributeTemplate createInstanceAttributeTemplateObject(InstanceAttributeTemplateDTO dto) {
        InstanceAttributeTemplate data;
        if(dto.getLovName()==null || dto.getLovName().isEmpty()) {
            DefaultAttributeTemplate defaultIA = new DefaultAttributeTemplate();
            defaultIA.setAttributeType(InstanceAttributeTemplate.AttributeType.valueOf(dto.getAttributeType().name()));
            data=defaultIA;
        }
        else {
            ListOfValuesAttributeTemplate lovA = new ListOfValuesAttributeTemplate();
            data=lovA;
        }

        data.setName(dto.getName());
        data.setMandatory(dto.isMandatory());
        data.setLocked(dto.isLocked());
        return data;
    }
}