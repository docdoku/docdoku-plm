package com.docdoku.server.http;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {
        pRequest.logout();
        pRequest.getSession().invalidate();
        pRequest.getSession().setAttribute("hasFail", false);
        pRequest.getSession().setAttribute("hasLogout", true);
        pResponse.sendRedirect(pRequest.getContextPath()+"/");
    }
}