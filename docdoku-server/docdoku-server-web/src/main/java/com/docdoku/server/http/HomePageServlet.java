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

package com.docdoku.server.http;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "HomePageServlet", urlPatterns = {"/home"})
public class HomePageServlet extends HttpServlet {


    private void handleRequest(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {
        pResponse.sendRedirect(pRequest.getContextPath() + "/document-management/");
    }
    @Override
    protected void doGet(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {
        handleRequest(pRequest, pResponse);
    }

    @Override
    protected void doPost(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {
        handleRequest(pRequest, pResponse);
    }
}
