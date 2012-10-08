/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.server.webrtc;

import com.sun.grizzly.websockets.WebSocketEngine;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;


@WebServlet(name = "WebRTCSocketServlet", urlPatterns = {"/webRTCSocketServlet"})
public class WebRTCSocketServlet  extends HttpServlet {

    private final WebRTCApplication app = new WebRTCApplication();

    /*
    @Override
    public void init(ServletConfig config) throws ServletException {
        WebSocketEngine.getEngine().register(app);
    }*/
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        WebSocketEngine.getEngine().register(
            config.getServletContext().getContextPath() + "/webRTCSocket", app);
    }
    
    @Override
    public void destroy() {
        WebSocketEngine.getEngine().unregister(app);
    }
}