/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.api;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.LoginRequestDTO;
import com.docdoku.api.services.AuthApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AuthApiTest {

    private AuthApi authApi = new AuthApi(TestConfig.GUEST_CLIENT);

    @Test
    public void wrongPasswordTest() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setLogin(TestConfig.LOGIN);
        loginRequestDTO.setPassword("wrongpassword");
        try {
            authApi.login(loginRequestDTO);
        } catch (ApiException e) {
            Assert.assertEquals(403, e.getCode());
        }
    }

    @Test
    public void wrongLoginTest() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setLogin("wronglogin");
        loginRequestDTO.setPassword("whatever");

        try {
            authApi.login(loginRequestDTO);
        } catch (ApiException e) {
            Assert.assertEquals(403, e.getCode());
        }
    }

}
