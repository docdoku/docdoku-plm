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

package com.docdoku.server.mainchannel.util;

public class ChannelMessagesType {

    public static final String USER_STATUS = "USER_STATUS";

    public static final String CHAT_MESSAGE = "CHAT_MESSAGE";
    public static final String CHAT_MESSAGE_ACK = "CHAT_MESSAGE_ACK";

    public static final String WEBRTC_INVITE = "WEBRTC_INVITE";
    public static final String WEBRTC_ACCEPT = "WEBRTC_ACCEPT";
    public static final String WEBRTC_REJECT = "WEBRTC_REJECT";
    public static final String WEBRTC_HANGUP = "WEBRTC_HANGUP";
    public static final String WEBRTC_ROOM_JOIN_EVENT = "WEBRTC_ROOM_JOIN_EVENT";
    public static final String WEBRTC_ROOM_REJECT_EVENT = "WEBRTC_ROOM_REJECT_EVENT";

    public static final String WEBRTC_OFFER  = "offer";
    public static final String WEBRTC_ANSWER = "answer";
    public static final String WEBRTC_BYE = "bye";
    public static final String WEBRTC_CANDIDATE = "candidate";

    public static final String COLLABORATIVE_CREATE = "COLLABORATIVE_CREATE";
    public static final String COLLABORATIVE_INVITE = "COLLABORATIVE_INVITE";
    public static final String COLLABORATIVE_JOIN = "COLLABORATIVE_JOIN";
    public static final String COLLABORATIVE_CONTEXT = "COLLABORATIVE_CONTEXT";
    public static final String COLLABORATIVE_COMMANDS = "COLLABORATIVE_COMMANDS";
    public static final String COLLABORATIVE_EXIT = "COLLABORATIVE_EXIT";
    public static final String COLLABORATIVE_KILL = "COLLABORATIVE_KILL";
    public static final String COLLABORATIVE_GIVE_HAND = "COLLABORATIVE_GIVE_HAND";
    public static final String COLLABORATIVE_KICK_USER = "COLLABORATIVE_KICK_USER";
    public static final String COLLABORATIVE_KICK_NOT_INVITED= "COLLABORATIVE_KICK_NOT_INVITED";
    public static final String COLLABORATIVE_WITHDRAW_INVITATION = "COLLABORATIVE_WITHDRAW_INVITATION";

    private ChannelMessagesType() {
    }

}
