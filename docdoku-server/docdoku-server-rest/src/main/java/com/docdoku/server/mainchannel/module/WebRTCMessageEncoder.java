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


public class WebRTCMessageEncoder implements Encoder.Text<WebRTCMessage> {

    @Override
    public String encode(WebRTCMessage webRTCMessage) throws EncodeException {
        JsonObjectBuilder jsonObject = Json.createObjectBuilder();
        jsonObject.add("type", webRTCMessage.getType());
        if(webRTCMessage.getRemoteUser()!=null){
            jsonObject.add("remoteUser", webRTCMessage.getRemoteUser());
        }
        if(webRTCMessage.getRoomKey()!=null){
            jsonObject.add("roomKey", webRTCMessage.getRoomKey());
        }
        if(webRTCMessage.getReason()!=null){
            jsonObject.add("reason", webRTCMessage.getReason());
        }
        if(webRTCMessage.getContext()!=null){
            jsonObject.add("context", webRTCMessage.getContext());
        }
        if(webRTCMessage.getRoomOccupancy()!=null){
            jsonObject.add("roomOccupancy", webRTCMessage.getRoomOccupancy());
        }
        if(webRTCMessage.getUserLogin()!=null){
            jsonObject.add("userLogin", webRTCMessage.getUserLogin());
        }
        if(webRTCMessage.getSdp()!=null){
            jsonObject.add("sdp", webRTCMessage.getSdp());
        }
        if(webRTCMessage.getId()!=null){
            jsonObject.add("id", webRTCMessage.getId());
        }
        if(webRTCMessage.getCandidate()!=null){
            jsonObject.add("candidate", webRTCMessage.getCandidate());
        }
        if(webRTCMessage.getLabel()!=null){
            jsonObject.add("label", webRTCMessage.getLabel());
        }

        return jsonObject.build().toString();
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
