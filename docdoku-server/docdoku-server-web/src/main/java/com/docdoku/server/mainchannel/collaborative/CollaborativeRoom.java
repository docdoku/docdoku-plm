package com.docdoku.server.mainchannel.collaborative;

import javax.json.*;
import javax.websocket.Session;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by docdoku on 30/06/14.
 */
public class CollaborativeRoom {

    private static final ConcurrentMap<String, CollaborativeRoom> DB = new ConcurrentHashMap<>();

    private String key;
    private Session master;
    private List<Session> slaves;
    private List<String> pendingUsers;
    private Date creationDate;
    private JsonObject sceneInfos;
    private String instancesId;

    public CollaborativeRoom(Session master, JsonObject sceneInfos) {
        this.key = UUID.randomUUID().toString();
        this.master = master;
        this.pendingUsers = new LinkedList<>();
        this.creationDate = new Date();
        this.slaves = new LinkedList<>();
        this.sceneInfos = sceneInfos;
        put();
    }

    /** Retrieve a {@link com.docdoku.server.mainchannel.util.Room} instance from database */
    public static CollaborativeRoom getByKeyName(String roomKey) {
        if(roomKey==null)
            return null;
        return DB.get(roomKey);
    }

    public static void removeSessionFromCollaborativeRoom(Session userSession) {
        Set<Map.Entry<String, CollaborativeRoom>> roomsEntries = new HashSet<>(DB.entrySet());
        for (Map.Entry<String, CollaborativeRoom> entry : roomsEntries) {
            CollaborativeRoom room = entry.getValue();
            if (room.getSlaves().contains(userSession)){
                CollaborativeRoomController.processExit(userSession,room);
            }
        }
    }

    public JsonObject getSceneInfos() {
        return sceneInfos;
    }

    public void setSceneInfos(JsonObject sceneInfos) {
        this.sceneInfos = sceneInfos;
    }

    public String toString() {
        return this.getContext().toString();
    }

    public JsonObject getContext() {
        JsonArrayBuilder slaves = Json.createArrayBuilder();
        for (Iterator<Session> iter = this.getSlaves().listIterator(); iter.hasNext(); ) {
            Session s = iter.next();
            slaves.add(s.getUserPrincipal().getName());
        }

        JsonArrayBuilder pendingUsers = Json.createArrayBuilder();
        for (Iterator<String> iter = this.getPendingUsers().listIterator(); iter.hasNext(); ) {
            String s = iter.next();
            pendingUsers.add(s);
        }

        return Json.createObjectBuilder()
                .add("master", this.getMaster().getUserPrincipal().getName())
                .add("users", slaves)
                .add("pendingUsers", pendingUsers).build();
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
        return creationDate;
    }

    public Session findUserSession(String user){
        Session userSession = null;
        for (Iterator<Session> iter = this.getSlaves().listIterator(); iter.hasNext(); ) {
            Session s = iter.next();
            if (s.getUserPrincipal().getName().equals(user)){
                userSession = s;
            }
        }
        return userSession;
    }
}
