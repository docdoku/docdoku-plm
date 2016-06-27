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

package com.docdoku.server.mainchannel.module;


import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;


public class CollaborativeMessageEncoder implements Encoder.Text<CollaborativeMessage> {

    @Override
    public String encode(CollaborativeMessage collaborativeMessage) throws EncodeException {
        JsonObjectBuilder b = Json.createObjectBuilder()
                .add("type", collaborativeMessage.getType())
                .add("remoteUser", collaborativeMessage.getRemoteUser())
                .add("key", collaborativeMessage.getKey());
        if(collaborativeMessage.getMessageBroadcast()==null){
            b.add("messageBroadcast", JsonValue.NULL);
        } else {
            b.add("messageBroadcast", collaborativeMessage.getMessageBroadcast());
        }
        String a = b.build().toString();
        return a;

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
