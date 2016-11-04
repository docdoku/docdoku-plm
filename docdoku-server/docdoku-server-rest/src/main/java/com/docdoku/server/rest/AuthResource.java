package com.docdoku.server.rest;

import com.docdoku.core.common.Account;
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.exceptions.PasswordRecoveryRequestNotFoundException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IContextManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.auth.jwt.JWTokenFactory;
import com.docdoku.server.rest.dto.AccountDTO;
import com.docdoku.server.rest.dto.LoginRequestDTO;
import com.docdoku.server.rest.dto.PasswordRecoverDTO;
import com.docdoku.server.rest.dto.PasswordRecoveryRequestDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final Logger LOGGER = Logger.getLogger(AuthResource.class.getName());
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
    public Response login(@Context HttpServletRequest request, @Context HttpServletResponse response,
                          @ApiParam(required = true, value = "Login request") LoginRequestDTO loginRequestDTO)
            throws AccountNotFoundException {

        String login = loginRequestDTO.getLogin();
        String password = loginRequestDTO.getPassword();

        Account account = accountManager.authenticateAccount(login, password);

        HttpSession session = request.getSession();

        if (account != null && account.isEnabled()) {

            try {
                LOGGER.log(Level.INFO, "Authenticating response");
                request.authenticate(response);
            } catch (IOException | ServletException e) {
                LOGGER.log(Level.WARNING, "Request.authenticate failed", e);
                return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
            }

            UserGroupMapping userGroupMapping = accountManager.getUserGroupMapping(login);
            session.setAttribute("login", login);
            session.setAttribute("groups", userGroupMapping.getGroupName());

            return Response.ok()
                    .entity(mapper.map(account, AccountDTO.class))
                    .header("jwt", JWTokenFactory.createToken(userGroupMapping))
                    .build();

        } else {
            session.invalidate();
            return Response.status(Response.Status.FORBIDDEN).build();
        }

    }

    @POST
    @Path("/recovery")
    @ApiOperation(value = "Send password recovery request", response = Response.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendPasswordRecovery(@ApiParam(required = true, value = "Password recovery request") PasswordRecoveryRequestDTO passwordRecoveryRequestDTO)
            throws AccountNotFoundException {
        String login = passwordRecoveryRequestDTO.getLogin();
        Account account = accountManager.getAccount(login);
        userManager.createPasswordRecoveryRequest(account);
        return Response.ok().build();
    }

    @POST
    @Path("/recover")
    @ApiOperation(value = "Recover password", response = Response.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendPasswordRecover(@ApiParam(required = true, value = "Password recovery process") PasswordRecoverDTO passwordRecoverDTO)
            throws PasswordRecoveryRequestNotFoundException {
        userManager.recoverPassword(passwordRecoverDTO.getUuid(), passwordRecoverDTO.getNewPassword());
        return Response.ok().build();
    }

    @GET
    @Path("/logout")
    @ApiOperation(value = "Log out connected user", response = Response.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@Context HttpServletRequest request) {
        request.getSession().invalidate();
        try {
            request.logout();
        } catch (ServletException e) {
            LOGGER.log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok().build();
    }
}
