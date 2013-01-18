/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.server.mainchannel;

import com.docdoku.server.mainchannel.util.ChannelMessagesType;
import com.docdoku.server.mainchannel.util.ChatMessagesBuilder;
import com.docdoku.server.mainchannel.util.Room;
import com.sun.grizzly.websockets.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class MainChannelApplication extends WebSocketApplication {

    private static final ConcurrentMap<String, MainChannelWebSocket> channels = new ConcurrentHashMap<String, MainChannelWebSocket>();

    @Override
    public WebSocket createWebSocket(ProtocolHandler protocolHandler, WebSocketListener[] listeners) {
        return new MainChannelWebSocket(protocolHandler, listeners);
    }

    @Override
    public void onMessage(WebSocket socket, String data) {

        // Process the signaling message

        MainChannelWebSocket ws = (MainChannelWebSocket) socket;

        // Peer declaration : listen:username
        if (data.startsWith("listen") && ws.getUserLogin() == null) {

            String[] dataSplit = data.split(":");

            if(dataSplit.length == 2){

                String callerLogin = dataSplit[1];

                if(callerLogin != null){

                    ws.setUserLogin(callerLogin);
                    channels.put(callerLogin, ws);
                    send(callerLogin, ChatMessagesBuilder.BuildWelcomeMessage(callerLogin));

                } else {
                    send(callerLogin, ChatMessagesBuilder.BuildApiErrorMessage());
                }
            }


        }

        // Parse a JSON Message and switch on message.type
        else {

            String callerLogin = ws.getUserLogin();

            try {

                log("message from "+callerLogin+ " : "+data);

                JSONObject jsobj = new JSONObject(data);
                String type = jsobj.getString("type");

                // Web RTC Messages
                if (ChannelMessagesType.WEBRTC_INVITE.equals(type)) {

                    String remoteUser = jsobj.getString("remoteUser");
                    String context = jsobj.getString("context");

                    log("Create new room : "+callerLogin + "-" + remoteUser);

                    Room room = Room.getByKeyName(callerLogin + "-" + remoteUser);

                    if(room == null)
                        room = new Room(callerLogin + "-" + remoteUser);

                    room.addUser(callerLogin);

                    boolean sent = send(remoteUser, ChatMessagesBuilder.BuildWebRTCInvitationMessage(callerLogin, context, room.key()));

                    if(!sent){
                        send(callerLogin, ChatMessagesBuilder.BuildWebRTCInvitationNotSentMessage(remoteUser));
                    }

                }

                else if (ChannelMessagesType.WEBRTC_ACCEPT.equals(type)) {

                    String remoteUser = jsobj.getString("remoteUser");
                    String roomKey = jsobj.getString("roomKey");

                    log("webRTC_accept get Room : "+roomKey);

                    Room room = Room.getByKeyName(roomKey);

                    if(room != null){
                        room.addUser(callerLogin);
                        boolean sent = send(remoteUser, ChatMessagesBuilder.BuildWebRTCAcceptMessage(callerLogin,room.key()));
                    }else{
                        // TODO : send remote the room doesn't exist
                    }


                }

                else if (ChannelMessagesType.WEBRTC_REJECT.equals(type)) {

                    String remoteUser = jsobj.getString("remoteUser");
                    String roomKey = jsobj.getString("roomKey");

                    log("webRTC_reject get Room : "+roomKey);

                    Room room = Room.getByKeyName(roomKey);

                    if(room != null){
                        boolean sent = send(remoteUser, ChatMessagesBuilder.BuildWebRTCRejectMessage(callerLogin,room.key()));
                    }else{
                        // nothing to do ...
                    }

                }

                else if (ChannelMessagesType.WEBRTC_HANGUP.equals(type)) {

                    String remoteUser = jsobj.getString("remoteUser");
                    String roomKey = jsobj.getString("roomKey");

                    Room room = Room.getByKeyName(roomKey);

                    if(room != null){
                        room.removeUser(callerLogin);
                        boolean sent = send(remoteUser, ChatMessagesBuilder.BuildWebRTCHangupMessage(callerLogin,room.key()));
                    }else{
                        // nothing to do
                    }

                }

                // webRTC P2P signaling messages
                // forward them to remote peer in the room

                else if (ChannelMessagesType.WEBRTC_ANSWER.equals(type)
                       || ChannelMessagesType.WEBRTC_OFFER.equals(type)
                       || ChannelMessagesType.WEBRTC_CANDIDATE.equals(type)
                       || ChannelMessagesType.WEBRTC_BYE.equals(type)) {


                    String roomKey = jsobj.getString("roomKey");
                    Room room = Room.getByKeyName(roomKey);

                    if(room != null){

                        if(room.hasUser(callerLogin)){

                            String remoteUser = room.getOtherUser(callerLogin);

                            if(remoteUser != null){

                                send(remoteUser,data);

                            }else{
                                // tell the user the room is empty ?
                            }
                        } else{
                            // tell the user he's not in the room ?
                        }
                    }else{
                        // tell the user the room doesn't exists ?
                    }

                }

                // Chat Messages
                else if (ChannelMessagesType.CHAT_MESSAGE.equals(type)) {
                   
                    String remoteUser = jsobj.getString("remoteUser");
                    String message = jsobj.getString("message");
                    String context = jsobj.getString("context");

                    boolean sent = send(remoteUser, ChatMessagesBuilder.BuildChatMessage(callerLogin, context , message));

                    if(!sent){
                        send(callerLogin, ChatMessagesBuilder.BuildChatMessageNotSentMessage(remoteUser, context));
                    }

                }

                // No operation found
                else {
                    send(callerLogin, ChatMessagesBuilder.BuildNoopMessage());
                }

            } catch (JSONException ex) {
                // send json exception to client
                log("JSONException on message " + ex.getMessage());
                send(callerLogin, ChatMessagesBuilder.BuildJsonExMessage());
            }

        }

    }

    private static boolean send(String userLogin, String message) {
        
        boolean success = false;
        
        MainChannelWebSocket ws = channels.get(userLogin);
        
        if (ws != null) {
            ws.send(message);
            success = true;
        }
        
        if (success) {
            log("Message sent to "+userLogin+" : " + message);
        } else {
            log("Message not sent to "+userLogin+" : " + message);
        }
        
        return success;
    }

    @Override
    public boolean isApplicationRequest(com.sun.grizzly.tcp.Request rqst) {
        return true;
    }

    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        
        MainChannelWebSocket ws = (MainChannelWebSocket) socket;
        String callerLogin = ws.getUserLogin();
        
        //log("closeWebSocket for " + callerLogin);

        // TODO : remove user from Eventually rooms.

        if (callerLogin != null) {
            channels.remove(callerLogin);
            Room.removeUserFromAllRoom(callerLogin);
        }
        //logChannels();
    }

    @Override
    public void onConnect(WebSocket socket) {
        //log("onConnect");
        //logChannels();
    }

    private void logChannels() {
        //log("Channels count : " + channels.size());
        //log("Rooms count : " + Room.getDB().size());
    }

    // TODO : implement functions :


    private boolean isRemoteReachable(String remoteUser){
        return channels.containsKey(remoteUser);
    }

    private boolean callerCanReachCallee(String caller, String callee){

        // TODO : Implement minimum of security
        // TODO : allow chat and video messages only if caller and callee have a common workspace
        // TODO :-> write JPQL named query for perfs

        return true ;
    }

    public static void log(String message) {
        Logger.getLogger(MainChannelApplication.class.getName()).log(Level.WARNING, "DEBUG WEBSOCKET : "+message);
    }

}
