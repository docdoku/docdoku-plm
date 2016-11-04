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

import com.docdoku.core.common.Account;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.server.auth.AuthServices;

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
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of basic authentication in requests headers
 *
 * @author Morgan Guimard
 */
public class BasicHeaderSAM extends CustomSAM {

    private static final Logger LOGGER = Logger.getLogger(BasicHeaderSAM.class.getName());

    public BasicHeaderSAM() {
    }

    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {


        HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();

        LOGGER.log(Level.INFO, "Validating request @" + request.getMethod() + " " + request.getRequestURI());

        String authorization = request.getHeader("Authorization");
        String[] splitAuthorization = authorization.split(" ");

        byte[] decoded = DatatypeConverter.parseBase64Binary(splitAuthorization[1]);

        String credentials = null;

        try {
            credentials = new String(decoded, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            // TODO re-throw or log, should send failure ?
            e.printStackTrace();
        }

        String[] splitCredentials = credentials.split(":");

        String login = splitCredentials[0];
        String password = splitCredentials[1];

        Account account = AuthServices.authenticateAccount(login, password);
        UserGroupMapping userGroupMapping = AuthServices.getUserGroupMapping(login);

        if (account != null) {
            CallerPrincipalCallback callerPrincipalCallback = new CallerPrincipalCallback(clientSubject, login);
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
        return authorization != null && authorization.startsWith("Basic ") && authorization.split(" ").length == 2;
    }
}
