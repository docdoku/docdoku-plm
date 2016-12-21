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

package com.docdoku.server.ws.status;


import com.docdoku.server.ws.WebSocketMessage;
import com.docdoku.server.ws.WebSocketModule;
import com.docdoku.server.ws.WebSocketSessionsManager;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.websocket.Session;

/**
 * Status module plugin implementation
 *
 * @author Morgan Guimard
 */

@StatusWebSocketModule
public class StatusWebSocketModuleImpl implements WebSocketModule {

    private final static String USER_STATUS = "USER_STATUS";
    private final static String USER_STATUS_OFFLINE = "USER_STATUS_OFFLINE";
    private final static String USER_STATUS_ONLINE = "USER_STATUS_ONLINE";
    @Inject
    private WebSocketSessionsManager webSocketSessionsManager;

    @Override
    public boolean canDecode(WebSocketMessage webSocketMessage) {
        return USER_STATUS.equals(webSocketMessage.getType());
    }

    @Override
    public void process(Session session, WebSocketMessage webSocketMessage) {

        String sender = webSocketSessionsManager.getHolder(session);
        String remoteUser = webSocketMessage.getString("remoteUser");

        if(! webSocketSessionsManager.isAllowedToReachUser(sender, remoteUser)){
            // should send 403 ?
            return;
        }

        boolean isRemoteUserOnline = webSocketSessionsManager.hasSessions(remoteUser);
        WebSocketMessage message = createMessage(USER_STATUS, remoteUser, isRemoteUserOnline);

        webSocketSessionsManager.send(session,message);

    }

    private WebSocketMessage createMessage(String type, String remoteUser, boolean onlineStatus){

        JsonObjectBuilder b = Json.createObjectBuilder()
                .add("type",type)
                .add("remoteUser", remoteUser)
                .add("status",onlineStatus ? USER_STATUS_ONLINE:USER_STATUS_OFFLINE);

        return new WebSocketMessage(b.build());
    }

}