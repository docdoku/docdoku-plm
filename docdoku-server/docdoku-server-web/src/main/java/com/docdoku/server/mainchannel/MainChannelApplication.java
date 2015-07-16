/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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
import com.docdoku.server.mainchannel.collaborative.CollaborativeRoom;
import com.docdoku.server.mainchannel.module.*;
import com.docdoku.server.mainchannel.util.ChannelMessagesBuilder;
import com.docdoku.server.mainchannel.util.ChannelMessagesType;
import com.docdoku.server.mainchannel.util.Room;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@ServerEndpoint(value = "/mainChannelSocket", decoders = {MessageDecoder.class}, encoders = {WebRTCMessageEncoder.class, StatusMessageEncoder.class, ChatMessageEncoder.class, CollaborativeMessageEncoder.class})
public class MainChannelApplication {

    // Users WebSockets Map : <UserLogin, <SessionId, Session>>
    private static final ConcurrentMap<String, Map<String, Session>> CHANNELS = new ConcurrentHashMap<>();

    private static final Logger LOGGER = Logger.getLogger(MainChannelApplication.class.getName());
    @EJB
    private IUserManagerLocal userManager;

    public static boolean hasChannels(String userLogin) {
        return CHANNELS!=null
                && userLogin !=null
                && CHANNELS.get(userLogin) != null
                && !CHANNELS.get(userLogin).isEmpty();
    }

    public static Map<String, Session> getUserChannels(String userLogin) {
        return CHANNELS.get(userLogin);
    }


    public static void sessionDestroyed(String userLogin) {
        if(userLogin!=null && hasChannels(userLogin)) {
            Map<String, Session> sessionMap = CHANNELS.get(userLogin);
            List<Session> sessionList = new ArrayList<>(sessionMap.values());

            for (Session session : sessionList) {
                try {
                    sessionMap.get(session.getId()).close();
                } catch (IOException e) {
                    Logger.getLogger(MainChannelApplication.class.getName()).log(Level.INFO, null, e);
                }
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
        LOGGER.log(Level.FINE, null, reason);
        closeSession(session);
    }

    @OnOpen
    public void open(Session session) {

        Principal userPrincipal = session.getUserPrincipal();
        String callerLogin = userPrincipal.getName();
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

        if(!(message instanceof CollaborativeMessage)){
            // Exit if remote user is null or caller tries to join himself
            if (message.getRemoteUser() == null || message.getRemoteUser().equals(callerLogin)) {
                return;
            }
            // Exit if caller is not allowed to reach callee (business)
            if (!callerIsAllowToReachCallee(callerLogin, message.getRemoteUser())) {
                return;
            }
        }

        WebRTCMessage webRTC;
        CollaborativeRoom room;
        switch (message.getType()) {
            case ChannelMessagesType.USER_STATUS:
                StatusMessage status = (StatusMessage) message;
                process(session, status);
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
                processWebRTCReject(callerLogin, webRTC);
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

            case ChannelMessagesType.COLLABORATIVE_CREATE:
                CollaborativeRoomController.processCreate(session, callerLogin);
                break;
            case ChannelMessagesType.COLLABORATIVE_INVITE:
                CollaborativeMessage inviteMessage = (CollaborativeMessage) message;
                room = CollaborativeRoom.getByKeyName(inviteMessage.getKey());
                String invitedUser = inviteMessage.getRemoteUser();
                String context = inviteMessage.getMessageBroadcast().getString("context");
                String url = inviteMessage.getMessageBroadcast().getString("url");
                if (callerIsAllowToReachCallee(callerLogin,invitedUser)) {
                    CollaborativeRoomController.processInvite(callerLogin, invitedUser, room, context, url);
                }
                break;
            case ChannelMessagesType.COLLABORATIVE_JOIN:
                CollaborativeMessage joinMessage = (CollaborativeMessage) message;
                room = CollaborativeRoom.getByKeyName(joinMessage.getKey());
                CollaborativeRoomController.processJoin(session, callerLogin, room, joinMessage);
                break;
            case ChannelMessagesType.COLLABORATIVE_COMMANDS:
                CollaborativeMessage commandsMessage = (CollaborativeMessage) message;
                room = CollaborativeRoom.getByKeyName(commandsMessage.getKey());
                CollaborativeRoomController.processCommands(callerLogin, room, commandsMessage);
                break;
            case ChannelMessagesType.COLLABORATIVE_EXIT:
                CollaborativeMessage exitMessage = (CollaborativeMessage) message;
                room = CollaborativeRoom.getByKeyName(exitMessage.getKey());
                CollaborativeRoomController.processExit(session, callerLogin, room);
                break;
            case ChannelMessagesType.COLLABORATIVE_KILL:
                CollaborativeMessage killMessage = (CollaborativeMessage) message;
                room = CollaborativeRoom.getByKeyName(killMessage.getKey());
                CollaborativeRoomController.processKill(callerLogin, room);
                break;
            case ChannelMessagesType.COLLABORATIVE_GIVE_HAND:
                CollaborativeMessage giveHandMessage = (CollaborativeMessage) message;
                room = CollaborativeRoom.getByKeyName(giveHandMessage.getKey());
                String promotedUser = giveHandMessage.getRemoteUser();
                CollaborativeRoomController.processGiveHand(callerLogin, promotedUser, room);
                break;
            case ChannelMessagesType.COLLABORATIVE_KICK_USER:
                CollaborativeMessage kickUserMessage = (CollaborativeMessage) message;
                room = CollaborativeRoom.getByKeyName(kickUserMessage.getKey());
                String kickedUser = kickUserMessage.getRemoteUser();
                CollaborativeRoomController.processKickUser(callerLogin, kickedUser, room, kickUserMessage);
                break;
            case ChannelMessagesType.COLLABORATIVE_WITHDRAW_INVITATION:
                CollaborativeMessage withdrawInvitationMessage = (CollaborativeMessage) message;
                room = CollaborativeRoom.getByKeyName(withdrawInvitationMessage.getKey());
                String messageContext = withdrawInvitationMessage.getMessageBroadcast().getString("context");
                String pendingUser = withdrawInvitationMessage.getRemoteUser();
                CollaborativeRoomController.processWithdrawInvitation(callerLogin, pendingUser, room, messageContext);
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
            CollaborativeRoomController.removeSessionFromCollaborativeRoom(session);
            // remove the session from the user hash map
            CHANNELS.get(userLogin).remove(sessionId);
            // clean from memory when no more channel left
            if(!hasChannels(userLogin)){
                CHANNELS.remove(userLogin);
            }
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

    private void processWebRTCReject(String callerLogin, WebRTCMessage webRTC) {
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
        MainChannelDispatcher.sendToAllUserChannels(webRTC.getRemoteUser(), new WebRTCMessage(ChannelMessagesType.WEBRTC_INVITE, callerLogin, roomKey, null, webRTC.getContext(), room.getOccupancy(), null));
    }

    private void processP2P(Session session, String callerLogin, WebRTCMessage webRTC) {
        // webRTC P2P signaling messages
        // These messages are forwarded to the remote peer(s) in the room
        Room room = Room.getByKeyName(webRTC.getRoomKey());
        if (room != null && room.hasUser(callerLogin)) {
            // forward the message to the other peer
            Session otherSession = room.getOtherUserSession(session);

            // on bye message, remove the user from the room
            if (ChannelMessagesType.WEBRTC_BYE.equals(webRTC.getType())) {
                room.removeUserSession(session);
            }

            if (otherSession != null) {
                MainChannelDispatcher.send(otherSession, webRTC);
            }
        }

    }

    private void process(Session session, StatusMessage status) {
        if (!MainChannelApplication.hasChannels(status.getRemoteUser())) {
            MainChannelDispatcher.send(session, new StatusMessage(ChannelMessagesType.USER_STATUS, status.getRemoteUser(), StatusMessage.USER_STATUS_OFFLINE));
        } else {
            MainChannelDispatcher.send(session, new StatusMessage(ChannelMessagesType.USER_STATUS, status.getRemoteUser(), StatusMessage.USER_STATUS_ONLINE));
        }
    }

    private void process(Session session, String callerLogin, ChatMessage chat) {
        if (!MainChannelApplication.hasChannels(chat.getRemoteUser())) {
            MainChannelDispatcher.send(session, new ChatMessage(ChannelMessagesType.CHAT_MESSAGE, chat.getRemoteUser(), "", "", chat.getContext(), ChatMessage.CHAT_MESSAGE_UNREACHABLE));
        } else {
            MainChannelDispatcher.sendToAllUserChannels(callerLogin, new ChatMessage(ChannelMessagesType.CHAT_MESSAGE_ACK, chat.getRemoteUser(), callerLogin, chat.getMessage(), chat.getContext(), null));
            MainChannelDispatcher.sendToAllUserChannels(chat.getRemoteUser(), new ChatMessage(ChannelMessagesType.CHAT_MESSAGE, callerLogin, callerLogin, chat.getMessage(), chat.getContext(), null));
        }
    }

}
