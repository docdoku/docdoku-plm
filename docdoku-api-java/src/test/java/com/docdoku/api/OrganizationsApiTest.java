package com.docdoku.api;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.AccountDTO;
import com.docdoku.api.models.OrganizationDTO;
import com.docdoku.api.models.UserDTO;
import com.docdoku.api.services.OrganizationsApi;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.UUID;

@RunWith(JUnit4.class)
public class OrganizationsApiTest {

    @Test
    public void createOrganizationTest() throws ApiException {
        OrganizationDTO organizationDTO = TestUtils.createOrganization();
        OrganizationDTO organization = new OrganizationsApi(new DocdokuPLMBasicClient(TestConfig.URL, TestConfig.LOGIN, TestConfig.PASSWORD).getClient()).getOrganization();
        Assert.assertEquals(organization.getName(), organizationDTO.getName());
    }

    @Test
    public void getOrganizationTest() throws ApiException {
        OrganizationDTO organization = new OrganizationsApi(TestConfig.BASIC_CLIENT).getOrganization();
        organization.setName(TestConfig.NAME);
        Assert.assertEquals(organization.getName(), TestConfig.NAME);
    }

    @Test
    public void updateOrganizationTest() throws ApiException {
        String newName = UUID.randomUUID().toString().substring(0, 8);
        OrganizationsApi organizationsApi = new OrganizationsApi(TestConfig.BASIC_CLIENT);
        OrganizationDTO organization = organizationsApi.getOrganization();
        organization.setName(newName);
        OrganizationDTO updatedOrganization = organizationsApi.updateOrganization(organization);
        Assert.assertEquals(updatedOrganization.getName(), newName);
        Assert.assertEquals(updatedOrganization, organization);
    }

    @Test
    public void addMemberToOrganizationTest() throws ApiException {
        AccountDTO newAccount = TestUtils.createAccount();
        UserDTO userToAdd = new UserDTO();
        userToAdd.setLogin(newAccount.getLogin());
        OrganizationsApi organizationsApi = new OrganizationsApi(TestConfig.BASIC_CLIENT);
        organizationsApi.addMember(userToAdd);
        List<AccountDTO> membersOfOrganization = organizationsApi.getMembers();
        Assert.assertEquals(membersOfOrganization.stream().filter(accountDTO -> accountDTO.getLogin().equals(userToAdd.getLogin())).count(), 1);
    }

    @Test
    public void getMembersTest() throws ApiException {
        OrganizationsApi organizationsApi = new OrganizationsApi(TestConfig.BASIC_CLIENT);
        Assert.assertEquals(organizationsApi.getMembers().size(), 2);
    }

    @Test
    public void moveMemberDownTest() throws ApiException {
        UserDTO firstUser = new UserDTO();
        firstUser.setLogin(TestConfig.LOGIN);
        OrganizationsApi organizationsApi = new OrganizationsApi(TestConfig.BASIC_CLIENT);
        organizationsApi.moveMemberDown(firstUser);
        List<AccountDTO> membersOfOrganization = organizationsApi.getMembers();
        Assert.assertEquals(membersOfOrganization.get(1).getLogin(), TestConfig.LOGIN);
    }

    @Test
    public void moveMemberUpTest() throws ApiException {
        UserDTO firstUser = new UserDTO();
        firstUser.setLogin(TestConfig.LOGIN);
        OrganizationsApi organizationsApi = new OrganizationsApi(TestConfig.BASIC_CLIENT);
        organizationsApi.moveMemberUp(firstUser);
        List<AccountDTO> membersOfOrganization = organizationsApi.getMembers();
        Assert.assertEquals(membersOfOrganization.get(0).getLogin(), TestConfig.LOGIN);
    }

    @Test
    public void removeMemberFromOrganizationTest() throws ApiException {
        UserDTO userToDelete = new UserDTO();
        OrganizationsApi organizationsApi = new OrganizationsApi(TestConfig.BASIC_CLIENT);
        List<AccountDTO> membersOfOrganizationBeforeDeletion = organizationsApi.getMembers();
        userToDelete.setLogin(membersOfOrganizationBeforeDeletion.get(1).getLogin());
        organizationsApi.removeMember(userToDelete);
        List<AccountDTO> membersOfOrganizationAfterDeletion = organizationsApi.getMembers();
        Assert.assertEquals(membersOfOrganizationAfterDeletion.stream().filter(accountDTO -> accountDTO.getLogin().equals(userToDelete.getLogin())).count(), 0);
    }

    @Test
    public void deleteOrganizationTest() throws ApiException {
        OrganizationsApi organizationsApi = new OrganizationsApi(TestConfig.BASIC_CLIENT);
        organizationsApi.deleteOrganization();
        Assert.assertEquals(organizationsApi.getOrganization().getName(), null);
    }

}
