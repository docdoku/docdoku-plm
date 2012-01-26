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
import com.docdoku.core.services.ICommandLocal;
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

@Stateless
@Path("documents")
public class DocumentResource {

    @EJB
    private ICommandLocal commandService;
    
    
    @Context
    private UriInfo context;

    public DocumentResource() {
    }

    /**
     * Retrieves representation of an instance of com.docdoku.server.rest.DocumentResource
     * @return an instance of com.docdoku.core.document.DocumentMaster
     */
    @GET
    @Produces("application/json;charset=UTF-8")
    public DocumentMaster getJson() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }
    /*
    @Override
    public DocumentMasterDTO[] findDocMsByFolder(String completePath) throws ApplicationException {
        try {
            DocumentMaster[] docMs = commandService.findDocMsByFolder(completePath);
            return setupDocMNotifications(createDTO(docMs), completePath.split("/")[0]);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }
    
    @Override
    public DocumentMasterDTO[] findDocMsByTag(String workspaceId, String label) throws ApplicationException {
        try {
            DocumentMaster[] docMs = commandService.findDocMsByTag(new TagKey(workspaceId, label));
            return setupDocMNotifications(createDTO(docMs), workspaceId);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }
    
     @Override
    public DocumentMasterDTO[] getCheckedOutDocMs(String workspaceId) throws ApplicationException {
        try {
            DocumentMaster[] docMs = commandService.getCheckedOutDocMs(workspaceId);
            return setupDocMNotifications(createDTO(docMs), workspaceId);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }
     
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
