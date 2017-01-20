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

package com.docdoku.server.auth.modules;

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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class set the caller group to guest if the requested resource is public
 *
 * @author Morgan Guimard
 */
public class GuestSAM extends CustomSAM {

    private static final Logger LOGGER = Logger.getLogger(GuestSAM.class.getName());

    public GuestSAM() {
    }

    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {

        HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        LOGGER.log(Level.FINE, "Validating request @" + request.getMethod() + " " + request.getRequestURI());

        CallerPrincipalCallback callerPrincipalCallback = new CallerPrincipalCallback(clientSubject, "");
        GroupPrincipalCallback groupPrincipalCallback = new GroupPrincipalCallback(clientSubject, new String[]{UserGroupMapping.GUEST_ROLE_ID});
        Callback[] callbacks = {callerPrincipalCallback, groupPrincipalCallback};

        try {
            callbackHandler.handle(callbacks);
        } catch (IOException | UnsupportedCallbackException e) {
            throw new RuntimeException(e);
        }

        return AuthStatus.SUCCESS;

    }

    @Override
    public boolean canHandle(MessageInfo messageInfo) {
        HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        return AuthServices.isPublicRequestURI(request.getContextPath(), request.getRequestURI());
    }

}
