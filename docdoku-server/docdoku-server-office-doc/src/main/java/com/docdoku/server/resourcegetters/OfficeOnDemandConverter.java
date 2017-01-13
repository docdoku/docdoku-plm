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
package com.docdoku.server.resourcegetters;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.exceptions.ConvertedResourceException;
import com.docdoku.core.exceptions.StorageException;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.services.IBinaryStorageManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.util.Tools;
import com.docdoku.server.InternalService;
import com.docdoku.server.converters.OnDemandConverter;
import com.docdoku.server.extras.TitleBlockGenerator;
import com.google.common.io.ByteStreams;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Florent Garin
 */
public class OfficeOnDemandConverter implements OnDemandConverter {

    private static final Logger LOGGER = Logger.getLogger(OnDemandConverter.class.getName());

    @Inject
    private FileConverter fileConverter;

    @InternalService
    @Inject
    private IBinaryStorageManagerLocal storageManager;


    @Override
    public boolean canConvert(String outputFormat, BinaryResource binaryResource) {
        return FileIO.isDocFile(binaryResource.getName()) && outputSupported(outputFormat);
    }

    private boolean outputSupported(String outputFormat) {
        return outputFormat != null && "pdf".equals(outputFormat);
    }

    @Override
    public InputStream getConvertedResource(String outputFormat, BinaryResource binaryResource, DocumentIteration docI, Locale locale) throws ConvertedResourceException {
        try {
            // TODO check for resources to be closed
            InputStream inputStream = null;

            if ("pdf".equals(outputFormat)) {
                inputStream = getPdfConvertedResource(binaryResource);
            }

            if ("documents".equals(binaryResource.getHolderType()) && docI != null) {
                LOGGER.log(Level.INFO, "Adding document information to first pages");
                return TitleBlockGenerator.addBlockTitleToPDF(inputStream, docI, locale);
            }

            return inputStream;
        } catch (StorageException | IOException e) {
            throw new ConvertedResourceException(locale, e);
        }
    }

    @Override
    public InputStream getConvertedResource(String outputFormat, BinaryResource binaryResource, PartIteration partIteration, Locale locale) throws ConvertedResourceException {
        try {
            InputStream inputStream = null;

            if ("pdf".equals(outputFormat)) {
                inputStream = getPdfConvertedResource(binaryResource);
            }

            if ("parts".equals(binaryResource.getHolderType()) && partIteration != null) {
                return TitleBlockGenerator.addBlockTitleToPDF(inputStream, partIteration, locale);
            }

            return inputStream;
        } catch (StorageException | IOException e) {
            throw new ConvertedResourceException(locale, e);
        }
    }

    private InputStream getPdfConvertedResource(BinaryResource binaryResource) throws StorageException, IOException {

        InputStream inputStream;

        String extension = FileIO.getExtension(binaryResource.getName());

        if ("pdf".equals(extension)) {
            LOGGER.log(Level.INFO, "File is already as pdf format");
            return storageManager.getBinaryResourceInputStream(binaryResource);
        }

        String pdfFileName = FileIO.getFileNameWithoutExtension(binaryResource.getName()) + ".pdf";

        if (storageManager.exists(binaryResource, pdfFileName) &&
                storageManager.getLastModified(binaryResource, pdfFileName).after(binaryResource.getLastModified())) {
            LOGGER.log(Level.INFO, "File is already converted to pdf");
            inputStream = storageManager.getGeneratedFileInputStream(binaryResource, pdfFileName);
        } else {
            LOGGER.log(Level.INFO, "Converting " + binaryResource.getName() + " to pdf");
            String normalizedName = Tools.unAccent(binaryResource.getName());

            //copy the converted file for further reuse
            // TODO check for resources to be closed
            try (OutputStream outputStream = storageManager.getGeneratedFileOutputStream(binaryResource, pdfFileName);
                 InputStream binaryResourceInputStream = storageManager.getBinaryResourceInputStream(binaryResource);
                 InputStream inputStreamConverted = fileConverter.convertToPDF(normalizedName, binaryResourceInputStream)) {
                ByteStreams.copy(inputStreamConverted, outputStream);
            }
            inputStream = storageManager.getGeneratedFileInputStream(binaryResource, pdfFileName);
        }


        return inputStream;
    }

}
