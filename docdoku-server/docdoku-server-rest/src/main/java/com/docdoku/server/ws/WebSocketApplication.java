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

package com.docdoku.server.ws;

import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.auth.jwt.JWTokenFactory;

import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class expose a web socket end point on path {contextPath}/ws
 * <p>
 * It maintains the list of current users sockets, receive, process and broadcast messages.
 * Authentication is made by passing a jwt token on first message.
 * <p>
 * Todo : create plugin system for chat module, webrtc, rooms, 3d collaborative, and many others ...
 */
@Stateless
@ServerEndpoint(
        value = "/ws",
        decoders = {
                WebSocketMessageDecoder.class
        },
        encoders = {
                WebSocketMessageEncoder.class
        }
)
public class WebSocketApplication {

    private static final Logger LOGGER = Logger.getLogger(WebSocketApplication.class.getName());

    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private WebSocketSessionsManager webSocketSessionsManager;

    private List<Session> unAuthenticatedSessions = new ArrayList<>();

    private static final String AUTH = "AUTH";

    @Inject
    @Any
    private Instance<WebSocketModule> webSocketModules;

    @OnError
    public void error(Session session, Throwable error) {
        LOGGER.log(Level.SEVERE, "WebSocket error", error);
        unTrackSession(session);
    }

    @OnClose
    public void close(Session session, CloseReason reason) {
        LOGGER.log(Level.FINE, "WebSocket closed with message '" +
                reason.getReasonPhrase() + "' and code " + reason.getCloseCode());
        unTrackSession(session);
    }

    @OnOpen
    public void open(Session session) {
        unAuthenticatedSessions.add(session);
    }

    @OnMessage
    public void message(Session session, WebSocketMessage message) {
        if(unAuthenticatedSessions.contains(session)){
            authenticateOrClose(session,message);
            return;
        }

        // Modules are responsible for :
        // Exit if caller is not allowed to reach callee (business)
        // Prevent caller to join himself

        WebSocketModule selectedModule = selectModule(message);

        if(null != selectedModule){
            selectedModule.process(session, message);
        }else{
            LOGGER.log(Level.WARNING,"No modules for type " + message.getType());
        }


    }

    private void authenticateOrClose(Session session, WebSocketMessage message) {

        String type = message.getType();

        if(AUTH.equals(type)){
            String jwt = message.getString("jwt");

            UserGroupMapping userGroupMapping = JWTokenFactory.validateToken(jwt);
            if(null != userGroupMapping){
                String login = userGroupMapping.getLogin();
                if (login != null) {
                    unAuthenticatedSessions.remove(session);
                    webSocketSessionsManager.addSession(login, session);
                    return;
                }
            }
        }

        // Authentication failed, close socket
        closeSession(session);
        unTrackSession(session);

    }

    private WebSocketModule selectModule(WebSocketMessage webSocketMessage){
        for(WebSocketModule webSocketModule:webSocketModules){
            if(webSocketModule.canDecode(webSocketMessage)){
                return webSocketModule;
            }
        }
        return null;
    }

    private void closeSession(Session session){
        try {
            session.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    private void unTrackSession(Session session) {
        if(unAuthenticatedSessions.contains(session)){
            unAuthenticatedSessions.remove(session);
        }else {
            webSocketSessionsManager.removeSession(session);
        }
    }


}