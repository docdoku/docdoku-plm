/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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

import com.docdoku.core.common.Account;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.ApplicationException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.rest.dto.AccountDTO;
import com.docdoku.server.rest.dto.GCMAccountDTO;
import com.docdoku.server.rest.dto.WorkspaceDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Path("accounts")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class AccountResource {

    @EJB
    private IUserManagerLocal userManager;

    @Resource
    private SessionContext ctx;

    private Mapper mapper;

    public AccountResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountDTO getAccount(){

        try{
            Account account = userManager.getAccount(ctx.getCallerPrincipal().getName());
            return mapper.map(account,AccountDTO.class);
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }


    @GET
    @Path("/workspaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<WorkspaceDTO> getWorkspaces(){

        Workspace[] workspacesWhereCallerIsActive = userManager.getWorkspacesWhereCallerIsActive();

        List<WorkspaceDTO> workspaceDTOs = new ArrayList<WorkspaceDTO>();
        for(int i = 0 ; i < workspacesWhereCallerIsActive.length;i++){
            workspaceDTOs.add(mapper.map(workspacesWhereCallerIsActive[i],WorkspaceDTO.class));
        }

        return workspaceDTOs;

    }

    @PUT
    @Path("gcm")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setGCMAccount(GCMAccountDTO data){
        try{
            userManager.setGCMAccount(data.getGcmId());
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }


    @DELETE
    @Path("gcm")
    public Response deleteGCMAccount(){
        try{
            userManager.deleteGCMAccount();
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

}
