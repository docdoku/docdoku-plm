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

import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.server.rest.dto.ComponentDTO;
import com.docdoku.server.rest.dto.PartDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

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
}