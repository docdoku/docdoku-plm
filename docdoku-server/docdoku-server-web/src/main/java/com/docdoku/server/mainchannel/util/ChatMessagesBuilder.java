package com.docdoku.server.mainchannel.util;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ChatMessagesBuilder {

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

    public static String BuildWebRTCInvitationMessage(String callerLogin, String context) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", "webRTC_invite");
        jsobj.put("remoteUser", callerLogin);
        jsobj.put("context", context);
        return jsobj.toString();
    }

    public static String BuildChatMessage(String callerLogin, String context, String message) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", "chat_message");
        jsobj.put("remoteUser", callerLogin);
        jsobj.put("sender",callerLogin);
        jsobj.put("message", message);
        jsobj.put("context",context);
        return jsobj.toString();
    }

    public static String BuildNoopMessage() throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", "noop");
        return jsobj.toString();
    }

    public static String BuildApplicationInfosMessage() throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", "application_infos");
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

    public static String BuildApiErrorMessage() {
        try {
            JSONObject jsobj = new JSONObject();
            jsobj.put("error", "api error");
            return jsobj.toString();
        } catch (JSONException ex) {
            return "api error";
        }
    }

    public static String BuildWebRTCInvitationNotSentMessage(String calleeLogin) throws JSONException {

        JSONObject jsobj = new JSONObject();
        jsobj.put("type", "webRTC_invite");
        jsobj.put("remoteUser", calleeLogin);
        jsobj.put("error", "unreachable");
        return jsobj.toString();

    }

    public static String BuildChatMessageNotSentMessage(String calleeLogin, String context) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", "chat_message");
        jsobj.put("remoteUser", calleeLogin);
        jsobj.put("error", "UNREACHABLE");
        jsobj.put("context",context);
        return jsobj.toString();
    }
}
