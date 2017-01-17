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

package com.docdoku.server.ws.chat;

import javax.websocket.Session;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class Room {

    private static final ConcurrentMap<String, Room> DB = new ConcurrentHashMap<>();

    private String keyName;
    private RoomSession userSession1;
    private RoomSession userSession2;

    public Room(String roomKey) {
        keyName = roomKey;
        put();
    }

    public String getUser1Login() {
        return userSession1.getLogin();
    }

    public String getUser2Login() {
        return userSession2.getLogin();
    }

    /**
     * Retrieve a {@link Room} instance from database
     */
    public static Room getByKeyName(String roomKey) {
        if (roomKey == null) {
            return null;
        }
        return DB.get(roomKey);
    }

    /**
     * @return a {@link String} representation of this room
     */
    @Override
    public String toString() {
        String str = "[";
        if (userSession1 != null) {
            str += getUser1Login();
        }
        if (userSession2 != null) {
            str += ", " + getUser2Login();
        }
        str += "]";
        return str;
    }

    /**
     * @return number of participant in this room
     */
    public int getOccupancy() {
        int occupancy = 0;
        if (userSession1 != null) {
            occupancy += 1;
        }
        if (userSession2 != null) {
            occupancy += 1;
        }
        return occupancy;
    }

    /**
     * @return the name of the other participant, null if none
     */
    public Session getOtherUserSession(Session userSession) {
        if (isUser1Session(userSession)) {
            return userSession2.getUserSession();
        } else if (isUser2Session(userSession)) {
            return userSession1.getUserSession();
        } else {
            return null;
        }
    }

    /**
     * @return true if one the participant is named as the input parameter, false otherwise
     */
    public boolean hasUser(String user) {
        if (user != null) {
            if (userSession1 != null && user.equals(getUser1Login())) {
                return true;
            }
            if (userSession2 != null && user.equals(getUser2Login())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if one the participant is named as the input parameter, false otherwise
     */
    public Session getUserSession(String user) {

        if (user != null) {
            if (userSession1 != null && user.equals(getUser1Login())) {
                return userSession1.getUserSession();
            }

            if (userSession2 != null && user.equals(getUser2Login())) {
                return userSession2.getUserSession();
            }

        }
        return null;
    }


    /**
     * Add a new participant to this room
     *
     * @return if participant is found
     */
    public boolean addUserSession(Session userSession, String login) {
        boolean success = true;

        // avoid a user to be added in the room many times.
        if (userSession != null && (isUser1Session(userSession) || isUser2Session(userSession))) {
            return true;
        }

        if (userSession1 == null) {
            userSession1 = new RoomSession(login, userSession);
        } else if (userSession2 == null) {
            userSession2 = new RoomSession(login, userSession);
        } else {
            // room is full, shouldn't happen
            success = false;
        }

        return success;
    }

    private boolean isUser1Session(Session userSession) {
        return userSession != null && userSession1 != null && userSession.equals(userSession1.getUserSession());
    }


    private boolean isUser2Session(Session userSession) {
        return userSession != null && userSession2 != null && userSession.equals(userSession2.getUserSession());
    }

    /**
     * Removed a participant form current room
     */
    public void removeSession(Session userSession) {

        if (isUser2Session(userSession)) {
            userSession2 = null;
        }

        if (isUser1Session(userSession)) {
            // User session 2 becomes user session 1
            if (userSession2 != null) {
                userSession1 = userSession2;
                userSession2 = null;
            } else {
                userSession1 = null;
            }
        }

        // auto delete ?
        if (getOccupancy() > 0) {
            put();
        } else {
            delete();
        }

    }

    /**
     * @return the key of this room.
     */
    public String key() {
        return keyName;
    }

    /**
     * Store current instance into database
     */
    public void put() {
        DB.put(keyName, this);
    }

    /**
     * Delete/Remove current {@link Room} instance from database
     */
    public void delete() {
        if (keyName != null) {
            DB.remove(keyName);
            keyName = null;
        }
    }

    public RoomSession getRoomSessionForUserLogin(String userLogin) {
        if (userSession1 != null && userLogin.equals(getUser1Login())) {
            return userSession1;
        } else if (userSession2 != null && userLogin.equals(getUser2Login())) {
            return userSession2;
        }

        return null;
    }

    public Session getSessionForUserLogin(String userLogin) {
        RoomSession roomSession = getRoomSessionForUserLogin(userLogin);
        if (roomSession != null) {
            return roomSession.getUserSession();
        }
        return null;
    }

    public static void removeUserFromAllRoom(String callerLogin) {
        Set<Map.Entry<String, Room>> roomsEntries = new HashSet<>(DB.entrySet());
        for (Map.Entry<String, Room> entry : roomsEntries) {
            Session session = entry.getValue().getSessionForUserLogin(callerLogin);
            if (session != null) {
                entry.getValue().removeSession(session);
            }
        }
    }
}