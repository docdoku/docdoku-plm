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
package com.docdoku.server;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentMasterTemplateKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.*;
import com.google.common.io.ByteStreams;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Florent Garin
 */
@MTOM
@Local(IUploadDownloadWS.class)
@Stateless(name = "UploadDownloadService")
@WebService(serviceName = "UploadDownloadService", endpointInterface = "com.docdoku.core.services.IUploadDownloadWS")
public class UploadDownloadService implements IUploadDownloadWS {

    @EJB
    private IDocumentManagerLocal documentService;

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IConverterManagerLocal converterService;

    @EJB
    private IDataManagerLocal dataManager;
    
    @RolesAllowed("users")
    @XmlMimeType("application/octet-stream")
    @Override
    public DataHandler downloadFromDocument(String workspaceId, String docMId, String docMVersion, int iteration, String fileName) throws NotAllowedException, FileNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        String fullName = workspaceId + "/documents/" + docMId + "/" + docMVersion + "/" + iteration + "/" + fileName;
        BinaryResource binaryResource = documentService.getBinaryResource(fullName);
        return new DataHandler(getBinaryResourceDataSource(binaryResource));
    }

    @RolesAllowed("users")
    @XmlMimeType("application/octet-stream")
    @Override
    public DataHandler downloadNativeFromPart(String workspaceId, String partMNumber, String partRVersion, int iteration, String fileName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, NotAllowedException {
        String fullName = workspaceId + "/parts/" + partMNumber + "/" + partRVersion + "/" + iteration + "/nativecad/" + fileName;
        BinaryResource binaryResource = productService.getBinaryResource(fullName);
        return new DataHandler(getBinaryResourceDataSource(binaryResource));
    }

    @RolesAllowed("users")
    @XmlMimeType("application/octet-stream")
    @Override
    public DataHandler downloadFromPart(String workspaceId, String partMNumber, String partRVersion, int iteration, String fileName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, NotAllowedException {
        String fullName = workspaceId + "/parts/" + partMNumber + "/" + partRVersion + "/" + iteration + "/" + fileName;
        BinaryResource binaryResource = productService.getBinaryResource(fullName);
        return new DataHandler(getBinaryResourceDataSource(binaryResource));
    }
    
    @RolesAllowed("users")
    @XmlMimeType("application/octet-stream")
    @Override
    public DataHandler downloadFromTemplate(String workspaceId, String templateID, String fileName) throws NotAllowedException, FileNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        String fullName = workspaceId + "/templates/" + templateID + "/" + fileName;
        BinaryResource binaryResource = documentService.getBinaryResource(fullName);
        return new DataHandler(getBinaryResourceDataSource(binaryResource));
    }

    @RolesAllowed("users")
    @Override
    public void uploadToDocument(String workspaceId, String docMId, String docMVersion, int iteration, String fileName,
            @XmlMimeType("application/octet-stream") DataHandler data) throws IOException, CreationException, WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, AccessRightException {

        DocumentIterationKey docPK = new DocumentIterationKey(workspaceId, docMId, docMVersion, iteration);

        BinaryResource binaryResource = documentService.saveFileInDocument(docPK, fileName, 0);

        long length = 0;

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = dataManager.getBinaryResourceOutputStream(binaryResource);
            inputStream = data.getInputStream();
            length = ByteStreams.copy(inputStream, outputStream);
        } catch (StorageException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }

        documentService.saveFileInDocument(docPK, fileName, length);
    }
    
    @RolesAllowed("users")
    @Override
    public void uploadGeometryToPart(String workspaceId, String partMNumber, String partRVersion, int iteration, String fileName, int quality,
            @XmlMimeType("application/octet-stream") DataHandler data) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException, IOException {

        PartIterationKey partIPK = new PartIterationKey(new PartRevisionKey(new PartMasterKey(workspaceId, partMNumber), partRVersion), iteration);
        BinaryResource binaryResource = productService.saveGeometryInPartIteration(partIPK, fileName, quality, 0, 0);

        long length = 0;

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = dataManager.getBinaryResourceOutputStream(binaryResource);
            inputStream = data.getInputStream();
            length = ByteStreams.copy(inputStream, outputStream);
        } catch (StorageException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }

        productService.saveGeometryInPartIteration(partIPK, fileName, quality, length, 0);
    }


    @RolesAllowed("users")
    @Override
    public void uploadNativeCADToPart(String workspaceId, String partMNumber, String partRVersion, int iteration, String fileName,
            @XmlMimeType("application/octet-stream") DataHandler data) throws Exception {

        PartIterationKey partIPK = new PartIterationKey(workspaceId, partMNumber, partRVersion, iteration);
        BinaryResource binaryResource = productService.saveNativeCADInPartIteration(partIPK, fileName, 0);

        long length = 0;

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = dataManager.getBinaryResourceOutputStream(binaryResource);
            inputStream = data.getInputStream();
            length = ByteStreams.copy(inputStream, outputStream);
        } catch (StorageException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }

        productService.saveNativeCADInPartIteration(partIPK, fileName, length);
        converterService.convertCADFileToJSON(partIPK, binaryResource);
    }

    @RolesAllowed("users")
    @Override
    public void uploadToPart(String workspaceId, String partMNumber, String partRVersion, int iteration, String fileName,
            @XmlMimeType("application/octet-stream") DataHandler data) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException, IOException {

        PartIterationKey partIPK = new PartIterationKey(new PartRevisionKey(new PartMasterKey(workspaceId, partMNumber), partRVersion), iteration);
        BinaryResource binaryResource = productService.saveFileInPartIteration(partIPK, fileName, 0);

        long length = 0;

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = dataManager.getBinaryResourceOutputStream(binaryResource);
            inputStream = data.getInputStream();
            length = ByteStreams.copy(inputStream, outputStream);
        } catch (StorageException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }

        productService.saveFileInPartIteration(partIPK, fileName, length);
    }    

    @RolesAllowed("users")
    @Override
    public void uploadToTemplate(String workspaceId, String templateID, String fileName,
            @XmlMimeType("application/octet-stream") DataHandler data) throws IOException, CreationException, WorkspaceNotFoundException, NotAllowedException, DocumentMasterTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, AccessRightException {

        DocumentMasterTemplateKey templatePK = new DocumentMasterTemplateKey(workspaceId, templateID);
        BinaryResource binaryResource = documentService.saveFileInTemplate(templatePK, fileName, 0);

        long length = 0;

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = dataManager.getBinaryResourceOutputStream(binaryResource);
            inputStream = data.getInputStream();
            length = ByteStreams.copy(inputStream, outputStream);
        } catch (StorageException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }

        documentService.saveFileInTemplate(templatePK, fileName, length);
    }

    private DataSource getBinaryResourceDataSource(final BinaryResource binaryResource) {
        try {

            final InputStream binaryContentInputStream = dataManager.getBinaryResourceInputStream(binaryResource);

            DataSource binaryResourceDataSource = new DataSource() {
                @Override
                public InputStream getInputStream() throws IOException {
                    return binaryContentInputStream;
                }

                @Override
                public OutputStream getOutputStream() throws IOException {
                    throw new UnsupportedOperationException("Not implemented");
                }

                @Override
                public String getContentType() {
                    return FileTypeMap.getDefaultFileTypeMap().getContentType(binaryResource.getName());
                }

                @Override
                public String getName() {
                    return binaryResource.getName();
                }
            };

            return binaryResourceDataSource;

        } catch (StorageException e) {
            e.printStackTrace();
            return null;
        }
    }
}
