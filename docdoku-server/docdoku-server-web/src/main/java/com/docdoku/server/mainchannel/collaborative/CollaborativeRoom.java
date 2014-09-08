package com.docdoku.server.mainchannel.collaborative;

import com.docdoku.server.mainchannel.module.CollaborativeMessage;
import org.apache.log4j.Level;

import javax.json.*;
import javax.websocket.Session;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * Created by docdoku on 30/06/14.
 */
public class CollaborativeRoom {

    private static final ConcurrentMap<String, CollaborativeRoom> DB = new ConcurrentHashMap<>();
    private final static Logger LOGGER = Logger.getLogger(CollaborativeRoom.class.getName());
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
                CollaborativeRoomController.processExit(userSession,userSession.getUserPrincipal().getName(),room);
            }
            if (room.getMaster()==userSession){
                CollaborativeRoomController.processExit(userSession,userSession.getUserPrincipal().getName(),room);
            }
        }
    }

    public String getLastMaster() {
        return lastMaster;
    }

    public void setLastMaster(String lastMaster) {
        this.lastMaster = lastMaster;
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
                .add("master", this.getMasterName())
                .add("lastMaster", this.getLastMaster())
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

    public JsonObject getCommands() {
        return saveJsonCommands.build();
    }

    public void saveCommand(JsonObject command) {
        if (command.containsKey("cameraInfos")) {
            saveJsonCommands.add("cameraInfos",command.getJsonObject("cameraInfos"));
        } else if (command.containsKey("smartPath")) {
            JsonArray path = command.getJsonArray("smartPath");
            if(path.size()==0){
                saveJsonCommands.add("smartPath", JsonValue.NULL);
            } else {
                saveJsonCommands.add("smartPath", path);
            }
        } else if (command.containsKey("editedMeshes")) {
            saveJsonCommands.add("editedMeshes",command.getJsonArray("editedMeshes"));
        } else if (command.containsKey("colourEditedMeshes")) {
            saveJsonCommands.add("colourEditedMeshes",command.getBoolean("colourEditedMeshes"));
        } else if (command.containsKey("explode")) {
            saveJsonCommands.add("explode",command.getString("explode"));
        } else if (command.containsKey("clipping")) {
            saveJsonCommands.add("clipping",command.getString("clipping"));
        }
    }
}
