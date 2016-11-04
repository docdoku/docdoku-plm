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
import com.docdoku.api.models.PlatformOptionsDTO;
import com.docdoku.api.models.WorkspaceDTO;
import com.docdoku.api.services.AdminApi;
import com.docdoku.api.services.WorkspacesApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.UUID;

@RunWith(JUnit4.class)
public class AdminApiTest {

    @Test
    public void getAccountsTest() throws ApiException {
        List<AccountDTO> accounts = new AdminApi(TestConfig.ROOT_CLIENT).getAccounts();
        Assert.assertFalse(accounts.isEmpty());
    }

    @Test
    public void enableAccountTest() throws ApiException {
        AdminApi adminApi = new AdminApi(TestConfig.ROOT_CLIENT);
        AccountDTO account = TestUtils.createAccount();
        AccountDTO disabledAccount = adminApi.enableAccount(account.getLogin(), false, "");
        Assert.assertFalse(disabledAccount.getEnabled());
        AccountDTO enabledAccount = adminApi.enableAccount(account.getLogin(), true, "");
        Assert.assertTrue(enabledAccount.getEnabled());
    }

    @Test
    public void enableWorkspaceTest() throws ApiException {

        AdminApi adminApi = new AdminApi(TestConfig.ROOT_CLIENT);
        WorkspacesApi workspacesApi = new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT);

        WorkspaceDTO workspace = new WorkspaceDTO();
        workspace.setId(UUID.randomUUID().toString());
        workspacesApi.createWorkspace(workspace, TestConfig.LOGIN);

        WorkspaceDTO disabledWorkspace = adminApi.enableWorkspace(workspace.getId(), false, "");
        Assert.assertFalse(disabledWorkspace.getEnabled());

        WorkspaceDTO enabledWorkspace = adminApi.enableWorkspace(workspace.getId(), true, "");
        Assert.assertTrue(enabledWorkspace.getEnabled());

        workspacesApi.deleteWorkspace(workspace.getId());

    }

    @Test
    public void updateAccountTest() throws ApiException {
        AccountDTO account = TestUtils.createAccount();

        String newName = UUID.randomUUID().toString().substring(0, 8);
        account.setName(newName);

        AdminApi adminApi = new AdminApi(TestConfig.ROOT_CLIENT);

        AccountDTO updatedAccount = adminApi.updateAccount(account);

        Assert.assertEquals(updatedAccount.getName(), newName);
        Assert.assertEquals(updatedAccount,account);

    }

    @Test
    public void platformOptionsTests() throws ApiException {

        AdminApi adminApi = new AdminApi(TestConfig.ROOT_CLIENT);

        // Getter
        PlatformOptionsDTO platformOptions = adminApi.getPlatformOptions();
        PlatformOptionsDTO.RegistrationStrategyEnum registrationStrategy = platformOptions.getRegistrationStrategy();
        PlatformOptionsDTO.WorkspaceCreationStrategyEnum workspaceCreationStrategy = platformOptions.getWorkspaceCreationStrategy();
        Assert.assertNotNull(registrationStrategy);
        Assert.assertNotNull(workspaceCreationStrategy);

        // change strategy
        platformOptions.setRegistrationStrategy(PlatformOptionsDTO.RegistrationStrategyEnum.ADMIN_VALIDATION);
        platformOptions.setWorkspaceCreationStrategy(PlatformOptionsDTO.WorkspaceCreationStrategyEnum.ADMIN_VALIDATION);
        adminApi.setPlatformOptions(platformOptions);

        // Assert modifications
        PlatformOptionsDTO updatedOptions = adminApi.getPlatformOptions();
        Assert.assertEquals(updatedOptions.getRegistrationStrategy(), platformOptions.getRegistrationStrategy());
        Assert.assertEquals(updatedOptions.getWorkspaceCreationStrategy(), platformOptions.getWorkspaceCreationStrategy());

        // Restore
        platformOptions.setRegistrationStrategy(PlatformOptionsDTO.RegistrationStrategyEnum.NONE);
        platformOptions.setWorkspaceCreationStrategy(PlatformOptionsDTO.WorkspaceCreationStrategyEnum.NONE);
        adminApi.setPlatformOptions(platformOptions);

    }


    @Test
    public void getStatsTests() throws ApiException {
        AdminApi adminApi = new AdminApi(TestConfig.ROOT_CLIENT);

        String documentsStats = adminApi.getDocumentsStats();
        Assert.assertNotNull(documentsStats);

        String partsStats = adminApi.getPartsStats();
        Assert.assertNotNull(partsStats);

        String productsStats = adminApi.getProductsStats();
        Assert.assertNotNull(productsStats);

        String usersStats = adminApi.getUsersStats();
        Assert.assertNotNull(usersStats);

        String diskSpaceUsageStats = adminApi.getDiskSpaceUsageStats();
        Assert.assertNotNull(diskSpaceUsageStats);


    }


}
