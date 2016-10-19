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
import com.docdoku.api.models.UserDTO;
import com.docdoku.api.services.UsersApi;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class UserApiTest {

    private UsersApi usersApi = new UsersApi(TestConfig.BASIC_CLIENT);

    @Test
    public void whoamiTest() throws ApiException {
        UserDTO currentUser = usersApi.whoAmI(TestConfig.WORKSPACE);
        Assert.assertEquals(currentUser.getLogin(),TestConfig.LOGIN);
        Assert.assertEquals(currentUser.getWorkspaceId(),TestConfig.WORKSPACE);
    }

    @Test
    public void getAdminInWorkspaceTest() throws ApiException {
        UserDTO admin = usersApi.getAdminInWorkspace(TestConfig.WORKSPACE);
        Assert.assertEquals(admin.getWorkspaceId(),TestConfig.WORKSPACE);
    }

    @Test
    public void getUsersInWorkspaceTest() throws ApiException {
        List<UserDTO> users = usersApi.getUsersInWorkspace(TestConfig.WORKSPACE);
        Assert.assertEquals(users.stream()
                .filter(user -> TestConfig.LOGIN.equals(user.getLogin()))
                .count(),1);
    }

}
