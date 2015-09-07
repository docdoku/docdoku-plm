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

import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.exceptions.UserNotFoundException;
import com.docdoku.core.exceptions.WorkspaceNotFoundException;
import com.docdoku.core.meta.InstanceAttributeDescriptor;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.InstanceAttributeDescriptorDTO;
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
import java.util.List;

/**
 * @author Morgan Guimard
 */

@RequestScoped
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class AttributesResource {

    @Inject
    private IProductManagerLocal productManager;

    private Mapper mapper;

    public AttributesResource(){
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Path("part-iterations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartIterationsAttributes(@PathParam("workspaceId") String workspaceId)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        List<InstanceAttributeDescriptor> attributes = productManager.getPartIterationsInstanceAttributesInWorkspace(workspaceId);
        List<InstanceAttributeDescriptorDTO> dtos = new ArrayList<>();
        for (InstanceAttributeDescriptor descriptor:attributes){
            dtos.add(mapper.map(descriptor,InstanceAttributeDescriptorDTO.class));
        }

        return Response.ok(new GenericEntity<List<InstanceAttributeDescriptorDTO>>((List<InstanceAttributeDescriptorDTO>) dtos) {
        }).build();
    }

    @GET
    @Path("path-data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPathDataAttributes(@PathParam("workspaceId") String workspaceId)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        List<InstanceAttributeDescriptor> attributes = productManager.getPathDataInstanceAttributesInWorkspace(workspaceId);
        List<InstanceAttributeDescriptorDTO> dtos = new ArrayList<>();
        for (InstanceAttributeDescriptor descriptor:attributes){
            dtos.add(mapper.map(descriptor,InstanceAttributeDescriptorDTO.class));
        }
        return Response.ok(new GenericEntity<List<InstanceAttributeDescriptorDTO>>((List<InstanceAttributeDescriptorDTO>) dtos) {
        }).build();
    }
}
