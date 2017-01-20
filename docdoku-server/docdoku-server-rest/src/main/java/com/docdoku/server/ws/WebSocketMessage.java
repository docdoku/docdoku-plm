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

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.Serializable;

/**
 * This class is a container for a WebSocket message
 *
 * @author Morgan Guimard
 */
public class WebSocketMessage implements Serializable {

    protected JsonObject object;

    private final static String DISCRIMINATOR_FIELD = "type";

    public WebSocketMessage(JsonObject object) {
        this.object = object;
    }

    public JsonObject getObject() {
        return object;
    }

    public void setObject(JsonObject object) {
        this.object = object;
    }

    public String getType(){
        return object.getString(DISCRIMINATOR_FIELD);
    }

    public String getString(String key){

        if(!object.containsKey(key)){
            return null;
        }

        JsonValue jsonValue = object.get(key);

        if(jsonValue.getValueType().equals(JsonValue.ValueType.STRING)){
            return object.getString(key);
        }

        return null;
    }

    public JsonObject getJsonObject(String key){
        if(!object.containsKey(key)){
            return null;
        }

        JsonValue jsonValue = object.get(key);

        if(jsonValue.getValueType().equals(JsonValue.ValueType.OBJECT)){
            return object.getJsonObject(key);
        }

        return null;
    }

}