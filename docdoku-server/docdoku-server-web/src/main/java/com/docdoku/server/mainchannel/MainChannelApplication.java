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
import com.docdoku.server.mainchannel.util.ChannelMessagesType;
import com.docdoku.server.mainchannel.util.ChatMessagesBuilder;
import com.docdoku.server.mainchannel.util.Room;
import com.sun.grizzly.websockets.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import javax.servlet.http.HttpSession;

public class MainChannelApplication extends WebSocketApplication {

    // WebSockets HashMap
    private static final ConcurrentMap<String, MainChannelWebSocket> channels = new ConcurrentHashMap<String, MainChannelWebSocket>();

    // Don't know an other way to pass the EJB ref to this class. Lookup fails, Injection fails.
    // TODO : find a better way to inject or lookup the UserManagerBean
    private IUserManagerLocal userManager;

    public void setUserManager(IUserManagerLocal pUserManager) {
        this.userManager = pUserManager;
    }

    @Override
    public WebSocket createWebSocket(ProtocolHandler protocolHandler, WebSocketListener[] listeners) {
        return new MainChannelWebSocket(protocolHandler, listeners);
    }

    @Override
    public void onConnect(WebSocket socket) {
        // Nothing to do
    }

    @Override
    public boolean isApplicationRequest(com.sun.grizzly.tcp.Request rqst) {
        return true;
    }

    @Override
    public void onClose(WebSocket socket, DataFrame frame) {

        MainChannelWebSocket ws = (MainChannelWebSocket) socket;
        String callerLogin = ws.getUserLogin();

        // if the websocket is in the hashmap, we remove it
        // and then remove the user from all rooms he's in.

        if (callerLogin != null) {
            channels.remove(callerLogin);
            Room.removeUserFromAllRoom(callerLogin);
        }

    }

    @Override
    public void onMessage(WebSocket socket, String data) {

        MainChannelWebSocket ws = (MainChannelWebSocket) socket;

        // Peer declaration : listen:sessionId

        if (data.startsWith("listen:") && ws.getUserLogin() == null) {
            onPeerDeclarationMessage(data, ws);
        } else {

            // Parse a JSON Message and switch on message.type
            String callerLogin = ws.getUserLogin();

            try {

                JSONObject jsobj = new JSONObject(data);
                String type = jsobj.getString("type");

                switch (type) {

                    case ChannelMessagesType.USER_STATUS:
                        onUserStatusRequestMessage(callerLogin, jsobj);
                        break;

                    case ChannelMessagesType.WEBRTC_INVITE:
                        onWebRTCInviteMessage(callerLogin, jsobj);
                        break;
                    case ChannelMessagesType.WEBRTC_INVITE_TIMEOUT:
                        onWebRTCInviteTimeoutMessage(callerLogin, jsobj);
                        break;
                    case ChannelMessagesType.WEBRTC_ACCEPT:
                        onWebRTCAcceptMessage(callerLogin, jsobj);
                        break;
                    case ChannelMessagesType.WEBRTC_REJECT:
                        onWebRTCRejectMessage(callerLogin, jsobj);
                        break;
                    case ChannelMessagesType.WEBRTC_HANGUP:
                        onWebRTCHangupMessage(callerLogin, jsobj);
                        break;

                    case ChannelMessagesType.WEBRTC_ANSWER:
                    case ChannelMessagesType.WEBRTC_OFFER:
                    case ChannelMessagesType.WEBRTC_CANDIDATE:
                    case ChannelMessagesType.WEBRTC_BYE:
                        onWebRTCSignalingMessage(callerLogin, jsobj, data, type);
                        break;

                    case ChannelMessagesType.CHAT_MESSAGE:
                        onChatMessage(callerLogin, jsobj);
                        break;

                    default:
                        onUnrecognizedMessageType(callerLogin);
                        break;

                }

            } catch (JSONException ex) {
                send(callerLogin, ChatMessagesBuilder.BuildJsonExMessage());
            }

        }

        Room.debug();

    }
    // on Message Methods

    private void onPeerDeclarationMessage(String data, MainChannelWebSocket ws) {

        String[] dataSplit = data.split(":");

        if (dataSplit.length == 2) {

            // parse the sessionId in the declaration message
            // retrieve the user's HttpSession
            // then, get his user login

            String sessionId = dataSplit[1];
            HttpSession httpSession = HttpSessionCollector.find(sessionId);
            Account account = (Account) httpSession.getAttribute("account");

            if (account != null) {

                String callerLogin = account.getLogin();

                if (callerLogin != null && !channels.containsKey(callerLogin)) {

                    // store the webSocket in the hashMap
                    // then, send him a welcome message

                    ws.setUserLogin(callerLogin);
                    channels.put(callerLogin, ws);
                    send(callerLogin, ChatMessagesBuilder.BuildWelcomeMessage(callerLogin));

                }

            } else {

                // user is not authenticated on server, nothing to do.

            }
        }
    }


    private void onWebRTCInviteMessage(String callerLogin, JSONObject jsobj) throws JSONException {

        String remoteUser = jsobj.getString("remoteUser");
        String context = jsobj.getString("context");

        if (callerIsAllowToReachCallee(callerLogin, remoteUser)) {

            String roomKey = callerLogin + "-" + remoteUser;
            Room room = Room.getByKeyName(roomKey);

            if (room == null) room = new Room(roomKey);

            room.addUser(callerLogin);

            boolean sent = send(remoteUser, ChatMessagesBuilder.BuildWebRTCInvitationMessage(callerLogin, context, roomKey));

            if (!sent) {
                send(callerLogin, ChatMessagesBuilder.BuildWebRTCInvitationNotSentMessage(remoteUser));
            }

        } else {
            // send a forbiden message ?
        }
    }


    private void onWebRTCInviteTimeoutMessage(String callerLogin, JSONObject jsobj)  throws JSONException {

        String remoteUser = jsobj.getString("remoteUser");
        String roomKey = jsobj.getString("roomKey");

        Room room = Room.getByKeyName(roomKey);

        if (room != null && room.hasUser(remoteUser)) {
            boolean sent = send(remoteUser, ChatMessagesBuilder.BuildWebRTCInviteTimeoutMessage(callerLogin, room.key()));
        } else {
            // tell the user the room doesn't exist ?
        }

    }

    private void onWebRTCAcceptMessage(String callerLogin, JSONObject jsobj) throws JSONException {

        String remoteUser = jsobj.getString("remoteUser");
        String roomKey = jsobj.getString("roomKey");

        Room room = Room.getByKeyName(roomKey);

        if (room != null && room.hasUser(remoteUser)) {
            room.addUser(callerLogin);
            boolean sent = send(remoteUser, ChatMessagesBuilder.BuildWebRTCAcceptMessage(callerLogin, room.key()));
        } else {
            // tell the user the room doesn't exist ?
        }

    }

    private void onWebRTCRejectMessage(String callerLogin, JSONObject jsobj) throws JSONException {

        String remoteUser = jsobj.getString("remoteUser");
        String roomKey = jsobj.getString("roomKey");

        Room room = Room.getByKeyName(roomKey);

        if (room != null) {
            boolean sent = send(remoteUser, ChatMessagesBuilder.BuildWebRTCRejectMessage(callerLogin, room.key()));
        } else {
            // nothing to do ...
        }

    }

    private void onWebRTCHangupMessage(String callerLogin, JSONObject jsobj) throws JSONException {

        String remoteUser = jsobj.getString("remoteUser");
        String roomKey = jsobj.getString("roomKey");

        Room room = Room.getByKeyName(roomKey);

        if (room != null) {
            room.removeUser(callerLogin);
            boolean sent = send(remoteUser, ChatMessagesBuilder.BuildWebRTCHangupMessage(callerLogin, room.key()));
        } else {
            // nothing to do
        }

    }

    // webRTC P2P signaling messages
    // These messages are forwarded to the remote peer(s) in the room
    private void onWebRTCSignalingMessage(String callerLogin, JSONObject jsobj, String data, String messageType) throws JSONException {

        String roomKey = jsobj.getString("roomKey");
        Room room = Room.getByKeyName(roomKey);

        if (room != null) {

            if (room.hasUser(callerLogin)) {

                String remoteUser = room.getOtherUser(callerLogin);

                if (remoteUser != null) {

                    send(remoteUser, data);

                    if(messageType.equals(ChannelMessagesType.WEBRTC_BYE)){
                        room.removeUser(callerLogin);
                    }

                } else {
                    // tell the user the room is empty ?
                }
            } else {
                // tell the user he's not in the room ?
            }
        } else {
            // tell the user the room doesn't exists ?
        }

    }

    private void onChatMessage(String callerLogin, JSONObject jsobj) throws JSONException {

        String remoteUser = jsobj.getString("remoteUser");
        String message = jsobj.getString("message");
        String context = jsobj.getString("context");

        if (callerIsAllowToReachCallee(callerLogin, remoteUser)) {

            boolean sent = send(remoteUser, ChatMessagesBuilder.BuildChatMessage(callerLogin, context, message));

            if (!sent) {
                send(callerLogin, ChatMessagesBuilder.BuildChatMessageNotSentMessage(remoteUser, context));
            }

        } else {
            // send forbidden message ?
        }

    }

    private void onUserStatusRequestMessage(String callerLogin, JSONObject jsobj) throws JSONException {

        String remoteUser = jsobj.getString("remoteUser");

        if (callerIsAllowToReachCallee(callerLogin, remoteUser)) {

            if(channels.containsKey(remoteUser)){
                send(callerLogin, ChatMessagesBuilder.BuildOnlineStatusMessage(remoteUser));
            }else{
                send(callerLogin, ChatMessagesBuilder.BuildOfflineStatusMessage(remoteUser));
            }

        } else {
            // send forbidden message ?
        }

    }

    private void onUnrecognizedMessageType(String callerLogin) throws JSONException {
        send(callerLogin, ChatMessagesBuilder.BuildNoopMessage());
    }


    // Send a message to given user
    private static boolean send(String userLogin, String message) {

        MainChannelWebSocket ws = channels.get(userLogin);

        if (ws != null) {
            ws.send(message);
            return true;
        }

        return false;
    }


    // Security Method
    private boolean callerIsAllowToReachCallee(String caller, String callee) {
        // Implement business security
        // caller and callee must have at least one common workspace to communicate
        return userManager.hasCommonWorkspace(caller, callee);

    }


}
