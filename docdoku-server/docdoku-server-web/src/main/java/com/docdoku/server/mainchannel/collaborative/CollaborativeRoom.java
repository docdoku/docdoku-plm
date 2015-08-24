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

package com.docdoku.server.mainchannel.collaborative;


import javax.json.*;
import javax.websocket.Session;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A room of Collaboration Module
 *
 * @author Arthur FRIN
 * @version 1.0, 30/06/14
 * @since   V2.0
 */
public class CollaborativeRoom {
    private static final ConcurrentMap<String, CollaborativeRoom> DB = new ConcurrentHashMap<>();
    private String key;
    private Session master;
    private List<Session> slaves;
    private List<String> pendingUsers;
    private Date creationDate;
    private JsonObjectBuilder saveJsonCommands;
    private String lastMaster;

    public CollaborativeRoom(Session master) {
        this.key = UUID.randomUUID().toString();
        this.master = master;
        this.pendingUsers = new LinkedList<>();
        this.creationDate = new Date();
        this.slaves = new LinkedList<>();
        this.lastMaster = getMasterName();
        this.saveJsonCommands = Json.createObjectBuilder();
        put();
    }

    /** Retrieve a {@link com.docdoku.server.mainchannel.collaborative.CollaborativeRoom} instance from database */
    public static CollaborativeRoom getByKeyName(String roomKey) {
        if(roomKey==null) {
            return null;
        }
        return DB.get(roomKey);
    }

    public static Set<CollaborativeRoom> getAllCollaborativeRooms() {

        return new HashSet<>(DB.values());
    }


    public String getLastMaster() {
        return lastMaster;
    }

    public void setLastMaster(String lastMaster) {
        this.lastMaster = lastMaster;
    }

    @Override
    public String toString() {
        return this.getContext().toString();
    }

    public JsonObject getContext() {
        JsonArrayBuilder contextSlaves = Json.createArrayBuilder();
        for (Session s : this.getSlaves()) {
            contextSlaves.add(s.getUserPrincipal().getName());
        }

        JsonArrayBuilder contextPendingUsers = Json.createArrayBuilder();
        for (String s : this.getPendingUsers()) {
            contextPendingUsers.add(s);
        }

        return Json.createObjectBuilder()
                .add("master", this.getMasterName())
                .add("lastMaster", this.getLastMaster())
                .add("users", contextSlaves)
                .add("pendingUsers", contextPendingUsers).build();
    }

    /** Store current instance into database */
    public void put() {
        DB.put(key, this);
    }

    /** Delete/Remove current {@link com.docdoku.server.mainchannel.util.Room} instance from database */
    public void delete() {
        if (key != null) {
            DB.remove(key);
            key = null;
        }
    }

    public Session getMaster() {
        return master;
    }

    public String getMasterName() {
        return (master==null)?"":master.getUserPrincipal().getName();
    }

    public void setMaster(Session master) {
        this.master = master;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Session> getSlaves() {
        return slaves;
    }

    public void addSlave(Session slave) {
        this.slaves.add(slave);
    }

    public boolean removeSlave(Session slave) {
        return this.slaves.remove(slave);
    }

    public List<String> getPendingUsers() {
        return pendingUsers;
    }

    public void addPendingUser(String user){
        this.pendingUsers.add(user);
    }

    public boolean removePendingUser(String user){
        return this.pendingUsers.remove(user);
    }

    public Date getCreationDate() {
        return (creationDate!=null) ? (Date) creationDate.clone() : null;
    }

    public Session findUserSession(String user){
        Session userSession = null;
        for (Session s : this.getSlaves()) {
            if (s.getUserPrincipal().getName().equals(user)) {
                userSession = s;
            }
        }
        return userSession;
    }

    public JsonObject getCommands() {
        return saveJsonCommands.build();
    }

    public void saveCommand(JsonObject command) {
        final String cameraInfosField = "cameraInfos";
        final String smartPath = "smartPath";
        final String editedObjects = "editedObjects";
        final String colourEditedMeshes = "colourEditedObjects";
        final String explode = "explode";
        final String clipping = "clipping";
        final String measures = "measures";

        if (command.containsKey(cameraInfosField)) {
            saveJsonCommands.add(cameraInfosField,command.getJsonObject(cameraInfosField));
        } else if (command.containsKey(smartPath)) {
            JsonValue path = command.getJsonArray(smartPath);
            path = ((JsonArray) path).isEmpty() ? JsonValue.NULL : path;
            saveJsonCommands.add(smartPath, path);
        } else if (command.containsKey(editedObjects)) {
            saveJsonCommands.add(editedObjects,command.getJsonArray(editedObjects));
        } else if (command.containsKey(colourEditedMeshes)) {
            saveJsonCommands.add(colourEditedMeshes,command.getBoolean(colourEditedMeshes));
        } else if (command.containsKey(explode)) {
            saveJsonCommands.add(explode,command.getString(explode));
        } else if (command.containsKey(clipping)) {
            saveJsonCommands.add(clipping,command.getString(clipping));
        }else if (command.containsKey(measures)) {
            saveJsonCommands.add(measures,command.getJsonArray(measures));
        }
    }
}