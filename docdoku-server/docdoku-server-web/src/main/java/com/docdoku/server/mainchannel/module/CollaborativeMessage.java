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



public class CollaborativeMessage extends AbstractMessage{

    private String key;
    private String messageBroadcast;

    public CollaborativeMessage(String type, String remoteUser) {
        super(type,remoteUser);
    }

    public CollaborativeMessage(String type, String key, String messageBroadcast, String remoteUser) {
        super(type,remoteUser);
        this.messageBroadcast = messageBroadcast;
        this.key = key;
    }

    public String getMessageBroadcast() {
        return messageBroadcast;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setMessageBroadcast(String messageBroadcast) {
        this.messageBroadcast = messageBroadcast;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }
}
