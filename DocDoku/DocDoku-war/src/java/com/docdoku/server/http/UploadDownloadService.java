/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.server.http;

import com.docdoku.core.FileAlreadyExistsException;
import com.docdoku.core.FileNotFoundException;
import com.docdoku.core.ICommandLocal;
import com.docdoku.core.MasterDocumentNotFoundException;
import com.docdoku.core.MasterDocumentTemplateNotFoundException;
import com.docdoku.core.NotAllowedException;
import com.docdoku.core.UserNotActiveException;
import com.docdoku.core.UserNotFoundException;
import com.docdoku.core.WorkspaceNotFoundException;
import com.docdoku.core.entities.keys.BasicElementKey;
import com.docdoku.core.entities.keys.DocumentKey;
import com.sun.xml.ws.developer.StreamingDataHandler;
import java.io.File;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.TransactionManagement;
import javax.jws.WebService;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;

/**
 *
 * @author Florent GARIN
 */
@MTOM
@WebService(name = "UploadDownload", serviceName = "UploadDownloadService", portName = "UploadDownloadPort", wsdlLocation = "")
public class UploadDownloadService {

    @EJB
    private ICommandLocal commandService;
    @Resource
    private UserTransaction utx;

    @RolesAllowed("users")
    public
    @XmlMimeType("application/octet-stream")
    DataHandler downloadFromDocument(String workspaceId, String mdocID, String mdocVersion, int iteration, String fileName) throws NotAllowedException, FileNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        String fullName = workspaceId + "/documents/" + mdocID + "/" + mdocVersion + "/" + iteration + "/" + fileName;
        File dataFile = commandService.getDataFile(fullName);

        return new DataHandler(new FileDataSource(dataFile));
    }

    @RolesAllowed("users")
    public
    @XmlMimeType("application/octet-stream")
    DataHandler downloadFromTemplate(String workspaceId, String templateID, String fileName) throws NotAllowedException, FileNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        String fullName = workspaceId + "/templates/" + templateID + "/" + fileName;
        File dataFile = commandService.getDataFile(fullName);

        return new DataHandler(new FileDataSource(dataFile));
    }

    @RolesAllowed("users")
    public void uploadToDocument(String workspaceId, String mdocID, String mdocVersion, int iteration, String fileName,
            @XmlMimeType("application/octet-stream") DataHandler data) throws NotAllowedException, MasterDocumentNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException {
        DocumentKey docPK = null;
        File vaultFile = null;
        try {
            utx.begin();
            docPK = new DocumentKey(workspaceId, mdocID, mdocVersion, iteration);
            vaultFile = commandService.saveFileInDocument(docPK, fileName, 0);
            vaultFile.getParentFile().mkdirs();
            vaultFile.createNewFile();

            StreamingDataHandler dh = (StreamingDataHandler) data;
            dh.moveTo(vaultFile);
            dh.close();
            commandService.saveFileInDocument(docPK, fileName, vaultFile.length());
            utx.commit();
        } catch (Exception pEx) {
            throw new WebServiceException("Error while uploading the file.", pEx);
        } finally {
            try {
                if (utx.getStatus() == Status.STATUS_ACTIVE || utx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                    utx.rollback();
                }
            } catch (Exception pRBEx) {
                throw new WebServiceException("Rollback failed.", pRBEx);
            }
        }
    }

    @RolesAllowed("users")
    public void uploadToTemplate(String workspaceId, String templateID, String fileName,
            @XmlMimeType("application/octet-stream") DataHandler data) throws NotAllowedException, MasterDocumentTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException {
        BasicElementKey templatePK = null;
        File vaultFile = null;
        try {
            utx.begin();
            templatePK = new BasicElementKey(workspaceId, templateID);
            vaultFile = commandService.saveFileInTemplate(templatePK, fileName, 0);
            vaultFile.getParentFile().mkdirs();
            vaultFile.createNewFile();

            StreamingDataHandler dh = (StreamingDataHandler) data;
            dh.moveTo(vaultFile);
            dh.close();
            commandService.saveFileInTemplate(templatePK, fileName, vaultFile.length());
            utx.commit();
        } catch (Exception pEx) {
            throw new WebServiceException("Error while uploading the file.", pEx);
        } finally {
            try {
                if (utx.getStatus() == Status.STATUS_ACTIVE || utx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                    utx.rollback();
                }
            } catch (Exception pRBEx) {
                throw new WebServiceException("Rollback failed.", pRBEx);
            }
        }
    }
}
