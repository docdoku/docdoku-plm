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

package com.docdoku.server;

import javax.security.auth.callback.*;
import java.io.IOException;

public class CallbackHandlerAdapter implements CallbackHandler {

    private String mLogin;
    private char[] mPassword;

    public CallbackHandlerAdapter(String pLogin, char[] pPassword) {
        mLogin = pLogin;
        mPassword = pPassword;
    }

    @Override
    public void handle(Callback[] pCallbacks)
            throws IOException, UnsupportedCallbackException {
        for (Callback pCallback : pCallbacks) {
            if (pCallback instanceof NameCallback) {
                NameCallback nc = (NameCallback) pCallback;
                nc.setName(mLogin);
            } else if (pCallback instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback) pCallback;
                pc.setPassword(mPassword);
            } else {
                throw new UnsupportedCallbackException(
                        pCallback,
                        "Unrecognized callback");
            }
        }
    }
}