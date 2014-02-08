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

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.*;
import com.docdoku.core.product.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.sharing.SharedPart;
import com.docdoku.core.workflow.Workflow;
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

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class PartResource {

    @EJB
    private IProductManagerLocal productService;


    @EJB
    private IUserManagerLocal userManager;

    public PartResource() {
    }

    private Mapper mapper;

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartDTO(@PathParam("workspaceId") String pWorkspaceId, @PathParam("partNumber") String partNumber , @PathParam("partVersion") String partVersion ) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException {
        try {
            PartRevisionKey revisionKey = new PartRevisionKey(pWorkspaceId,partNumber,partVersion);
            PartRevision partRevision = productService.getPartRevision(revisionKey);
            PartDTO partDTO = Tools.mapPartRevisionToPartDTO(partRevision);
            return Response.ok(partDTO).build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/iterations/{partIteration}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePartIteration(@PathParam("workspaceId") String pWorkspaceId, @PathParam("partNumber") String partNumber , @PathParam("partVersion") String partVersion, @PathParam("partIteration") int partIteration, PartIterationDTO data) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException {
        try {
            PartRevisionKey revisionKey = new PartRevisionKey(pWorkspaceId,partNumber,partVersion);
            PartRevision partRevision = productService.getPartRevision(revisionKey);

            PartIterationKey pKey = new PartIterationKey(revisionKey, partIteration);

            List<InstanceAttributeDTO> instanceAttributes = data.getInstanceAttributes();
            List<InstanceAttribute> attributes = null;
            if (instanceAttributes != null) {
                attributes = createInstanceAttribute(instanceAttributes);
            }

            List<PartUsageLinkDTO> components = data.getComponents();
            List<PartUsageLink> newComponents = null;
            if(components != null){
                newComponents = createComponents(pWorkspaceId, components);
            }

            List<DocumentIterationDTO> linkedDocs = data.getLinkedDocuments();
            DocumentIterationKey[] links = null;
            if (linkedDocs != null) {
                links = createDocumentIterationKey(linkedDocs);
            }

            PartIteration.Source sameSource = partRevision.getIteration(partIteration).getSource();

            PartRevision partRevisionUpdated = productService.updatePartIteration(pKey, data.getIterationNote(), sameSource, newComponents, attributes, links);

            PartDTO partDTO = Tools.mapPartRevisionToPartDTO(partRevisionUpdated);
            return Response.ok(partDTO).build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }


    @PUT
    @Path("/checkin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkIn(@PathParam("workspaceId") String workspaceId,@PathParam("partNumber") String partNumber , @PathParam("partVersion") String partVersion) {
        try {
            PartRevisionKey revisionKey = new PartRevisionKey(workspaceId,partNumber,partVersion);
            productService.checkInPart(revisionKey);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/checkout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkOut(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber , @PathParam("partVersion") String partVersion) {
        try {
            PartRevisionKey revisionKey = new PartRevisionKey(workspaceId,partNumber,partVersion);
            productService.checkOutPart(revisionKey);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/undocheckout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response undoCheckOut(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber , @PathParam("partVersion") String partVersion) {
        try {
            PartRevisionKey revisionKey = new PartRevisionKey(workspaceId,partNumber,partVersion);
            productService.undoCheckOutPart(revisionKey);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateACL(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber , @PathParam("partVersion") String partVersion, ACLDTO acl) {
        try {

            PartRevisionKey revisionKey = new PartRevisionKey(workspaceId,partNumber,partVersion);

            if (acl.getGroupEntries().size() > 0 || acl.getUserEntries().size() > 0) {

                Map<String,String> userEntries = new HashMap<String,String>();
                Map<String,String> groupEntries = new HashMap<String,String>();

                for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                    userEntries.put(entry.getKey(), entry.getValue().name());
                }

                for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                    groupEntries.put(entry.getKey(), entry.getValue().name());
                }

                productService.updatePartRevisionACL(workspaceId, revisionKey, userEntries, groupEntries);

            }else{
                productService.removeACLFromPartRevision(revisionKey);
            }
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/newVersion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewVersion(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber , @PathParam("partVersion") String partVersion, PartCreationDTO partCreationDTO) {

        RoleMappingDTO[] rolesMappingDTO = partCreationDTO.getRoleMapping();
        ACLDTO acl = partCreationDTO.getAcl();
        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId,partNumber,partVersion);
        String description = partCreationDTO.getDescription();
        String workflowModelId = partCreationDTO.getWorkflowModelId();

        try {

            ACLUserEntry[] userEntries = null;
            ACLUserGroupEntry[] userGroupEntries = null;
            if (acl != null) {
                userEntries = new ACLUserEntry[acl.getUserEntries().size()];
                userGroupEntries = new ACLUserGroupEntry[acl.getGroupEntries().size()];
                int i = 0;
                for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                    userEntries[i] = new ACLUserEntry();
                    userEntries[i].setPrincipal(new User(new Workspace(workspaceId), entry.getKey()));
                    userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
                i = 0;
                for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                    userGroupEntries[i] = new ACLUserGroupEntry();
                    userGroupEntries[i].setPrincipal(new UserGroup(new Workspace(workspaceId), entry.getKey()));
                    userGroupEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
            }

            Map<String, String> roleMappings = new HashMap<>();

            if (rolesMappingDTO != null) {
                for(RoleMappingDTO roleMappingDTO : rolesMappingDTO) {
                    roleMappings.put(roleMappingDTO.getRoleName(), roleMappingDTO.getUserLogin());
                }
            }

            productService.createPartRevision(revisionKey, description, workflowModelId, userEntries, userGroupEntries, roleMappings);

            return Response.ok().build();

        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber , @PathParam("partVersion") String partVersion) {
        try {
            PartRevisionKey revisionKey = new PartRevisionKey(workspaceId,partNumber,partVersion);
            productService.deletePartRevision(revisionKey);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/iterations/{partIteration}/files/{fileName}")
    public Response removeAttachedFile(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber , @PathParam("partVersion") String partVersion, @PathParam("partIteration") int partIteration, @PathParam("fileName") String fileName) {
        try {
            PartIterationKey partIKey = new PartIterationKey(workspaceId, partNumber, partVersion, partIteration);
            productService.removeCADFileFromPartIteration(partIKey);
            return Response.ok().build();

        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/share")
    public Response createSharedPart(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber , @PathParam("partVersion") String partVersion, SharedPartDTO pSharedPartDTO) {

        String password = pSharedPartDTO.getPassword();
        Date expireDate = pSharedPartDTO.getExpireDate();

        try {
            SharedPart sharedPart = productService.createSharedPart(new PartRevisionKey(workspaceId, partNumber, partVersion), password, expireDate);
            SharedPartDTO sharedPartDTO = mapper.map(sharedPart,SharedPartDTO.class);
            return Response.ok().entity(sharedPartDTO).build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/publish")
    public Response publishPartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber , @PathParam("partVersion") String partVersion) {
        try {
            PartRevision partRevision = productService.getPartRevision(new PartRevisionKey(workspaceId,partNumber,partVersion));
            partRevision.setPublicShared(true);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/unpublish")
    public Response unPublishPartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber , @PathParam("partVersion") String partVersion) {
        try {
            PartRevision partRevision = productService.getPartRevision(new PartRevisionKey(workspaceId,partNumber,partVersion));
            partRevision.setPublicShared(false);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("/aborted-workflows")
    @Produces(MediaType.APPLICATION_JSON)
    public List<WorkflowDTO> getAbortedWorkflows(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber , @PathParam("partVersion") String partVersion) {

        try {

            PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
            PartRevision partRevision = productService.getPartRevision(revisionKey);

            List<Workflow> abortedWorkflows = partRevision.getAbortedWorkflows();
            List<WorkflowDTO> abortedWorkflowsDTO = new ArrayList<WorkflowDTO>();

            for(Workflow abortedWorkflow:abortedWorkflows){
                abortedWorkflowsDTO.add(mapper.map(abortedWorkflow,WorkflowDTO.class));
            }

            return abortedWorkflowsDTO;

        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private List<InstanceAttribute> createInstanceAttribute(List<InstanceAttributeDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        List<InstanceAttribute> data = new ArrayList<InstanceAttribute>();
        int i = 0;
        for (InstanceAttributeDTO dto : dtos) {
            data.add(createObject(dto));
        }

        return data;
    }

    private InstanceAttribute createObject(InstanceAttributeDTO dto) {
        if (dto.getType().equals(InstanceAttributeDTO.Type.BOOLEAN)) {
            InstanceBooleanAttribute attr = new InstanceBooleanAttribute();
            attr.setName(dto.getName());
            attr.setBooleanValue(Boolean.parseBoolean(dto.getValue()));
            return attr;
        } else if (dto.getType().equals(InstanceAttributeDTO.Type.TEXT)) {
            InstanceTextAttribute attr = new InstanceTextAttribute();
            attr.setName(dto.getName());
            attr.setTextValue(dto.getValue());
            return attr;
        } else if (dto.getType().equals(InstanceAttributeDTO.Type.NUMBER)) {
            InstanceNumberAttribute attr = new InstanceNumberAttribute();
            attr.setName(dto.getName());
            try{
                attr.setNumberValue(Float.parseFloat(dto.getValue()));
            }catch(NumberFormatException ex){
                attr.setNumberValue(0);
            }
            return attr;
        } else if (dto.getType().equals(InstanceAttributeDTO.Type.DATE)) {
            InstanceDateAttribute attr = new InstanceDateAttribute();
            attr.setName(dto.getName());
            try{
                attr.setDateValue(new Date(Long.parseLong(dto.getValue())));
            }catch(NumberFormatException ex){
                attr.setDateValue(null);
            }
            return attr;
        } else if (dto.getType().equals(InstanceAttributeDTO.Type.URL)) {
            InstanceURLAttribute attr = new InstanceURLAttribute();
            attr.setName(dto.getName());
            attr.setUrlValue(dto.getValue());
            return attr;
        } else {
            throw new IllegalArgumentException("Instance attribute not supported");
        }
    }


    private List<PartUsageLink> createComponents(String workspaceId, List<PartUsageLinkDTO> pComponents) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, CreationException, UserNotFoundException, PartMasterAlreadyExistsException, UserNotActiveException, WorkflowModelNotFoundException, PartMasterTemplateNotFoundException, FileAlreadyExistsException, RoleNotFoundException {

        List<PartUsageLink> components = new ArrayList<PartUsageLink>();
        for(PartUsageLinkDTO partUsageLinkDTO : pComponents){

            PartMaster component = findOrCreatePartMaster(workspaceId, partUsageLinkDTO.getComponent());

            if(component != null){
                PartUsageLink partUsageLink = new PartUsageLink();

                List<CADInstance> cadInstances = new ArrayList<CADInstance>();

                if( partUsageLinkDTO.getCadInstances() != null){
                    for(CADInstanceDTO cadInstanceDTO : partUsageLinkDTO.getCadInstances()){
                        cadInstances.add(new CADInstance(
                                cadInstanceDTO.getTx(),
                                cadInstanceDTO.getTy(),
                                cadInstanceDTO.getTz(),
                                cadInstanceDTO.getRx(),
                                cadInstanceDTO.getRy(),
                                cadInstanceDTO.getRz()));
                    }
                }else{
                    for(double i = 0 ; i < partUsageLinkDTO.getAmount() ; i ++){
                        cadInstances.add(new CADInstance(0, 0, 0, 0, 0, 0));
                    }
                }
                partUsageLink.setComponent(component);
                partUsageLink.setAmount(cadInstances.size());
                partUsageLink.setCadInstances(cadInstances);
                components.add(partUsageLink);
            }

        }

        return components;

    }

    private PartMaster findOrCreatePartMaster(String workspaceId, ComponentDTO componentDTO) throws NotAllowedException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, PartMasterAlreadyExistsException, CreationException, WorkflowModelNotFoundException, PartMasterTemplateNotFoundException, FileAlreadyExistsException, RoleNotFoundException {
        String componentNumber = componentDTO.getNumber();
        PartMasterKey partMasterKey = new PartMasterKey(workspaceId,componentNumber);
        if(productService.partMasterExists(partMasterKey)){
            return new PartMaster(userManager.getWorkspace(workspaceId),componentNumber);
        }else{
           return productService.createPartMaster(workspaceId, componentDTO.getNumber(), componentDTO.getName(), componentDTO.isStandardPart(), null, componentDTO.getDescription(), null, null, null, null);
        }
    }

    private DocumentIterationKey[] createDocumentIterationKey(List<DocumentIterationDTO> dtos) {
        DocumentIterationKey[] data = new DocumentIterationKey[dtos.size()];
        int i = 0;
        for (DocumentIterationDTO dto : dtos) {
            data[i++] = createObject(dto);
        }
        return data;
    }

    private DocumentIterationKey createObject(DocumentIterationDTO dto) {
        return new DocumentIterationKey(dto.getWorkspaceId(), dto.getDocumentMasterId(), dto.getDocumentRevisionVersion(), dto.getIteration());
    }

}