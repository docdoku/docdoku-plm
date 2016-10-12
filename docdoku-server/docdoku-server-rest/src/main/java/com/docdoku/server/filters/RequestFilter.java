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

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Last filter in chain
 *
 */
public class RequestFilter implements Filter {

    @Inject
    private IAccountManagerLocal accountManager;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if(!FilterUtils.isAuthenticated(servletRequest)){
            FilterUtils.sendUnauthorized(servletResponse);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String remoteUser = httpRequest.getRemoteUser();

        if(remoteUser != null && !isAccountEnabled(remoteUser)){
            httpRequest.logout();
            httpRequest.getSession();
            FilterUtils.sendUnauthorized(servletResponse);
            return;
        }

        filterChain.doFilter(servletRequest,servletResponse);
    }

    private boolean isAccountEnabled(String userLogin) {
        try {
            return accountManager.isAccountEnabled(userLogin);
        } catch (AccountNotFoundException e) {
            return false;
        }
    }

    @Override
    public void destroy() {
    }

}
