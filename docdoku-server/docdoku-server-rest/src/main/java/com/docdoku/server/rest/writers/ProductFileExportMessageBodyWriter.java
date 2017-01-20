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
import com.docdoku.core.configuration.ProductInstanceIteration;
import com.docdoku.core.configuration.ProductInstanceMaster;
import com.docdoku.core.configuration.ProductInstanceMasterKey;
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.services.IBinaryStorageManagerLocal;
import com.docdoku.core.services.IProductInstanceManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.util.ProductFileExport;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

@Provider
public class ProductFileExportMessageBodyWriter implements MessageBodyWriter<ProductFileExport> {

    private static final Logger LOGGER = Logger.getLogger(ProductFileExportMessageBodyWriter.class.getName());
    @Inject
    private IBinaryStorageManagerLocal storageManager;
    @Inject
    private IProductManagerLocal productService;
    @Inject
    private IProductInstanceManagerLocal productInstanceService;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(ProductFileExport.class);
    }

    @Override
    public long getSize(ProductFileExport productFileExport, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(ProductFileExport productFileExport, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {

        ZipOutputStream zs = new ZipOutputStream(outputStream);

        try {

            Map<String, Set<BinaryResource>> binariesInTree = productService.getBinariesInTree(productFileExport.getBaselineId(), productFileExport.getConfigurationItemKey().getWorkspace(), productFileExport.getConfigurationItemKey(), productFileExport.getPsFilter(), productFileExport.isExportNativeCADFile(), productFileExport.isExportDocumentLinks());
            Set<Map.Entry<String, Set<BinaryResource>>> entries = binariesInTree.entrySet();
            List<String> baselinedSourcesName = new ArrayList<>();

            if (productFileExport.isExportDocumentLinks() && productFileExport.getBaselineId() != null) {
                List<BinaryResource> baselinedSources = productService.getBinaryResourceFromBaseline(productFileExport.getBaselineId());

                for (BinaryResource binaryResource : baselinedSources) {
                    String[] parts = binaryResource.getFullName().split("/");
                    String folderName = parts[2] + "-" + parts[3] + "-" + parts[4];
                    baselinedSourcesName.add(folderName);
                    addToZipFile(binaryResource, "links/" + folderName, zs);
                }
            }

            for (Map.Entry<String, Set<BinaryResource>> entry : entries) {
                String partNumberFolderName = entry.getKey();

                Set<BinaryResource> files = entry.getValue();

                for (BinaryResource binaryResource : files) {
                    addToZip(binaryResource, partNumberFolderName, zs);
                }
            }

            if (productFileExport.getSerialNumber() != null) {
                addProductInstanceDataToZip(zs, productFileExport.getConfigurationItemKey(), productFileExport.getSerialNumber(), baselinedSourcesName);
            }

        } catch (UserNotFoundException | UserNotActiveException | WorkspaceNotFoundException | ConfigurationItemNotFoundException |
                NotAllowedException | EntityConstraintException | PartMasterNotFoundException | ProductInstanceMasterNotFoundException |
                StorageException | WorkspaceNotEnabledException e) {
            LOGGER.log(Level.FINEST, null, e);
        }

        zs.close();

    }

    private void addToZip(BinaryResource binaryResource, String partNumberFolderName, ZipOutputStream zs) {
        try {
            String fileType = binaryResource.getFileType();
            String folderName = partNumberFolderName + (fileType == null ? "" : "/" + fileType);
            addToZipFile(binaryResource, folderName, zs);
        } catch (IOException | StorageException e) {
            LOGGER.log(Level.SEVERE, "Something went wrong while adding file to zip", e);
        }
    }

    private void addProductInstanceDataToZip(ZipOutputStream zs, ConfigurationItemKey configurationItemKey, String serialNumber, List<String> baselinedSourcesName) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ProductInstanceMasterNotFoundException, IOException, StorageException, WorkspaceNotEnabledException {
        ProductInstanceMaster productInstanceMaster = productInstanceService.getProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, configurationItemKey));
        ProductInstanceIteration lastIteration = productInstanceMaster.getLastIteration();

        for (BinaryResource attachedFile : lastIteration.getAttachedFiles()) {
            addToZipFile(attachedFile, "attachedfiles", zs);
        }

        for (DocumentLink docLink : lastIteration.getLinkedDocuments()) {
            for (BinaryResource linkedFile : docLink.getTargetDocument().getLastIteration().getAttachedFiles()) {
                String folderName = docLink.getTargetDocument().getLastIteration().toString();

                if (!baselinedSourcesName.contains(folderName)) {
                    addToZipFile(linkedFile, "links/" + folderName, zs);
                }
            }
        }
    }

    public void addToZipFile(BinaryResource binaryResource, String folderName, ZipOutputStream zos) throws IOException, StorageException {

        try (InputStream binaryResourceInputStream = storageManager.getBinaryResourceInputStream(binaryResource)) {
            FileExportTools.addToZipFile(binaryResourceInputStream, binaryResource.getName(), folderName, zos);
        }
    }

}
