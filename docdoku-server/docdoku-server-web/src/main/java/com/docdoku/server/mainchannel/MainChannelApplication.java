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
import com.docdoku.server.mainchannel.module.*;
import com.docdoku.server.mainchannel.util.ChannelMessagesBuilder;
import com.docdoku.server.mainchannel.util.ChannelMessagesType;
import com.docdoku.server.mainchannel.util.Room;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.StringReader;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@ServerEndpoint(value = "/mainChannelSocket", decoders = {MessageDecoder.class}, encoders = {WebRTCMessageEncoder.class, StatusMessageEncoder.class, ChatMessageEncoder.class})
public class MainChannelApplication {

    // Users WebSockets Map : <UserLogin, <SessionId, Session>>
    private static final ConcurrentMap<String, Map<String, Session>> CHANNELS = new ConcurrentHashMap<>();

    private final static Logger LOGGER = Logger.getLogger(MainChannelApplication.class.getName());
    @EJB
    private IUserManagerLocal userManager;

    public static boolean hasChannels(String userLogin) {
        if (CHANNELS.get(userLogin) == null) {
            return false;
        }
        if (CHANNELS.get(userLogin).values() == null) {
            return false;
        }
        return !CHANNELS.get(userLogin).values().isEmpty();
    }

    public static Map<String, Session> getUserChannels(String userLogin) {
        return CHANNELS.get(userLogin);
    }

    public static void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession httpSession = httpSessionEvent.getSession();
        //Account account = httpSession.getAttribute("account");
        String account = ((Account) httpSession.getAttribute("account")).getLogin();
        Map<String, Session> sessionMap = CHANNELS.get(account);

        /*Session session = (Session) httpSession;
        Principal userPrincipal = session.getUserPrincipal();
        String callerLogin = userPrincipal.getName();*/

            /*Map<String, Session> userChannels = getUserChannels(callerLogin);
            Set chanKey = ConcurrentMap.keySet();
            for (Object aChanKey : chanKey) {
                userChannels.get(aChanKey).close();
            }*/

            for (Session session : sessionMap.values()){
                try {
                    session.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    @OnError
    public void error(Session session, Throwable error) {
        LOGGER.log(Level.SEVERE, null, error);
        closeSession(session);
    }

    @OnClose
    public void close(Session session, CloseReason reason) {
        closeSession(session);
    }

    @OnOpen
    public void open(Session session) {

            Principal userPrincipal = session.getUserPrincipal(); String callerLogin = userPrincipal.getName();
        String sessionId = session.getId();

        if (callerLogin != null && sessionId != null) {

            // Store the webSocket in the user socket Map
            // If user does not have a channels map yet then we create his channels map
            if (CHANNELS.get(callerLogin) == null) {
                CHANNELS.put(callerLogin, new HashMap<String, Session>());
            }

            // Store the session in the user channels hashMap
            CHANNELS.get(callerLogin).put(sessionId, session);

            // Send him a welcome message
            MainChannelDispatcher.send(session, ChannelMessagesBuilder.buildWelcomeMessage(callerLogin));
        }
    }

    @OnMessage
    public void message(Session session, AbstractMessage message) {
        Principal userPrincipal = session.getUserPrincipal();
        String callerLogin = userPrincipal.getName();

        // Exit if remote user is null or caller tries to join himself
        if (message.getRemoteUser() == null || message.getRemoteUser().equals(callerLogin)) {
            return;
        }
        // Exit if caller cannot reach callee (business)
        if (!callerIsAllowToReachCallee(callerLogin, message.getRemoteUser())) {
            return;
        }
        WebRTCMessage webRTC;
        switch (message.getType()) {
            case ChannelMessagesType.USER_STATUS:
                StatusMessage status = (StatusMessage) message;
                process(session, callerLogin, status);
                break;

            case ChannelMessagesType.WEBRTC_INVITE:
                webRTC = (WebRTCMessage) message;
                processWebRTCInvite(session, callerLogin, webRTC);
                break;
            case ChannelMessagesType.WEBRTC_ACCEPT:
                webRTC = (WebRTCMessage) message;
                processWebRTCAccept(session, callerLogin, webRTC);
                break;
            case ChannelMessagesType.WEBRTC_REJECT:
                webRTC = (WebRTCMessage) message;
                processWebRTCReject(session, callerLogin, webRTC);
                break;
            case ChannelMessagesType.WEBRTC_HANGUP:
                webRTC = (WebRTCMessage) message;
                processWebRTCHangup(session, callerLogin, webRTC);
                break;
            case ChannelMessagesType.WEBRTC_ANSWER:
            case ChannelMessagesType.WEBRTC_OFFER:
            case ChannelMessagesType.WEBRTC_CANDIDATE:
            case ChannelMessagesType.WEBRTC_BYE:
                webRTC = (WebRTCMessage) message;
                processP2P(session, callerLogin, webRTC);
                break;

            case ChannelMessagesType.CHAT_MESSAGE:
                ChatMessage chat = (ChatMessage) message;
                process(session, callerLogin, chat);
                break;

            default:
                break;
        }


    }

    private void closeSession(Session session) {
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

    private boolean callerIsAllowToReachCallee(String caller, String callee) {
        // Allow users to communicate only if they have a common workspace
        return userManager.hasCommonWorkspace(caller, callee);
    }

    private void processWebRTCHangup(Session session, String callerLogin, WebRTCMessage webRTC) {
        Room room = Room.getByKeyName(webRTC.getRoomKey());

        if (room != null) {
            Session otherSession = room.getOtherUserSession(session);
            room.removeUserSession(session);
            MainChannelDispatcher.send(otherSession, new WebRTCMessage(ChannelMessagesType.WEBRTC_HANGUP, callerLogin, webRTC.getRoomKey(), null, null, 0, null));
        }
    }

    private void processWebRTCReject(Session session, String callerLogin, WebRTCMessage webRTC) {
        Room room = Room.getByKeyName(webRTC.getRoomKey());
        if (room != null) {
            // send "room reject event" to caller, to remove invitations in other tabs if any
            MainChannelDispatcher.sendToAllUserChannels(callerLogin, new WebRTCMessage(ChannelMessagesType.WEBRTC_ROOM_REJECT_EVENT, null, webRTC.getRoomKey(), webRTC.getReason(), null, room.getOccupancy(), callerLogin));
            Session otherSession = room.getUserSession(webRTC.getRemoteUser());
            if (otherSession != null) {
                MainChannelDispatcher.send(otherSession, new WebRTCMessage(ChannelMessagesType.WEBRTC_REJECT, callerLogin, webRTC.getRoomKey(), webRTC.getReason(), null, 0, null));
            }
        }
    }

    private void processWebRTCAccept(Session session, String callerLogin, WebRTCMessage webRTC) {
        Room room = Room.getByKeyName(webRTC.getRoomKey());

        if (room != null && room.hasUser(webRTC.getRemoteUser())) {

            room.addUserSession(session);
            // send room join event to caller (all channels to remove invitations if any)
            MainChannelDispatcher.sendToAllUserChannels(callerLogin, new WebRTCMessage(ChannelMessagesType.WEBRTC_ROOM_JOIN_EVENT, callerLogin, webRTC.getRoomKey(), null, null, room.getOccupancy(), callerLogin));

            // send room join event to the other user in room
            Session otherSession = room.getOtherUserSession(session);
            if (otherSession != null) {
                MainChannelDispatcher.send(otherSession, new WebRTCMessage(ChannelMessagesType.WEBRTC_ACCEPT, callerLogin, webRTC.getRoomKey(), null, null, 0, null));
            }
        }
    }

    private void processWebRTCInvite(Session session, String callerLogin, WebRTCMessage webRTC) {
        String roomKey = callerLogin + "-" + webRTC.getRemoteUser();
        if (!MainChannelApplication.hasChannels(webRTC.getRemoteUser())) {
            MainChannelDispatcher.send(session, new WebRTCMessage(ChannelMessagesType.WEBRTC_REJECT, null, roomKey, WebRTCMessage.WEBRTC_OFFLINE, null, 0, webRTC.getRemoteUser()));
            return;
        }
        Room room = Room.getByKeyName(roomKey);
        if (room == null) {
            room = new Room(roomKey);
        }
        //else :  multiple invitations, caller is spamming or something goes wrong.
        // the room is ready to receive user sessions.
        // add the caller session in the room
        room.addUserSession(session);

        // send room join event to caller session (single channel)
        MainChannelDispatcher.send(session, new WebRTCMessage(ChannelMessagesType.WEBRTC_ROOM_JOIN_EVENT, null, roomKey, null, null, room.getOccupancy(), callerLogin));

        // send invitation to the remote user sessions (all channels)
        MainChannelDispatcher.sendToAllUserChannels(webRTC.getRemoteUser(), new WebRTCMessage(ChannelMessagesType.WEBRTC_INVITE, webRTC.getRemoteUser(), roomKey, null, webRTC.getContext(), 0, null));
    }

    private void processP2P(Session session, String callerLogin, WebRTCMessage webRTC) {
        // webRTC P2P signaling messages
        // These messages are forwarded to the remote peer(s) in the room
        Room room = Room.getByKeyName(webRTC.getRoomKey());
        if (room != null) {
            if (room.hasUser(callerLogin)) {
                // forward the message to the other peer
                Session otherSession = room.getOtherUserSession(session);

                // on bye message, remove the user from the room
                if (ChannelMessagesType.WEBRTC_BYE.equals(webRTC.getType())) {
                    room.removeUserSession(session);
                }

                if (otherSession != null) {
                    MainChannelDispatcher.send(otherSession, webRTC);
                } //else {
                // tell the user the room is empty ?
                //}
            } //else {
            // tell the user he's not in the room ?
            //}
        } //else {
        // tell the user the room doesn't exists ?
        //}
    }

    private void process(Session session, String callerLogin, StatusMessage status) {
        if (!MainChannelApplication.hasChannels(status.getRemoteUser())) {
            MainChannelDispatcher.send(session, new StatusMessage(ChannelMessagesType.USER_STATUS, status.getRemoteUser(), StatusMessage.USER_STATUS_OFFLINE));
        } else {
            MainChannelDispatcher.send(session, new StatusMessage(ChannelMessagesType.USER_STATUS, status.getRemoteUser(), StatusMessage.USER_STATUS_ONLINE));
        }
    }

    private void process(Session session, String callerLogin, ChatMessage chat) {
        if (!MainChannelApplication.hasChannels(chat.getRemoteUser())) {
            MainChannelDispatcher.send(session, new ChatMessage(ChannelMessagesType.CHAT_MESSAGE, chat.getRemoteUser(), null, null, chat.getContext(), ChatMessage.CHAT_MESSAGE_UNREACHABLE));
        } else {
            MainChannelDispatcher.sendToAllUserChannels(callerLogin, new ChatMessage(ChannelMessagesType.CHAT_MESSAGE_ACK, chat.getRemoteUser(), callerLogin, chat.getMessage(), chat.getContext(), null));
            MainChannelDispatcher.sendToAllUserChannels(chat.getRemoteUser(), new ChatMessage(ChannelMessagesType.CHAT_MESSAGE, callerLogin, callerLogin, chat.getMessage(), chat.getContext(), null));
        }
    }

}
