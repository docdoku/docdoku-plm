/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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
package com.docdoku.server.webrtc;

import com.docdoku.server.webrtc.util.Helper;
import com.docdoku.server.webrtc.util.Room;
import com.sun.grizzly.websockets.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WebRTCApplication extends WebSocketApplication {

    private static final ConcurrentMap<String, WebRTCWebSocket> channels = new ConcurrentHashMap<String, WebRTCWebSocket>();

    @Override
    public WebSocket createWebSocket(ProtocolHandler protocolHandler, WebSocketListener[] listeners) {
        return new WebRTCWebSocket(protocolHandler, listeners);
    }

    @Override
    public void onMessage(WebSocket socket, String data) {
        if (data.startsWith("token")) { // peer declaration
            int index = data.indexOf(":");
            String token = data.substring(index + 1);
            WebRTCWebSocket rtcWS = (WebRTCWebSocket) socket;
            rtcWS.setToken(token);
            channels.put(token, rtcWS);
        } else {
            WebRTCWebSocket rtcWS = (WebRTCWebSocket) socket;
            String token = rtcWS.getToken();
            String roomKey = Helper.getRoomKey(token);
            Room room = Room.getByKeyName(roomKey);
            String user = Helper.getUser(token);
            String otherUser = room.getOtherUser(user);
            String otherToken = Helper.makeToken(room, otherUser);
            send(otherToken, data);
        }
    }

    public static boolean send(String token, String message) {
        boolean success = false;
        WebRTCWebSocket rtcWS = channels.get(token);
        if (rtcWS != null) {
            rtcWS.send(message);
            success = true;
        }
        return success;
    }

    @Override
    public boolean isApplicationRequest(com.sun.grizzly.tcp.Request rqst) {
        return true;//"/webrtc".equals(rqst.getRequestURI());
    }

    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        WebRTCWebSocket rtcWS = (WebRTCWebSocket) socket;
        String token = rtcWS.getToken();
        if (token != null) {
            Room.disconnect(token);
            channels.remove(token);
        }
    }
    


    @Override
    public void onConnect(WebSocket socket) {
    }
}
