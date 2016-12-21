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

package com.docdoku.server.ws.chat;


import com.docdoku.server.ws.WebSocketMessage;
import com.docdoku.server.ws.WebSocketModule;
import com.docdoku.server.ws.WebSocketSessionsManager;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.websocket.Session;

/**
 * Implementation of chat module
 *
 * @author Morgan Guimard
 */

@ChatWebSocketModule
public class ChatWebSocketModuleImpl implements WebSocketModule {

    private static final String CHAT_MESSAGE = "CHAT_MESSAGE";
    private static final String CHAT_MESSAGE_ACK = "CHAT_MESSAGE_ACK";
    public static final String CHAT_MESSAGE_UNREACHABLE = "UNREACHABLE";

    @Inject
    private WebSocketSessionsManager webSocketSessionsManager;

    @Override
    public boolean canDecode(WebSocketMessage webSocketMessage) {
        return CHAT_MESSAGE.equals(webSocketMessage.getType());
    }

    @Override
    public void process(Session session, WebSocketMessage webSocketMessage) {

        String sender = webSocketSessionsManager.getHolder(session);
        String remoteUser = webSocketMessage.getString("remoteUser");

        if(!webSocketSessionsManager.isAllowedToReachUser(sender,remoteUser)){
            // Maybe send 403 ?
            return;
        }

        String context = webSocketMessage.getString("context");
        String message = webSocketMessage.getString("message");

        if (!webSocketSessionsManager.hasSessions(remoteUser)) {
            // Tell the sender the remote is offline
            WebSocketMessage messageForSender = createMessage(CHAT_MESSAGE, remoteUser, "", "", context, CHAT_MESSAGE_UNREACHABLE);
            webSocketSessionsManager.send(session,messageForSender);
        } else {
            WebSocketMessage messageForSender = createMessage(CHAT_MESSAGE_ACK,remoteUser,sender,message,context, "");
            WebSocketMessage messageForRemoteUser = createMessage(CHAT_MESSAGE,sender,remoteUser,message,context,"");
            // Broadcast to both sender/remoteUser the message on each opened sessions
            webSocketSessionsManager.broadcast(sender, messageForSender);
            webSocketSessionsManager.broadcast(remoteUser, messageForRemoteUser);
        }
    }

    private WebSocketMessage createMessage(String type, String remoteUser, String sender, String message, String context, String error){

        JsonObjectBuilder b = Json.createObjectBuilder()
                .add("type",type)
                .add("remoteUser", remoteUser)
                .add("sender",sender)
                .add("message", message)
                .add("context",context);

        if(error!=null && !error.isEmpty()) {
            b.add("error", error);
        }

        return new WebSocketMessage(b.build());
    }

}