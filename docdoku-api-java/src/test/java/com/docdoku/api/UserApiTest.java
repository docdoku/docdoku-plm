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
        UserDTO currentUser = usersApi.whoami(TestConfig.WORKSPACE);
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
