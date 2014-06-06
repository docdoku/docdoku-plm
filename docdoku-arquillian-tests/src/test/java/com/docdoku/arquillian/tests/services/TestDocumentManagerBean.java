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

package com.docdoku.arquillian.tests.services;

import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.sun.enterprise.security.ee.auth.login.ProgrammaticLogin;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * @author Taylor LABEJOF
 */

@LocalBean
@Stateless
public class TestDocumentManagerBean {

    @EJB
    private IDocumentManagerLocal documentManagerLocal;

    private ProgrammaticLogin loginP = new ProgrammaticLogin();
    private String password = "password";


    public Folder testFolderCreation(String login, String pWorkspace, String pFolder) throws UserNotActiveException, WorkspaceNotFoundException, AccessRightException, CreationException, FolderNotFoundException, UserNotFoundException, FolderAlreadyExistsException, NotAllowedException {
        loginP.login(login, password.toCharArray());
        Folder folder = documentManagerLocal.createFolder(pWorkspace, pFolder);
        loginP.logout();
        return folder;
    }

    public DocumentRevision testDocumentCreation(String login, String path, String documentId, ACLUserEntry[] pACLUserEntries, ACLUserGroupEntry[] pACLUserGroupEntries) throws WorkspaceNotFoundException, RoleNotFoundException, AccessRightException, CreationException, FolderNotFoundException, UserNotFoundException, NotAllowedException, DocumentMasterAlreadyExistsException, DocumentMasterTemplateNotFoundException, WorkflowModelNotFoundException, FileAlreadyExistsException, DocumentRevisionAlreadyExistsException {
        loginP.login(login, password.toCharArray());
        DocumentRevision documentRevision = documentManagerLocal.createDocumentMaster(path, documentId, "", "", null, null, pACLUserEntries, pACLUserGroupEntries, null);
        loginP.logout();
        return documentRevision;
    }

    public void testDocumentCheckIn(String login, DocumentRevisionKey documentRevisionKey) throws UserNotActiveException, WorkspaceNotFoundException, AccessRightException, UserNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, ESServerException {
        loginP.login(login, password.toCharArray());
        documentManagerLocal.checkInDocument(documentRevisionKey);
        loginP.logout();
    }

    public void testDocumentCheckOut(String login, DocumentRevisionKey documentRevisionKey) throws UserNotActiveException, WorkspaceNotFoundException, AccessRightException, UserNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, CreationException, FileAlreadyExistsException {
        loginP.login(login, password.toCharArray());
        documentManagerLocal.checkOutDocument(documentRevisionKey);
        loginP.logout();
    }
}