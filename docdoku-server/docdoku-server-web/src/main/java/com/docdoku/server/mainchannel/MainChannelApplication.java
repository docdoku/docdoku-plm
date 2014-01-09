/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.server.mainchannel;

import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.mainchannel.modules.ChatModule;
import com.docdoku.server.mainchannel.modules.UserStatusModule;
import com.docdoku.server.mainchannel.modules.WebRTCModule;
import com.docdoku.server.mainchannel.util.ChannelMessagesBuilder;
import com.docdoku.server.mainchannel.util.ChannelMessagesType;
import com.docdoku.server.mainchannel.util.Room;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.StringReader;
import java.security.Principal;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Stateless
@ServerEndpoint("/mainChannelSocket")
public class MainChannelApplication {

    // Users WebSockets HashMap : <UserLogin, <SessionId, Session>>
    private static final ConcurrentMap<String, HashMap<String, Session>> CHANNELS = new ConcurrentHashMap<>();

    @EJB
    private IUserManagerLocal userManager;

    public static boolean hasChannels(String userLogin) {
        if (CHANNELS.get(userLogin) == null){
            return false;
        }
        if (CHANNELS.get(userLogin).values() == null){
            return false;
        }
        return !CHANNELS.get(userLogin).values().isEmpty();
    }

    public static HashMap<String, Session> getUserChannels(String userLogin) {
        return CHANNELS.get(userLogin);
    }



    @OnClose
    public void close(Session session, CloseReason reason) {

        Principal userPrincipal = session.getUserPrincipal();

        String userLogin = userPrincipal.getName();
        String sessionId = session.getId();

        if (sessionId != null && userLogin != null) {
            // find whom the session belongs
            Room.removeUserFromAllRoom(userLogin);
            // remove the session from the user hash map
            CHANNELS.get(userLogin).remove(sessionId);
        }

    }

    @OnMessage
    public void message(Session session, String message) {

        Principal userPrincipal = session.getUserPrincipal();
        String callerLogin = userPrincipal.getName();

        if (message.equals(ChannelMessagesType.PEER_DECLARATION)) {

           onPeerDeclarationMessage(session);

        } else {

            try {

                JsonObject jsObj = Json.createReader(new StringReader(message)).readObject();

                String remoteUser = jsObj.getString("remoteUser");

                // Exit if remote user is null or caller tries to join himself
                if(remoteUser == null || callerLogin == remoteUser){
                    return;
                }

                // Exit if caller cannot reach callee (business)
                if (!callerIsAllowToReachCallee(callerLogin, remoteUser)) {
                    return ;
                }

                String type = jsObj.getString("type");

                switch (type) {

                    case ChannelMessagesType.USER_STATUS:
                        UserStatusModule.onUserStatusRequestMessage(session, jsObj);
                        break;

                    case ChannelMessagesType.WEBRTC_INVITE:
                        WebRTCModule.onWebRTCInviteMessage(session, jsObj);
                        break;
                    case ChannelMessagesType.WEBRTC_ACCEPT:
                        WebRTCModule.onWebRTCAcceptMessage(session, jsObj);
                        break;
                    case ChannelMessagesType.WEBRTC_REJECT:
                        WebRTCModule.onWebRTCRejectMessage(session, jsObj);
                        break;
                    case ChannelMessagesType.WEBRTC_HANGUP:
                        WebRTCModule.onWebRTCHangupMessage(session, jsObj);
                        break;
                    case ChannelMessagesType.WEBRTC_ANSWER:
                    case ChannelMessagesType.WEBRTC_OFFER:
                    case ChannelMessagesType.WEBRTC_CANDIDATE:
                    case ChannelMessagesType.WEBRTC_BYE:
                        WebRTCModule.onWebRTCSignalingMessage(session, jsObj, message);
                        break;

                    case ChannelMessagesType.CHAT_MESSAGE:
                        ChatModule.onChatMessage(session, jsObj);
                        break;

                    default:
                        break;
                }

            } catch (JsonException ex) {
                MainChannelDispatcher.send(session, ChannelMessagesBuilder.BuildJsonExMessage());
            }

        }

    }

    private void onPeerDeclarationMessage(Session session) {

        Principal userPrincipal = session.getUserPrincipal();
        String callerLogin = userPrincipal.getName();
        String sessionId = session.getId();

        if (callerLogin != null && sessionId != null) {

            // Store the webSocket in the user socket hashMap
            // If user does not have a channels map yet then we create his channels map
            if (CHANNELS.get(callerLogin) == null) {
                CHANNELS.put(callerLogin,new HashMap<String, Session>());
            }

            // Store the session in the user channels hashMap
            CHANNELS.get(callerLogin).put(sessionId,session);

            // Send him a welcome message
            MainChannelDispatcher.send(session, ChannelMessagesBuilder.BuildWelcomeMessage(callerLogin));
        }
    }

    private boolean callerIsAllowToReachCallee(String caller, String callee) {
        // Allow users to communicate only if they have a common workspace
        return userManager.hasCommonWorkspace(caller, callee);
    }


}
