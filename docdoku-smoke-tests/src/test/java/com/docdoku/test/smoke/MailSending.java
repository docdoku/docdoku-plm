/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.test.smoke;

import com.docdoku.cli.ScriptingTools;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.services.IDocumentManagerWS;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;


/**
 * @author Asmae Chadid
 *
 */
public class MailSending {

    private final static String FOLDER_NAME = "test";
    private final static String DOCUMENT_ID = "Test-Document";
    private final static String MAIL_SUBJECT = "Iteration notification";
    private SmokeTestProperties properties = new SmokeTestProperties();


    public void checkInCheckOut() throws Exception {

        IDocumentManagerWS documentS = ScriptingTools.createDocumentService(properties.getURL(), properties.getLoginForUser1(), properties.getPassword());
        assertNotNull(documentS);
        documentS.createFolder(properties.getWorkspace(), FOLDER_NAME);
        documentS.createDocumentMaster(properties.getWorkspace() + "/" + FOLDER_NAME, DOCUMENT_ID, "", null, null, null, null, null, null);
        DocumentRevisionKey docRevision = new DocumentRevisionKey(new DocumentMasterKey(properties.getWorkspace(), DOCUMENT_ID),"A");

        documentS.subscribeToIterationChangeEvent(docRevision);
        documentS.checkInDocument(docRevision);

        IDocumentManagerWS documentS2 = ScriptingTools.createDocumentService(properties.getURL(), properties.getLoginForUser2(), properties.getPassword());
        documentS2.checkOutDocument(docRevision);
        documentS.deleteFolder(properties.getWorkspace() + "/" + FOLDER_NAME);
    }


    public void checkMailReception() throws Exception {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect(properties.getImapServer(), properties.getImapLogin(), properties.getImapPassword());
        javax.mail.Folder inbox = store.getFolder("Inbox");
        inbox.open(javax.mail.Folder.READ_WRITE);
        FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

        Message messages[] = inbox.search(ft);
        Message serverMessage = null;
        for (Message message : messages) {
            if (message.getSubject().equals(MAIL_SUBJECT)) {
                serverMessage = message;
                break;
            }
        }
        assertFalse("Mail wasn't delivered.", serverMessage == null);
        serverMessage.setFlag(Flags.Flag.DELETED, true);
    }



}
