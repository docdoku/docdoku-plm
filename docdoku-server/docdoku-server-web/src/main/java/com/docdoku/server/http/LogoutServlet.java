package com.docdoku.server.http;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {
        pRequest.logout();
        pRequest.getSession().invalidate();
        HttpSession newSession = pRequest.getSession(true);
        newSession.setAttribute("hasFail", false);
        newSession.setAttribute("hasLogout", true);
        pResponse.sendRedirect(pRequest.getContextPath()+"/");
    }
}