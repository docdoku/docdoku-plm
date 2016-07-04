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

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;


public class BasicFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if(!FilterUtils.isAuthenticated(request)){

            HttpServletRequest httpRequest = (HttpServletRequest) request;

            String authHeaderVal = httpRequest.getHeader("Authorization");

            if(authHeaderVal != null && authHeaderVal.startsWith("Basic")) {

                String authorization = httpRequest.getHeader("Authorization");
                String[] splitAuthorization = authorization.split(" ");
                if(splitAuthorization.length == 2){
                    byte[] decoded = DatatypeConverter.parseBase64Binary(splitAuthorization[1]);
                    String credentials = new String(decoded, "US-ASCII");
                    String[] splitCredentials = credentials.split(":");
                    String userLogin = splitCredentials[0];
                    String userPassword = splitCredentials[1];
                    httpRequest.getSession();
                    httpRequest.login(userLogin, userPassword);
                    FilterUtils.authenticate(request);
                }
            }
        }

        chain.doFilter(request,response);

    }

    @Override
    public void destroy() {

    }
}
