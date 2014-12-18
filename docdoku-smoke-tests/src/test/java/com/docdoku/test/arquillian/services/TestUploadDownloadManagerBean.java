package com.docdoku.test.arquillian.services;

import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.IUploadDownloadWS;
import com.docdoku.server.esindexer.ESIndexer;
import com.sun.enterprise.security.ee.auth.login.ProgrammaticLogin;

import javax.activation.DataHandler;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.io.IOException;

/**
 * @author Asmae CHADID
 */
@LocalBean
@Stateless
public class TestUploadDownloadManagerBean {

    @EJB
    private IUploadDownloadWS uploadDownloadWS;

    @EJB
    private ESIndexer esIndexer;

    private ProgrammaticLogin loginP = new ProgrammaticLogin();
    private String password = "password";

    public DataHandler downloadFromDocument(String login, String workspaceId, String docMId, String docMVersion, int iteration, String fileName) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, FileNotFoundException, UserNotActiveException {
        loginP.login(login, password.toCharArray());
        DataHandler dataHandler = uploadDownloadWS.downloadFromDocument(workspaceId, docMId, docMVersion, iteration, fileName);
        loginP.logout();
        return dataHandler;
    }

    public DataHandler downloadFromTemplate(String login, String workspaceId, String templateId, String fileName) throws NotAllowedException, FileNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        loginP.login(login, password.toCharArray());
        DataHandler dataHandler = uploadDownloadWS.downloadFromTemplate(workspaceId, templateId, fileName);
        loginP.logout();
        return dataHandler;
    }

    public DataHandler downloadFromPart(String login, String workspaceId, String partMNumber, String partRVersion, int iteration, String fileName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, NotAllowedException {
        loginP.login(login, password.toCharArray());
        DataHandler dataHandler = uploadDownloadWS.downloadFromPart(workspaceId, partMNumber, partRVersion, iteration, fileName);
        loginP.logout();
        return dataHandler;
    }

    public DataHandler downloadNativeFromPart(String login, String workspaceId, String partMNumber, String partRVersion, int iteration, String fileName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, NotAllowedException {
        loginP.login(login, password.toCharArray());
        DataHandler dataHandler = uploadDownloadWS.downloadNativeFromPart(workspaceId, partMNumber, partRVersion, iteration, fileName);
        loginP.logout();
        return dataHandler;
    }

    public void uploadToDocument(String login, String workspaceId, String docMId, String docMVersion, int iteration, String fileName, DataHandler data) throws CreationException, WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, AccessRightException, IOException {
        loginP.login(login, password.toCharArray());
        uploadDownloadWS.uploadToDocument(workspaceId, docMId, docMVersion, iteration, fileName, data);
        loginP.logout();

    }

    public void uploadToTemplate(String login, String workspaceId, String templateId, String fileName, DataHandler data) throws IOException, CreationException, WorkspaceNotFoundException, NotAllowedException, DocumentMasterTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, AccessRightException {
        loginP.login(login, password.toCharArray());
        loginP.login(login, password.toCharArray());
        uploadDownloadWS.uploadToTemplate(workspaceId, templateId, fileName, data);
        loginP.logout();

    }

    public void uploadGeometryToPart(String login, String workspaceId, String partMNumber, String partRVersion, int iteration, String fileName, int quality, DataHandler data) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException, IOException {
        loginP.login(login, password.toCharArray());
        uploadDownloadWS.uploadGeometryToPart(workspaceId, partMNumber, partRVersion, iteration, fileName, quality, data);
        loginP.logout();
    }

    public void uploadToPart(String login, String workspaceId, String partMNumber, String partRVersion, int iteration, String fileName, DataHandler data) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException, IOException {
        loginP.login(login, password.toCharArray());
        uploadDownloadWS.uploadToPart(workspaceId, partMNumber, partRVersion, iteration, fileName, data);
        loginP.logout();
    }

    public void uploadNativeCADToPart(String login, String workspaceId, String partMNumber, String partRVersion, int iteration, String fileName, DataHandler data) throws Exception {
        loginP.login(login, password.toCharArray());
        uploadDownloadWS.uploadNativeCADToPart(workspaceId, partMNumber, partRVersion, iteration, fileName, data);
        loginP.logout();
    }
}
