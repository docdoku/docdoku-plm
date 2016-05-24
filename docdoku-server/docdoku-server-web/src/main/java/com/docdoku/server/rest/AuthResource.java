package com.docdoku.server.rest;

import com.docdoku.core.common.Account;
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IContextManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.rest.dto.AccountDTO;
import com.docdoku.server.rest.dto.LoginRequestDTO;
import com.docdoku.server.rest.dto.PasswordRecoveryRequestDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("auth")
@Api(value = "auth", description = "Operations about authentication")
public class AuthResource {

    @Inject
    private IAccountManagerLocal accountManager;

    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private IContextManagerLocal contextManager;

    private Mapper mapper;

    public AuthResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @POST
    @Path("/login")
    @ApiOperation(value = "Try to authenticate", response = AccountDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AccountDTO login(@Context HttpServletRequest request, LoginRequestDTO loginRequestDTO) throws ServletException, AccountNotFoundException {
        request.logout();
        request.login(loginRequestDTO.getLogin(),loginRequestDTO.getPassword());
        Account account = accountManager.getAccount(loginRequestDTO.getLogin());
        // Create JWT, send in headers
        return mapper.map(account,AccountDTO.class);
    }

    @POST
    @Path("/recovery")
    @ApiOperation(value = "Send password recovery request", response = Response.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendPasswordRecovery(PasswordRecoveryRequestDTO passwordRecoveryRequestDTO) throws AccountNotFoundException {
        String login = passwordRecoveryRequestDTO.getLogin();
        Account account = accountManager.getAccount(login);
        userManager.createPasswordRecoveryRequest(account);
        return Response.ok().build();
    }

    @GET
    @Path("/logout")
    @ApiOperation(value = "Log out connected user", response = Response.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@Context HttpServletRequest request) throws ServletException {
        //
        request.logout();
        return Response.ok().build();
    }
}
