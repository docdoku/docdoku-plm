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


import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.StringReader;

/**
 * This class is used for decoding web socket payloads
 * <p>
 * Decode only json objects
 * <p>
 * Type field is mandatory
 *
 * @author Morgan Guimard
 */
public class WebSocketMessageDecoder implements Decoder.Text<WebSocketMessage> {

    @Override
    public WebSocketMessage decode(String messageAsString) throws DecodeException {
        JsonObject jsObj = Json.createReader(new StringReader(messageAsString)).readObject();
        return new WebSocketMessage(jsObj);
    }


    @Override
    public boolean willDecode(String s) {
        JsonObject jsonObject = Json.createReader(new StringReader(s)).readObject();
        return jsonObject.containsKey("type");
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        // Nothing to do
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

}
