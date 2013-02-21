package com.docdoku.server.mainchannel.modules;

import com.docdoku.server.mainchannel.MainChannelApplication;
import com.docdoku.server.mainchannel.MainChannelDispatcher;
import com.docdoku.server.mainchannel.MainChannelWebSocket;
import com.docdoku.server.mainchannel.util.ChannelMessagesType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.HashMap;

public class UserStatusModule {

    private UserStatusModule(){
    }

    public static void onUserStatusRequestMessage(MainChannelWebSocket socket, JSONObject jsobj) throws JSONException {

        String remoteUser = jsobj.getString("remoteUser");
        // check if remoteUser has at least a socket

        if(!MainChannelApplication.hasChannels(remoteUser)){
            MainChannelDispatcher.send(socket, buildStatusMessage(remoteUser, "OFFLINE"));
        }else{
            MainChannelDispatcher.send(socket, buildStatusMessage(remoteUser, "ONLINE"));
        }

    }

    private static String buildStatusMessage(String remoteUser, String status) throws JSONException {
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", ChannelMessagesType.USER_STATUS);
        jsobj.put("remoteUser", remoteUser);
        jsobj.put("status", status);
        return jsobj.toString();
    }

}
