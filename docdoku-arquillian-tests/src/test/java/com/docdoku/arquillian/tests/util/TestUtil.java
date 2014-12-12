package com.docdoku.arquillian.tests.util;

import com.docdoku.arquillian.tests.services.TestDocumentManagerBean;
import com.docdoku.arquillian.tests.services.TestUserManagerBean;

import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.File;


/**
 * @author Asmae CHADID
 */
public class TestUtil {

    public static final String FOLDER_TEST = "TEST_FOLDER";
    public static String WORKSPACE_TEST = "TEST_WORKSPACE";
    public static String USER1_TEST = "user1";
    public static String USER2_TEST = "user2";
    public static String USER3_TEST = "user3";
    private boolean init = false;


    public  void init( TestUserManagerBean userManagerBean,TestDocumentManagerBean documentManagerBean) throws Exception {
       if (!init)
       {userManagerBean.testWorkspaceCreation(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST);
        userManagerBean.testAddingUserInWorkspace(TestUtil.USER1_TEST, TestUtil.USER2_TEST, TestUtil.WORKSPACE_TEST);
        userManagerBean.testAddingUserInWorkspace(TestUtil.USER1_TEST, TestUtil.USER3_TEST, TestUtil.WORKSPACE_TEST);
        documentManagerBean.createFolder(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, TestUtil.FOLDER_TEST);
           init = true;
       }
    }


    public static void deleteDownloadedFiles(String fileName) {
        File index = new File(fileName);
        if (index.exists()) {
            while (index.isDirectory()) {
                if (index.listFiles()[0].isDirectory())
                    index = index.listFiles()[0];
                else break;
            }


            for (File file : index.listFiles()) {
                file.delete();
            }

            while (!index.getName().equals(fileName) && index.isDirectory()) {
                if (index.getParentFile().isDirectory()) {
                    File directory = index;
                    index = index.getParentFile();
                    directory.delete();
                }
            }

            File directory = new File(fileName);
            directory.delete();
        }

    }

}
