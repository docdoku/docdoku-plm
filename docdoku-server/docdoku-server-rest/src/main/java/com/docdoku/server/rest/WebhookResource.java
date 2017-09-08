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

import com.docdoku.core.exceptions.*;
import com.docdoku.core.hooks.SNSWebhookApp;
import com.docdoku.core.hooks.SimpleWebhookApp;
import com.docdoku.core.hooks.Webhook;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IWebhookManagerLocal;
import com.docdoku.server.rest.dto.SNSWebhookDTO;
import com.docdoku.server.rest.dto.SimpleWebhookDTO;
import com.docdoku.server.rest.dto.WebhookDTO;
import io.swagger.annotations.*;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
@Api(hidden = true, value = "webhook", description = "Operations about webhooks")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class WebhookResource {

    @Inject
    private IWebhookManagerLocal webhookManager;

    private Mapper mapper;

    public WebhookResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }


    @GET
    @ApiOperation(value = "Get webhooks",
            response = WebhookDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of WebhookDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public WebhookDTO[] getWebhooks(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId
    ) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException {
        List<Webhook> webHooks = webhookManager.getWebHooks(workspaceId);
        List<WebhookDTO> webHookDTOs = new ArrayList<>();
        for (Webhook webhook : webHooks) {
            webHookDTOs.add(mapper.map(webhook, WebhookDTO.class));
        }
        return webHookDTOs.toArray(new WebhookDTO[webHookDTOs.size()]);
    }

    @GET
    @Path("/{webhookId}")
    @ApiOperation(value = "Get webhook",
            response = WebhookDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of WebhookDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public WebhookDTO getWebhook(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Webhook id") @PathParam("webhookId") Integer webhookId
    ) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, WebhookNotFoundException {
        Webhook webHook = webhookManager.getWebHook(workspaceId, webhookId);
        return mapper.map(webHook, WebhookDTO.class);
    }

    @POST
    @ApiOperation(value = "Create webhook",
            response = WebhookDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful creation of webhook"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public WebhookDTO createWebhook(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Webhook definition") WebhookDTO webhookDTO
    ) throws AccessRightException, UserNotActiveException, AccountNotFoundException, WorkspaceNotFoundException,
            UserNotFoundException, WorkspaceNotEnabledException {
        Webhook webHook = webhookManager.createWebhook(workspaceId, webhookDTO.getName(), webhookDTO.isActive());
        return mapper.map(webHook, WebhookDTO.class);
    }

    @PUT
    @Path("/{webhookId}")
    @ApiOperation(value = "Update webhook",
            response = WebhookDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful update of webhook"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Webhook not found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public WebhookDTO updateWebhook(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Webhook id") @PathParam("webhookId") Integer webhookId,
            @ApiParam(required = true, value = "Webhook definition") WebhookDTO webhookDTO
    ) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, WebhookNotFoundException {

        Webhook webHook = webhookManager.updateWebHook(workspaceId, webhookId, webhookDTO.getName(), webhookDTO.isActive());
        return mapper.map(webHook, WebhookDTO.class);
    }

    @DELETE
    @Path("/{webhookId}")
    @ApiOperation(value = "Delete webhook",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful delete of webhook"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Webhook not found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteWebhook(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Webhook id") @PathParam("webhookId") Integer webhookId
    ) throws WorkspaceNotFoundException, AccessRightException, WebhookNotFoundException, AccountNotFoundException {
        webhookManager.deleteWebhook(workspaceId, webhookId);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{webhookId}/simple-webhook")
    @ApiOperation(value = "Associate a simple-webhook configuration to webhook",
            response = SimpleWebhookDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful configuration of webhook"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Webhook not found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public SimpleWebhookDTO configureSimpleWebhook(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Webhook id") @PathParam("webhookId") Integer webhookId,
            @ApiParam(required = true, value = "Simple webhook configuration") SimpleWebhookDTO simpleWebhookDTO
    ) throws WorkspaceNotFoundException, AccessRightException, WebhookNotFoundException, AccountNotFoundException {
        SimpleWebhookApp simpleWebhookApp = webhookManager.configureSimpleWebhook(workspaceId, webhookId, simpleWebhookDTO.getMethod(), simpleWebhookDTO.getUri(), simpleWebhookDTO.getAuthorization());
        return mapper.map(simpleWebhookApp, SimpleWebhookDTO.class);
    }

    @PUT
    @Path("/{webhookId}/sns-webhook")
    @ApiOperation(value = "Associate a sns-webhook configuration to webhook",
            response = SNSWebhookDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful configuration of webhook"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Webhook not found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public SNSWebhookDTO configureSNSWebhook(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Webhook id") @PathParam("webhookId") Integer webhookId,
            @ApiParam(required = true, value = "SNS webhook configuration") SNSWebhookDTO snsWebhookDTO
    ) throws WorkspaceNotFoundException, AccessRightException, WebhookNotFoundException, AccountNotFoundException {
        SNSWebhookApp snsWebhookApp = webhookManager.configureSNSWebhook(workspaceId, webhookId, snsWebhookDTO.getTopicArn(), snsWebhookDTO.getRegion(), snsWebhookDTO.getAwsAccount(), snsWebhookDTO.getAwsSecret());
        return mapper.map(snsWebhookApp, SNSWebhookDTO.class);
    }
}
