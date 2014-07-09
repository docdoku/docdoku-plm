package com.docdoku.server.http;

import com.docdoku.core.common.Account;
import com.docdoku.server.mainchannel.MainChannelApplication;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Created by docdoku on 03/06/14.
 */
public class WebSessionListener implements HttpSessionListener {

    //Notification that a session was created.
    @Override
    public void sessionCreated(HttpSessionEvent httpSessionCreatedEvent) {
    }

    //Notification that a session is about to be invalidated.
    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionDestroyedEvent) {
        HttpSession httpSession = httpSessionDestroyedEvent.getSession();
        String remoteUser = (String)httpSession.getAttribute("remoteUser");
        MainChannelApplication.sessionDestroyed(remoteUser);
    }

}