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

package com.docdoku.server.mainchannel.module;

import com.docdoku.server.mainchannel.util.ChannelMessagesType;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;


public class WebRTCMessageEncoder implements Encoder.Text<WebRTCMessage> {



    @Override
    public String encode(WebRTCMessage webRTCMessage) throws EncodeException {
            return Json.createObjectBuilder()
                    .add("type", webRTCMessage.getType())
                    .add("remoteUser", webRTCMessage.getRemoteUser())
                    .add("roomKey", webRTCMessage.getRoomKey())
                    .add("reason", webRTCMessage.getReason())
                    .add("context", webRTCMessage.getContext())
                    .add("roomOccupancy", webRTCMessage.getRoomOccupancy())
                    .add("userLogin", webRTCMessage.getUserLogin()).build().toString();

    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
