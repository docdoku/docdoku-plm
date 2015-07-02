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
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;


public class ChatMessageEncoder implements Encoder.Text<ChatMessage> {
    @Override
    public String encode(ChatMessage chatMessage) throws EncodeException {
        JsonObjectBuilder b = Json.createObjectBuilder()
                .add("type", chatMessage.getType())
                .add("remoteUser", chatMessage.getRemoteUser())
                .add("sender",chatMessage.getSender())
                .add("message", chatMessage.getMessage())
                .add("context",chatMessage.getContext());

        if(chatMessage.getError()!=null)
                b.add("error", chatMessage.getError());

        return b.build().toString();

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
