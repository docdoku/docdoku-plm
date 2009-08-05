/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009 DocDoku SARL
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

package com.docdoku.server.http;

import com.docdoku.core.entities.Account;
import com.docdoku.core.entities.Workspace;
import com.sun.appserv.security.ProgrammaticLogin;
import java.io.IOException;
import java.util.HashMap;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

import com.docdoku.core.*;
import java.util.HashSet;

public class RegisterServlet extends HttpServlet {
    
    @EJB
    private ICommandLocal commandService;
    
    @Override
    protected void doPost(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {
        try {
            if(pRequest.getCharacterEncoding()==null)
                pRequest.setCharacterEncoding("UTF-8");
            
            String login = pRequest.getParameter("login");
            String name = pRequest.getParameter("name");
            String email = pRequest.getParameter("email");
            String password = pRequest.getParameter("password");
            String language = pRequest.getLocale().getLanguage();
            
            Account account = commandService.createAccount(login,name,email,language,password);
            new ProgrammaticLogin().login(login,password,"docdokuRealm",(HttpServletRequest)pRequest,(HttpServletResponse)pResponse,true);
                
            HttpSession sessionHTTP = pRequest.getSession();
            sessionHTTP.setAttribute("account", account);
            sessionHTTP.setAttribute("administeredWorkspaces", new HashMap<String, Workspace>());
            sessionHTTP.setAttribute("regularWorkspaces", new HashSet<Workspace>());
            pRequest.getRequestDispatcher("/WEB-INF/register.jsp").forward(pRequest, pResponse);
            
        } catch (Exception pEx) {
            throw new ServletException("Error while registering a new account.", pEx);
        }
    }
}
