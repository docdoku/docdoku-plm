package com.docdoku.arquillian.tests.util;

import com.docdoku.arquillian.tests.services.TestDocumentManagerBean;
import com.docdoku.arquillian.tests.services.TestUserManagerBean;

import javax.ejb.EJB;

/**
 * Created by asmae on 03/12/14.
 */
public class TestUtil {

    public static final String FOLDER_TEST = "Folder1";
    public static String WORKSPACE_TEST = "TEST_WORKSPACE";
    public static String USER1_TEST = "user1";
    public static String USER2_TEST = "user2";
    public static String USER3_TEST = "user3";


    @EJB
    private static TestDocumentManagerBean documentManagerBean;
    @EJB
    private static TestUserManagerBean userManagerBean;
    
    public static void init() throws Exception{
        userManagerBean.testWorkspaceCreation(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST);
        userManagerBean.testAddingUserInWorkspace(TestUtil.USER1_TEST, TestUtil.USER2_TEST, TestUtil.WORKSPACE_TEST);
        userManagerBean.testAddingUserInWorkspace(TestUtil.USER1_TEST, TestUtil.USER3_TEST, TestUtil.WORKSPACE_TEST);
        documentManagerBean.createFolder(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST,TestUtil.FOLDER_TEST);

    }
}
