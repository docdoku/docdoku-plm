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
import com.docdoku.core.util.FileIO;
import viewers.DocumentViewer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

public class DocViewerImpl implements DocumentViewer {

    @Override
    public boolean canGetResourceForViewer(File file, HttpServletRequest pRequest) {
        return FileIO.isDocFile(file.getName()) && hasValidFPV(pRequest);
    }

    private boolean hasValidFPV(HttpServletRequest pRequest) {
        String fpvParam = pRequest.getParameter("fpv");
        return fpvParam != null &&
                ("pdf".equals(fpvParam) || "swf".equals(fpvParam));
    }

    @Override
    public File getFileForViewer(HttpServletRequest pRequest, HttpServletResponse pResponse, ServletContext servletContext, File dataFile) throws Exception {
        File result = null;
        if ("pdf".equals(pRequest.getParameter("fpv"))) {
            pResponse.setContentType("application/pdf");
            String ooHome = servletContext.getInitParameter("OO_HOME");
            int ooPort = Integer.parseInt(servletContext.getInitParameter("OO_PORT"));
            result = new FileConverter(ooHome, ooPort).convertToPDF(dataFile);
        } else if ("swf".equals(pRequest.getParameter("fpv"))) {
            pResponse.setContentType("application/x-shockwave-flash");
            String pdf2SWFHome = servletContext.getInitParameter("PDF2SWF_HOME");
            String ooHome = servletContext.getInitParameter("OO_HOME");
            int ooPort = Integer.parseInt(servletContext.getInitParameter("OO_PORT"));
            FileConverter fileConverter = new FileConverter(pdf2SWFHome, ooHome, ooPort);
            result = fileConverter.convertToSWF(dataFile);
        }
        return result;
    }

    @Override
    public boolean canVisualize(String fileName) {
        return FileIO.isDocFile(fileName);
    }

    @Override
    public String getHtmlForViewer(BinaryResource file) {
        return null;
    }

}
