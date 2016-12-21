/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.server.rest.writers;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.configuration.BaselinedDocumentBinaryResourceCollection;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.IBinaryStorageManagerLocal;
import com.docdoku.core.services.IDocumentBaselineManagerLocal;
import com.docdoku.server.rest.util.DocumentBaselineFileExport;
import com.docdoku.server.rest.util.FileExportTools;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

@Provider
public class DocumentBaselineFileExportMessageBodyWriter implements MessageBodyWriter<DocumentBaselineFileExport> {

    private static final Logger LOGGER = Logger.getLogger(DocumentBaselineFileExportMessageBodyWriter.class.getName());
    @Inject
    private IBinaryStorageManagerLocal storageManager;
    @Inject
    private IDocumentBaselineManagerLocal documentBaselineService;

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return type.equals(DocumentBaselineFileExport.class);
    }

    @Override
    public long getSize(DocumentBaselineFileExport documentBaselineFileExport, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(DocumentBaselineFileExport documentBaselineFileExport, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream)
            throws IOException, WebApplicationException {

        ZipOutputStream zs = new ZipOutputStream(outputStream);

        try {
            List<BaselinedDocumentBinaryResourceCollection> binaryResourceCollections = documentBaselineService.getBinaryResourcesFromBaseline(documentBaselineFileExport.getWorkspaceId(), documentBaselineFileExport.getBaselineId());
            for (BaselinedDocumentBinaryResourceCollection collection : binaryResourceCollections) {
                for (BinaryResource binaryResource : collection.getAttachedFiles()) {
                    addToZip(collection, binaryResource, zs);
                }
            }
        } catch (UserNotFoundException | UserNotActiveException | WorkspaceNotFoundException | WorkspaceNotEnabledException | BaselineNotFoundException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }

        zs.close();
    }

    private void addToZip(BaselinedDocumentBinaryResourceCollection collection, BinaryResource binaryResource, ZipOutputStream zs) {
        try {
            String folderName = collection.getRootFoldername() + "/attachedFiles";
            addToZipFile(binaryResource, folderName, zs);
        } catch (IOException | StorageException e) {
            LOGGER.log(Level.SEVERE, "Something went wrong while adding file to zip", e);
        }
    }

    public void addToZipFile(BinaryResource binaryResource, String folderName, ZipOutputStream zos)
            throws IOException, StorageException {

        try (InputStream binaryResourceInputStream = storageManager.getBinaryResourceInputStream(binaryResource)) {
            FileExportTools.addToZipFile(binaryResourceInputStream, binaryResource.getName(), folderName, zos);
        }
    }
}
