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


import com.docdoku.server.ws.WebSocketMessage;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;


/**
 * This class is used for encoding web socket payloads
 * <p>
 * Encode only json WebSocketMessage
 *
 * @author Morgan Guimard
 */
public class WebSocketMessageEncoder implements Encoder.Text<WebSocketMessage> {

    @Override
    public String encode(WebSocketMessage WebSocketMessage) throws EncodeException {
        return WebSocketMessage.getObject().toString();
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
