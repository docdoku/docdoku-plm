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

import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.TagKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.ICommandLocal;
import com.docdoku.gwt.explorer.shared.ApplicationException;
import com.docdoku.server.rest.dto.DocumentDTO;
import com.docdoku.server.rest.dto.DocumentMasterDTO;
import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    public void init(){
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    /**
     * Retrieves representation of an instance of com.docdoku.server.rest.DocumentResource
     * @return an instance of com.docdoku.core.document.DocumentMaster
     */
    @GET
    @Path("{completePath:.*}")
    @Produces("application/json;charset=UTF-8")
    public DocumentMasterDTO[] getJson(@PathParam("completePath") String completePath) {
        try {
            completePath=Tools.stripTrailingSlash(Tools.stripLeadingSlash(completePath));
            DocumentMaster[] docM = commandService.findDocumentMastersByFolder(completePath);
            DocumentMasterDTO[] dtos = new DocumentMasterDTO[docM.length];
            
            for(int i = 0; i<docM.length;i++)
                dtos[i]= mapper.map(docM[i], DocumentMasterDTO.class);
           
            return dtos;
        }catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{workspaceId}/")
    @Produces("application/json;charset=UTF-8")    
    public DocumentMasterDTO[] findDocMsByTag(@PathParam("workspaceId")String workspaceId, @QueryParam("tag") String label) throws ApplicationException {
        try {
            DocumentMaster[] docMs = commandService.findDocumentMastersByTag(new TagKey(workspaceId, label));
            DocumentMasterDTO[] docMsDTO = new DocumentMasterDTO[docMs.length];
            
            for(int i = 0; i<docMs.length;i++)
                docMsDTO[i]= mapper.map(docMs[i], DocumentMasterDTO.class);
                        
            return docMsDTO;
            
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }
    
   
    @GET
    @Path("{workspaceId}/checkedout")
    @Produces("application/json;charset=UTF-8") 
    public DocumentMasterDTO[] getCheckedOutDocMs(@PathParam("workspaceId") String workspaceId) throws ApplicationException {
        try {
            DocumentMaster[] checkedOutdocMs = commandService.getCheckedOutDocumentMasters(workspaceId);
            DocumentMasterDTO[] checkedOutdocMsDTO = new DocumentMasterDTO[checkedOutdocMs.length];
            
            for(int i = 0; i<checkedOutdocMs.length;i++)
                checkedOutdocMsDTO[i]= mapper.map(checkedOutdocMs[i], DocumentMasterDTO.class);
                        
            return checkedOutdocMsDTO;
            
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

/* 
        @Override
    public DocumentMasterDTO getDocM(String workspaceId, String id, String version) throws ApplicationException {
        try {
            DocumentMaster docM = commandService.getDocM(new DocumentMasterKey(workspaceId, id, version));
            return createDTO(docM);

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }
     
     */

    /**
     * PUT method for updating or creating an instance of DocumentResource
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/json;charset=UTF-8")
    public void putJson(DocumentMaster content) {
    }
    /*
    public DocumentMasterDTO createDocM(String pParentFolder, String pDocMID, String pTitle, String pDescription, String pDocMTemplateId, String pWorkflowModelId, ACLDTO acl) throws ApplicationException {
        try {
            ACLUserEntry[] userEntries = null;
            ACLUserGroupEntry[] userGroupEntries = null;
            if (acl != null) {
                String workspaceId = Folder.parseWorkspaceId(pParentFolder);
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
            DocumentMaster docM = commandService.createDocM(pParentFolder, pDocMId, pTitle, pDescription, pDocMTemplateId, pWorkflowModelId, userEntries, userGroupEntries);
            return createDTO(docM);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }
    */
    
    /**
     * DELETE method for deleting an instance of DocumentResource
     * @param parent folder path
     * @return the array of the documents that have also been deleted
     */
    @DELETE
    @Path("{workspaceId}/{docId}/{docVersion}")
    @Produces("application/json;charset=UTF-8")
    public void deleteJson(@PathParam("workspaceId") String workspaceId, @PathParam("docId") String docId, @PathParam("docVersion") String docVersion) {
        try {
            commandService.deleteDocumentMaster(new DocumentMasterKey(workspaceId, docId, docVersion));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }
    
    
   

}
