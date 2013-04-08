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
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentViewerManagerLocal;
import viewers.DocumentViewer;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@Stateless(name="DocumentViewerBean")
public class DocumentViewerBean implements IDocumentViewerManagerLocal {

    @EJB
    private IDocumentManagerLocal documentService;

    @Inject
    @Any
    private Instance<DocumentViewer> documentViewers;

    @Resource(name = "vaultPath")
    private String vaultPath;

    private DocumentViewer selectViewer(String resourceFullName) {
        DocumentViewer selectedDocumentViewer = null;
        for (DocumentViewer documentViewer : documentViewers) {
            if (documentViewer.canVisualize(resourceFullName)) {
                selectedDocumentViewer = documentViewer;
                break;
            }
        }
        return selectedDocumentViewer;
    }

    @Override
    public File getFileForViewer(HttpServletRequest pRequest, HttpServletResponse pResponse, ServletContext servletContext, File dataFile) throws Exception {
        DocumentViewer selectedDocumentViewer = null;
        for (DocumentViewer documentViewer : documentViewers) {
            if (documentViewer.canGetResourceForViewer(dataFile,pRequest)) {
                selectedDocumentViewer = documentViewer;
                break;
            }
        }

        if (selectedDocumentViewer != null) {
            return selectedDocumentViewer.getFileForViewer(pRequest, pResponse, servletContext,dataFile);
        }
        return null;
    }

    @Override
    public String getHtmlForViewer(BinaryResource file) {
        String template = "";
        DocumentViewer documentViewerSelected = selectViewer(file.getFullName());
        if (documentViewerSelected != null) {
            template = documentViewerSelected.getHtmlForViewer(file);
        }
        return template;
    }

}
