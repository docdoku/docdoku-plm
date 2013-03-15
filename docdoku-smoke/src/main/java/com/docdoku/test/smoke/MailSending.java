package com.docdoku.test.smoke;

import com.docdoku.cli.ScriptingTools;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.services.IDocumentManagerWS;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import java.util.Properties;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


/**
 * Created with IntelliJ IDEA.
 * User: asmaechadid
 * Date: 08/03/13
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public class MailSending {

    private static String folder = "testingFolderMail";

    private SmokeTestProperties properties = new SmokeTestProperties();

    public void checkInCheckOut() throws Exception {

        String fileName = "testingFileMail";
        IDocumentManagerWS documentS = ScriptingTools.createDocumentService(properties.getURL(), properties.getLoginForUser1(), properties.getPassword());
        assertTrue(documentS != null);
        documentS.createFolder(properties.getWorkspace(), folder);
        documentS.createDocumentMaster(properties.getWorkspace() + "/" + folder, fileName, "", null, null, null, null, null);
        DocumentMasterKey docMaster = new DocumentMasterKey();
        docMaster.setWorkspaceId(properties.getWorkspace());
        docMaster.setVersion("A");
        docMaster.setId(fileName);

        documentS.getStateChangeEventSubscriptions(properties.getWorkspace());
        documentS.subscribeToStateChangeEvent(docMaster);
        documentS.subscribeToIterationChangeEvent(docMaster);
        documentS.checkInDocument(docMaster);

        IDocumentManagerWS documentS2 = ScriptingTools.createDocumentService(properties.getURL(), properties.getLoginForUser2(), properties.getPassword());
        documentS2.createVersion(docMaster, "", "", null, null, null);
        documentS2.checkOutDocument(docMaster);
        documentS.deleteFolder(properties.getWorkspace() + "/" + folder);


        System.out.println("Mail sending test has been executed with success ");


    }


    public void checkMailReception() throws Exception {
        String mailSubject = "Iteration notification";
        //TestParameters params = new TestParameters();
        Thread.sleep(6000);
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", "demo0313", "password0313");
        javax.mail.Folder inbox = store.getFolder("Inbox");
        inbox.open(javax.mail.Folder.READ_WRITE);
        FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

        Message messages[] = inbox.search(ft);
        Message serverMessage = null;
        for (Message message : messages) {
            if (message.getFrom()[0].toString().equals(properties.getServerMailAddress()) && message.getSubject().equals(mailSubject)) {
                serverMessage = message;
                System.out.println(message.getFrom()[0].toString());
            }
            message.setFlag(Flags.Flag.SEEN, true);
        }
        assertFalse("Test mail failed. Notification Mail wasn't delivered.", serverMessage == null);


    }



}
