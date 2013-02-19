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

package com.docdoku.server.mainchannel;

import java.util.Collection;
import java.util.HashMap;

public class MainChannelDispatcher {

    private  MainChannelDispatcher(){
    }

    /* Send a message to multiple channels */
    public static void sendToAllUserChannels(String userLogin, String message){

        if(userLogin != null && !userLogin.equals("") ){

            if(MainChannelApplication.getUserChannels(userLogin) != null) {

                Collection<MainChannelWebSocket> sockets = MainChannelApplication.getUserChannels(userLogin).values();

                for(MainChannelWebSocket socket:sockets){
                    send(socket, message);
                }

            }

        }

    }

    /* Send a message to single channel */
    public static boolean send(MainChannelWebSocket socket, String message){

        if (socket != null) {
            socket.send(message);
            return true;
        }

        return false;

    }


}
