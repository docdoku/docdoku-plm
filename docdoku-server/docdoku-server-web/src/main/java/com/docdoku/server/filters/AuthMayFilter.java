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

package com.docdoku.server.filters;

import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.jsf.actions.AccountBean;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthMayFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthMayFilter.class.getName());

    @Inject
    private AccountBean accountBean;

    @EJB
    private IUserManagerLocal userManager;

    @EJB
    private IAccountManagerLocal accountManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String remoteUser=httpRequest.getRemoteUser();

        if(remoteUser != null){
            try{
                FilterUtils.hookAccountBeanData(remoteUser, userManager, accountManager, accountBean);
            }catch(AccountNotFoundException e){
                LOGGER.log(Level.SEVERE, "Cannot find account for " + remoteUser);
                LOGGER.log(Level.FINEST, null, e);
            }
        }

        chain.doFilter(request, response);

    }

    @Override
    public void destroy() {
        // Nothing to do
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Nothing to do
    }
}
