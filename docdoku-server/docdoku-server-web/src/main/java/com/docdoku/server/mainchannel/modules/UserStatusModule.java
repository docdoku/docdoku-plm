package com.docdoku.server.mainchannel.modules;

import com.docdoku.server.mainchannel.MainChannelApplication;
import com.docdoku.server.mainchannel.MainChannelDispatcher;
import com.docdoku.server.mainchannel.util.ChannelMessagesType;


import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.Session;

public class UserStatusModule {

    private UserStatusModule(){
    }

    public static void onUserStatusRequestMessage(Session session, JsonObject jsobj){

        String remoteUser = jsobj.getString("remoteUser");
        // check if remoteUser has at least a session

        if(!MainChannelApplication.hasChannels(remoteUser)){
            MainChannelDispatcher.send(session, buildStatusMessage(remoteUser, "OFFLINE"));
        }else{
            MainChannelDispatcher.send(session, buildStatusMessage(remoteUser, "ONLINE"));
        }

    }

    private static String buildStatusMessage(String remoteUser, String status) {
        return Json.createObjectBuilder()
        .add("type", ChannelMessagesType.USER_STATUS)
        .add("remoteUser", remoteUser)
        .add("status", status).build().toString();
    }

}
