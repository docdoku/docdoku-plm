package com.docdoku.test.smoke;


import com.docdoku.core.services.IDocumentManagerWS;
import com.docdoku.cli.ScriptingTools;

import java.net.URL;


/**
 * Created with IntelliJ IDEA.
 * User: asmaechadid
 * Date: 11/03/13
 * Time: 10:10
 * To change this template use File | Settings | File Templates.
 */

public class DocumentCreation {

    private static String fileName = "testingFile";
    private static String folder = "testingFolder";
   // private TestParameters parameters = new TestParameters();
    private SmokeTestProperties properties = new SmokeTestProperties();

    public void createDocument() throws Exception {
        System.out.println(properties.getWebappURL());
        IDocumentManagerWS documentS = ScriptingTools.createDocumentService(properties.getURL(),properties.getLoginForUser1(),properties.getPassword());
        documentS.createFolder(properties.getWorkspace(), folder);
        System.out.println("Folder creation test has succeed");
        documentS.createDocumentMaster(properties.getWorkspace() + "/" + folder, fileName, "", null, null, null, null, null);
        System.out.println("Document creation test has succeed");
        documentS.deleteFolder(properties.getWorkspace() + "/" + folder);

    }


    public void deleteDocument() throws Exception {

        IDocumentManagerWS documentS = ScriptingTools.createDocumentService(properties.getURL(), properties.getLoginForUser1(), properties.getPassword());

        for (String oldFolder : documentS.getFolders(properties.getWorkspace())) {
            if (oldFolder.equals(folder)) {
                System.out.println(oldFolder + " was deleted");
                documentS.deleteFolder(properties.getWorkspace() + "/" + folder);
            }
            System.out.println("Folder was deleted successfully");
        }
    }




}