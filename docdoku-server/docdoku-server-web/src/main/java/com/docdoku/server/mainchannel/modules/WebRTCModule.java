package com.docdoku.server.mainchannel.modules;

import com.docdoku.server.mainchannel.MainChannelApplication;
import com.docdoku.server.mainchannel.MainChannelDispatcher;
import com.docdoku.server.mainchannel.MainChannelWebSocket;
import com.docdoku.server.mainchannel.util.ChannelMessagesBuilder;
import com.docdoku.server.mainchannel.util.ChannelMessagesType;
import com.docdoku.server.mainchannel.util.Room;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class WebRTCModule {

    public static void onWebRTCInviteMessage(MainChannelWebSocket ws, JSONObject jsobj) throws JSONException {

        String callerLogin = ws.getUserLogin();

        String remoteUser = jsobj.getString("remoteUser");
        String context = jsobj.getString("context");

        String roomKey = callerLogin + "-" + remoteUser;

        if(! MainChannelApplication.hasChannels(remoteUser)){
            MainChannelDispatcher.send(ws, buildUserOfflineMessage(remoteUser,roomKey));
            return ;
        }

        Room room = Room.getByKeyName(roomKey);

        if (room == null) room = new Room(roomKey);
        //else :  multiple invitations, caller is spamming or something goes wrong.

        // the room is ready to receive user sockets.

        // add the caller socket in the room
        room.addUserSocket(ws);

        // send room join event to caller socket (single channel)
        MainChannelDispatcher.send(ws, buildRoomJoinEventMessage(ws.getUserLogin(), room));

        // send invitation to the remote user sockets (all channels)
        MainChannelDispatcher.sendToAllUserChannels(remoteUser, buildWebRTCInvitationMessage(callerLogin, context, roomKey));

    }

    public static void onWebRTCAcceptMessage(MainChannelWebSocket ws, JSONObject jsobj) throws JSONException {

        String remoteUser = jsobj.getString("remoteUser");
        String roomKey = jsobj.getString("roomKey");

        Room room = Room.getByKeyName(roomKey);

        if (room != null && room.hasUser(remoteUser)) {

            room.addUserSocket(ws);

            // send room join event to caller (all channels to remove invitations if any)
            MainChannelDispatcher.sendToAllUserChannels(ws.getUserLogin(),buildRoomJoinEventMessage(ws.getUserLogin(), room));

            // send room join event to the other user in room
            MainChannelWebSocket otherSocket = room.getOtherUserSocket(ws);

            if(otherSocket != null){
                MainChannelDispatcher.send(otherSocket, buildWebRTCAcceptMessage(ws.getUserLogin(),room.key()));
            }

        }

    }

    public static void onWebRTCRejectMessage(MainChannelWebSocket ws, JSONObject jsobj) throws JSONException {

        String remoteUser = jsobj.getString("remoteUser");
        String roomKey = jsobj.getString("roomKey");
        String reason = jsobj.getString("reason");

        Room room = Room.getByKeyName(roomKey);

        if (room != null) {

            // send "room reject event" to caller, to remove invitations in other tabs if any
            MainChannelDispatcher.sendToAllUserChannels(ws.getUserLogin(), buildRoomRejectEventMessage(ws.getUserLogin(), room));

            MainChannelWebSocket otherSocket = room.getUserSocket(remoteUser);
            if(otherSocket != null){
                MainChannelDispatcher.send(otherSocket, buildWebRTCRejectMessage(ws.getUserLogin(), room.key(), reason));
            }

        }

    }

    public static void onWebRTCHangupMessage(MainChannelWebSocket ws, JSONObject jsobj) throws JSONException {

        String remoteUser = jsobj.getString("remoteUser");
        String roomKey = jsobj.getString("roomKey");

        Room room = Room.getByKeyName(roomKey);

        if (room != null) {

            MainChannelWebSocket otherSocket = room.getOtherUserSocket(ws);
            room.removeUserSocket(ws);

            MainChannelDispatcher.send(otherSocket, buildWebRTCHangupMessage(ws.getUserLogin(), room.key()));

        }

    }

    // webRTC P2P signaling messages
    // These messages are forwarded to the remote peer(s) in the room
    public static void onWebRTCSignalingMessage(MainChannelWebSocket ws, JSONObject jsobj, String data) throws JSONException {

        String roomKey = jsobj.getString("roomKey");
        Room room = Room.getByKeyName(roomKey);

        if (room != null) {

            if (room.hasUser(ws.getUserLogin())) {

                // forward the message to the other peer
                MainChannelWebSocket otherSocket = room.getOtherUserSocket(ws);

                // on bye message, remove the user from the room
                if(jsobj.getString("type").equals(ChannelMessagesType.WEBRTC_BYE)){
                    System.out.println("#### "+ws.getUserLogin()+" said bye !");
                    room.removeUserSocket(ws);
                }

                if (otherSocket != null) {
                    MainChannelDispatcher.send(otherSocket, data);
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


    // Web RTC
    private static String buildWebRTCInvitationMessage(String callerLogin, String context, String roomKey) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.WEBRTC_INVITE);
        jsobj.put("remoteUser", callerLogin);
        jsobj.put("context", context);
        jsobj.put("roomKey", roomKey);
        return jsobj.toString();
    }

    private static String buildWebRTCAcceptMessage(String callerLogin, String roomKey) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.WEBRTC_ACCEPT);
        jsobj.put("remoteUser", callerLogin);
        jsobj.put("roomKey", roomKey);
        return jsobj.toString();
    }

    private static String buildWebRTCRejectMessage(String callerLogin, String roomKey, String reason) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.WEBRTC_REJECT);
        jsobj.put("remoteUser", callerLogin);
        jsobj.put("roomKey", roomKey);
        jsobj.put("reason", reason);
        return jsobj.toString();
    }

    private static String buildWebRTCHangupMessage(String callerLogin, String roomKey) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.WEBRTC_HANGUP);
        jsobj.put("remoteUser", callerLogin);
        jsobj.put("roomKey", roomKey);
        return jsobj.toString();
    }

    private static String buildRoomJoinEventMessage(String userLogin, Room room)  throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.WEBRTC_ROOM_JOIN_EVENT);
        jsobj.put("roomKey", room.key());
        jsobj.put("roomOccupancy", room.getOccupancy());
        jsobj.put("userLogin", userLogin);
        return jsobj.toString();
    }

    private static String buildRoomRejectEventMessage(String userLogin, Room room)  throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.WEBRTC_ROOM_REJECT_EVENT);
        jsobj.put("roomKey", room.key());
        jsobj.put("roomOccupancy", room.getOccupancy());
        jsobj.put("userLogin", userLogin);
        return jsobj.toString();
    }

    private static String buildUserOfflineMessage(String remoteUser, String roomKey)  throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.WEBRTC_REJECT);
        jsobj.put("roomKey", roomKey);
        jsobj.put("userLogin", remoteUser);
        jsobj.put("reason", "OFFLINE");
        return jsobj.toString();
    }

}
