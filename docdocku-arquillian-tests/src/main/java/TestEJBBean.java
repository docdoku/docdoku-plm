import com.docdoku.core.common.*;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.services.*;
import com.sun.appserv.security.ProgrammaticLogin;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author: Asmae CHADID
 */


@LocalBean
@Stateless
public class TestEJBBean {

    @EJB
    private IDocumentManagerLocal documentManagerLocal;

    @EJB
    private IUserManagerLocal userManagerLocal;

    @PersistenceContext
    private EntityManager em;

    private final String FOLDER_NAME = "TEST_FOLDER";
    private final String WORKSPACE_NAME = "TEST_WORKSPACE";
    private final String DOCUMENT_ID = "DOC001";
    private final String DOCUMENT_NAME = "TEST_DOCUMENT";
    private String USER1_NAME = "user1";
    private String USER2_NAME = "user2";
    private String USER3_NAME = "user3";
    private String PASSWORD = "password";

    public void testEJB(String name) {
        com.sun.appserv.security.ProgrammaticLogin loginP = new ProgrammaticLogin();
        System.out.println("LOGIN RETURN::::" + loginP.login("user1", "password".toCharArray()));
        //WARNING: DPL8019: The run-as principal users was assigned by the deployment system based on the specified role.  Please consider defining an explicit run-as principal in the sun-specific deployment descriptor.

        try {

            System.out.println("worspace size: " + userManagerLocal.getAdministratedWorkspaces().length);
            System.out.println("worspace 1: " + userManagerLocal.getWorkspaces()[0].getId());

            userManagerLocal.createWorkspace("myworkspace", userManagerLocal.getAccount("user1"), "demo", Workspace.VaultType.LARGE, false);
            System.out.println("FOLDER CREATED :" + documentManagerLocal.createFolder("myworkspace", "folder1").getShortName());
            System.out.println("DOCUMENT CREATED " + documentManagerLocal.createDocumentMaster("myworkspace/folder1", "firstDoc", "", "Description doc1", null, null, null, null, null).getDescription());
            // System.out.println("disque usage: "+workspaceManagerBean.getDiskUsageInWorkspace("w1"));
            userManagerLocal.addUserInWorkspace("myworkspace", "user2");
            System.out.println("document count in myworkspace: " + documentManagerLocal.getDocumentsCountInWorkspace("myworkspace"));

            String[] logins = new String[1];
            logins[0] = "user2";
            userManagerLocal.grantUserAccess("myworkspace", logins, false);
            //userManagerLocal.grantUserAccess("myworkspace",logins,true);
            loginP.logout();
            System.out.println(loginP.login("user2", "password".toCharArray()));

            System.out.println("who am i returns:::::" + documentManagerLocal.whoAmI("w2").getEmail());
            //   userManagerLocal.checkWorkspaceWriteAccess("w1");
            System.out.println("worspace size: " + userManagerLocal.getWorkspaces().length);
            System.out.println(userManagerLocal.checkWorkspaceReadAccess("myworkspace").getLogin());
            System.out.println("USER2 ATTEMPT TO CREATE FOLDER " + documentManagerLocal.createFolder("myworkspace", "folder2").getShortName());


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public void workspaceReadOnlyAccess() throws UserNotActiveException, WorkspaceNotFoundException, AccessRightException, CreationException, FolderNotFoundException, UserNotFoundException, FolderAlreadyExistsException, NotAllowedException, RoleNotFoundException, DocumentMasterAlreadyExistsException, DocumentMasterTemplateNotFoundException, WorkflowModelNotFoundException, FileAlreadyExistsException, UserAlreadyExistsException, AccountNotFoundException, WorkspaceAlreadyExistsException {
        String[] logins = new String[1];
        logins[0] = "user2";

        //login as user1
        com.sun.appserv.security.ProgrammaticLogin loginP = new ProgrammaticLogin();
        loginP.login(USER1_NAME, PASSWORD.toCharArray());

        //create workspace and a folder then add user2 to that workspace (readonly)

        userManagerLocal.createWorkspace(WORKSPACE_NAME, userManagerLocal.getAccount(USER1_NAME), "Test workspace", Workspace.VaultType.LARGE, false);
        documentManagerLocal.createFolder(WORKSPACE_NAME, FOLDER_NAME).getShortName();
        documentManagerLocal.createDocumentMaster(WORKSPACE_NAME + "/" + FOLDER_NAME, DOCUMENT_ID, DOCUMENT_NAME, "Description", null, null, null, null, null);
        userManagerLocal.addUserInWorkspace(WORKSPACE_NAME, USER2_NAME);
        userManagerLocal.grantUserAccess(WORKSPACE_NAME, logins, false);

        //logout and login as user2
        loginP.logout();
        loginP.login(USER2_NAME, PASSWORD.toCharArray());

        //attempt to create a folder in the workspace that he was added to
        documentManagerLocal.createFolder(WORKSPACE_NAME, "Folder2").getShortName();
        loginP.logout();
    }

    public void UserAndGroupAccess(Boolean userAccess, Boolean grpAccess) throws UserNotActiveException, WorkspaceNotFoundException, AccessRightException, CreationException, FolderNotFoundException, UserNotFoundException, FolderAlreadyExistsException, NotAllowedException, RoleNotFoundException, DocumentMasterAlreadyExistsException, DocumentMasterTemplateNotFoundException, WorkflowModelNotFoundException, FileAlreadyExistsException, UserAlreadyExistsException, AccountNotFoundException, WorkspaceAlreadyExistsException, UserGroupAlreadyExistsException, UserGroupNotFoundException {
        String[] groups = new String[1];
        String[] logins = new String[1];
        logins[0] = "user3";

        //login as user1
        com.sun.appserv.security.ProgrammaticLogin loginP = new ProgrammaticLogin();
        loginP.login(USER1_NAME, PASSWORD.toCharArray());

        userManagerLocal.createWorkspace(WORKSPACE_NAME, userManagerLocal.getAccount(USER1_NAME), "Test workspace", Workspace.VaultType.LARGE, false);
        documentManagerLocal.createFolder(WORKSPACE_NAME, FOLDER_NAME).getShortName();
        documentManagerLocal.createDocumentMaster(WORKSPACE_NAME + "/" + FOLDER_NAME, DOCUMENT_ID, DOCUMENT_NAME, "Description", null, null, null, null, null);
        String userGroupId = userManagerLocal.createUserGroup("group1", userManagerLocal.getWorkspace(WORKSPACE_NAME)).getId();
        groups[0] = userGroupId;
        userManagerLocal.grantGroupAccess(WORKSPACE_NAME, groups, grpAccess);
        userManagerLocal.addUserInGroup(new UserGroupKey(WORKSPACE_NAME, userGroupId), USER3_NAME);

        userManagerLocal.addUserInWorkspace(WORKSPACE_NAME, USER3_NAME);
        userManagerLocal.grantUserAccess(WORKSPACE_NAME, logins, userAccess);
        loginP.logout();
        loginP.login(USER3_NAME, PASSWORD.toCharArray());
        documentManagerLocal.createDocumentMaster(WORKSPACE_NAME + "/" + FOLDER_NAME, "SecondDoc", DOCUMENT_NAME, "Description", null, null, null, null, null);

    }

    public void UserInSeveralGroups(Boolean grp1Access, Boolean grp2Access) throws UserNotActiveException, WorkspaceNotFoundException, AccessRightException, CreationException, FolderNotFoundException, UserNotFoundException, FolderAlreadyExistsException, NotAllowedException, RoleNotFoundException, DocumentMasterAlreadyExistsException, DocumentMasterTemplateNotFoundException, WorkflowModelNotFoundException, FileAlreadyExistsException, UserAlreadyExistsException, AccountNotFoundException, WorkspaceAlreadyExistsException, UserGroupAlreadyExistsException, UserGroupNotFoundException {

        //login as user1
        com.sun.appserv.security.ProgrammaticLogin loginP = new ProgrammaticLogin();
        loginP.login(USER1_NAME, PASSWORD.toCharArray());

        String[] group1 = new String[1];
        group1[0] = "group1";
        String[] group2 = new String[1];
        group2[0] = "group2";

        userManagerLocal.createWorkspace(WORKSPACE_NAME, userManagerLocal.getAccount(USER1_NAME), "Test workspace", Workspace.VaultType.LARGE, false);
        documentManagerLocal.createFolder(WORKSPACE_NAME, FOLDER_NAME).getShortName();
        documentManagerLocal.createDocumentMaster(WORKSPACE_NAME + "/" + FOLDER_NAME, DOCUMENT_ID, DOCUMENT_NAME, "Description", null, null, null, null, null);
        documentManagerLocal.createDocumentMaster(WORKSPACE_NAME + "/" + FOLDER_NAME, "doc_002", DOCUMENT_NAME, "Description", null, null, null, null, null);


        String userGroupId1 = userManagerLocal.createUserGroup("group1", userManagerLocal.getWorkspace(WORKSPACE_NAME)).getId();
        if (grp1Access == null)
            userManagerLocal.passivateUserGroups(WORKSPACE_NAME, group1);
        else
            userManagerLocal.grantGroupAccess(WORKSPACE_NAME, group1, grp1Access);
        userManagerLocal.addUserInGroup(new UserGroupKey(WORKSPACE_NAME, userGroupId1), USER2_NAME);

        String userGroupId2 = userManagerLocal.createUserGroup("group2", userManagerLocal.getWorkspace(WORKSPACE_NAME)).getId();
        if (grp2Access == null)
            userManagerLocal.passivateUserGroups(WORKSPACE_NAME, group2);
        else
            userManagerLocal.grantGroupAccess(WORKSPACE_NAME, group2, grp2Access);

        userManagerLocal.addUserInGroup(new UserGroupKey(WORKSPACE_NAME, userGroupId2), USER2_NAME);

        loginP.logout();
        loginP.login(USER2_NAME, PASSWORD.toCharArray());
        System.out.println("User 2 check the total documents in workspace... " + documentManagerLocal.getDocumentsCountInWorkspace(WORKSPACE_NAME));

        System.out.println("User 2 attempt to create a new document ...");
        documentManagerLocal.createDocumentMaster(WORKSPACE_NAME + "/" + FOLDER_NAME, "SecondDoc", DOCUMENT_NAME, "Description", null, null, null, null, null);

        loginP.logout();

    }


    public void UserAndACLOnSpecificEntity(Boolean userAccess, ACL.Permission permission) throws UserNotActiveException, WorkspaceNotFoundException, AccessRightException, CreationException, FolderNotFoundException, UserNotFoundException, FolderAlreadyExistsException, NotAllowedException, RoleNotFoundException, DocumentMasterAlreadyExistsException, DocumentMasterTemplateNotFoundException, WorkflowModelNotFoundException, FileAlreadyExistsException, AccountNotFoundException, UserAlreadyExistsException, WorkspaceAlreadyExistsException, DocumentMasterNotFoundException, UserGroupAlreadyExistsException, UserGroupNotFoundException {


        //login as user1
        com.sun.appserv.security.ProgrammaticLogin loginP = new ProgrammaticLogin();
        loginP.login(USER1_NAME, PASSWORD.toCharArray());

        userManagerLocal.createWorkspace(WORKSPACE_NAME, userManagerLocal.getAccount(USER1_NAME), "Test workspace", Workspace.VaultType.LARGE, false);
        documentManagerLocal.createFolder(WORKSPACE_NAME, FOLDER_NAME).getShortName();


        userManagerLocal.addUserInWorkspace(WORKSPACE_NAME, USER2_NAME);
        String[] logins = new String[1];
        logins[0] = "user2";

        User user = em.find(User.class, new UserKey(WORKSPACE_NAME, "user2"));
        System.out.println("USER:::::" + user.getLogin());
        ACLUserEntry[] aclUserEntries = new ACLUserEntry[1];
        aclUserEntries[0] = new ACLUserEntry(new ACL(), user, permission);

        userManagerLocal.grantUserAccess(WORKSPACE_NAME, logins, userAccess);

        documentManagerLocal.createDocumentMaster(WORKSPACE_NAME + "/" + FOLDER_NAME, DOCUMENT_ID, DOCUMENT_NAME, "Description", null, null, aclUserEntries, null, null);

        System.out.println("CheckIn ...");
        documentManagerLocal.checkInDocument(new DocumentMasterKey(WORKSPACE_NAME, DOCUMENT_ID, "A"));

        loginP.logout();

        loginP.login(USER2_NAME, PASSWORD.toCharArray());
        System.out.println("User 2 check out the document ... ");
        documentManagerLocal.checkOutDocument(new DocumentMasterKey(WORKSPACE_NAME, DOCUMENT_ID, "A"));
        documentManagerLocal.getDocumentMaster(new DocumentMasterKey(WORKSPACE_NAME, DOCUMENT_ID, "A")).getIteration(1).setRevisionNote("This is my revision note");
        documentManagerLocal.checkInDocument(new DocumentMasterKey(WORKSPACE_NAME, DOCUMENT_ID, "A"));
        System.out.println("Revision Note: " + documentManagerLocal.getDocumentMaster(new DocumentMasterKey(WORKSPACE_NAME, DOCUMENT_ID, "A")).getIteration(1).getRevisionNote());
        loginP.logout();


    }

    public void UserGrpAndACLOnSpecificEntity(Boolean grpAccess, ACL.Permission permission) throws UserNotActiveException, WorkspaceNotFoundException, AccessRightException, CreationException, FolderNotFoundException, UserNotFoundException, FolderAlreadyExistsException, NotAllowedException, RoleNotFoundException, DocumentMasterAlreadyExistsException, DocumentMasterTemplateNotFoundException, WorkflowModelNotFoundException, FileAlreadyExistsException, AccountNotFoundException, UserAlreadyExistsException, WorkspaceAlreadyExistsException, DocumentMasterNotFoundException, UserGroupAlreadyExistsException, UserGroupNotFoundException {

        //login as user1
        com.sun.appserv.security.ProgrammaticLogin loginP = new ProgrammaticLogin();
        loginP.login(USER1_NAME, PASSWORD.toCharArray());

        userManagerLocal.createWorkspace(WORKSPACE_NAME, userManagerLocal.getAccount(USER1_NAME), "Test workspace", Workspace.VaultType.LARGE, false);
        documentManagerLocal.createFolder(WORKSPACE_NAME, FOLDER_NAME).getShortName();

        String[] group = new String[1];
        group[0] = "group1";
        userManagerLocal.createUserGroup("group1", userManagerLocal.getWorkspace(WORKSPACE_NAME)).getId();
        userManagerLocal.grantGroupAccess(WORKSPACE_NAME, group, grpAccess);
        userManagerLocal.addUserInGroup(new UserGroupKey(WORKSPACE_NAME, "group1"), USER2_NAME);


        UserGroup userGroup = em.find(UserGroup.class, new UserGroupKey(WORKSPACE_NAME, "group1"));
        ACLUserGroupEntry[] aclUserGroupEntries = new ACLUserGroupEntry[1];
        aclUserGroupEntries[0] = new ACLUserGroupEntry(new ACL(), userGroup, permission);

        documentManagerLocal.createDocumentMaster(WORKSPACE_NAME + "/" + FOLDER_NAME, DOCUMENT_ID, DOCUMENT_NAME, "Description", null, null, null, aclUserGroupEntries, null);

        System.out.println("CheckIn ...");
        documentManagerLocal.checkInDocument(new DocumentMasterKey(WORKSPACE_NAME, DOCUMENT_ID, "A"));

        loginP.logout();

        loginP.login(USER2_NAME, PASSWORD.toCharArray());
        System.out.println("User 2 check out the document ... ");
        documentManagerLocal.checkOutDocument(new DocumentMasterKey(WORKSPACE_NAME, DOCUMENT_ID, "A"));
        documentManagerLocal.getDocumentMaster(new DocumentMasterKey(WORKSPACE_NAME, DOCUMENT_ID, "A")).getIteration(1).setRevisionNote("This is my revision note");
        documentManagerLocal.checkInDocument(new DocumentMasterKey(WORKSPACE_NAME, DOCUMENT_ID, "A"));
        System.out.println("Revision Note: " + documentManagerLocal.getDocumentMaster(new DocumentMasterKey(WORKSPACE_NAME, DOCUMENT_ID, "A")).getIteration(1).getRevisionNote());
        loginP.logout();
    }


}