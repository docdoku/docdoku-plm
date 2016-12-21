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

package com.docdoku.server.ws.collaborative;


import com.docdoku.server.ws.WebSocketMessage;
import com.docdoku.server.ws.WebSocketModule;
import com.docdoku.server.ws.WebSocketSessionsManager;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.websocket.Session;
import java.util.Arrays;
import java.util.List;

/**
 * Collaborative module plugin implementation
 *
 * @author Morgan Guimard
 */
@CollaborativeWebSocketModule
public class CollaborativeWebSocketModuleImpl implements WebSocketModule {


    @Inject
    private WebSocketSessionsManager webSocketSessionsManager;


    public static final String COLLABORATIVE_CREATE = "COLLABORATIVE_CREATE";
    public static final String COLLABORATIVE_INVITE = "COLLABORATIVE_INVITE";
    public static final String COLLABORATIVE_JOIN = "COLLABORATIVE_JOIN";
    public static final String COLLABORATIVE_CONTEXT = "COLLABORATIVE_CONTEXT";
    public static final String COLLABORATIVE_COMMANDS = "COLLABORATIVE_COMMANDS";
    public static final String COLLABORATIVE_EXIT = "COLLABORATIVE_EXIT";
    public static final String COLLABORATIVE_KILL = "COLLABORATIVE_KILL";
    public static final String COLLABORATIVE_GIVE_HAND = "COLLABORATIVE_GIVE_HAND";
    public static final String COLLABORATIVE_KICK_USER = "COLLABORATIVE_KICK_USER";
    public static final String COLLABORATIVE_KICK_NOT_INVITED = "COLLABORATIVE_KICK_NOT_INVITED";
    public static final String COLLABORATIVE_WITHDRAW_INVITATION = "COLLABORATIVE_WITHDRAW_INVITATION";

    public static final String CHAT_MESSAGE = "CHAT_MESSAGE";

    private final static List<String> SUPPORTED_TYPES = Arrays.asList(
            COLLABORATIVE_CREATE,
            COLLABORATIVE_INVITE,
            COLLABORATIVE_COMMANDS,
            COLLABORATIVE_CONTEXT,
            COLLABORATIVE_EXIT,
            COLLABORATIVE_GIVE_HAND,
            COLLABORATIVE_JOIN,
            COLLABORATIVE_KICK_USER,
            COLLABORATIVE_KICK_NOT_INVITED,
            COLLABORATIVE_KILL,
            COLLABORATIVE_WITHDRAW_INVITATION
    );

    @Override
    public boolean canDecode(WebSocketMessage webSocketMessage) {
        return SUPPORTED_TYPES.contains(webSocketMessage.getType());
    }

    @Override
    public void process(Session session, WebSocketMessage webSocketMessage) {

        String sender = webSocketSessionsManager.getHolder(session);
        String type = webSocketMessage.getType();

        switch (type) {

            case COLLABORATIVE_CREATE:
                onCollaborativeCreateMessage(sender, session, webSocketMessage);
                break;

            case COLLABORATIVE_INVITE:
                onCollaborativeInviteMessage(sender, session, webSocketMessage);
                break;

            case COLLABORATIVE_JOIN:
                onCollaborativeJoinMessage(sender, session, webSocketMessage);
                break;

            case COLLABORATIVE_COMMANDS:
                onCollaborativeCommandsMessage(sender, session, webSocketMessage);
                break;

            case COLLABORATIVE_EXIT:
                onCollaborativeExitMessage(sender, session, webSocketMessage);
                break;

            case COLLABORATIVE_KILL:
                onCollaborativeKillMessage(sender, session, webSocketMessage);
                break;

            case COLLABORATIVE_GIVE_HAND:
                onCollaborativeGiveHandMessage(sender, session, webSocketMessage);
                break;

            case COLLABORATIVE_KICK_USER:
                onCollaborativeKickUserMessage(sender, session, webSocketMessage);
                break;

            case COLLABORATIVE_WITHDRAW_INVITATION:
                onCollaborativeWithdrawInvitationMessage(sender, session, webSocketMessage);
                break;

            default:
                break;
        }


    }

    private void onCollaborativeWithdrawInvitationMessage(String sender, Session session, WebSocketMessage webSocketMessage) {

        CollaborativeRoom room = CollaborativeRoom.getByKeyName(webSocketMessage.getString("key"));
        JsonObject broadcastMessage = webSocketMessage.getJsonObject("broadcastMessage");
        String context = broadcastMessage.getString("context");
        String pendingUser = webSocketMessage.getString("remoteUser");

        if (room.getMasterName().equals(sender)) {
            // the master sent the invitation
            room.removePendingUser(pendingUser);
            // Send chat message
            JsonObjectBuilder b = Json.createObjectBuilder()
                    .add("type", CHAT_MESSAGE)
                    .add("remoteUser", sender)
                    .add("sender", sender)
                    .add("message", "/withdrawInvitation")
                    .add("context", context);
            WebSocketMessage message = new WebSocketMessage(b.build());
            webSocketSessionsManager.broadcast(pendingUser, message);

            broadcastNewContext(room);
        }


    }

    private void onCollaborativeKickUserMessage(String sender, Session session, WebSocketMessage webSocketMessage) {

        CollaborativeRoom room = CollaborativeRoom.getByKeyName(webSocketMessage.getString("key"));

        String kickedUser = webSocketMessage.getString("remoteUser");

        if (room.getMasterName().equals(sender)) {
            // the master sent the kick
            Session userSession = room.findUserSession(kickedUser);
            if (room.removeSlave(userSession)) {
                webSocketSessionsManager.send(userSession, webSocketMessage);
                broadcastNewContext(room);
            }
        }

    }

    private void onCollaborativeGiveHandMessage(String sender, Session session, WebSocketMessage webSocketMessage) {

        CollaborativeRoom room = CollaborativeRoom.getByKeyName(webSocketMessage.getString("key"));

        String promotedUser = webSocketMessage.getString("remoteUser");

        if (room.getMasterName().equals(sender)) {
            // the master sent the invitation
            room.addSlave(room.getMaster());
            Session userSession = room.findUserSession(promotedUser);
            if (room.removeSlave(userSession)) {
                room.setMaster(userSession);
                broadcastNewContext(room);
            }
        }

    }

    private void onCollaborativeKillMessage(String sender, Session session, WebSocketMessage webSocketMessage) {

        CollaborativeRoom room = CollaborativeRoom.getByKeyName(webSocketMessage.getString("key"));

        if (room.getMasterName().equals(sender)) {
            // the master sent the invitation
            String roomKey = room.getKey();

            for (Session slave : room.getSlaves()) {
                WebSocketMessage message = createMessage(COLLABORATIVE_KICK_USER, roomKey,
                        JsonValue.NULL, webSocketSessionsManager.getHolder(slave));
                webSocketSessionsManager.send(slave, message);
            }
            room.delete();
        }

    }

    private void onCollaborativeExitMessage(String sender, Session session, WebSocketMessage webSocketMessage) {

        CollaborativeRoom room = CollaborativeRoom.getByKeyName(webSocketMessage.getString("key"));

        if (room.getMasterName().equals(sender)) {
            // exit for the master
            room.setMaster(null);
            room.setLastMaster(sender);
        } else {
            room.getSlaves().remove(session);
            // add the user in the pending list
            if (!room.getPendingUsers().contains(sender)) {
                room.addPendingUser(sender);
            }
        }

        broadcastNewContext(room);

    }

    private void onCollaborativeCommandsMessage(String sender, Session session, WebSocketMessage webSocketMessage) {

        CollaborativeRoom room = CollaborativeRoom.getByKeyName(webSocketMessage.getString("key"));

        if (room.getMasterName().equals(sender)) {
            // the master sent the invitation
            // save camera info
            JsonObject command = webSocketMessage.getJsonObject("broadcastMessage");

            room.saveCommand(command);

            webSocketSessionsManager.send(room.getSlaves(), webSocketMessage);

        }

    }

    private void onCollaborativeJoinMessage(String sender, Session session, WebSocketMessage webSocketMessage) {

        CollaborativeRoom room = CollaborativeRoom.getByKeyName(webSocketMessage.getString("key"));

        if (room == null) {
            //Room not found
            return;
        }

        // Master
        if ("".equals(room.getMasterName()) && room.getLastMaster().equals(sender)) {
            // if the room has no master, allow the last master to take the lead
            room.setMaster(session);
            sendAllCommands(session, room);
            broadcastNewContext(room);
            return;
        }

        // User
        if (!room.removePendingUser(sender)) {
            // if the user is not in the pending list reject him
            WebSocketMessage message = createMessage(COLLABORATIVE_KICK_NOT_INVITED, room.getKey(), JsonValue.NULL, sender);
            webSocketSessionsManager.send(session, message);
        } else {
            room.addSlave(session);
            sendAllCommands(session, room);
            broadcastNewContext(room);
        }

    }

    private void onCollaborativeInviteMessage(String sender, Session session, WebSocketMessage webSocketMessage) {

        String invitedUser = webSocketMessage.getString("remoteUser");
        JsonObject messageBroadcast = webSocketMessage.getJsonObject("messageBroadcast");
        String context = messageBroadcast.getString("context");
        String url = messageBroadcast.getString("url");

        CollaborativeRoom room = CollaborativeRoom.getByKeyName(webSocketMessage.getString("key"));

        if (webSocketSessionsManager.isAllowedToReachUser(sender, invitedUser)) {

            if (room.getMasterName().equals(sender) && room.findUserSession(invitedUser) == null) {
                // the master sent the invitation
                // the user is not already in the room
                if (!room.getPendingUsers().contains(invitedUser)) {
                    // the user is not yet in the pending list, add him.
                    room.addPendingUser(invitedUser);
                }

                String invite = "/invite " + url + "/room/" + room.getKey();

                JsonObjectBuilder b = Json.createObjectBuilder()
                        .add("type", CHAT_MESSAGE)
                        .add("remoteUser", sender)
                        .add("sender", sender)
                        .add("message", invite)
                        .add("context", context);
                WebSocketMessage message = new WebSocketMessage(b.build());
                webSocketSessionsManager.broadcast(invitedUser, message);
                broadcastNewContext(room);
            }


        }
    }

    private void onCollaborativeCreateMessage(String sender, Session session, WebSocketMessage webSocketMessage) {
        CollaborativeRoom room = new CollaborativeRoom(session);

        WebSocketMessage message = createMessage(COLLABORATIVE_CREATE, room.getKey(),
                JsonValue.NULL, sender);

        webSocketSessionsManager.send(session, message);
    }

    private void broadcastNewContext(CollaborativeRoom room) {
        String master = room.getMasterName();

        WebSocketMessage message = createMessage(COLLABORATIVE_CONTEXT, room.getKey(),
                room.getContext(), master);

        webSocketSessionsManager.broadcast(master, message);

        for (Session slave : room.getSlaves()) {
            WebSocketMessage slaveMessage = createMessage(COLLABORATIVE_CONTEXT, room.getKey(),
                    room.getContext(), webSocketSessionsManager.getHolder(slave));
            webSocketSessionsManager.send(slave, slaveMessage);
        }
    }

    public void sendAllCommands(Session session, CollaborativeRoom room) {
        WebSocketMessage message = createMessage(COLLABORATIVE_JOIN, room.getKey(),
                room.getCommands(), webSocketSessionsManager.getHolder(session));
        webSocketSessionsManager.send(session, message);
    }

    private WebSocketMessage createMessage(String type, String key, JsonValue messageBroadcast, String remoteUser) {
        JsonObjectBuilder b = Json.createObjectBuilder()
                .add("type", type)
                .add("key", key)
                .add("messageBroadcast", messageBroadcast)
                .add("remoteUser", remoteUser);

        return new WebSocketMessage(b.build());
    }

}