package com.docdoku.server.webrtc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

public class Helper {


    /** Used to generate a random room number */
    public static String generateRandom(int len) {
        String generated = "";
        for (int i = 0; i < len; i++) {
            int index = ((int) Math.round(Math.random() * 10)) % 10;
            generated += "0123456789".charAt(index);
        }
        return generated;
    }

    public static String sanitize(String key) {
        return key.replace("[^a-zA-Z0-9\\-]", "-");
    }

    /**@return a token for a given room instance and a participant */
    public static String makeToken(Room room, String user) {
        return room.key() + "/" + user;
    }

    /**@return a token for a given room key and a participant */
    public static String makeToken(String roomKey, String user) {
        return roomKey + "/" + user;
    }

    /**Check if the token in parameter corresponds to an existent room that has a participant identified in the token 
     * @return true if token is valid, false otherwise. */
    public static boolean isValidToken(String token) {
        boolean valid = false;
        Room room = Room.getByKeyName(getRoomKey(token));
        String user = getUser(token);
        if (room != null && room.hasUser(user)) {
            valid = true;
        }
        return valid;
    }

    /** @return room key from the token parameter */
    public static String getRoomKey(String token) {
        String roomKey = null;
        if (token != null) {
            String[] values = token.split("/");
            if (values != null && values.length > 0) {
                roomKey = values[0];
            }
        }
        return roomKey;
    }

    /** @return user from the token parameter */
    public static String getUser(String token) {
        String user = null;
        if (token != null) {
            String[] values = token.split("/");
            if (values != null && values.length > 1) {
                user = values[1];
            }
        }
        return user;
    }

    public static String makePCConfig(String stunServer) {
        if (stunServer != null && !stunServer.equals("")) {
            return "STUN " + stunServer;
        } else {
            return "STUN stun.l.google.com:19302";
        }
    }

    /** Create a {@link Map} from a {@link String} representing an URL query */
    public static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    /** Create a {@link String} from an {@link InputStream} */
    public static String getStringFromStream(InputStream input) {
        String output = null;
        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(input, writer);
            output = writer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

}
