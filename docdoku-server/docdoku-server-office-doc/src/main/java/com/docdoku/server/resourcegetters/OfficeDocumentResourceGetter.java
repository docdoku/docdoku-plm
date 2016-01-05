/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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
package com.docdoku.server.resourcegetters;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.exceptions.ConvertedResourceException;
import com.docdoku.core.exceptions.StorageException;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.util.Tools;
import com.docdoku.server.InternalService;
import com.docdoku.server.extras.TitleBlockGenerator;
import com.google.common.io.ByteStreams;
import com.itextpdf.text.DocumentException;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.logging.Logger;

public class OfficeDocumentResourceGetter implements DocumentResourceGetter {

    private static final Logger LOGGER = Logger.getLogger(DocumentResourceGetter.class.getName());

    @Inject
    private FileConverter fileConverter;

    @InternalService
    @Inject
    private IDataManagerLocal dataManager;


    @Override
    public boolean canGetConvertedResource(String outputFormat, BinaryResource binaryResource) {
        return FileIO.isDocFile(binaryResource.getName()) && outputSupported(outputFormat);
    }

    private boolean outputSupported(String outputFormat) {
        return outputFormat != null && "pdf".equals(outputFormat);
    }

    @Override
    public InputStream getConvertedResource(String outputFormat, BinaryResource binaryResource, DocumentIteration docI, Locale locale) throws ConvertedResourceException {
        try {
            // TODO check for resources to be closed
            InputStream inputStream=null;

            if ("pdf".equals(outputFormat)) {
                inputStream = getPdfConvertedResource(binaryResource);
            }

            if ("documents".equals(binaryResource.getOwnerType()) && docI != null){
                return TitleBlockGenerator.addBlockTitleToPDF(inputStream, docI, locale);
            }

            return inputStream;
        } catch (StorageException | DocumentException | IOException e) {
            throw new ConvertedResourceException(locale,e);
        }
    }

    @Override
    public InputStream getConvertedResource(String outputFormat, BinaryResource binaryResource, PartIteration partIteration, Locale locale) throws ConvertedResourceException {
        try {
            InputStream inputStream=null;

            if("pdf".equals(outputFormat)) {
                inputStream = getPdfConvertedResource(binaryResource);
            }

            if("parts".equals(binaryResource.getOwnerType()) && partIteration != null) {
                return TitleBlockGenerator.addBlockTitleToPDF(inputStream,partIteration,locale);
            }

            return inputStream;
        } catch (StorageException | DocumentException | IOException e) {
            throw new ConvertedResourceException(locale,e);
        }
    }

    private InputStream getPdfConvertedResource(BinaryResource binaryResource) throws StorageException, IOException {
        String extension = FileIO.getExtension(binaryResource.getName());
        InputStream inputStream;

        if ("pdf".equals(extension)) {
            inputStream = dataManager.getBinaryResourceInputStream(binaryResource);
        } else {
            String subResourceVirtualPath = FileIO.getFileNameWithoutExtension(binaryResource.getName()) + ".pdf";
            if (dataManager.exists(binaryResource, subResourceVirtualPath) &&
                    dataManager.getLastModified(binaryResource, subResourceVirtualPath).after(binaryResource.getLastModified())) {
                //if the resource is already converted, return it
                inputStream = dataManager.getBinarySubResourceInputStream(binaryResource, subResourceVirtualPath);
            } else {
                String normalizedName = Tools.unAccent(binaryResource.getName());
                //copy the converted file for further reuse

                // TODO check for resources to be closed
                try (OutputStream outputStream = dataManager.getBinarySubResourceOutputStream(binaryResource, subResourceVirtualPath);
                     InputStream binaryResourceInputStream = dataManager.getBinaryResourceInputStream(binaryResource);
                     InputStream inputStreamConverted = fileConverter.convertToPDF(normalizedName,binaryResourceInputStream)) {
                    ByteStreams.copy(inputStreamConverted, outputStream);
                }
                inputStream = dataManager.getBinarySubResourceInputStream(binaryResource, subResourceVirtualPath);
            }
        }

        return inputStream;
    }

    @Override
    public boolean canGetSubResourceVirtualPath(BinaryResource binaryResource) {
        return false;
    }

    @Override
    public String getSubResourceVirtualPath(BinaryResource binaryResource, String subResourceUri) {
        return null;
    }

}
