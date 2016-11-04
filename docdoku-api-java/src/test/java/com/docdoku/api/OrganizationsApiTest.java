package com.docdoku.api;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.AccountDTO;
import com.docdoku.api.models.OrganizationDTO;
import com.docdoku.api.models.UserDTO;
import com.docdoku.api.services.OrganizationsApi;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.UUID;

@RunWith(JUnit4.class)
public class OrganizationsApiTest {

    private final static OrganizationsApi organizationsApi = new OrganizationsApi(TestConfig.REGULAR_USER_CLIENT);

    @BeforeClass
    public static void initOrganization() throws ApiException {
        OrganizationDTO oldOrganization = organizationsApi.getOrganization();
        if(oldOrganization.getName() != null) {
            organizationsApi.deleteOrganization();
        }
        OrganizationDTO organization = TestUtils.createOrganization();
        OrganizationDTO fetchedOrganization = organizationsApi.getOrganization();
        Assert.assertEquals(organization, fetchedOrganization);
    }

    @Test
    public void updateOrganizationTest() throws ApiException {
        String newName = UUID.randomUUID().toString().substring(0, 8);
        OrganizationDTO organization = organizationsApi.getOrganization();
        organization.setName(newName);
        OrganizationDTO updatedOrganization = organizationsApi.updateOrganization(organization);
        Assert.assertEquals(newName,updatedOrganization.getName());
        Assert.assertEquals(organization, updatedOrganization);
    }


    @Test
    public void memberTestSuite() throws ApiException {
        addMemberToOrganizationTest();
        getMembersTest();
        moveMemberDownTest();
        moveMemberUpTest();
        removeMemberFromOrganizationTest();
    }


    private void addMemberToOrganizationTest() throws ApiException {
        AccountDTO newAccount = TestUtils.createAccount();
        UserDTO userToAdd = new UserDTO();
        userToAdd.setLogin(newAccount.getLogin());
        organizationsApi.addMember(userToAdd);
        List<AccountDTO> membersOfOrganization = organizationsApi.getMembers();
        Assert.assertEquals(membersOfOrganization.stream().filter(accountDTO -> accountDTO.getLogin().equals(userToAdd.getLogin())).count(), 1);
    }


    private void getMembersTest() throws ApiException {
        Assert.assertEquals(organizationsApi.getMembers().size(), 2);
    }

    private void moveMemberDownTest() throws ApiException {
        UserDTO firstUser = new UserDTO();
        firstUser.setLogin(TestConfig.LOGIN);
        organizationsApi.moveMemberDown(firstUser);
        List<AccountDTO> membersOfOrganization = organizationsApi.getMembers();
        Assert.assertEquals(membersOfOrganization.get(1).getLogin(), TestConfig.LOGIN);
    }

    private void moveMemberUpTest() throws ApiException {
        UserDTO firstUser = new UserDTO();
        firstUser.setLogin(TestConfig.LOGIN);
        organizationsApi.moveMemberUp(firstUser);
        List<AccountDTO> membersOfOrganization = organizationsApi.getMembers();
        Assert.assertEquals(membersOfOrganization.get(0).getLogin(), TestConfig.LOGIN);
    }

    private void removeMemberFromOrganizationTest() throws ApiException {
        UserDTO userToDelete = new UserDTO();
        List<AccountDTO> membersOfOrganizationBeforeDeletion = organizationsApi.getMembers();
        userToDelete.setLogin(membersOfOrganizationBeforeDeletion.get(1).getLogin());
        organizationsApi.removeMember(userToDelete);
        List<AccountDTO> membersOfOrganizationAfterDeletion = organizationsApi.getMembers();
        Assert.assertEquals(membersOfOrganizationAfterDeletion.stream().filter(accountDTO -> accountDTO.getLogin().equals(userToDelete.getLogin())).count(), 0);
    }

    @AfterClass
    public static void deleteOrganization() throws ApiException {
        organizationsApi.deleteOrganization();
        OrganizationDTO organization = organizationsApi.getOrganization();
        Assert.assertNull(organization.getName());
    }

}
