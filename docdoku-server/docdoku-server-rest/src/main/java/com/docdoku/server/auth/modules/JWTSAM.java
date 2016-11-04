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

package com.docdoku.server.auth.modules;

import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.server.auth.jwt.JWTokenFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of jwt authentication in requests headers
 *
 * @author Morgan Guimard
 */
public class JWTSAM extends CustomSAM {

    private static final Logger LOGGER = Logger.getLogger(JWTSAM.class.getName());

    public JWTSAM() {
    }

    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {

        HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();

        LOGGER.log(Level.INFO, "Validating request @" + request.getMethod() + " " + request.getRequestURI());

        String authorization = request.getHeader("Authorization");
        String[] splitAuthorization = authorization.split(" ");

        String jwt = splitAuthorization[1];

        UserGroupMapping userGroupMapping = JWTokenFactory.validateToken(jwt);

        if (userGroupMapping != null) {
            CallerPrincipalCallback callerPrincipalCallback = new CallerPrincipalCallback(clientSubject, userGroupMapping.getLogin());
            GroupPrincipalCallback groupPrincipalCallback = new GroupPrincipalCallback(clientSubject, new String[]{userGroupMapping.getGroupName()});
            Callback[] callbacks = new Callback[]{callerPrincipalCallback, groupPrincipalCallback};

            try {
                callbackHandler.handle(callbacks);
            } catch (IOException | UnsupportedCallbackException e) {
                throw new RuntimeException(e);
            }

            return AuthStatus.SUCCESS;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return AuthStatus.FAILURE;

    }

    @Override
    public boolean canHandle(MessageInfo messageInfo) {
        HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.split(" ").length == 2;
        }
        return false;
    }


}
