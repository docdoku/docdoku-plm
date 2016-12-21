/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.server.ws;

import com.docdoku.core.services.IUserManagerLocal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The aim of this class is to maintain active sockets
 *
 * @author Morgan Guimard
 * */
@ApplicationScoped
public class WebSocketSessionsManager {

    @Inject
    private IUserManagerLocal userManager;

    /**
     * Users WebSockets map, store a list af sessions for each user
     */
    private static final ConcurrentMap<String, List<Session>> CHANNELS = new ConcurrentHashMap<>();

    public boolean hasSessions(String login) {
        List<Session> sessions = getSessions(login);
        return sessions != null && !sessions.isEmpty();
    }

    public List<Session> getSessions(String userLogin) {
        return CHANNELS.get(userLogin);
    }

    public String getHolder(Session session){
        return CHANNELS.entrySet().stream().
                filter(stringListEntry -> stringListEntry.getValue().contains(session))
                .map(stringListEntry -> stringListEntry.getKey())
                .findFirst()
                .orElse(null);
    }

    public void removeSession(Session session) {

        String login = getHolder(session);

        if(null != login){
            List<Session> sessions = getSessions(login);
            if(null != sessions){
                sessions.remove(session);
                if(sessions.isEmpty()){
                    CHANNELS.remove(login);
                }
            }
        }

    }

    public void addSession(String login, Session session) {
        List<Session> sessions = getSessions(login);
        if(null == sessions){
            sessions = new ArrayList<>();
            CHANNELS.put(login,sessions);
        }
        sessions.add(session);
    }

    public boolean isAllowedToReachUser(String sender, String remoteUser){
        return userManager.hasCommonWorkspace(sender,remoteUser);
    }

    public void broadcast(String userLogin, WebSocketMessage webSocketMessage){
        List<Session> sessions = getSessions(userLogin);
        send(sessions,webSocketMessage);
    }

    public void send(List<Session> sessions, WebSocketMessage webSocketMessage){
        for(Session session : sessions){
            send(session,webSocketMessage);
        }
    }

    public void send(Session session, WebSocketMessage webSocketMessage){
        if (session != null) {
            session.getAsyncRemote().sendObject(webSocketMessage);
        }
    }
}