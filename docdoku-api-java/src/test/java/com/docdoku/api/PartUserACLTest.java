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
import com.docdoku.api.models.*;
import com.docdoku.api.services.WorkspacesApi;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

@RunWith(JUnit4.class)
public class PartUserACLTest {

    private static final WorkspacesApi workspacesApi = new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT);

    private static WorkspaceDTO workspace;
    private static ApiClient user1Client;
    private static ApiClient user2Client;

    private static ACLEntryDTO user1FullAccess = new ACLEntryDTO();
    private static ACLEntryDTO user2FullAccess = new ACLEntryDTO();
    private static ACLEntryDTO user1ReadOnly = new ACLEntryDTO();
    private static ACLEntryDTO user2ReadOnly = new ACLEntryDTO();
    private static ACLEntryDTO user1Forbidden = new ACLEntryDTO();
    private static ACLEntryDTO user2Forbidden = new ACLEntryDTO();
    private static ACLEntryDTO groupFullAccess = new ACLEntryDTO();

    @BeforeClass
    public static void initTestData() throws ApiException {

        workspace = TestUtils.createWorkspace();

        AccountDTO account1 = TestUtils.createAccount();
        AccountDTO account2 = TestUtils.createAccount();

        UserDTO user1 = new UserDTO();
        user1.setLogin(account1.getLogin());
        UserDTO user2 = new UserDTO();
        user2.setLogin(account2.getLogin());

        user1Client = DocdokuPLMClientFactory.createJWTClient(TestConfig.URL, account1.getLogin(), TestConfig.PASSWORD);
        user2Client = DocdokuPLMClientFactory.createJWTClient(TestConfig.URL, account2.getLogin(), TestConfig.PASSWORD);

        UserGroupDTO group = new UserGroupDTO();
        group.setId(TestUtils.randomString());
        workspacesApi.createGroup(workspace.getId(), group);

        workspacesApi.addUser(workspace.getId(), user1, null);
        workspacesApi.addUser(workspace.getId(), user2, null);

        user1FullAccess.setKey(user1.getLogin());
        user1FullAccess.setValue(ACLEntryDTO.ValueEnum.FULL_ACCESS);

        user2FullAccess.setKey(user2.getLogin());
        user2FullAccess.setValue(ACLEntryDTO.ValueEnum.FULL_ACCESS);

        user1ReadOnly.setKey(user1.getLogin());
        user1ReadOnly.setValue(ACLEntryDTO.ValueEnum.READ_ONLY);

        user2ReadOnly.setKey(user2.getLogin());
        user2ReadOnly.setValue(ACLEntryDTO.ValueEnum.READ_ONLY);

        user1Forbidden.setKey(user1.getLogin());
        user1Forbidden.setValue(ACLEntryDTO.ValueEnum.FORBIDDEN);

        user2Forbidden.setKey(user2.getLogin());
        user2Forbidden.setValue(ACLEntryDTO.ValueEnum.FORBIDDEN);

        groupFullAccess.setKey(group.getId());
        groupFullAccess.setValue(ACLEntryDTO.ValueEnum.FULL_ACCESS);

    }

    @Test
    public void testFullAccessGroupACL() throws ApiException {

        PartRevisionDTO part = TestUtils.createPart(workspace.getId(),
                Arrays.asList(user1FullAccess, user2FullAccess), null);

        TestUtils.assertUserCanRetrievePart(user1Client, part, true);
        TestUtils.assertUserCanRetrievePart(user2Client, part, true);
        TestUtils.assertUserCanEditPart(user1Client, part, true);
        TestUtils.assertUserCanEditPart(user2Client, part, true);

        part = TestUtils.createPart(workspace.getId(),
                Arrays.asList(user1FullAccess, user2ReadOnly), null);

        TestUtils.assertUserCanRetrievePart(user1Client, part, true);
        TestUtils.assertUserCanRetrievePart(user2Client, part, true);
        TestUtils.assertUserCanEditPart(user1Client, part, true);
        TestUtils.assertUserCanEditPart(user2Client, part, false);

        part = TestUtils.createPart(workspace.getId(),
                Arrays.asList(user1FullAccess, user2Forbidden), null);

        TestUtils.assertUserCanRetrievePart(user1Client, part, true);
        TestUtils.assertUserCanRetrievePart(user2Client, part, false);
        TestUtils.assertUserCanEditPart(user1Client, part, true);
        TestUtils.assertUserCanEditPart(user2Client, part, false);

    }


    @AfterClass
    public static void deleteWorkspace() throws ApiException {
        workspacesApi.deleteWorkspace(workspace.getId());
    }


}
