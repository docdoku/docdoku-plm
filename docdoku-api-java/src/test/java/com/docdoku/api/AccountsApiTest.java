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

import com.docdoku.api.client.ApiClient;
import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.AccountDTO;
import com.docdoku.api.services.AccountsApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.Response;

@RunWith(JUnit4.class)
public class AccountsApiTest {

    @Test
    public void createAccountTest() throws ApiException {
        AccountDTO accountDTO = TestUtils.createAccount();
        ApiClient accountClient = DocDokuPLMClientFactory.createJWTClient(TestConfig.URL, accountDTO.getLogin(), TestConfig.PASSWORD);
        AccountDTO account = new AccountsApi(accountClient).getAccount();
        Assert.assertEquals(account.getLogin(), accountDTO.getLogin());
    }

    @Test
    public void getAccountTest() throws ApiException {
        AccountDTO account = new AccountsApi(TestConfig.REGULAR_USER_CLIENT).getAccount();
        Assert.assertEquals(account.getLogin(), TestConfig.LOGIN);
    }

    @Test
    public void updateAccountTest() throws ApiException {

        String newName = TestUtils.randomString();

        AccountsApi accountsApi = new AccountsApi(TestConfig.REGULAR_USER_CLIENT);
        AccountDTO account = accountsApi.getAccount();
        account.setName(newName);
        account.setPassword(TestConfig.PASSWORD);

        AccountDTO updatedAccount = accountsApi.updateAccount(account);
        Assert.assertEquals(updatedAccount.getName(), account.getName());
    }

    @Test
    public void createAlreadyExistingAccount() throws ApiException {

        AccountsApi accountsApi = new AccountsApi(TestConfig.GUEST_CLIENT);
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setEmail(TestConfig.EMAIL);
        accountDTO.setNewPassword(TestConfig.PASSWORD);
        accountDTO.setLanguage(TestConfig.LANGUAGE);
        accountDTO.setName(TestConfig.LOGIN);
        accountDTO.setTimeZone(TestConfig.TIMEZONE);

        try {
            accountsApi.createAccount(accountDTO);
        } catch (ApiException e) {
            Assert.assertEquals(Response.Status.CONFLICT.getStatusCode(), e.getCode());
        }

    }

}
