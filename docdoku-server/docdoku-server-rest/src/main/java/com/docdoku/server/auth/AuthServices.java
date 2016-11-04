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

package com.docdoku.server.auth;

import com.docdoku.core.common.Account;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IAccountManagerLocal;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * This class is an helper class for authentication modules.
 * <p>
 * Allows to access AccountManagerBean from a class not managed by the container
 * Provides web.xml config values
 *
 * @author Morgan Guimard
 */
public class AuthServices {

    private static final Logger LOGGER = Logger.getLogger(AuthServices.class.getName());
    private static final String ACCOUNT_MANAGER = "java:global/docdoku-server-ear/docdoku-server-ejb/AccountManagerBean!com.docdoku.core.services.IAccountManagerLocal";
    private static final String JAVA_COMP_ENV = "java:comp/env";

    private static IAccountManagerLocal accountManager;
    private static String[] publicPaths;

    static {

        try {
            InitialContext context = new InitialContext();
            accountManager = (IAccountManagerLocal) context.lookup(ACCOUNT_MANAGER);
            Context env = (Context) context.lookup(JAVA_COMP_ENV);
            final String publicPathsValue = (String) env.lookup("public-paths");

            if (publicPathsValue != null) {
                publicPaths = publicPathsValue.split(",");

                for (int i = 0; i < publicPaths.length; i++) {
                    boolean endLess = false;
                    if (publicPaths[i].endsWith("/**")) {
                        publicPaths[i] = publicPaths[i].substring(0, publicPaths[i].length() - 2);
                        endLess = true;
                    }
                    publicPaths[i] = publicPaths[i].replace("*", "[^/]+?");
                    if (endLess) {
                        publicPaths[i] += ".*";
                    }
                }

            }
        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE, "Cannot initialize AuthServices", e);
        }
    }


    public static Account authenticateAccount(String login, String password) {
        return accountManager.authenticateAccount(login, password);
    }

    public static UserGroupMapping getUserGroupMapping(String login) {
        return accountManager.getUserGroupMapping(login);
    }

    public static boolean isPublicRequestURI(String requestURI) {
        if (requestURI != null && publicPaths != null) {
            for (String excludedPath : publicPaths) {
                if (Pattern.matches(excludedPath, requestURI)) {
                    return true;
                }
            }
        }
        return false;
    }

}
