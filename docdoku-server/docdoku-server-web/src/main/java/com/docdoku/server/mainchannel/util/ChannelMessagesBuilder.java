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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ChannelMessagesBuilder {

    // Peer declaration
    public static String BuildWelcomeMessage(String userLogin) {
        try {
            JSONObject jsobj = new JSONObject();
            jsobj.put("type", "listen");
            jsobj.put("text", "welcome " + userLogin);
            return jsobj.toString();
        } catch (JSONException ex) {
            return "welcome";
        }
    }

    public static String BuildJsonExMessage() {
        try {
            JSONObject jsobj = new JSONObject();
            jsobj.put("error", "JSON_EXCEPTION");
            return jsobj.toString();
        } catch (JSONException ex) {
            return "json exception";
        }
    }
}
