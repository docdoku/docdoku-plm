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

import com.docdoku.server.auth.modules.*;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Authentication config, returns authentication context to provider
 *
 * @author Morgan Guimard
 */
public class CustomServerAuthConfig implements ServerAuthConfig {

    private static final Logger LOGGER = Logger.getLogger(CustomServerAuthConfig.class.getName());

    private String layer;
    private String appContext;

    /**
     * Declare modules to use (highest priority first)
     */
    private List<CustomSAM> serverAuthModules = Arrays.asList(
            new JWTSAM(),
            new SessionSAM(),
            new BasicHeaderSAM(),
            new GuestSAM()
    );

    public CustomServerAuthConfig(String layer, String appContext, CallbackHandler handler) {

        this.layer = layer;
        this.appContext = appContext;

        LOGGER.log(Level.INFO, "Initializing modules");

        serverAuthModules.forEach(serverAuthModule -> {
            try {
                serverAuthModule.initialize(null, null, handler, Collections.<String, String>emptyMap());
            } catch (AuthException e) {
                LOGGER.log(Level.SEVERE, "Cannot initialize SAM : " + serverAuthModule.getClass().getName(), e);
            }
        });

    }

    @Override
    public ServerAuthContext getAuthContext(String authContextID, Subject serviceSubject, Map properties) throws AuthException {
        return new CustomServerAuthContext(serverAuthModules);
    }

    @Override
    public String getMessageLayer() {
        return layer;
    }

    @Override
    public String getAppContext() {
        return appContext;
    }

    @Override
    public String getAuthContextID(MessageInfo messageInfo) {
        return appContext;
    }

    @Override
    public void refresh() {

    }

    @Override
    public boolean isProtected() {
        return false;
    }
}
