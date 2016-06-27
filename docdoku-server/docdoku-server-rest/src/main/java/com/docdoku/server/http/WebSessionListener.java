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

package com.docdoku.server.http;

import com.docdoku.server.mainchannel.MainChannelApplication;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Arthur FRIN
 * @version 1.0, 03/06/14
 * @since   V2.0
 */
public class WebSessionListener implements HttpSessionListener {
    private static final Logger LOGGER = Logger.getLogger(WebSessionListener.class.getName());

    //Notification that a session was created.
    @Override
    public void sessionCreated(HttpSessionEvent httpSessionCreatedEvent) {
        // Nothing to do
    }

    //Notification that a session is about to be invalidated.
    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionDestroyedEvent) {
        HttpSession httpSession = httpSessionDestroyedEvent.getSession();
        String remoteUser = (String)httpSession.getAttribute("remoteUser");
        // Remote User can be null on unauthenticated http session
        if(remoteUser!=null){
            LOGGER.log(Level.FINE, " [MainChannelApplication] Session destroy for a remote user.");
            MainChannelApplication.sessionDestroyed(remoteUser);
        }
    }

}