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

package com.docdoku.server.ws.webrtc;


import com.docdoku.server.ws.WebSocketMessage;
import com.docdoku.server.ws.WebSocketModule;
import com.docdoku.server.ws.WebSocketSessionsManager;
import com.docdoku.server.ws.chat.Room;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.websocket.Session;
import java.util.Arrays;
import java.util.List;

/**
 * WebRTC module plugin implementation
 *
 * @author Morgan Guimard
 */
@WebRTCWebSocketModule
public class WebRTCWebSocketModuleImpl implements WebSocketModule {

    private static final String WEBRTC_INVITE = "WEBRTC_INVITE";
    private static final String WEBRTC_ACCEPT = "WEBRTC_ACCEPT";
    private static final String WEBRTC_REJECT = "WEBRTC_REJECT";
    private static final String WEBRTC_HANGUP = "WEBRTC_HANGUP";
    private static final String WEBRTC_ROOM_JOIN_EVENT = "WEBRTC_ROOM_JOIN_EVENT";
    private static final String WEBRTC_ROOM_REJECT_EVENT = "WEBRTC_ROOM_REJECT_EVENT";
    private static final String WEBRTC_OFFLINE = "OFFLINE";

    private static final String WEBRTC_OFFER = "offer";
    private static final String WEBRTC_ANSWER = "answer";
    private static final String WEBRTC_BYE = "bye";
    private static final String WEBRTC_CANDIDATE = "candidate";

    private final static List<String> SUPPORTED_TYPES = Arrays.asList(
            WEBRTC_INVITE, WEBRTC_ACCEPT, WEBRTC_REJECT, WEBRTC_HANGUP,
            WEBRTC_OFFER, WEBRTC_ANSWER, WEBRTC_BYE, WEBRTC_CANDIDATE
    );

    @Inject
    private WebSocketSessionsManager webSocketSessionsManager;

    @Override
    public boolean canDecode(WebSocketMessage webSocketMessage) {
        return SUPPORTED_TYPES.contains(webSocketMessage.getType());
    }

    @Override
    public void process(Session session, WebSocketMessage webSocketMessage) {

        String sender = webSocketSessionsManager.getHolder(session);

        switch (webSocketMessage.getType()) {

            case WEBRTC_INVITE:
                onWebRTCInviteMessage(sender, session, webSocketMessage);
                break;

            case WEBRTC_ACCEPT:
                onWebRTCAcceptMessage(sender, session, webSocketMessage);
                break;

            case WEBRTC_REJECT:
                onWebRTCRejectMessage(sender, session, webSocketMessage);
                break;

            case WEBRTC_HANGUP:
                onWebRTCHangupMessage(sender, session, webSocketMessage);
                break;

            case WEBRTC_ANSWER:
            case WEBRTC_OFFER:
            case WEBRTC_CANDIDATE:
            case WEBRTC_BYE:
                processP2P(sender, session, webSocketMessage);
                break;

            default:
                break;
        }
    }



    private void processP2P(String sender,Session session, WebSocketMessage webSocketMessage) {
        // webRTC P2P signaling messages
        // These messages are forwarded to the remote peer(s) in the room

        String roomKey = webSocketMessage.getString("roomKey");
        Room room = Room.getByKeyName(roomKey);

        if (room != null && room.hasUser(sender)) {
            // forward the message to the other peer
            Session otherSession = room.getOtherUserSession(session);

            // on bye message, remove the user from the room
            if (WEBRTC_BYE.equals(webSocketMessage.getType())) {
                room.removeUserSession(session);
            }

            if (otherSession != null) {
                webSocketSessionsManager.send(otherSession,webSocketMessage);
            }
        }

    }

    private void onWebRTCHangupMessage(String sender, Session session, WebSocketMessage webSocketMessage) {
        String roomKey = webSocketMessage.getString("roomKey");
        Room room = Room.getByKeyName(roomKey);

        if (room != null) {
            Session otherSession = room.getOtherUserSession(session);
            room.removeUserSession(session);

            WebSocketMessage message = createMessage(WEBRTC_HANGUP, sender, roomKey, null, null, 0, null, null, null, null, null);
            webSocketSessionsManager.send(otherSession,message);

        }
    }

    private void onWebRTCRejectMessage(String sender, Session session, WebSocketMessage webSocketMessage) {
        String roomKey = webSocketMessage.getString("roomKey");
        String reason = webSocketMessage.getString("reason");
        Room room = Room.getByKeyName(roomKey);
        String remoteUser = webSocketMessage.getString("remoteUser");

        if (room != null) {
            // send "room reject event" to caller, to remove invitations in other tabs if any

            WebSocketMessage message = createMessage(WEBRTC_ROOM_REJECT_EVENT, null, roomKey, reason, null, room.getOccupancy(), sender, null, null, null, null);
            webSocketSessionsManager.broadcast(sender, message);

            Session otherSession = room.getUserSession(remoteUser);
            if (otherSession != null) {
                WebSocketMessage otherMessage = createMessage(WEBRTC_REJECT, sender, roomKey, reason, null, 0, null, null, null, null, null);
                webSocketSessionsManager.send(otherSession,otherMessage);
            }
        }
    }

    private void onWebRTCAcceptMessage(String sender, Session session, WebSocketMessage webSocketMessage) {
        String roomKey = webSocketMessage.getString("roomKey");
        Room room = Room.getByKeyName(roomKey);
        String remoteUser = webSocketMessage.getString("remoteUser");

        if (room != null && room.hasUser(remoteUser)) {

            room.addUserSession(session);
            // send room join event to caller (all channels to remove invitations if any)
            WebSocketMessage message = createMessage(WEBRTC_ROOM_JOIN_EVENT, sender, roomKey, null, null, room.getOccupancy(), sender, null, null, null, null);
            webSocketSessionsManager.broadcast(sender, message);

            // send room join event to the other user in room
            Session otherSession = room.getOtherUserSession(session);

            if (otherSession != null) {
                WebSocketMessage otherMessage = createMessage(WEBRTC_ACCEPT, sender, roomKey, null, null, 0, null, null, null, null, null);
                webSocketSessionsManager.send(otherSession,otherMessage);
            }
        }
    }


    private void onWebRTCInviteMessage(String sender, Session session, WebSocketMessage webSocketMessage) {

        String remoteUser = webSocketMessage.getString("remoteUser");
        String roomKey = sender + "-" + remoteUser;

        if (!webSocketSessionsManager.hasSessions(remoteUser)) {
            WebSocketMessage message = createMessage(WEBRTC_REJECT, null, roomKey, WEBRTC_OFFLINE, null, 0, remoteUser, null, null, null, null);
            webSocketSessionsManager.send(session, message);
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
        WebSocketMessage message = createMessage(WEBRTC_ROOM_JOIN_EVENT, null, roomKey, null, null, room.getOccupancy(), sender, null, null, null, null);
        webSocketSessionsManager.send(session, message);

        // send invitation to the remote user sessions (all channels)
        WebSocketMessage remoteUserMessage = createMessage(WEBRTC_INVITE, sender, roomKey, null, webSocketMessage.getString("context"), room.getOccupancy(), null, null, null, null, null);
        webSocketSessionsManager.broadcast(remoteUser, remoteUserMessage);
    }

    private WebSocketMessage createMessage(String type, String remoteUser, String roomKey, String reason, String context, Integer roomOccupancy, String userLogin, String sdp, Integer label, String id, String candidate) {

        JsonObjectBuilder jsonObject = Json.createObjectBuilder();
        jsonObject.add("type", type);

        if (remoteUser != null) {
            jsonObject.add("remoteUser", remoteUser);
        }
        if (roomKey != null) {
            jsonObject.add("roomKey", roomKey);
        }
        if (reason != null) {
            jsonObject.add("reason", reason);
        }
        if (context != null) {
            jsonObject.add("context", context);
        }
        if (roomOccupancy != null) {
            jsonObject.add("roomOccupancy", roomOccupancy);
        }
        if (userLogin != null) {
            jsonObject.add("userLogin", userLogin);
        }

        // Signals
        if (sdp != null) {
            jsonObject.add("sdp", sdp);
        }
        if (id != null) {
            jsonObject.add("id", id);
        }
        if (candidate != null) {
            jsonObject.add("candidate", candidate);
        }
        if (label != null) {
            jsonObject.add("label", label);
        }

        return new WebSocketMessage(jsonObject.build());

    }

}