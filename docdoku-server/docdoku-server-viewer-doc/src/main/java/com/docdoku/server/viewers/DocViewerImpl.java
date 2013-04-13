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
package com.docdoku.server.viewers;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.StorageException;
import com.docdoku.core.util.FileIO;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DocViewerImpl implements DocumentViewer {

    @EJB
    private IDataManagerLocal dataManager;

    @Override
    public boolean canPrepareFileForViewer(BinaryResource binaryResource, HttpServletRequest pRequest) {
        return FileIO.isDocFile(binaryResource.getName()) && hasValidFPV(pRequest);
    }

    private boolean hasValidFPV(HttpServletRequest pRequest) {
        String fpvParam = pRequest.getParameter("fpv");
        return fpvParam != null &&
                ("pdf".equals(fpvParam) || "swf".equals(fpvParam));
    }

    @Override
    public InputStream prepareFileForViewer(HttpServletRequest pRequest, HttpServletResponse pResponse, ServletContext servletContext, final BinaryResource binaryResource) throws Exception {
        File result = null;

        //TODO : check if we already got the converted file, if not do the conversion and store the converted file

        String flexPaperViewerType = pRequest.getParameter("fpv");

        String ooHome = servletContext.getInitParameter("OO_HOME");
        int ooPort = Integer.parseInt(servletContext.getInitParameter("OO_PORT"));

        File tmpDir = com.google.common.io.Files.createTempDir();
        File tmpDocFile = new File(tmpDir, binaryResource.getName());
        Files.copy(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                try {
                    return dataManager.getBinaryContentInputStream(binaryResource);
                } catch (StorageException e) {
                    e.printStackTrace();
                    throw new IOException(e);
                }
            }
        }, tmpDocFile);

        if ("pdf".equals(flexPaperViewerType)) {
            pResponse.setContentType("application/pdf");
            result = new FileConverter(ooHome, ooPort).convertToPDF(tmpDocFile);
        } else if ("swf".equals(flexPaperViewerType)) {
            pResponse.setContentType("application/x-shockwave-flash");
            String pdf2SWFHome = servletContext.getInitParameter("PDF2SWF_HOME");
            result = new FileConverter(pdf2SWFHome, ooHome, ooPort).convertToSWF(tmpDocFile);
        }

        return new FileInputStream(result);
    }

    @Override
    public boolean canRenderViewerTemplate(BinaryResource binaryResource) {
        return FileIO.isDocFile(binaryResource.getName());
    }

    @Override
    public String renderHtmlForViewer(BinaryResource docResource) throws Exception {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("com/docdoku/server/viewers/document_viewer.mustache");
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("uriResource", ViewerUtils.getURI(docResource));
        scopes.put("fileName", docResource.getName());
        StringWriter templateWriter = new StringWriter();
        mustache.execute(templateWriter, scopes).flush();
        return templateWriter.toString();
    }

}
