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

import com.docdoku.core.services.IUserManagerLocal;
import com.sun.grizzly.websockets.WebSocketEngine;
import javax.ejb.EJB;
import javax.ejb.Startup;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

@WebServlet(name = "MainChannelSocketServlet", urlPatterns = {"/mainChannelSocket"}, loadOnStartup = 1)
public class MainChannelSocketServlet extends HttpServlet {

    @EJB
    IUserManagerLocal userManager;

    private final MainChannelApplication app = new MainChannelApplication();

    @Override
    public void init(ServletConfig config) throws ServletException {
        WebSocketEngine.getEngine().register(
                config.getServletContext().getContextPath() + "/mainChannelSocket", app);

        app.setUserManager(userManager);

    }

    @Override
    public void destroy() {
        WebSocketEngine.getEngine().unregister(app);
    }
}
