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
import com.docdoku.server.rest.util.SearchQueryParser;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class PartsResource {

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IUserManagerLocal userManager;

    public PartsResource() {
    }

    private Mapper mapper;

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Path("{partKey}")
    @Produces("application/json;charset=UTF-8")
    public Response getPartDTO(@PathParam("workspaceId") String pWorkspaceId, @PathParam("partKey") String pPartKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException {
        try {
            PartRevisionKey revisionKey = new PartRevisionKey(new PartMasterKey(pWorkspaceId, getPartNumber(pPartKey)), getPartRevision(pPartKey));
            PartRevision partRevision = productService.getPartRevision(revisionKey);
            PartDTO partDTO = Tools.mapPartRevisionToPartDTO(partRevision);
            return Response.ok(partDTO).build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("{partKey}/iterations/{partIteration}")
    @Produces("application/json;charset=UTF-8")
    @Consumes("application/json;charset=UTF-8")
    public Response updatePartIteration(@PathParam("workspaceId") String pWorkspaceId, @PathParam("partKey") String pPartKey, @PathParam("partIteration") int partIteration, PartIterationDTO data) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException {
        try {
            PartRevisionKey revisionKey = new PartRevisionKey(new PartMasterKey(pWorkspaceId, getPartNumber(pPartKey)), getPartRevision(pPartKey));
            PartRevision partRevision = productService.getPartRevision(revisionKey);

            PartIterationKey pKey = new PartIterationKey(pWorkspaceId,partRevision.getPartNumber(), partRevision.getVersion(), partIteration);

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
    @Path("{partKey}/checkin")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public Response checkIn(@PathParam("workspaceId") String workspaceId, @PathParam("partKey") String partKey) {
        try {
            PartRevisionKey revisionKey = new PartRevisionKey(new PartMasterKey(workspaceId, getPartNumber(partKey)), getPartRevision(partKey));
            productService.checkInPart(revisionKey);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("{partKey}/checkout")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public Response checkOut(@PathParam("workspaceId") String workspaceId, @PathParam("partKey") String partKey) {
        try {
            PartRevisionKey revisionKey = new PartRevisionKey(new PartMasterKey(workspaceId, getPartNumber(partKey)), getPartRevision(partKey));
            productService.checkOutPart(revisionKey);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("{partKey}/undocheckout")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public Response undoCheckOut(@PathParam("workspaceId") String workspaceId, @PathParam("partKey") String partKey) {
        try {
            PartRevisionKey revisionKey = new PartRevisionKey(new PartMasterKey(workspaceId, getPartNumber(partKey)), getPartRevision(partKey));
            productService.undoCheckOutPart(revisionKey);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("{partKey}/acl")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public Response updateACL(@PathParam("workspaceId") String workspaceId, @PathParam("partKey") String partKey, ACLDTO acl) {
        try {

            PartRevisionKey revisionKey = new PartRevisionKey(new PartMasterKey(workspaceId, getPartNumber(partKey)), getPartRevision(partKey));

            if (acl.getGroupEntries().size() > 0 && acl.getUserEntries().size() > 0) {

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
    @Path("{partKey}/newVersion")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public Response createNewVersion(@PathParam("workspaceId") String pWorkspaceId, @PathParam("partKey") String partKey, PartCreationDTO partCreationDTO) {

        RoleMappingDTO[] rolesMappingDTO = partCreationDTO.getRoleMapping();
        ACLDTO acl = partCreationDTO.getAcl();
        PartRevisionKey revisionKey = new PartRevisionKey(new PartMasterKey(pWorkspaceId, getPartNumber(partKey)), getPartRevision(partKey));
        String name = partCreationDTO.getName();
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
                    userEntries[i].setPrincipal(new User(new Workspace(pWorkspaceId), entry.getKey()));
                    userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
                i = 0;
                for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                    userGroupEntries[i] = new ACLUserGroupEntry();
                    userGroupEntries[i].setPrincipal(new UserGroup(new Workspace(pWorkspaceId), entry.getKey()));
                    userGroupEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
            }

            Map<String, String> roleMappings = new HashMap<>();

            if (rolesMappingDTO != null) {
                for(RoleMappingDTO roleMappingDTO : rolesMappingDTO) {
                    roleMappings.put(roleMappingDTO.getRoleName(), roleMappingDTO.getUserLogin());
                }
            }

            PartRevision partR = productService.createPartVersion(revisionKey, description, workflowModelId, userEntries, userGroupEntries,roleMappings);

            return Response.ok().build();

        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public List<PartDTO> getPartRevisions(@PathParam("workspaceId") String workspaceId, @QueryParam("start") int start) {
        try {
            int maxResults = 20;
            List<PartRevision> partRevisions = productService.getPartRevisions(Tools.stripTrailingSlash(workspaceId), start, maxResults);
            List<PartDTO> partDTOs = new ArrayList<PartDTO>();

            for(PartRevision partRevision : partRevisions){
                partDTOs.add(Tools.mapPartRevisionToPartDTO(partRevision));
            }

            return partDTOs;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("count")
    @Produces("application/json;charset=UTF-8")
    public PartCountDTO getPartRevisionCount(@PathParam("workspaceId") String workspaceId) {
        try {
            return new PartCountDTO(productService.getPartRevisionsCount(Tools.stripTrailingSlash(workspaceId)));
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("search/{query}")
    @Produces("application/json;charset=UTF-8")
    public List<PartDTO> searchPartRevisions(@PathParam("workspaceId") String workspaceId, @PathParam("query") String pStringQuery) {
        try{

            PartSearchQuery partSearchQuery = SearchQueryParser.parsePartStringQuery(workspaceId, pStringQuery);

            List<PartRevision> partRevisions = productService.searchPartRevisions(partSearchQuery);
            List<PartDTO> partDTOs = new ArrayList<PartDTO>();

            for(PartRevision partRevision : partRevisions){
                partDTOs.add(Tools.mapPartRevisionToPartDTO(partRevision));
            }

            return partDTOs;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("numbers")
    @Produces("application/json;charset=UTF-8")
    public List<LightPartMasterDTO> searchPartNumbers(@PathParam("workspaceId") String workspaceId, @QueryParam("q") String q) {
        try {
            List<PartMaster> partMasters = productService.findPartMasters(Tools.stripTrailingSlash(workspaceId), "%" + q + "%", 8);
            List<LightPartMasterDTO> partsMastersDTO = new ArrayList<LightPartMasterDTO>();
            for(PartMaster p : partMasters){
                partsMastersDTO.add(new LightPartMasterDTO(p.getNumber()));
            }
            return partsMastersDTO;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private String getPartNumber(String partKey) {
        int lastDash = partKey.lastIndexOf('-');
        return partKey.substring(0, lastDash);
    }

    private String getPartRevision(String partKey) {
        int lastDash = partKey.lastIndexOf('-');
        return partKey.substring(lastDash + 1, partKey.length());
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    public PartDTO createNewPart(@PathParam("workspaceId") String workspaceId, PartCreationDTO partCreationDTO){

        try {
            String pWorkflowModelId = partCreationDTO.getWorkflowModelId();
            RoleMappingDTO[] rolesMappingDTO = partCreationDTO.getRoleMapping();

            Map<String, String> roleMappings = new HashMap<String,String>();

            if(rolesMappingDTO != null){
                for(RoleMappingDTO roleMappingDTO : rolesMappingDTO){
                    roleMappings.put(roleMappingDTO.getRoleName(),roleMappingDTO.getUserLogin());
                }
            }

            ACLDTO acl = partCreationDTO.getAcl();
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

            PartMaster partMaster = productService.createPartMaster(workspaceId, partCreationDTO.getNumber(), partCreationDTO.getName(), partCreationDTO.getDescription(), partCreationDTO.isStandardPart(), pWorkflowModelId, partCreationDTO.getDescription(), partCreationDTO.getTemplateId(), roleMappings, userEntries, userGroupEntries);
            PartDTO partDTO = Tools.mapPartRevisionToPartDTO(partMaster.getLastRevision());
            return partDTO;

        } catch (Exception ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @DELETE
    @Path("{partKey}")
    @Produces("application/json;charset=UTF-8")
    public Response deletePartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("partKey") String partKey) {
        try {
            PartRevisionKey revisionKey = new PartRevisionKey(new PartMasterKey(workspaceId, getPartNumber(partKey)), getPartRevision(partKey));
            productService.deletePartRevision(revisionKey);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }


    @DELETE
    @Consumes("application/json;charset=UTF-8")
    @Path("{partKey}/iterations/{partIteration}/files/{fileName}")
    public Response removeAttachedFile(@PathParam("workspaceId") String workspaceId, @PathParam("partKey") String partKey, @PathParam("partIteration") int partIteration, @PathParam("fileName") String fileName) {
        try {
            int lastDash = partKey.lastIndexOf('-');
            String number = partKey.substring(0, lastDash);
            String version = partKey.substring(lastDash + 1, partKey.length());

            PartIterationKey partIKey = new PartIterationKey(workspaceId, number, version, partIteration);
            productService.removeCADFileFromPartIteration(partIKey);
            return Response.ok().build();

        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("{partKey}/share")
    public Response createSharedPart(@PathParam("workspaceId") String workspaceId, SharedPartDTO pSharedPartDTO) {

        String number = pSharedPartDTO.getPartMasterNumber();
        String version = pSharedPartDTO.getPartMasterVersion();
        String password = pSharedPartDTO.getPassword();
        Date expireDate = pSharedPartDTO.getExpireDate();

        try {
            SharedPart sharedPart = productService.createSharedPart(new PartRevisionKey(workspaceId, number, version), password, expireDate);
            SharedPartDTO sharedPartDTO = mapper.map(sharedPart,SharedPartDTO.class);
            return Response.ok().entity(sharedPartDTO).build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Path("{partKey}/publish")
    public Response publishPartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("partKey") String partKey) {
        try {
            int lastDash = partKey.lastIndexOf('-');
            String number = partKey.substring(0, lastDash);
            String version = partKey.substring(lastDash + 1, partKey.length());
            PartRevision partRevision = productService.getPartRevision(new PartRevisionKey(workspaceId,number,version));
            partRevision.setPublicShared(true);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Path("{partKey}/unpublish")
    public Response unPublishPartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("partKey") String partKey) {
        try {
            int lastDash = partKey.lastIndexOf('-');
            String number = partKey.substring(0, lastDash);
            String version = partKey.substring(lastDash + 1, partKey.length());
            PartRevision partRevision = productService.getPartRevision(new PartRevisionKey(workspaceId,number,version));
            partRevision.setPublicShared(false);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{partKey}/aborted-workflows")
    @Produces("application/json;charset=UTF-8")
    public List<WorkflowDTO> getAbortedWorkflows(@PathParam("workspaceId") String workspaceId, @PathParam("partKey") String partKey) {

        try {
            PartRevisionKey revisionKey = new PartRevisionKey(new PartMasterKey(workspaceId, getPartNumber(partKey)), getPartRevision(partKey));
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
            attr.setTextValue((String) dto.getValue());
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
           return productService.createPartMaster(workspaceId, componentDTO.getNumber(), componentDTO.getName(), componentDTO.getDescription(), componentDTO.isStandardPart(), null, componentDTO.getDescription(), null, null, null, null);
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
        return new DocumentIterationKey(dto.getWorkspaceId(), dto.getDocumentMasterId(), dto.getDocumentMasterVersion(), dto.getIteration());
    }

}