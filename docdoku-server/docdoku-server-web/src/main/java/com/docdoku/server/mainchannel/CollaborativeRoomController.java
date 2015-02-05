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

import com.docdoku.server.mainchannel.collaborative.CollaborativeRoom;
import com.docdoku.server.mainchannel.module.ChatMessage;
import com.docdoku.server.mainchannel.module.CollaborativeMessage;
import com.docdoku.server.mainchannel.util.ChannelMessagesType;

import javax.json.JsonObject;
import javax.websocket.Session;

/**
 * A controller for manage a room of Collaboration Module
 *
 * @author Arthur FRIN
 * @version 1.0, 30/06/14
 * @since   V2.0
 */
public class CollaborativeRoomController {

    private CollaborativeRoomController(){
        super();
    }


    public static void broadcastNewContext(CollaborativeRoom room){
        String master = room.getMasterName();
        CollaborativeMessage message = new CollaborativeMessage(ChannelMessagesType.COLLABORATIVE_CONTEXT,room.getKey(),room.getContext(),master);
        MainChannelDispatcher.sendToAllUserChannels(master,message);
        for(Session slave : room.getSlaves()){
            String remoteUser = slave.getUserPrincipal().getName();
            message.setRemoteUser(remoteUser);
            MainChannelDispatcher.send(slave, message);
        }
    }

    public static void processCreate(Session session, String callerLogin){
        CollaborativeRoom room = new CollaborativeRoom(session);
        CollaborativeMessage message = new CollaborativeMessage(ChannelMessagesType.COLLABORATIVE_CREATE,room.getKey(),null,callerLogin);
        MainChannelDispatcher.send(session, message);
    }

    public static void processInvite(String callerLogin, String invitedUser, CollaborativeRoom room, String context, String url){
        if (room.getMasterName().equals(callerLogin) && room.findUserSession(invitedUser)==null) {
            // the master sent the invitation
            // the user is not already in the room
            if (!room.getPendingUsers().contains(invitedUser)) {
                // the user is not yet in the pending list, add him.
                room.addPendingUser(invitedUser);
            }
            // Chat message
            ChatMessage chatMessage = new ChatMessage(ChannelMessagesType.CHAT_MESSAGE,invitedUser);
            String invite = "/invite " + url + "/room/" + room.getKey();
            chatMessage.setMessage(invite);
            chatMessage.setContext(context);
            chatMessage.setRemoteUser(callerLogin);
            chatMessage.setSender(callerLogin);
            MainChannelDispatcher.sendToAllUserChannels(invitedUser,chatMessage);

            CollaborativeRoomController.broadcastNewContext(room);
        }
    }

    public static void processJoin(Session callerSession, String callerLogin, CollaborativeRoom room, CollaborativeMessage collaborativeMessage){
        if (room == null){
            //Room not found
            return;
        }

        // Master
        if ("".equals(room.getMasterName()) && room.getLastMaster().equals(callerLogin)) {
            // if the room has no master, allow the last master to take the lead
            room.setMaster(callerSession);
            CollaborativeRoomController.sendAllCommands(callerSession,room);
            //MainChannelDispatcher.send(callerSession, collaborativeMessage); // notify the user that he joined the room
            CollaborativeRoomController.broadcastNewContext(room);
            return;
        }

        // User
        if (!room.removePendingUser(callerLogin)){
            // if the user is not in the pending list reject him
            CollaborativeMessage message = new CollaborativeMessage(ChannelMessagesType.COLLABORATIVE_KICK_NOT_INVITED,room.getKey(),null,callerLogin);
            MainChannelDispatcher.send(callerSession, message);
        } else {
            room.addSlave(callerSession);
            CollaborativeRoomController.sendAllCommands(callerSession,room);
            //MainChannelDispatcher.send(callerSession, collaborativeMessage); // notify the user that he joined the room
            CollaborativeRoomController.broadcastNewContext(room);

        }
    }

    public static void removeSessionFromCollaborativeRoom(Session userSession) {

        for (CollaborativeRoom room : CollaborativeRoom.getAllCollaborativeRooms()) {
            if (room.getSlaves().contains(userSession)){
                CollaborativeRoomController.processExit(userSession, userSession.getUserPrincipal().getName(), room);
            }
            if (room.getMaster()==userSession){
                CollaborativeRoomController.processExit(userSession,userSession.getUserPrincipal().getName(),room);
            }
        }
    }

    public static void sendAllCommands(Session user, CollaborativeRoom room){
        CollaborativeMessage mess = new CollaborativeMessage(ChannelMessagesType.COLLABORATIVE_JOIN, room.getKey(), room.getCommands(), user.getUserPrincipal().getName());
        MainChannelDispatcher.send(user, mess);
    }

    public static void processCommands(String callerLogin, CollaborativeRoom room, CollaborativeMessage collaborativeMessage){
        if (room.getMasterName().equals(callerLogin)) {
            // the master sent the invitation
            // save camera infos
            JsonObject command = collaborativeMessage.getMessageBroadcast();
            room.saveCommand(command);
            for (Session slave : room.getSlaves()) {
                MainChannelDispatcher.send(slave, collaborativeMessage);
            }
        }
    }

    public static void processExit(Session session, String callerLogin, CollaborativeRoom room){
        if (room.getMasterName().equals(callerLogin)) {
            // exit for the master
            room.setMaster(null);
            room.setLastMaster(callerLogin);
        } else {
            room.getSlaves().remove(session);
            // add the user in the pending list
            if (!room.getPendingUsers().contains(callerLogin)) {
                room.addPendingUser(callerLogin);
            }
        }
        CollaborativeRoomController.broadcastNewContext(room);
    }

    public static void processKill(String callerLogin, CollaborativeRoom room){
        if (room.getMasterName().equals(callerLogin)) {
            // the master sent the invitation
            String roomKey = room.getKey();
            CollaborativeMessage kickMessage;
            for (Session slave : room.getSlaves()) {
                String remoteUser = slave.getUserPrincipal().getName();
                kickMessage = new CollaborativeMessage(ChannelMessagesType.COLLABORATIVE_KICK_USER, roomKey, null, remoteUser);
                MainChannelDispatcher.send(slave, kickMessage);
            }
            room.delete();
        }
    }

    public static void processGiveHand(String callerLogin, String promotedUser, CollaborativeRoom room){
        if (room.getMasterName().equals(callerLogin)) {
            // the master sent the invitation
            room.addSlave(room.getMaster());
            Session userSession = room.findUserSession(promotedUser);
            if(room.removeSlave(userSession)){
                room.setMaster(userSession);
                CollaborativeRoomController.broadcastNewContext(room);
            }
        }
    }
    public static void processKickUser(String callerLogin, String kickedUser, CollaborativeRoom room, CollaborativeMessage collaborativeMessage){
        if (room.getMasterName().equals(callerLogin)) {
            // the master sent the invitation
            Session userSession = room.findUserSession(kickedUser);
            if(room.removeSlave(userSession)) {
                // user has been removed
                MainChannelDispatcher.send(userSession, collaborativeMessage);
                CollaborativeRoomController.broadcastNewContext(room);
            }
        }
    }
    public static void processWithdrawInvitation(String callerLogin, String pendingUser, CollaborativeRoom room, String context){
        if (room.getMasterName().equals(callerLogin)) {
            // the master sent the invitation
            room.removePendingUser(pendingUser);
            // Chat message
            ChatMessage chatMessage = new ChatMessage(ChannelMessagesType.CHAT_MESSAGE, pendingUser);
            chatMessage.setMessage("/withdrawInvitation");
            chatMessage.setContext(context);
            chatMessage.setRemoteUser(callerLogin);
            chatMessage.setSender(callerLogin);
            MainChannelDispatcher.sendToAllUserChannels(pendingUser, chatMessage);

            CollaborativeRoomController.broadcastNewContext(room);
        }
    }
}
