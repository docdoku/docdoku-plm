/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */
package com.docdoku.server.rest;

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.TagKey;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.ICommandLocal;
import com.docdoku.core.services.UserNotActiveException;
import com.docdoku.core.services.UserNotFoundException;
import com.docdoku.core.services.WorkspaceNotFoundException;
import com.docdoku.gwt.explorer.shared.ACLDTO;
import com.docdoku.server.rest.dto.DocumentMasterDTO;
import com.docdoku.server.rest.dto.DocumentMasterLightDTO;
import com.docdoku.server.rest.exceptions.ApplicationException;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

@Stateless
@Path("documents")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class DocumentResource {

    @EJB
    private ICommandLocal commandService;
    @Context
    private UriInfo context;
    private Mapper mapper;

    public DocumentResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    /**
     * Retrieves representation of an instance of
     * com.docdoku.server.rest.DocumentResource
     *
     * @return an instance of com.docdoku.core.document.DocumentMaster
     */
    
    @GET
    @Path("{workspaceId}")
    @Produces("application/json;charset=UTF-8")
    public DocumentMasterLightDTO[] getJson(@PathParam("workspaceId") String workspaceId, @QueryParam("tag") String label, @QueryParam("path") String path) {
        try {
            DocumentMasterLightDTO[] docMsResultDTO = null;
            if (label != null) {
                docMsResultDTO = findDocsByTag(workspaceId, label);
            }
            if (path != null) {
                String completePath = Tools.stripTrailingSlash(workspaceId + "/" + path);
                docMsResultDTO = findDocsInGivenPath(completePath);
            }

            //TODO if label and path not null            
            
            return docMsResultDTO;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{workspaceId}/checkedout")
    @Produces("application/json;charset=UTF-8")
    public DocumentMasterLightDTO[] getCheckedOutDocMs(@PathParam("workspaceId") String workspaceId) throws ApplicationException {
        try {
            DocumentMaster[] checkedOutdocMs = commandService.getCheckedOutDocumentMasters(workspaceId);
            DocumentMasterLightDTO[] checkedOutdocMsDTO = new DocumentMasterLightDTO[checkedOutdocMs.length];

            for (int i = 0; i < checkedOutdocMs.length; i++) {
                checkedOutdocMsDTO[i] = mapper.map(checkedOutdocMs[i], DocumentMasterLightDTO.class);
            }

            return checkedOutdocMsDTO;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{workspaceId}/{docMsId}/{docMsVersion}")
    @Produces("application/json;charset=UTF-8")
    public DocumentMasterDTO getDocM(@PathParam("workspaceId") String workspaceId, @PathParam("docMsId") String id, @PathParam("docMsVersion") String version){
        try {
            DocumentMaster docM = commandService.getDocumentMaster(new DocumentMasterKey(workspaceId, id, version));
            
            return mapper.map(docM, DocumentMasterDTO.class);

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }

    /**
     * PUT method for updating or creating an instance of DocumentResource
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    
    @POST
    @Consumes("application/json;charset=UTF-8")
    @Path("{workspaceId}")
    public Response putJson(@PathParam("workspaceId") String workspaceId, DocumentMasterDTO docMsDTO, @QueryParam("workflowModelId") String pWorkflowModelId , @QueryParam("documentMasterTemplateId") String pDocMTemplateId){
                      
        String pDocMID = docMsDTO.getId();
        String pTitle = docMsDTO.getTitle();
        String pDescription = docMsDTO.getDescription();
        String pPath = docMsDTO.getPath();
        String pParentFolder = Tools.stripTrailingSlash(workspaceId+"/"+pPath);
        
        /* Null value for test purpose only */
        ACLDTO acl = null;

        
        try {
            ACLUserEntry[] userEntries = null;
            ACLUserGroupEntry[] userGroupEntries = null;
            if (acl != null) {
                userEntries = new ACLUserEntry[acl.getUserEntries().size()];
                userGroupEntries = new ACLUserGroupEntry[acl.getGroupEntries().size()];
                int i = 0;
                for (Map.Entry<String, ACLDTO.Permission> entry : acl.getUserEntries().entrySet()) {
                    userEntries[i] = new ACLUserEntry();
                    userEntries[i].setPrincipal(new User(new Workspace(workspaceId), entry.getKey()));
                    userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
                i = 0;
                for (Map.Entry<String, ACLDTO.Permission> entry : acl.getGroupEntries().entrySet()) {
                    userGroupEntries[i] = new ACLUserGroupEntry();
                    userGroupEntries[i].setPrincipal(new UserGroup(new Workspace(workspaceId), entry.getKey()));
                    userGroupEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
            }

            commandService.createDocumentMaster(pParentFolder, pDocMID, pTitle, pDescription, pDocMTemplateId, pWorkflowModelId, userEntries, userGroupEntries);           
            return Response.ok().build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }

    /**
     * DELETE method for deleting an instance of DocumentResource
     *
     * @param parent folder path
     * @return the array of the documents that have also been deleted
     */
    @DELETE
    @Path("{workspaceId}/{docId}/{docVersion}")
    @Produces("application/json;charset=UTF-8")
    public Response deleteJson(@PathParam("workspaceId") String workspaceId, @PathParam("docId") String docId, @PathParam("docVersion") String docVersion) {
        try {
            commandService.deleteDocumentMaster(new DocumentMasterKey(workspaceId, docId, docVersion));
            return Response.status(Response.Status.OK).build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }

    /*
     *
     */
    public DocumentMasterLightDTO[] findDocsByTag(String pWorkspaceId, String pLabel) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {

        DocumentMaster[] docMs = commandService.findDocumentMastersByTag(new TagKey(pWorkspaceId, pLabel));
        DocumentMasterLightDTO[] docMsDTO = new DocumentMasterLightDTO[docMs.length];

        for (int i = 0; i < docMs.length; i++) {
            docMsDTO[i] = mapper.map(docMs[i], DocumentMasterLightDTO.class);
        }

        return docMsDTO;
    }

    public DocumentMasterLightDTO[] findDocsInGivenPath(String pCompletePath) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        pCompletePath = Tools.stripTrailingSlash(pCompletePath);
        DocumentMaster[] docM = commandService.findDocumentMastersByFolder(pCompletePath);
        DocumentMasterLightDTO[] dtos = new DocumentMasterLightDTO[docM.length];

        for (int i = 0; i < docM.length; i++) {
            dtos[i] = mapper.map(docM[i], DocumentMasterLightDTO.class);
            dtos[i].setAuthorName(docM[i].getAuthor().getName());

            if (docM[i].getLastIteration() != null) {
                dtos[i].setLastIterationNumber(docM[i].getLastIteration().getIteration());
                dtos[i].setLastIterationDate(docM[i].getLastIteration().getCreationDate());
            }

            if (docM[i].getLifeCycleState() != null) {
                dtos[i].setLifeCycleState(docM[i].getLifeCycleState());
            }
            if (docM[i].getCheckOutUser() != null) {
                dtos[i].setCheckOutUserName(docM[i].getCheckOutUser().getName());
            }
        }

        return dtos;

    }
}
