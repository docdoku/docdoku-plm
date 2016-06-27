package com.docdoku.server.filters;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FilterUtils {

    public static void authenticate(ServletRequest request){
        request.setAttribute("authenticated",true);
    }

    public static boolean isAuthenticated(ServletRequest request){
        Object authenticated = request.getAttribute("authenticated");
        if(authenticated != null){
            return (boolean) authenticated;
        }
        return false;
    }

    public static void sendUnauthorized(ServletResponse pResponse) throws IOException {
        HttpServletResponse response = (HttpServletResponse) pResponse;
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
