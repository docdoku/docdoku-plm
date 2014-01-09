package com.docdoku.server.mainchannel.modules;

import com.docdoku.server.mainchannel.MainChannelApplication;
import com.docdoku.server.mainchannel.MainChannelDispatcher;
import com.docdoku.server.mainchannel.util.ChannelMessagesType;

import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.Session;
import java.security.Principal;

public class ChatModule {

    private ChatModule(){
    }

    public static void onChatMessage(Session session, JsonObject jsobj) {

        String remoteUser = jsobj.getString("remoteUser");
        String message = jsobj.getString("message");
        String context = jsobj.getString("context");

        if(!MainChannelApplication.hasChannels(remoteUser)){
            MainChannelDispatcher.send(session, buildChatMessageNotSentMessage(remoteUser, context));
        }else{
            Principal userPrincipal = session.getUserPrincipal();
            String userLogin = userPrincipal.getName();
            MainChannelDispatcher.sendToAllUserChannels(userLogin,buildChatMessageACK(userLogin,remoteUser, context, message));
            MainChannelDispatcher.sendToAllUserChannels(remoteUser,buildChatMessage(userLogin, context, message));
        }

    }


    private static String buildChatMessage(String callerLogin, String context, String message) {
         return Json.createObjectBuilder()
        .add("type", ChannelMessagesType.CHAT_MESSAGE)
        .add("remoteUser", callerLogin)
        .add("sender",callerLogin)
        .add("message", message)
        .add("context",context).build().toString();
    }

    private static String buildChatMessageACK(String caller, String remoteUser, String context, String message) {
        return Json.createObjectBuilder()
        .add("type", ChannelMessagesType.CHAT_MESSAGE_ACK)
        .add("remoteUser", remoteUser)
        .add("sender",caller)
        .add("message", message)
        .add("context",context).build().toString();
    }

    private static String buildChatMessageNotSentMessage(String calleeLogin, String context) {
        return Json.createObjectBuilder()
        .add("type", ChannelMessagesType.CHAT_MESSAGE)
        .add("remoteUser", calleeLogin)
        .add("error", "UNREACHABLE")
        .add("context",context).build().toString();
    }
}
