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

import com.docdoku.core.exceptions.PlatformHealthException;
import com.docdoku.core.services.IPlatformHealthManagerLocal;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Api(value = "Platforms", description = "Operations about platform")
@Path("platform")
public class PlatformResource {

    @Inject
    private IPlatformHealthManagerLocal platformHealthManager;

    public PlatformResource() {
    }

    @PostConstruct
    public void init() {
    }

    @GET
    @Path("health")
    @ApiOperation(value = "Get platform health status",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Server health is ok"),
            @ApiResponse(code = 500, message = "Server health is ko or partial")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlatformHealthStatus() {
        try {
            platformHealthManager.runHealthCheck();
            return Response.noContent().build();
        } catch (PlatformHealthException e) {
            return Response.serverError().build();
        }
    }
}