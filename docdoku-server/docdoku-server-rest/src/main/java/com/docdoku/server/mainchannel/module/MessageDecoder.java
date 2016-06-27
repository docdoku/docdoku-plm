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

package com.docdoku.server.mainchannel.module;


import com.docdoku.server.mainchannel.util.ChannelMessagesType;

import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageDecoder implements Decoder.Text<AbstractMessage>{

    @Override
    public AbstractMessage decode(String s) throws DecodeException {
        JsonObject jsObj = Json.createReader(new StringReader(s)).readObject();
        String remoteUser = jsObj.getString("remoteUser");
        String type = jsObj.getString("type");
        String context = jsObj.containsKey("context")?jsObj.getString("context"):null;
        AbstractMessage message=null;
        switch(type){

            case ChannelMessagesType.USER_STATUS:
                StatusMessage statusMsg=new StatusMessage(type, remoteUser);
                String status = jsObj.containsKey("status")?jsObj.getString("status"):null;
                statusMsg.setStatus(status);
                message=statusMsg;
                break;
            case ChannelMessagesType.CHAT_MESSAGE:
                ChatMessage chatMsg=new ChatMessage(type, remoteUser);
                String contentMessage = jsObj.containsKey("message")?jsObj.getString("message"):null;
                chatMsg.setContext(context);
                chatMsg.setMessage(contentMessage);
                message=chatMsg;
                break;
            case ChannelMessagesType.WEBRTC_INVITE:
            case ChannelMessagesType.WEBRTC_ACCEPT:
            case ChannelMessagesType.WEBRTC_REJECT:
            case ChannelMessagesType.WEBRTC_HANGUP:
            case ChannelMessagesType.WEBRTC_ANSWER:
            case ChannelMessagesType.WEBRTC_BYE:
            case ChannelMessagesType.WEBRTC_CANDIDATE:
            case ChannelMessagesType.WEBRTC_OFFER:
                WebRTCMessage webRTCMsg=new WebRTCMessage(type,remoteUser);
                String roomKey = jsObj.containsKey("roomKey")?jsObj.getString("roomKey"):null;
                String reason = jsObj.containsKey("reason")?jsObj.getString("reason"):null;
                String sdp = jsObj.containsKey("sdp")?jsObj.getString("sdp"):null;
                String id = jsObj.containsKey("id")?jsObj.getString("id"):null;
                String candidate = jsObj.containsKey("candidate")?jsObj.getString("candidate"):null;
                Integer label = jsObj.containsKey("label")?jsObj.getInt("label"):null;
                webRTCMsg.setContext(context);
                webRTCMsg.setReason(reason);
                webRTCMsg.setRoomKey(roomKey);
                webRTCMsg.setSignals(sdp,id,candidate,label);
                message=webRTCMsg;
                break;
            case ChannelMessagesType.COLLABORATIVE_CREATE:
            case ChannelMessagesType.COLLABORATIVE_INVITE:
            case ChannelMessagesType.COLLABORATIVE_COMMANDS:
            case ChannelMessagesType.COLLABORATIVE_CONTEXT:
            case ChannelMessagesType.COLLABORATIVE_EXIT:
            case ChannelMessagesType.COLLABORATIVE_GIVE_HAND:
            case ChannelMessagesType.COLLABORATIVE_JOIN:
            case ChannelMessagesType.COLLABORATIVE_KICK_USER:
            case ChannelMessagesType.COLLABORATIVE_KICK_NOT_INVITED:
            case ChannelMessagesType.COLLABORATIVE_KILL:
            case ChannelMessagesType.COLLABORATIVE_WITHDRAW_INVITATION:
                CollaborativeMessage collaborativeMessage = new CollaborativeMessage(type,remoteUser);
                String key = jsObj.containsKey("key")?jsObj.getString("key"):null;
                JsonObject messageBroadcast = jsObj.containsKey("messageBroadcast")?jsObj.getJsonObject("messageBroadcast"):null;
                collaborativeMessage.setMessageBroadcast(messageBroadcast);
                collaborativeMessage.setKey(key);
                message=collaborativeMessage;
                break;
            default :
                Logger.getLogger(MessageDecoder.class.getName()).log(Level.WARNING, "Type of message not recognized."+type);
                break;
        }
        return message;
    }

    @Override
    public boolean willDecode(String s) {
        JsonObject jsObj = Json.createReader(new StringReader(s)).readObject();
        return jsObj.containsKey("type") && jsObj.containsKey("remoteUser");
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        // Nothing to do
    }

    @Override
    public void destroy() {
        // Nothing to do
    }
}
