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

import com.docdoku.server.mainchannel.util.ChatMessagesBuilder;
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

        MainChannelWebSocket ws = (MainChannelWebSocket) socket;

        // peer declaration
        if (data.startsWith("listen")) {

            int index = data.indexOf(":");
            String callerLogin = data.substring(index + 1);
            
            if(callerLogin != null){
                
                ws.setUserLogin(callerLogin);
                channels.put(callerLogin, ws);
                
                send(callerLogin, ChatMessagesBuilder.BuildWelcomeMessage(callerLogin));
                log(callerLogin + " is listening");
                logChannels();
                
            } else {
                
                log("api error");
                send(callerLogin, ChatMessagesBuilder.BuildApiErrorMessage());
                
            }

        } // Parse a JSON Message
        else {

            String callerLogin = ws.getUserLogin();

            try {

                JSONObject jsobj = new JSONObject(data);

                String type = jsobj.getString("type");

                if ("webRTC_invite".equals(type)) {                
                    
                    String calleeLogin = jsobj.getString("callee");
                    String context = jsobj.getString("context");

                    boolean sent = send(calleeLogin, ChatMessagesBuilder.BuildWebRTCInvitationMessage(callerLogin, context));
                    if(!sent){
                        send(callerLogin, ChatMessagesBuilder.BuildWebRTCInvitationNotSentMessage(callerLogin));
                    }

                } else if ("chat_message".equals(type)) {
                   
                    String calleeLogin = jsobj.getString("callee");
                    String message = jsobj.getString("message");
                    String context = jsobj.getString("context");

                    boolean sent = send(calleeLogin, ChatMessagesBuilder.BuildChatMessage(callerLogin, context , message));

                    if(!sent){
                        send(callerLogin, ChatMessagesBuilder.BuildChatMessageNotSentMessage(calleeLogin, context));
                    }

                } else if ("application_infos".equals(type)) {

                    send(callerLogin, ChatMessagesBuilder.BuildApplicationInfosMessage());

                } else {

                    send(callerLogin, ChatMessagesBuilder.BuildNoopMessage());
                }

            } catch (JSONException ex) {

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
        
        log("closeWebSocket for " + callerLogin);
       
        if (callerLogin != null) {
            channels.remove(callerLogin);
        }
        
        logChannels();
        
    }

    public static void log(String message) {
        
        Logger.getLogger(MainChannelApplication.class.getName()).log(Level.WARNING, "DEBUG WEBSOCKET : "+message);
        
    }

    @Override
    public void onConnect(WebSocket socket) {
        
        log("onConnect");
        
        logChannels();
    
    }

    private void logChannels() {
    
        log("Channels count : " + channels.size());
    
    }

    private boolean callerCanReachCallee(String caller, String callee){

        // TODO : Implement minimum of security
        // allow chat an invite messages only if caller and callee have a common workspace
        // -> write JPQL named query for perfs

        return true ;
    }

}
