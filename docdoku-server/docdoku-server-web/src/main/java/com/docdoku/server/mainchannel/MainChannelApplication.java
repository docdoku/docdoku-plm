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
import com.sun.grizzly.websockets.*;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpSession;

public class MainChannelApplication extends WebSocketApplication {

    // Users WebSockets HashMap : <UserLogin, <Token, Socket>>
    private static final ConcurrentMap<String, HashMap<String, MainChannelWebSocket>> CHANNELS = new ConcurrentHashMap<String, HashMap<String, MainChannelWebSocket>>();

    private IUserManagerLocal userManager;

    public void setUserManager(IUserManagerLocal pUserManager) {
        this.userManager = pUserManager;
    }

    public static boolean hasChannels(String userLogin) {
        if (CHANNELS.get(userLogin) == null){
            return false;
        }
        if (CHANNELS.get(userLogin).values() == null){
            return false;
        }
        return !CHANNELS.get(userLogin).values().isEmpty();
    }

    public static HashMap<String, MainChannelWebSocket> getUserChannels(String userLogin) {
        return CHANNELS.get(userLogin);
    }

    @Override
    public WebSocket createWebSocket(ProtocolHandler protocolHandler, WebSocketListener[] listeners) {
        return new MainChannelWebSocket(protocolHandler, listeners);
    }

    @Override
    public void onConnect(WebSocket socket) {
    }

    @Override
    public boolean isApplicationRequest(com.sun.grizzly.tcp.Request rqst) {
        return true;
    }

    @Override
    public void onClose(WebSocket pSocket, DataFrame frame) {

        MainChannelWebSocket socket = (MainChannelWebSocket) pSocket;

        String token = socket.getToken();

        if (token != null) {

            // find whom the socket belongs
            String callerLogin = socket.getUserLogin();

            // remove the user from all rooms he might be
            Room.removeUserFromAllRoom(callerLogin);

            // remove the socket from the user hash map
            CHANNELS.get(callerLogin).remove(token);

        }

    }

    @Override
    public void onMessage(WebSocket pSocket, String message) {

        MainChannelWebSocket socket = (MainChannelWebSocket) pSocket;

        if (socket.getToken() == null && message.startsWith("MainChannelApplicationNewClient:")) {
           // Peer declaration message must match "MainChannelApplicationNewClient:${sessionId}"
           onPeerDeclarationMessage(message, socket);

        } else {

            try {

                JSONObject jsObj = new JSONObject(message);

                String remoteUser = null;

                try{
                    remoteUser = jsObj.getString("remoteUser");
                    if(remoteUser != null){
                        if(socket.getUserLogin() != remoteUser){
                            if (!callerIsAllowToReachCallee(socket.getUserLogin(), remoteUser)) {
                                // caller is not allowed to reach user.
                                return ;
                            }
                        }else{
                            // try to join him self ?
                            return;
                        }
                    }
                } catch (JSONException ex){
                    // remoteUser is empty
                    remoteUser = "";
                }

                String type = jsObj.getString("type");

                switch (type) {

                    case ChannelMessagesType.USER_STATUS:
                        UserStatusModule.onUserStatusRequestMessage(socket, jsObj);
                        break;

                    case ChannelMessagesType.WEBRTC_INVITE:
                        WebRTCModule.onWebRTCInviteMessage(socket, jsObj);
                        break;
                    case ChannelMessagesType.WEBRTC_ACCEPT:
                        WebRTCModule.onWebRTCAcceptMessage(socket, jsObj);
                        break;
                    case ChannelMessagesType.WEBRTC_REJECT:
                        WebRTCModule.onWebRTCRejectMessage(socket, jsObj);
                        break;
                    case ChannelMessagesType.WEBRTC_HANGUP:
                        WebRTCModule.onWebRTCHangupMessage(socket, jsObj);
                        break;
                    case ChannelMessagesType.WEBRTC_ANSWER:
                    case ChannelMessagesType.WEBRTC_OFFER:
                    case ChannelMessagesType.WEBRTC_CANDIDATE:
                    case ChannelMessagesType.WEBRTC_BYE:
                        WebRTCModule.onWebRTCSignalingMessage(socket, jsObj, message);
                        break;

                    case ChannelMessagesType.CHAT_MESSAGE:
                        ChatModule.onChatMessage(socket, jsObj);
                        break;

                    default:
                        break;
                }

            } catch (JSONException ex) {
                MainChannelDispatcher.send(socket, ChannelMessagesBuilder.BuildJsonExMessage());
            }

        }

    }

    private void onPeerDeclarationMessage(String data, MainChannelWebSocket socket) {

        String[] dataSplit = data.split(":");

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

                    // hook data on the socket
                    socket.setToken(UUID.randomUUID().toString());
                    socket.setUserLogin(callerLogin);

                    // store the webSocket in the user socket hashMap
                    if (CHANNELS.get(callerLogin) == null) {
                        // user does not have a channels map yet. Let's create his channels map.
                        CHANNELS.put(callerLogin,new HashMap<String, MainChannelWebSocket>());
                    }
                    // store the socket in the user channels hashmap
                    CHANNELS.get(callerLogin).put(socket.getToken(),socket);

                    // send him a welcome message
                    MainChannelDispatcher.send(socket, ChannelMessagesBuilder.BuildWelcomeMessage(socket.getUserLogin()));

                }
                // else : account without login doesn't make sense. Nothing to do. Maybe close the socket ?

            } //else {
                // user is not authenticated on server, nothing to do. Maybe close the socket ?
            //}
        }
    }

    private boolean callerIsAllowToReachCallee(String caller, String callee) {
        // allow users to communicate only if they have a common workspace
        return userManager.hasCommonWorkspace(caller, callee);
    }


}
