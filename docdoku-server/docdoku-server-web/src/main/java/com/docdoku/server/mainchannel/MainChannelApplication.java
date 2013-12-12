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

import com.docdoku.core.common.Account;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.http.HttpSessionCollector;
import com.docdoku.server.mainchannel.modules.ChatModule;
import com.docdoku.server.mainchannel.modules.UserStatusModule;
import com.docdoku.server.mainchannel.modules.WebRTCModule;
import com.docdoku.server.mainchannel.util.ChannelMessagesBuilder;
import com.docdoku.server.mainchannel.util.ChannelMessagesType;
import com.docdoku.server.mainchannel.util.Room;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/mainChannelSocket")
public class MainChannelApplication {

    // Users WebSockets HashMap : <UserLogin, <Token, Session>>
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
        Map<String, Object> uProps = session.getUserProperties();
        String userLogin= (String)uProps.get("userLogin");
        String token= (String)uProps.get("token");

        if (token != null) {
            // find whom the session belongs
            Room.removeUserFromAllRoom(userLogin);
            // remove the session from the user hash map
            CHANNELS.get(userLogin).remove(token);
        }

    }

    @OnMessage
    public void message(Session session, String message) {

        Map<String, Object> uProps = session.getUserProperties();
        String userLogin= (String)uProps.get("userLogin");
        String token= (String)uProps.get("token");

        if (token == null && message.startsWith("MainChannelApplicationNewClient:")) {
           // Peer declaration message must match "MainChannelApplicationNewClient:${sessionId}"
           onPeerDeclarationMessage(session, message);

        } else {

            try {

                JsonObject jsObj = Json.createReader(new StringReader(message)).readObject();

                String remoteUser = null;

                try{
                    remoteUser = jsObj.getString("remoteUser");
                    if(remoteUser != null){
                        if(userLogin != remoteUser){
                            if (!callerIsAllowToReachCallee(userLogin, remoteUser)) {
                                // caller is not allowed to reach user.
                                return ;
                            }
                        }else{
                            // try to join him self ?
                            return;
                        }
                    }
                } catch (JsonException ex){
                    // remoteUser is empty
                    remoteUser = "";
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

    private void onPeerDeclarationMessage(Session session, String message) {
        Map<String, Object> uProps = session.getUserProperties();
        String userLogin= (String)uProps.get("userLogin");
        String token= (String)uProps.get("token");

        String[] dataSplit = message.split(":");
        if (dataSplit.length == 2) {
            // parse the sessionId in the declaration message
            String sessionId = dataSplit[1];

            // retrieve the user's HttpSession
            HttpSession httpSession = HttpSessionCollector.find(sessionId);
            // find the associated user account
            Account account = (Account) httpSession.getAttribute("account");
            if (account != null) {
                String callerLogin = account.getLogin();

                if (callerLogin != null) {

                    // hook data on the session
                    uProps.put("token", UUID.randomUUID().toString());
                    uProps.put("userLogin", callerLogin);

                    // store the webSocket in the user socket hashMap
                    if (CHANNELS.get(callerLogin) == null) {
                        // user does not have a channels map yet. Let's create his channels map.
                        CHANNELS.put(callerLogin,new HashMap<String, Session>());
                    }
                    // store the session in the user channels hashmap
                    CHANNELS.get(callerLogin).put(token,session);

                    // send him a welcome message
                    MainChannelDispatcher.send(session, ChannelMessagesBuilder.BuildWelcomeMessage(userLogin));

                }
                // else : account without login doesn't make sense. Nothing to do. Maybe close the session ?

            } //else {
                // user is not authenticated on server, nothing to do. Maybe close the session ?
            //}
        }
    }

    private boolean callerIsAllowToReachCallee(String caller, String callee) {
        // allow users to communicate only if they have a common workspace
        return userManager.hasCommonWorkspace(caller, callee);
    }


}
