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
        String login = "USER-"+ UUID.randomUUID().toString().substring(0,6);
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setLogin(login);
        accountDTO.setEmail("my@email.com");
        accountDTO.setNewPassword("password");
        accountDTO.setLanguage("en");
        accountDTO.setName("Mr " + login);
        accountDTO.setTimeZone("CET");
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

        String newName = UUID.randomUUID().toString().substring(0,6);

        AccountsApi accountsApi = new AccountsApi(TestConfig.BASIC_CLIENT);
        AccountDTO account = accountsApi.getAccount();
        account.setName(newName);

        AccountDTO updatedAccount = accountsApi.updateAccount(account);
        Assert.assertEquals(updatedAccount.getName(), newName);
        Assert.assertEquals(updatedAccount,account);

    }

}
