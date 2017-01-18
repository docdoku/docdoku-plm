/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.exceptions.UserNotFoundException;
import com.docdoku.core.exceptions.WorkspaceNotEnabledException;
import com.docdoku.core.exceptions.WorkspaceNotFoundException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import io.swagger.annotations.*;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Morgan Guimard
 */

@RequestScoped
@Api(hidden = true, value = "attributes", description = "Operations about attributes")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class AttributesResource {

    @Inject
    private IProductManagerLocal productManager;

    private Mapper mapper;

    public AttributesResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Path("part-iterations")
    @ApiOperation(value = "Get parts instance attributes list for given workspace",
            response = InstanceAttributeDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of InstanceAttributeDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartIterationsAttributes(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        List<InstanceAttribute> attributes = productManager.getPartIterationsInstanceAttributesInWorkspace(workspaceId);
        List<InstanceAttributeDTO> attributeDTOList = new ArrayList<>();
        Set<String> seen=new HashSet<>();

        for (InstanceAttribute attribute : attributes) {
            if(attribute==null)
                continue;

            InstanceAttributeDTO dto = mapper.map(attribute, InstanceAttributeDTO.class);
            if(seen.add(dto.getType()+"."+dto.getName())) {
                dto.setValue(null);
                dto.setMandatory(false);
                dto.setLocked(false);
                dto.setLovName(null);
                attributeDTOList.add(dto);
            }
        }

        return Response.ok(new GenericEntity<List<InstanceAttributeDTO>>((List<InstanceAttributeDTO>) attributeDTOList) {
        }).build();
    }

    @GET
    @Path("path-data")
    @ApiOperation(value = "Get path data instance attributes list for given workspace",
            response = InstanceAttributeDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of InstanceAttributeDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPathDataAttributes(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        List<InstanceAttribute> attributes = productManager.getPathDataInstanceAttributesInWorkspace(workspaceId);
        List<InstanceAttributeDTO> attributeDTOList = new ArrayList<>();
        Set<String> seen=new HashSet<>();

        for (InstanceAttribute attribute : attributes) {
            if(attribute==null)
                continue;

            InstanceAttributeDTO dto = mapper.map(attribute, InstanceAttributeDTO.class);
            if(seen.add(dto.getType()+"."+dto.getName())) {
                dto.setValue(null);
                dto.setMandatory(false);
                dto.setLocked(false);
                dto.setLovName(null);
                attributeDTOList.add(dto);
            }
        }
        return Response.ok(new GenericEntity<List<InstanceAttributeDTO>>((List<InstanceAttributeDTO>) attributeDTOList) {
        }).build();
    }
}
