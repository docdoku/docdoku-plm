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
        if (room.getMasterName().equals(callerLogin)){
            // the master sent the invitation

            if (room.findUserSession(invitedUser)==null) {
                // the user is not already in the room
                if (!room.getPendingUsers().contains(invitedUser)) {
                    // the user is not yet in the pending list, add him.
                    room.addPendingUser(invitedUser);
                }
                // Chat message
                ChatMessage chatMessage = new ChatMessage(ChannelMessagesType.CHAT_MESSAGE,invitedUser);
                String invite = "/invite " + url + "#room=" + room.getKey();
                chatMessage.setMessage(invite);
                chatMessage.setContext(context);
                chatMessage.setRemoteUser(callerLogin);
                chatMessage.setSender(callerLogin);
                MainChannelDispatcher.sendToAllUserChannels(invitedUser,chatMessage);

                CollaborativeRoomController.broadcastNewContext(room);
            } else {
                System.out.println("Invite not allowed for '"+invitedUser+"'.");
            }
        }

    }

    public static void processJoin(Session callerSession, String callerLogin, CollaborativeRoom room, CollaborativeMessage collaborativeMessage){
        if (room == null){
            System.out.println("Room not found");
            //TODO : faire le test en amont?
            return;
        }

        // Master
        if (room.getMasterName().equals("")){
            // if the room has no master, allow the last master to take the lead
            if (room.getLastMaster().equals(callerLogin)) {
                room.setMaster(callerSession);

                MainChannelDispatcher.send(callerSession, collaborativeMessage); // notify the user that he joined the room
                CollaborativeRoomController.broadcastNewContext(room);
                sendAllCommands(callerSession,room);
                return;
            }
        }

        // User
        if (!room.removePendingUser(callerLogin))
        {
            // if the user is not in the pending list reject him
            CollaborativeMessage message = new CollaborativeMessage(ChannelMessagesType.COLLABORATIVE_KICK_NOT_INVITED,room.getKey(),null,callerLogin);
            MainChannelDispatcher.send(callerSession, message);
        } else {
            room.addSlave(callerSession);
            MainChannelDispatcher.send(callerSession, collaborativeMessage); // notify the user that he joined the room
            CollaborativeRoomController.broadcastNewContext(room);
            sendAllCommands(callerSession,room);

        }
    }

    public static void sendAllCommands(Session user, CollaborativeRoom room){
        MainChannelDispatcher.send(user, room.getCameraInfos());
        MainChannelDispatcher.send(user, room.getSmartPath());
        MainChannelDispatcher.send(user, room.getEditedMeshes());
        MainChannelDispatcher.send(user, room.getColourEditedMeshes());
        MainChannelDispatcher.send(user, room.getExplode());
    }

    public static void processCommands(String callerLogin, CollaborativeRoom room, CollaborativeMessage collaborativeMessage){
        // if the master sent the command
        if (room.getMasterName().equals(callerLogin)) {
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
            if (command.containsKey("colourEditedMeshes")) {
                room.setColourEditedMeshes(collaborativeMessage);
            }
            if (command.containsKey("explode")) {
                room.setExplode(collaborativeMessage);
            }
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
        // if the master sent the invitation
        if (room.getMasterName().equals(callerLogin)) {
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
        if (room.getMasterName().equals(callerLogin)) {
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
        if (room.getMasterName().equals(callerLogin)) {
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
        if (room.getMasterName().equals(callerLogin)) {
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
