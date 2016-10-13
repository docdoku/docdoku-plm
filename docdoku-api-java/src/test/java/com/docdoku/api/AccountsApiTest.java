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

package com.docdoku.api;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.AccountDTO;
import com.docdoku.api.services.AccountsApi;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.UUID;

@RunWith(JUnit4.class)
public class AccountsApiTest {

    @Test
    public void createAccountTest() throws ApiException {
        String login = "TMP-USER-"+ UUID.randomUUID().toString().substring(0,8);
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setLogin(login);
        accountDTO.setEmail(TestConfig.EMAIL);
        accountDTO.setNewPassword(TestConfig.PASSWORD);
        accountDTO.setLanguage(TestConfig.LANGUAGE);
        accountDTO.setName(login);
        accountDTO.setTimeZone(TestConfig.TIMEZONE);
        AccountDTO account = new AccountsApi(TestConfig.GUEST_CLIENT).createAccount(accountDTO);
        Assert.assertEquals(account.getLogin(), login);
    }

    @Test
    public void getAccountTest() throws ApiException {
        AccountDTO account = new AccountsApi(TestConfig.BASIC_CLIENT).getAccount();
        Assert.assertEquals(account.getLogin(), TestConfig.LOGIN);
    }

    @Test
    public void updateAccountTest() throws ApiException {

        String newName = UUID.randomUUID().toString().substring(0, 8);

        AccountsApi accountsApi = new AccountsApi(TestConfig.BASIC_CLIENT);
        AccountDTO account = accountsApi.getAccount();
        account.setName(newName);

        AccountDTO updatedAccount = accountsApi.updateAccount(account);
        Assert.assertEquals(updatedAccount.getName(), newName);
        Assert.assertEquals(updatedAccount,account);

    }

}
