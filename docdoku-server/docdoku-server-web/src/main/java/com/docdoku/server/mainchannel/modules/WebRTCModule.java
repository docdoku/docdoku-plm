package com.docdoku.server.mainchannel.modules;

import com.docdoku.server.mainchannel.MainChannelApplication;
import com.docdoku.server.mainchannel.MainChannelDispatcher;
import com.docdoku.server.mainchannel.util.ChannelMessagesType;
import com.docdoku.server.mainchannel.util.Room;

import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.Session;

public class WebRTCModule {

    private WebRTCModule(){
    }

    public static void onWebRTCInviteMessage(Session session, JsonObject jsobj) {

        String callerLogin = (String)session.getUserProperties().get("userLogin");

        String remoteUser = jsobj.getString("remoteUser");
        String context = jsobj.getString("context");

        String roomKey = callerLogin + "-" + remoteUser;

        if(! MainChannelApplication.hasChannels(remoteUser)){
            MainChannelDispatcher.send(session, buildUserOfflineMessage(remoteUser,roomKey));
            return ;
        }

        Room room = Room.getByKeyName(roomKey);

        if (room == null){
            room = new Room(roomKey);
        }
        //else :  multiple invitations, caller is spamming or something goes wrong.

        // the room is ready to receive user sessions.

        // add the caller session in the room
        room.addUserSession(session);

        // send room join event to caller session (single channel)
        MainChannelDispatcher.send(session, buildRoomJoinEventMessage(callerLogin, room));

        // send invitation to the remote user sessions (all channels)
        MainChannelDispatcher.sendToAllUserChannels(remoteUser, buildWebRTCInvitationMessage(callerLogin, context, roomKey));

    }

    public static void onWebRTCAcceptMessage(Session session, JsonObject jsobj) {

        String callerLogin = (String)session.getUserProperties().get("userLogin");
        String remoteUser = jsobj.getString("remoteUser");
        String roomKey = jsobj.getString("roomKey");

        Room room = Room.getByKeyName(roomKey);

        if (room != null && room.hasUser(remoteUser)) {

            room.addUserSession(session);

            // send room join event to caller (all channels to remove invitations if any)
            MainChannelDispatcher.sendToAllUserChannels(callerLogin,buildRoomJoinEventMessage(callerLogin, room));

            // send room join event to the other user in room
            Session otherSession = room.getOtherUserSession(session);

            if(otherSession != null){
                MainChannelDispatcher.send(otherSession, buildWebRTCAcceptMessage(callerLogin,room.key()));
            }

        }

    }

    public static void onWebRTCRejectMessage(Session session, JsonObject jsobj) {

        String callerLogin = (String)session.getUserProperties().get("userLogin");
        String remoteUser = jsobj.getString("remoteUser");
        String roomKey = jsobj.getString("roomKey");
        String reason = jsobj.getString("reason");

        Room room = Room.getByKeyName(roomKey);

        if (room != null) {

            // send "room reject event" to caller, to remove invitations in other tabs if any
            MainChannelDispatcher.sendToAllUserChannels(callerLogin, buildRoomRejectEventMessage(callerLogin, room));

            Session otherSession = room.getUserSession(remoteUser);
            if(otherSession != null){
                MainChannelDispatcher.send(otherSession, buildWebRTCRejectMessage(callerLogin, room.key(), reason));
            }

        }

    }

    public static void onWebRTCHangupMessage(Session session, JsonObject jsobj) {

        String callerLogin = (String)session.getUserProperties().get("userLogin");
        String roomKey = jsobj.getString("roomKey");
        Room room = Room.getByKeyName(roomKey);

        if (room != null) {

            Session otherSession = room.getOtherUserSession(session);
            room.removeUserSession(session);

            MainChannelDispatcher.send(otherSession, buildWebRTCHangupMessage(callerLogin, room.key()));

        }

    }

    // webRTC P2P signaling messages
    // These messages are forwarded to the remote peer(s) in the room
    public static void onWebRTCSignalingMessage(Session session, JsonObject jsobj, String data) {

        String callerLogin = (String)session.getUserProperties().get("userLogin");
        String roomKey = jsobj.getString("roomKey");
        Room room = Room.getByKeyName(roomKey);

        if (room != null) {

            if (room.hasUser(callerLogin)) {

                // forward the message to the other peer
                Session otherSession = room.getOtherUserSession(session);

                // on bye message, remove the user from the room
                if(jsobj.getString("type").equals(ChannelMessagesType.WEBRTC_BYE)){
                    room.removeUserSession(session);
                }

                if (otherSession != null) {
                    MainChannelDispatcher.send(otherSession, data);
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


    // Web RTC
    private static String buildWebRTCInvitationMessage(String callerLogin, String context, String roomKey) {
        return Json.createObjectBuilder()
        .add("type", ChannelMessagesType.WEBRTC_INVITE)
        .add("remoteUser", callerLogin)
        .add("context", context)
        .add("roomKey", roomKey).build().toString();
    }

    private static String buildWebRTCAcceptMessage(String callerLogin, String roomKey) {
        return Json.createObjectBuilder()
        .add("type", ChannelMessagesType.WEBRTC_ACCEPT)
        .add("remoteUser", callerLogin)
        .add("roomKey", roomKey).build().toString();
    }

    private static String buildWebRTCRejectMessage(String callerLogin, String roomKey, String reason) {
        return Json.createObjectBuilder()
        .add("type", ChannelMessagesType.WEBRTC_REJECT)
        .add("remoteUser", callerLogin)
        .add("roomKey", roomKey)
        .add("reason", reason).build().toString();
    }

    private static String buildWebRTCHangupMessage(String callerLogin, String roomKey) {
        return Json.createObjectBuilder()
        .add("type", ChannelMessagesType.WEBRTC_HANGUP)
        .add("remoteUser", callerLogin)
        .add("roomKey", roomKey).build().toString();
    }

    private static String buildRoomJoinEventMessage(String userLogin, Room room) {
        return Json.createObjectBuilder()
        .add("type", ChannelMessagesType.WEBRTC_ROOM_JOIN_EVENT)
        .add("roomKey", room.key())
        .add("roomOccupancy", room.getOccupancy())
        .add("userLogin", userLogin).build().toString();
    }

    private static String buildRoomRejectEventMessage(String userLogin, Room room) {
        return Json.createObjectBuilder()
        .add("type", ChannelMessagesType.WEBRTC_ROOM_REJECT_EVENT)
        .add("roomKey", room.key())
        .add("roomOccupancy", room.getOccupancy())
        .add("userLogin", userLogin).build().toString();
    }

    private static String buildUserOfflineMessage(String remoteUser, String roomKey) {
        return Json.createObjectBuilder()
        .add("type", ChannelMessagesType.WEBRTC_REJECT)
        .add("roomKey", roomKey)
        .add("userLogin", remoteUser)
        .add("reason", "OFFLINE").build().toString();
    }

}
