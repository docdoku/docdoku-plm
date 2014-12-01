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