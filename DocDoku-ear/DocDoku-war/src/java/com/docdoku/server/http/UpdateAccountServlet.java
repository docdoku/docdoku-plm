/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.common.Account;
import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

import com.docdoku.core.*;


public class UpdateAccountServlet extends HttpServlet {
    
    @EJB
    private IUserManagerLocal userManager;
    
    @Override
    protected void doPost(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {
        try {
            if(pRequest.getCharacterEncoding()==null)
                pRequest.setCharacterEncoding("UTF-8");
            
            HttpSession sessionHTTP = pRequest.getSession();
            Account account = (Account) sessionHTTP.getAttribute("account");
            userManager.updateAccount(pRequest.getParameter("name"),pRequest.getParameter("email"),pRequest.getLocale().getLanguage(),pRequest.getParameter("password"));
            account.setName(pRequest.getParameter("name"));
            account.setEmail(pRequest.getParameter("email")); 
            account.setLanguage(pRequest.getLocale().getLanguage());
            
            pRequest.getRequestDispatcher("/WEB-INF/editAccount.jsp").forward(pRequest, pResponse);
        } catch (Exception pEx) {
            throw new ServletException("Error while editing an account.", pEx);
        }
    }
}
