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

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Abstract class for every authentication modules
 *
 * @author Morgan Guimard
 */
public abstract class CustomSAM implements ServerAuthModule {

    protected static final Class[] supportedMessageTypes = new Class[]{HttpServletRequest.class};
    protected CallbackHandler callbackHandler;

    @Override
    public void initialize(MessagePolicy messagePolicy, MessagePolicy messagePolicy1, CallbackHandler callbackHandler, Map map) throws AuthException {
        this.callbackHandler = callbackHandler;
    }

    @Override
    public Class[] getSupportedMessageTypes() {
        return supportedMessageTypes;
    }


    @Override
    public AuthStatus secureResponse(MessageInfo messageInfo, Subject subject) throws AuthException {
        return AuthStatus.SEND_SUCCESS;
    }

    @Override
    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
    }

    public abstract boolean canHandle(MessageInfo messageInfo);

}