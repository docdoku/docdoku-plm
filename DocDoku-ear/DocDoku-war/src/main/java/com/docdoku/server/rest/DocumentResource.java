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
import com.docdoku.server.rest.dto.DocumentCreationDTO;
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
        System.out.println(" GET documentMaster listing in workspace");
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
        
        System.out.println(" GET checked out documentMaster");
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
    @Path("{workspaceId}/{docKey}")
    @Produces("application/json;charset=UTF-8")
    public DocumentMasterDTO getDocM(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey) {
        
            System.out.println(" GET specific documentMaster");
            
            int lastDash = docKey.lastIndexOf('-');
            String id = docKey.substring(0, lastDash);
            String version = docKey.substring(lastDash+1, docKey.length());            
            System.out.println("docId : "+id);
            System.out.println("docVersion : "+version);
            
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
    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Path("{workspaceId}/{docKey}/checkin")
    public Response checkInDocument(@PathParam("workspaceId") String workspaceId,@PathParam("docKey") String docKey) {
        try {
            
            int lastDash = docKey.lastIndexOf('-');
            String docId = docKey.substring(0, lastDash);
            String docVersion = docKey.substring(lastDash+1, docKey.length());             
            
            commandService.checkIn(new DocumentMasterKey(workspaceId, docId, docVersion));
            return Response.ok().build();
            
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Path("{workspaceId}/{docKey}/checkout")
    public Response checkOutDocument(@PathParam("workspaceId") String workspaceId,  @PathParam("docKey") String docKey) {
        try {
            
            int lastDash = docKey.lastIndexOf('-');
            String docId = docKey.substring(0, lastDash);
            String docVersion = docKey.substring(lastDash+1, docKey.length()); 
            
            commandService.checkOut(new DocumentMasterKey(workspaceId, docId, docVersion));
            return Response.ok().build();

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Path("{workspaceId}/{docKey}/undocheckout")
    public Response undoCheckOutDocument(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey){
        try {
            
            int lastDash = docKey.lastIndexOf('-');
            String docId = docKey.substring(0, lastDash);
            String docVersion = docKey.substring(lastDash+1, docKey.length()); 
            
            commandService.undoCheckOut(new DocumentMasterKey(workspaceId, docId, docVersion));
            return Response.ok().build();

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }
    
    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Path("{workspaceId}/{docKey}/move")
    public Response moveDocument(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey, DocumentCreationDTO docCreationDTO) {
        try {
            
            int lastDash = docKey.lastIndexOf('-');
            String docId = docKey.substring(0, lastDash);
            String docVersion = docKey.substring(lastDash+1, docKey.length()); 
            String parentFolderPath = docCreationDTO.getPath();
            String newCompletePath = Tools.stripTrailingSlash(workspaceId + "/" + parentFolderPath);
            
            DocumentMasterKey docMsKey = new DocumentMasterKey(workspaceId, docId, docVersion);
            commandService.moveDocumentMaster(newCompletePath, docMsKey);
            return Response.ok().build();

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }
    
    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Path("{workspaceId}/{docKey}/newVersion")
    public Response createNewVersion(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey,DocumentCreationDTO docCreationDTO) {
        
        int lastDash = docKey.lastIndexOf('-');
        String pID = docKey.substring(0, lastDash);
        String pVersion = docKey.substring(lastDash+1, docKey.length()); 
        String pWorkspaceId = workspaceId;
        String pTitle = docCreationDTO.getTitle();
        String pDescription = docCreationDTO.getDescription();
        String pWorkflowModelId = null;

        if (docCreationDTO.getWorkflowModel() != null) {
            pWorkflowModelId = docCreationDTO.getWorkflowModel().getId();
        }
                
        /*
         * Null value for test purpose only
         */
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
                    userEntries[i].setPrincipal(new User(new Workspace(pWorkspaceId), entry.getKey()));
                    userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
                i = 0;
                for (Map.Entry<String, ACLDTO.Permission> entry : acl.getGroupEntries().entrySet()) {
                    userGroupEntries[i] = new ACLUserGroupEntry();
                    userGroupEntries[i].setPrincipal(new UserGroup(new Workspace(pWorkspaceId), entry.getKey()));
                    userGroupEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
            }
            commandService.createVersion(new DocumentMasterKey(pWorkspaceId, pID, pVersion), pTitle, pDescription, pWorkflowModelId, userEntries, userGroupEntries);
            return Response.ok().build();            
            
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }

    
    /**
     *
     * POST method
     *
     */

    @POST
    @Consumes("application/json;charset=UTF-8")
    @Path("{workspaceId}")
    public Response putJson(@PathParam("workspaceId") String workspaceId, DocumentCreationDTO docCreationDTO) {
        
        String pDocMID = docCreationDTO.getReference();
        String pTitle = docCreationDTO.getTitle();
        String pDescription = docCreationDTO.getDescription();
        String pPath = docCreationDTO.getPath();
        String pParentFolder = Tools.stripTrailingSlash(workspaceId + "/" + pPath);
        String pWorkflowModelId = null;
        String pDocMTemplateId = null;

        if (docCreationDTO.getWorkflowModel() != null) {
            pWorkflowModelId = docCreationDTO.getWorkflowModel().getId();
        }

        if (docCreationDTO.getDocumentMsTemplate() != null) {
            pDocMTemplateId = docCreationDTO.getDocumentMsTemplate().getId();
        }

        /*
         * Null value for test purpose only
         */
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
    @Path("{workspaceId}/{docKey}")
    @Produces("application/json;charset=UTF-8")
    public Response deleteJson(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey) {
        
            int lastDash = docKey.lastIndexOf('-');
            String id = docKey.substring(0, lastDash);
            String version = docKey.substring(lastDash+1, docKey.length()); 
            
        try {
            commandService.deleteDocumentMaster(new DocumentMasterKey(workspaceId, id, version));
            return Response.status(Response.Status.OK).build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }
    
    
    @DELETE
    @Consumes("application/json;charset=UTF-8")
    @Path("{workspaceId}/{docKey}/iterations/{docIteration}/remove_attached_file/{fileName}")
    public Response removeAttachedFile(@PathParam("workspaceId") String workspaceId,@PathParam("docKey") String docKey, @PathParam("docIteration") String docIteration, @PathParam("fileName") String fileName){
        try { 
            int lastDash = docKey.lastIndexOf('-');
            String id = docKey.substring(0, lastDash);
            String version = docKey.substring(lastDash+1, docKey.length());             

            String fileFullName = workspaceId+"/documents/"+id+"/"+version+"/"+docIteration+"/"+fileName;
            System.out.println("fileFullName : "+fileFullName);
            
            commandService.removeFileFromDocument(fileFullName);
            return Response.ok().build();

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
