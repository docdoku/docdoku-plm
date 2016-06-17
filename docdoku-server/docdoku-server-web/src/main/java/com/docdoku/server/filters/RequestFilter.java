package com.docdoku.server.filters;

import javax.servlet.*;
import java.io.IOException;

/**
 * Last filter in chain
 *
 */
public class RequestFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if(FilterUtils.isAuthenticated(servletRequest)){
            filterChain.doFilter(servletRequest,servletResponse);
        }else{
            FilterUtils.sendUnauthorized(servletResponse);
        }
    }

    @Override
    public void destroy() {
    }

}
