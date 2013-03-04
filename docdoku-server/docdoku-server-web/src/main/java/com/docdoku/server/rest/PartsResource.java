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

import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.meta.*;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.server.rest.dto.*;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

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
            PartDTO partDTO = mapper.map(partRevision, PartDTO.class);
            partDTO.setNumber(partRevision.getPartNumber());
            partDTO.setPartKey(partRevision.getPartNumber() + "-" + partRevision.getVersion());
            partDTO.setName(partRevision.getPartMaster().getName());
            partDTO.setStandardPart(partRevision.getPartMaster().isStandardPart());

            List<PartIterationDTO> partIterationDTOs = new ArrayList<PartIterationDTO>();

            for(PartIteration partIteration : partRevision.getPartIterations()){

                List<PartUsageLinkDTO> usageLinksDTO = new ArrayList<PartUsageLinkDTO>();

                PartIterationDTO partIterationDTO = mapper.map(partIteration, PartIterationDTO.class);

                for(PartUsageLink partUsageLink : partIteration.getComponents()){
                    PartUsageLinkDTO partUsageLinkDTO = mapper.map(partUsageLink, PartUsageLinkDTO.class);
                    usageLinksDTO.add(partUsageLinkDTO);
                }

                partIterationDTO.setComponents(usageLinksDTO);
                partIterationDTOs.add(partIterationDTO);
            }

            partDTO.setPartIterations(partIterationDTOs);

            return Response.ok(partDTO).build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
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

            PartDTO partDTO = mapper.map(partRevisionUpdated, PartDTO.class);
            partDTO.setNumber(partRevisionUpdated.getPartNumber());
            partDTO.setPartKey(partRevisionUpdated.getPartNumber() + "-" + partRevisionUpdated.getVersion());
            partDTO.setName(partRevisionUpdated.getPartMaster().getName());
            partDTO.setStandardPart(partRevisionUpdated.getPartMaster().isStandardPart());

            return Response.ok(partDTO).build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
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
        } catch (com.docdoku.core.services.ApplicationException ex) {
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
        } catch (com.docdoku.core.services.ApplicationException ex) {
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
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public String[] searchPartNumbers(@PathParam("workspaceId") String workspaceId, @QueryParam("q") String q) {
        try {
            List<PartMaster> partMasters = productService.findPartMasters(Tools.stripTrailingSlash(workspaceId), "%" + q + "%", 8);
            String[] partNumbers = new String[partMasters.size()];
            for (int i = 0; i < partMasters.size(); i++) {
                partNumbers[i] = partMasters.get(i).getNumber();
            }
            return partNumbers;
        } catch (com.docdoku.core.services.ApplicationException ex) {
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

    @PUT
    @Produces("application/json;charset=UTF-8")
    public ComponentDTO createNewPart(@PathParam("workspaceId") String workspaceId, ComponentDTO componentDTO){

        try {
            PartMaster partMaster = productService.createPartMaster(workspaceId, componentDTO.getNumber(), componentDTO.getName(), componentDTO.getDescription(), componentDTO.isStandardPart(), null, componentDTO.getDescription());

            ComponentDTO dto = new ComponentDTO();

            dto.setNumber(partMaster.getNumber());

            return componentDTO;

        } catch (Exception ex) {
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

        } catch (com.docdoku.core.services.ApplicationException ex) {
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
            attr.setNumberValue(Float.parseFloat(dto.getValue()));
            return attr;
        } else if (dto.getType().equals(InstanceAttributeDTO.Type.DATE)) {
            InstanceDateAttribute attr = new InstanceDateAttribute();
            attr.setName(dto.getName());
            attr.setDateValue(new Date(Long.parseLong(dto.getValue())));
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


    private List<PartUsageLink> createComponents(String workspaceId, List<PartUsageLinkDTO> pComponents) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, CreationException, UserNotFoundException, PartMasterAlreadyExistsException, UserNotActiveException, WorkflowModelNotFoundException {

        List<PartUsageLink> components = new ArrayList<PartUsageLink>();
        for(PartUsageLinkDTO partUsageLinkDTO : pComponents){

            PartMaster component = findOrCreatePartMaster(workspaceId, partUsageLinkDTO.getComponent());

            if(component != null){
                PartUsageLink partUsageLink = new PartUsageLink();

                List<CADInstance> cadInstances = new ArrayList<CADInstance>();
                for(double i = 0 ; i < partUsageLinkDTO.getAmount() ; i ++){
                    cadInstances.add(new CADInstance(0, 0, 0, 0, 0, 0, CADInstance.Positioning.ABSOLUTE));
                }

                partUsageLink.setComponent(component);
                partUsageLink.setAmount(partUsageLinkDTO.getAmount());
                partUsageLink.setCadInstances(cadInstances);
                components.add(partUsageLink);
            }

        }

        return components;


    }

    private PartMaster findOrCreatePartMaster(String workspaceId, ComponentDTO componentDTO) throws NotAllowedException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, PartMasterAlreadyExistsException, CreationException, WorkflowModelNotFoundException {
        String componentNumber = componentDTO.getNumber();
        PartMasterKey partMasterKey = new PartMasterKey(workspaceId,componentNumber);
        if(productService.partMasterExists(partMasterKey)){

            return new PartMaster(userManager.getWorkspace(workspaceId),componentNumber);
        }else{
           return productService.createPartMaster(workspaceId, componentDTO.getNumber(), componentDTO.getName(), componentDTO.getDescription(), componentDTO.isStandardPart(), null, componentDTO.getDescription());
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