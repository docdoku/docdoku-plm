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
import com.docdoku.api.models.UserDTO;
import com.docdoku.api.models.WorkspaceDTO;
import com.docdoku.api.services.UsersApi;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class UserApiTest {

    private UsersApi usersApi = new UsersApi(TestConfig.REGULAR_USER_CLIENT);
    private static WorkspaceDTO workspace;

    @BeforeClass
    public static void initWorkspace() throws ApiException {
        workspace = TestUtils.createWorkspace(UserApiTest.class.getName());
    }

    @AfterClass
    public static void deleteWorkspace() throws ApiException {
        TestUtils.deleteWorkspace(workspace);
    }

    @Test
    public void whoAmITest() throws ApiException {
        UserDTO currentUser = usersApi.whoAmI(workspace.getId());
        Assert.assertEquals(currentUser.getLogin(),TestConfig.LOGIN);
        Assert.assertEquals(currentUser.getWorkspaceId(),workspace.getId());
    }

    @Test
    public void getAdminInWorkspaceTest() throws ApiException {
        UserDTO admin = usersApi.getAdminInWorkspace(workspace.getId());
        Assert.assertEquals(admin.getWorkspaceId(),workspace.getId());
    }

    @Test
    public void getUsersInWorkspaceTest() throws ApiException {
        List<UserDTO> users = usersApi.getUsersInWorkspace(workspace.getId());
        Assert.assertEquals(users.stream()
                .filter(user -> TestConfig.LOGIN.equals(user.getLogin()))
                .count(),1);
    }

}
