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

import com.docdoku.server.mainchannel.MainChannelWebSocket;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Room {

    private static final ConcurrentMap<String, Room> DB = new ConcurrentHashMap<String, Room>();

    private String keyName;
    private MainChannelWebSocket userSocket1;
    private MainChannelWebSocket userSocket2;

    public Room(String roomKey) {
        keyName = roomKey;
        put();
    }

    public MainChannelWebSocket getUserSocket1(){
        return userSocket1;
    }
    public MainChannelWebSocket getUserSocket2(){
        return userSocket2;
    }
    
    /** Retrieve a {@link com.docdoku.server.mainchannel.util.Room} instance from database */
    public static Room getByKeyName(String roomKey) {
        return DB.get(roomKey);
    }

    /** @return a {@link String} representation of this room */
    @Override
    public String toString() {
        String str = "[";
        if (userSocket1 != null) {
            str += userSocket1.getUserLogin();
        }
        if (userSocket2 != null) {
            str += ", " + userSocket2.getUserLogin();
        }
        str += "]";
        return str;
    }

    /** @return number of participant in this room */
    public int getOccupancy() {
        int occupancy = 0;
        if (userSocket1 != null) {
            occupancy += 1;
        }
        if (userSocket2 != null) {
            occupancy += 1;
        }
        return occupancy;
    }

    /** @return the name of the other participant, null if none */
    public MainChannelWebSocket getOtherUserSocket(MainChannelWebSocket userSocket) {
        if (userSocket.equals(userSocket1)) {
            return userSocket2;
        } else if (userSocket.equals(userSocket2)) {
            return userSocket1;
        } else {
            return null;
        }
    }

    /** @return true if one the participant is named as the input parameter, false otherwise */
    public boolean hasUser(String user) {

        if(user != null) {

            if(userSocket1 != null){
                if(user.equals(userSocket1.getUserLogin())){
                    return true;
                }
            }

            if(userSocket2 != null){
                if(user.equals(userSocket2.getUserLogin())){
                    return true;
                }
            }

        }
        return false;
    }

    /** @return true if one the participant is named as the input parameter, false otherwise */
    public MainChannelWebSocket getUserSocket(String user) {

        if(user != null) {

            if(userSocket1 != null){
                if(user.equals(userSocket1.getUserLogin())){
                    return userSocket1;
                }
            }

            if(userSocket2 != null){
                if(user.equals(userSocket2.getUserLogin())){
                    return userSocket2;
                }
            }

        }
        return null;
    }

    /** Removed a participant form current room */
    public void removeUser(String user) {

        if(user != null) {

            if(userSocket1 != null){
                if(user.equals(userSocket1.getUserLogin())){
                    removeUserSocket(userSocket1);
                }
            }

            if(userSocket2 != null){
                if(user.equals(userSocket2.getUserLogin())){
                    removeUserSocket(userSocket2);
                }
            }

        }

    }

    /** Add a new participant to this room
     * @return if participant is found */
    public boolean addUserSocket(MainChannelWebSocket userSocket) {

        boolean success = true;

        // avoid a user to be added in the room many times.
        if(userSocket != null){
            if(userSocket.equals(userSocket1) || userSocket.equals(userSocket2)){
                return success;
            }
        }

        if (userSocket1 == null) {
            userSocket1 = userSocket;
        } else if (userSocket2 == null) {
            userSocket2 = userSocket;
        } else {
            // room is full, shouldn't happen
            success = false;
        }

        return success;
    }

    /** Removed a participant form current room */
    public void removeUserSocket(MainChannelWebSocket userSocket) {

        if (userSocket != null && userSocket.equals(userSocket2)) {
            userSocket2 = null;
        }

        if (userSocket != null && userSocket.equals(userSocket1)) {
            if (userSocket1 != null) {
                userSocket1 = userSocket2;
                userSocket2 = null;
            } else {
                userSocket1 = null;
            }
        }

        // auto delete ?
        if (getOccupancy() > 0) {
            put();
        } else {
            delete();
        }

    }

    /**@return the key of this room. */
    public String key() {
        return keyName;
    }

    /** Store current instance into database */
    public void put() {
        DB.put(keyName, this);
    }

    /** Delete/Remove current {@link com.docdoku.server.mainchannel.util.Room} instance from database */
    public void delete() {
        if (keyName != null) {
            DB.remove(keyName);
            keyName = null;
        }
    }

    public MainChannelWebSocket getSocketForUserLogin(String userLogin){

        if (userSocket1 != null) {
            if(userLogin.equals(userSocket1.getUserLogin())){
                return userSocket1;
            }
        } else if (userSocket2 != null) {
            if(userLogin.equals(userSocket2.getUserLogin())){
                return userSocket2;
            }
        }

        return null;
    }

    public static void removeUserFromAllRoom(String callerLogin) {
        Set<Map.Entry<String, Room>> roomsEntries = new HashSet<Map.Entry<String, Room>>(DB.entrySet());
        for (Map.Entry<String, Room> entry : roomsEntries) {
            MainChannelWebSocket socket = entry.getValue().getSocketForUserLogin(callerLogin);
            if (socket != null) {
                entry.getValue().removeUserSocket(socket);
            }
        }
    }
}
