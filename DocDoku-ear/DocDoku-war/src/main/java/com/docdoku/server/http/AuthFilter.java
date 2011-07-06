/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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

import com.docdoku.core.common.Workspace;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;

import com.docdoku.core.common.Account;
import com.docdoku.core.services.AccountNotFoundException;
import com.docdoku.core.services.IUserManagerLocal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AuthFilter implements Filter {

    @EJB
    private IUserManagerLocal userManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (httpRequest.getCharacterEncoding() == null) {
            httpRequest.setCharacterEncoding("UTF-8");
        }

        HttpSession sessionHTTP = httpRequest.getSession();
        Account account = (Account) sessionHTTP.getAttribute("account");

        String login = request.getParameter("login");
        String password = request.getParameter("password");
        if ("Login".equals(request.getParameter("auth"))) {
            try {
                httpRequest.login(login, password);

                account = userManager.getAccount(login);
                sessionHTTP.setAttribute("account", account);

                Map<String, Workspace> administeredWorkspaces = new HashMap<String, Workspace>();
                for (Workspace wks : userManager.getAdministratedWorkspaces()) {
                    administeredWorkspaces.put(wks.getId(), wks);
                }

                sessionHTTP.setAttribute("administeredWorkspaces", administeredWorkspaces);

                Set<Workspace> regularWorkspaces = new HashSet<Workspace>();
                regularWorkspaces.addAll(Arrays.asList(userManager.getWorkspaces()));
                regularWorkspaces.removeAll(administeredWorkspaces.values());
                sessionHTTP.setAttribute("regularWorkspaces", regularWorkspaces);

                sessionHTTP.removeAttribute("selectedWorkspace");
                chain.doFilter(request, response);
            } catch (AccountNotFoundException pANFEx) {
                throw new ServletException("Error while fetching your account.", pANFEx);
            } catch (Exception pEx) {
                throw new ServletException("Unexpected error: unable to perform authentication", pEx);
            }
        } else if (account == null) {
            httpRequest.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
        } else {
            chain.doFilter(request, response);
        }

    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }
}
