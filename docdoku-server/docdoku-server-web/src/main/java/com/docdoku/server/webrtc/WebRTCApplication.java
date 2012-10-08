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
package com.docdoku.server.webrtc;

import com.docdoku.server.webrtc.util.Helper;
import com.docdoku.server.webrtc.util.Room;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WebRTCApplication extends WebSocketApplication {

    private static final ConcurrentMap<String, WebRTCWebSocket> channels = new ConcurrentHashMap<String, WebRTCWebSocket>();

    @Override
    public WebSocket createSocket(ProtocolHandler handler, org.glassfish.grizzly.http.HttpRequestPacket requestPacket, WebSocketListener[] listeners) {
        return new WebRTCWebSocket(handler, listeners);
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
    public boolean isApplicationRequest(org.glassfish.grizzly.http.HttpRequestPacket rqst) {
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
