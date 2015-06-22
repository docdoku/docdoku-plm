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

import com.docdoku.core.exceptions.EntityNotFoundException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.security.WorkspaceUserGroupMembership;
import com.docdoku.core.security.WorkspaceUserMembership;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.rest.dto.WorkspaceUserGroupMemberShipDTO;
import com.docdoku.server.rest.dto.WorkspaceUserMemberShipDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Morgan Guimard
 */
@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class WorkspaceMembershipResource {

    @EJB
    private IUserManagerLocal userManager;

    private Mapper mapper;

    public WorkspaceMembershipResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkspaceUserMemberShipDTO[] getWorkspaceUserMemberShips (@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        WorkspaceUserMembership[] workspaceUserMemberships = userManager.getWorkspaceUserMemberships(workspaceId);
        WorkspaceUserMemberShipDTO[] workspaceUserMemberShipDTO = new WorkspaceUserMemberShipDTO[workspaceUserMemberships.length];
        for(int i = 0 ; i< workspaceUserMemberships.length ; i++){
            workspaceUserMemberShipDTO[i] = mapper.map(workspaceUserMemberships[i],WorkspaceUserMemberShipDTO.class);
        }
        return workspaceUserMemberShipDTO;
    }

    @GET
    @Path("users/me")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkspaceUserMemberShipDTO getWorkspaceSpecificUserMemberShips (@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        WorkspaceUserMembership workspaceUserMemberships = userManager.getWorkspaceSpecificUserMemberships(workspaceId);
        return mapper.map(workspaceUserMemberships,WorkspaceUserMemberShipDTO.class);
    }

    @GET
    @Path("usergroups")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkspaceUserGroupMemberShipDTO[] getWorkspaceUserGroupMemberShips (@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        WorkspaceUserGroupMembership[] workspaceUserGroupMemberships = userManager.getWorkspaceUserGroupMemberships(workspaceId);
        WorkspaceUserGroupMemberShipDTO[] workspaceUserGroupMemberShipDTO = new WorkspaceUserGroupMemberShipDTO[workspaceUserGroupMemberships.length];
        for(int i = 0 ; i< workspaceUserGroupMemberships.length ; i++){
            workspaceUserGroupMemberShipDTO[i] = mapper.map(workspaceUserGroupMemberships[i],WorkspaceUserGroupMemberShipDTO.class);
        }
        return workspaceUserGroupMemberShipDTO;
    }

    @GET
    @Path("usergroups/me")
    @Produces(MediaType.APPLICATION_JSON)
    public List<WorkspaceUserGroupMemberShipDTO> getWorkspaceSpecificUserGroupMemberShips (@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        WorkspaceUserGroupMembership[] workspaceUserGroupMemberships = userManager.getWorkspaceSpecificUserGroupMemberships(workspaceId);
        List<WorkspaceUserGroupMemberShipDTO> workspaceUserGroupMemberShipDTO = new ArrayList<>();
        for(int i = 0 ; i< workspaceUserGroupMemberships.length ; i++){
            if(workspaceUserGroupMemberships[i] != null){
                workspaceUserGroupMemberShipDTO.add(mapper.map(workspaceUserGroupMemberships[i],WorkspaceUserGroupMemberShipDTO.class));
            }
        }
        return workspaceUserGroupMemberShipDTO;
    }

}
