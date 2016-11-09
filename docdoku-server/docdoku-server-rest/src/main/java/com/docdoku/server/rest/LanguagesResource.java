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

import com.docdoku.core.security.UserGroupMapping;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("languages")
@Api(value = "languages", description = "Operations about languages")
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
@RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
public class LanguagesResource {

    private final static String[] SUPPORTED_LANGUAGES = {"fr","en"};

    public LanguagesResource() {
    }

    @GET
    @ApiOperation(value = "Get languages",
            response = String.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of supported languages"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLanguages()  {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for(String lang: SUPPORTED_LANGUAGES){
            arrayBuilder.add(lang);
        }
        return Response.ok().entity(arrayBuilder.build()).build();
    }

}
