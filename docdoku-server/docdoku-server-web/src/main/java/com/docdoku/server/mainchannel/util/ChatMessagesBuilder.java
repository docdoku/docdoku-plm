/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.server.mainchannel.util;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ChatMessagesBuilder {

    // Peer declaration
    public static String BuildWelcomeMessage(String userLogin) {
        try {
            JSONObject jsobj = new JSONObject();
            jsobj.put("type", "listen");
            jsobj.put("text", "welcome " + userLogin);
            return jsobj.toString();
        } catch (JSONException ex) {
            return "welcome";
        }
    }

    // Web RTC

    public static String BuildWebRTCInvitationMessage(String callerLogin, String context, String roomKey) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.WEBRTC_INVITE);
        jsobj.put("remoteUser", callerLogin);
        jsobj.put("context", context);
        jsobj.put("roomKey", roomKey);
        return jsobj.toString();
    }

    public static String BuildWebRTCInvitationNotSentMessage(String remoteUser) throws JSONException {

        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.WEBRTC_INVITE);
        jsobj.put("remoteUser", remoteUser);
        jsobj.put("error", "unreachable");
        return jsobj.toString();

    }

    public static String BuildWebRTCAcceptMessage(String callerLogin, String roomKey) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.WEBRTC_ACCEPT);
        jsobj.put("remoteUser", callerLogin);
        jsobj.put("roomKey", roomKey);
        return jsobj.toString();
    }

    public static String BuildWebRTCRejectMessage(String callerLogin, String roomKey) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.WEBRTC_REJECT);
        jsobj.put("remoteUser", callerLogin);
        jsobj.put("roomKey", roomKey);
        return jsobj.toString();
    }

    public static String BuildWebRTCHangupMessage(String callerLogin, String roomKey) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.WEBRTC_HANGUP);
        jsobj.put("remoteUser", callerLogin);
        jsobj.put("roomKey", roomKey);
        return jsobj.toString();
    }



    // Chat

    public static String BuildChatMessage(String callerLogin, String context, String message) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.CHAT_MESSAGE);
        jsobj.put("remoteUser", callerLogin);
        jsobj.put("sender",callerLogin);
        jsobj.put("message", message);
        jsobj.put("context",context);
        return jsobj.toString();
    }

    public static String BuildChatMessageNotSentMessage(String calleeLogin, String context) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.CHAT_MESSAGE);
        jsobj.put("remoteUser", calleeLogin);
        jsobj.put("error", "UNREACHABLE");
        jsobj.put("context",context);
        return jsobj.toString();
    }

    // Others

    public static String BuildNoopMessage() throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", "noop");
        return jsobj.toString();
    }

    public static String BuildJsonExMessage() {
        try {
            JSONObject jsobj = new JSONObject();
            jsobj.put("error", "json exception");
            return jsobj.toString();
        } catch (JSONException ex) {
            return "json exception";
        }
    }

    public static String BuildOfflineStatusMessage(String remoteUser) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.USER_STATUS);
        jsobj.put("remoteUser", remoteUser);
        jsobj.put("status", "OFFLINE");
        return jsobj.toString();
    }

    public static String BuildOnlineStatusMessage(String remoteUser) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.USER_STATUS);
        jsobj.put("remoteUser", remoteUser);
        jsobj.put("status", "ONLINE");
        return jsobj.toString();
    }
}
