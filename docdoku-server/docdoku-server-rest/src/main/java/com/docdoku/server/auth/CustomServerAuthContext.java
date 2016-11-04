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

package com.docdoku.server.auth;

import com.docdoku.server.auth.modules.CustomSAM;

import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthContext;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Authentication context
 * <p>
 * Instantiate and maintain all security modules. Use the appropriate module for incoming requests.
 *
 * @author Morgan Guimard
 */
public class CustomServerAuthContext implements ServerAuthContext {

    private final List<CustomSAM> serverAuthModules;

    public CustomServerAuthContext(List<CustomSAM> serverAuthModules) {
        this.serverAuthModules = serverAuthModules;
    }

    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {
        CustomSAM module = getModule(messageInfo);

        if (module != null) {
            return module.validateRequest(messageInfo, clientSubject, serviceSubject);
        }

        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return AuthStatus.FAILURE;

    }

    @Override
    public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException {
        CustomSAM module = getModule(messageInfo);

        if (module != null) {
            return module.secureResponse(messageInfo, serviceSubject);
        }

        return AuthStatus.SEND_FAILURE;
    }

    @Override
    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
        CustomSAM module = getModule(messageInfo);
        if (module != null) {
            module.cleanSubject(messageInfo, subject);
        }
    }

    private CustomSAM getModule(MessageInfo messageInfo) {
        return serverAuthModules.stream()
                .filter(serverAuthModule -> serverAuthModule.canHandle(messageInfo))
                .findFirst().orElse(null);
    }
}
