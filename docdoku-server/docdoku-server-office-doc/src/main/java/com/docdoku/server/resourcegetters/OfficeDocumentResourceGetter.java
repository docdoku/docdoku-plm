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
package com.docdoku.server.resourcegetters;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.extras.TitleBlockGenerator;
import com.google.common.io.ByteStreams;

import javax.ejb.EJB;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class OfficeDocumentResourceGetter implements DocumentResourceGetter {

    @EJB
    private IDataManagerLocal dataManager;

    @EJB
    private IDocumentManagerLocal documentService;

    @EJB
    private FileConverter fileConverter;

    @Override
    public boolean canGetConvertedResource(String outputFormat, BinaryResource binaryResource) {
        return FileIO.isDocFile(binaryResource.getName()) && outputSupported(outputFormat);
    }

    private boolean outputSupported(String outputFormat) {
        return outputFormat != null && outputFormat.equals("pdf");
    }

    @Override
    public InputStream getConvertedResource(String outputFormat, BinaryResource binaryResource, DocumentIteration docI, User user) throws Exception {

        String extension = FileIO.getExtension(binaryResource.getName());

        InputStream inputStream = null;

        if ("pdf".equals(outputFormat)) {

            if (extension.equals("pdf")) {
                inputStream = dataManager.getBinaryResourceInputStream(binaryResource);
            } else {
                String subResourceVirtualPath = FileIO.getFileNameWithoutExtension(binaryResource.getName()) + ".pdf";
                if (dataManager.exists(binaryResource, subResourceVirtualPath) &&
                        dataManager.getLastModified(binaryResource, subResourceVirtualPath).after(binaryResource.getLastModified())) {
                    //if the resource is already converted, return it
                    inputStream = dataManager.getBinarySubResourceInputStream(binaryResource, subResourceVirtualPath);
                } else {
                    InputStream inputStreamConverted = fileConverter.convertToPDF(binaryResource.getName(), dataManager.getBinaryResourceInputStream(binaryResource));
                    //copy the converted file for further reuse
                    OutputStream outputStream = dataManager.getBinarySubResourceOutputStream(binaryResource, subResourceVirtualPath);
                    try {
                        ByteStreams.copy(inputStreamConverted, outputStream);
                    } finally {
                        inputStreamConverted.close();
                        outputStream.flush();
                        outputStream.close();
                    }
                    inputStream = dataManager.getBinarySubResourceInputStream(binaryResource, subResourceVirtualPath);
                }
            }
        }

        if(binaryResource.getOwnerType().equals("documents")){
            if(docI != null){
                return TitleBlockGenerator.addBlockTitleToPDF(inputStream, docI, new Locale(user.getLanguage()));
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
