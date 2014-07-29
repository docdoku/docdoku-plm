package com.docdoku.server.mainchannel.collaborative;

import com.docdoku.server.mainchannel.MainChannelDispatcher;
import com.docdoku.server.mainchannel.module.ChatMessage;
import com.docdoku.server.mainchannel.module.CollaborativeMessage;
import com.docdoku.server.mainchannel.util.ChannelMessagesType;

import javax.json.JsonObject;
import javax.websocket.Session;
/**
 * Created by docdoku on 30/06/14.
 */
public class CollaborativeRoomController {

    public static void broadcastNewContext(CollaborativeRoom room){
        String master = room.getMaster().getUserPrincipal().getName();
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

    public static void processInvite(String callerLogin, String invitedUser, CollaborativeRoom room, String context){
        if (room.getMaster().getUserPrincipal().toString().equals(callerLogin)){
            // the master sent the invitation

            if (room.findUserSession(invitedUser)==null) {
                // the user is not already in the room
                if (!room.getPendingUsers().contains(invitedUser)) {
                    // the user is not yet in the pending list, add him.
                    room.addPendingUser(invitedUser);
                }
                // Chat message
                ChatMessage chatMessage = new ChatMessage(ChannelMessagesType.CHAT_MESSAGE,invitedUser);
                String url = "Vous êtes invités à rejoindre une salle collaborative : http://localhost:8080/product-structure/" + context + "#room=" + room.getKey();
                chatMessage.setMessage(url);
                chatMessage.setContext(context);
                chatMessage.setSender(callerLogin);
                MainChannelDispatcher.sendToAllUserChannels(invitedUser,chatMessage);

                CollaborativeRoomController.broadcastNewContext(room);
            } else {
                System.out.println("Invite not allowed for '"+invitedUser+"'.");
            }
        }

    }

    public static void processJoin(Session callerSession, String callerLogin, CollaborativeRoom room){
        if (room == null){
            System.out.println("Room not found");
            //TODO : faire le test en amont?
            return;
        }
        // if the user is not in the pending list reject him
        if (!room.removePendingUser(callerLogin))
        {
            CollaborativeMessage message = new CollaborativeMessage(ChannelMessagesType.COLLABORATIVE_KICK_NOT_INVITED,room.getKey(),null,callerLogin);
            MainChannelDispatcher.send(callerSession, message);
        } else {
            room.addSlave(callerSession);
            CollaborativeRoomController.broadcastNewContext(room);
            // send the last context scene
            MainChannelDispatcher.send(callerSession, room.getCameraInfos());
            MainChannelDispatcher.send(callerSession, room.getSmartPath());
            MainChannelDispatcher.send(callerSession, room.getEditedMeshes());
        }
    }

    public static void processCommands(String callerLogin, CollaborativeRoom room, CollaborativeMessage collaborativeMessage){
        // if the master sent the command
        if (room.getMaster().getUserPrincipal().toString().equals(callerLogin)) {
            // save camera infos
            JsonObject command = collaborativeMessage.getMessageBroadcast();
            if (command.containsKey("cameraInfos")) {
                room.setCameraInfos(collaborativeMessage);
            }
            if (command.containsKey("smartPath")) {
                room.setSmartPath(collaborativeMessage);
            }
            if (command.containsKey("editedMeshes")) {
                room.setEditedMeshes(collaborativeMessage);
            }
            for (Session slave : room.getSlaves()) {
                MainChannelDispatcher.send(slave, collaborativeMessage);
            }
        }
    }

    public static void processExit(Session session, CollaborativeRoom room){
        room.getSlaves().remove(session);
        // if the user is not yet in the pending list
        if (!room.getPendingUsers().contains(session.getUserPrincipal().getName())) {
            room.addPendingUser(session.getUserPrincipal().getName());
        }

        CollaborativeRoomController.broadcastNewContext(room);
    }

    public static void processKill(String callerLogin, CollaborativeRoom room){
        // if the master sent the invitation
        if (room.getMaster().getUserPrincipal().toString().equals(callerLogin)) {
            String roomKey = room.getKey();
            CollaborativeMessage kickMessage;
            for (Session slave : room.getSlaves()) {
                String remoteUser = slave.getUserPrincipal().getName();
                kickMessage = new CollaborativeMessage(ChannelMessagesType.COLLABORATIVE_KICK_USER, roomKey, null, remoteUser);
                MainChannelDispatcher.send(slave, kickMessage);
            }
            //collaborativeRoom.delete();
        }
    }

    public static void processRequestHand(Session session, String callerLogin, CollaborativeMessage collaborativeMessage){
        CollaborativeRoom collaborativeRoom = CollaborativeRoom.getByKeyName(collaborativeMessage.getKey());
        //TODO master has to confirm/cancel a request to take the hand
    }

    public static void processGiveHand(String callerLogin, String promotedUser, CollaborativeRoom room, CollaborativeMessage collaborativeMessage){
        // if the master sent the invitation
        if (room.getMaster().getUserPrincipal().toString().equals(callerLogin)) {
            room.addSlave(room.getMaster());
            Session userSession = room.findUserSession(promotedUser);
            if(room.removeSlave(userSession)){
                room.setMaster(userSession);
                MainChannelDispatcher.send(room.getMaster(), collaborativeMessage);

                CollaborativeRoomController.broadcastNewContext(room);
            }
        }
    }
    public static void processKickUser(String callerLogin, String kickedUser, CollaborativeRoom room, CollaborativeMessage collaborativeMessage){
        // if the master sent the invitation
        if (room.getMaster().getUserPrincipal().toString().equals(callerLogin)) {
            Session userSession = room.findUserSession(kickedUser);
            if(room.removeSlave(userSession)) {
                MainChannelDispatcher.send(userSession, collaborativeMessage);

                CollaborativeRoomController.broadcastNewContext(room);
            } else {
                System.out.println("Error : can't remove the user "+kickedUser);
            }
        }
    }
    public static void processWithdrawInvitation(String callerLogin, String pendingUser, CollaborativeRoom room, String context){
        // if the master sent the invitation
        if (room.getMaster().getUserPrincipal().toString().equals(callerLogin)) {
            room.removePendingUser(pendingUser);
            // Chat message
            ChatMessage chatMessage = new ChatMessage(ChannelMessagesType.CHAT_MESSAGE, pendingUser);
            chatMessage.setMessage("Invitation annulée.");
            chatMessage.setContext(context);
            chatMessage.setSender(callerLogin);
            MainChannelDispatcher.sendToAllUserChannels(pendingUser, chatMessage);

            CollaborativeRoomController.broadcastNewContext(room);
        }
    }
}
