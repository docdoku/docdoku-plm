package com.docdoku.server.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;

public class UserRoleRequestWrapper extends HttpServletRequestWrapper {

    String user;
    String role = null;
    HttpServletRequest request;

    public UserRoleRequestWrapper(String user, String role, HttpServletRequest request) {
        super(request);
        this.user = user;
        this.role = role;
        this.request = request;
    }

    @Override
    public boolean isUserInRole(String role) {
        if (role == null) {
            return this.request.isUserInRole(role);
        }
        return role.equals(this.role);
    }

    @Override
    public Principal getUserPrincipal() {
        if (this.user == null) {
            return request.getUserPrincipal();
        }

        return new Principal() {
            @Override
            public String getName() {
                return user;
            }
        };
    }
}
