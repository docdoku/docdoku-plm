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

package com.docdoku.server.mainchannel;

import com.docdoku.server.mainchannel.module.ChatMessage;
import com.docdoku.server.mainchannel.module.CollaborativeMessage;
import com.docdoku.server.mainchannel.module.StatusMessage;
import com.docdoku.server.mainchannel.module.WebRTCMessage;

import javax.websocket.Session;
import java.util.Collection;

public final class MainChannelDispatcher {

    private  MainChannelDispatcher(){
    }

    /* Send a message to multiple channels */
    public static void sendToAllUserChannels(String userLogin, ChatMessage message){
        if(userLogin != null && !"".equals(userLogin) && MainChannelApplication.getUserChannels(userLogin) != null) {
            Collection<Session> sessions = MainChannelApplication.getUserChannels(userLogin).values();
            for(Session session:sessions){
                send(session, message);
            }
        }
    }

    /* Send a message to multiple channels */
    public static void sendToAllUserChannels(String userLogin, WebRTCMessage message){
        if(userLogin != null && !"".equals(userLogin) && MainChannelApplication.getUserChannels(userLogin) != null) {
            Collection<Session> sessions = MainChannelApplication.getUserChannels(userLogin).values();
            for(Session session:sessions){
                send(session, message);
            }
        }
    }

    /* Send a message to multiple channels */
    public static void sendToAllUserChannels(String userLogin, CollaborativeMessage message){
        if(userLogin != null && !"".equals(userLogin) && MainChannelApplication.getUserChannels(userLogin) != null) {
            Collection<Session> sessions = MainChannelApplication.getUserChannels(userLogin).values();
            for(Session session:sessions){
                send(session, message);
            }
        }
    }


    /* Send a message to single channel */
    public static boolean send(Session session, String message){
        if (session != null) {
            session.getAsyncRemote().sendText(message);
            return true;
        }
        return false;
    }
    /* Send a message to single channel */
    public static boolean send(Session session, StatusMessage status){
        if (session != null) {
            session.getAsyncRemote().sendObject(status);
            return true;
        }
        return false;
    }

    /* Send a message to single channel */
    public static boolean send(Session session, ChatMessage message){
        if (session != null) {
            session.getAsyncRemote().sendObject(message);
            return true;
        }
        return false;
    }

    /* Send a message to single channel */
    public static boolean send(Session session, WebRTCMessage message){
        if (session != null) {
            session.getAsyncRemote().sendObject(message);
            return true;
        }
        return false;
    }

    public static boolean send(Session session, CollaborativeMessage message) {
        if (session != null) {
            session.getAsyncRemote().sendObject(message);
            return true;
        }
        return false;
    }
}