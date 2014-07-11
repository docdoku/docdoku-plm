package com.docdoku.server.mainchannel.collaborative;

import com.docdoku.server.mainchannel.MainChannelDispatcher;
import com.docdoku.server.mainchannel.module.ChatMessage;
import com.docdoku.server.mainchannel.module.CollaborativeMessage;
import com.docdoku.server.mainchannel.util.ChannelMessagesType;

import javax.enterprise.inject.New;
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
        CollaborativeMessage message = new CollaborativeMessage(ChannelMessagesType.COLLABORATIVE_CREATE,room.getKey(),"room created !",callerLogin);
        MainChannelDispatcher.send(session, message);
    }


    public static void processInvite(Session session, String callerLogin, CollaborativeMessage collaborativeMessage){
        CollaborativeRoom collaborativeRoom = CollaborativeRoom.getByKeyName(collaborativeMessage.getKey());
        /*
        try {
            String jsonString = collaborativeMessage.getMessageBroadcast();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode obj = mapper.readTree(jsonString);
            JsonNode response = obj.get("response");

            List<Session> pendingUsers = mapper.readValue(response, new TypeReference<List<Session>>() {});
            collaborativeRoom.setPendingUsers(pendingUsers);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        String user = collaborativeMessage.getRemoteUser();
        collaborativeRoom.addPendingUser(user);
        // Chat message
        ChatMessage chatMessage = new ChatMessage(ChannelMessagesType.CHAT_MESSAGE,user);
        String url = "Vous êtes invités à rejoindre une salle collaborative : http://localhost:8080/product-structure/" + collaborativeMessage.getMessageBroadcast() + "#room=" + collaborativeMessage.getKey();
        chatMessage.setMessage(url);
        chatMessage.setContext(collaborativeMessage.getMessageBroadcast());
        chatMessage.setSender(callerLogin);
        MainChannelDispatcher.sendToAllUserChannels(user,chatMessage);

        CollaborativeRoomController.broadcastNewContext(collaborativeRoom);
    }

    public static void processJoin(Session session, String callerLogin, CollaborativeMessage collaborativeMessage){
        CollaborativeRoom collaborativeRoom = CollaborativeRoom.getByKeyName(collaborativeMessage.getKey());
        if (collaborativeRoom == null){
            System.out.println("Room not found");
            return;
        }

        // if the user is on the pending list reject him
        if (!collaborativeRoom.removePendingUser(callerLogin))
        {
            System.out.println("User not find in the pending users list !");
            CollaborativeMessage message = new CollaborativeMessage(ChannelMessagesType.COLLABORATIVE_KICK_USER,collaborativeRoom.getKey(),"You've been kicked. Reason : not invited",callerLogin);
            MainChannelDispatcher.send(session, message);

        } else {
            collaborativeRoom.addSlave(session);
            // send the context to the new slave
            //CollaborativeMessage message = new CollaborativeMessage(ChannelMessagesType.COLLABORATIVE_CONTEXT,collaborativeRoom.getKey(),collaborativeRoom.getContext(),callerLogin);
            //MainChannelDispatcher.send(session, message);
            CollaborativeRoomController.broadcastNewContext(collaborativeRoom);
        }
    }


    public static void processInfo(Session session, String callerLogin, CollaborativeMessage infoMessage) {
        CollaborativeRoom collaborativeRoom = CollaborativeRoom.getByKeyName(infoMessage.getKey());
        CollaborativeMessage message = new CollaborativeMessage(ChannelMessagesType.COLLABORATIVE_CONTEXT,collaborativeRoom.getKey(),collaborativeRoom.getContext(),callerLogin);
        MainChannelDispatcher.send(session, message);
    }

    public static void processCommands(Session session, String callerLogin, CollaborativeMessage collaborativeMessage){
        CollaborativeRoom collaborativeRoom = CollaborativeRoom.getByKeyName(collaborativeMessage.getKey());
        for(Session slave : collaborativeRoom.getSlaves()){
            String remoteUser = slave.getUserPrincipal().getName();
            MainChannelDispatcher.send(slave,collaborativeMessage);
        }
    }

    public static void processExit(Session session, String callerLogin, CollaborativeMessage collaborativeMessage){
        CollaborativeRoom collaborativeRoom = CollaborativeRoom.getByKeyName(collaborativeMessage.getKey());
        collaborativeRoom.getSlaves().remove(session);

        CollaborativeRoomController.broadcastNewContext(collaborativeRoom);
    }

    public static void processKill(Session session, String callerLogin, CollaborativeMessage collaborativeMessage){
        CollaborativeRoom collaborativeRoom = CollaborativeRoom.getByKeyName(collaborativeMessage.getKey());
        String roomKey = collaborativeRoom.getKey();
        CollaborativeMessage kickMessage;
        for(Session slave : collaborativeRoom.getSlaves()){
            String remoteUser = slave.getUserPrincipal().getName();
            kickMessage = new CollaborativeMessage(ChannelMessagesType.COLLABORATIVE_KICK_USER,roomKey,"You've been kicked. Reason : room destroyed",remoteUser);
            MainChannelDispatcher.send(slave,kickMessage);
        }
        //collaborativeRoom.delete();
    }

    public static void processRequestHand(Session session, String callerLogin, CollaborativeMessage collaborativeMessage){
        CollaborativeRoom collaborativeRoom = CollaborativeRoom.getByKeyName(collaborativeMessage.getKey());
        //TODO master has to confirm/cancel the request
        Session master = collaborativeRoom.getMaster();
        MainChannelDispatcher.sendToAllUserChannels(master.getUserPrincipal().getName(),collaborativeMessage);

        // Info update ?
        for(Session slave : collaborativeRoom.getSlaves()){
            MainChannelDispatcher.sendToAllUserChannels(slave.getUserPrincipal().getName(),collaborativeMessage);
        }

    }

    public static void processGiveHand(Session session, String callerLogin, CollaborativeMessage collaborativeMessage){
        CollaborativeRoom collaborativeRoom = CollaborativeRoom.getByKeyName(collaborativeMessage.getKey());
        collaborativeRoom.addSlave(collaborativeRoom.getMaster());
        String promotedUser = collaborativeMessage.getRemoteUser();
        Session userSession = collaborativeRoom.findUserSession(promotedUser);
        collaborativeRoom.removeSlave(userSession);
        collaborativeRoom.setMaster(userSession);
        MainChannelDispatcher.send(collaborativeRoom.getMaster(),collaborativeMessage);

        CollaborativeRoomController.broadcastNewContext(collaborativeRoom);
    }
    public static void processKickUser(Session session, String callerLogin, CollaborativeMessage collaborativeMessage){
        CollaborativeRoom collaborativeRoom = CollaborativeRoom.getByKeyName(collaborativeMessage.getKey());
        String kickedUser = collaborativeMessage.getRemoteUser();
        // TODO : we can have multiple sessions for one UserName ! Kick them all or specify the session to kick ?
        Session userSession = collaborativeRoom.findUserSession(kickedUser);
        collaborativeRoom.removeSlave(userSession);
        MainChannelDispatcher.send(userSession,collaborativeMessage);

        CollaborativeRoomController.broadcastNewContext(collaborativeRoom);
    }
    public static void processWithdrawInvitation(Session session, String callerLogin, CollaborativeMessage collaborativeMessage){
        CollaborativeRoom collaborativeRoom = CollaborativeRoom.getByKeyName(collaborativeMessage.getKey());
        String pendingUser = collaborativeMessage.getRemoteUser();
        collaborativeRoom.removePendingUser(pendingUser);
        // Chat message
        ChatMessage chatMessage = new ChatMessage(ChannelMessagesType.CHAT_MESSAGE,pendingUser);
        chatMessage.setMessage("Invitation annulée.");
        chatMessage.setContext(collaborativeMessage.getMessageBroadcast());
        chatMessage.setSender(callerLogin);
        MainChannelDispatcher.sendToAllUserChannels(pendingUser,chatMessage);

        CollaborativeRoomController.broadcastNewContext(collaborativeRoom);
    }
}
