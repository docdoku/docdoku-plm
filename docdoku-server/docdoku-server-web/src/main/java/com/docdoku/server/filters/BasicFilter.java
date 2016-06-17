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
