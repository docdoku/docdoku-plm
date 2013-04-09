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
import com.docdoku.server.viewers.DocumentViewer;

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

    @Override
    public File prepareFileForViewer(HttpServletRequest pRequest, HttpServletResponse pResponse, ServletContext servletContext, File dataFile) throws Exception {
        DocumentViewer selectedDocumentViewer = null;
        for (DocumentViewer documentViewer : documentViewers) {
            if (documentViewer.canPrepareFileForViewer(dataFile, pRequest)) {
                selectedDocumentViewer = documentViewer;
                break;
            }
        }

        if (selectedDocumentViewer != null) {
            return selectedDocumentViewer.prepareFileForViewer(pRequest, pResponse, servletContext, dataFile);
        }
        return dataFile;
    }

    @Override
    public String getHtmlForViewer(BinaryResource binaryResource) {
        String template = "";
        DocumentViewer documentViewerSelected = selectViewerForTemplate(binaryResource);
        if (documentViewerSelected != null) {
            try {
                template = documentViewerSelected.renderHtmlForViewer(vaultPath, binaryResource);
            } catch (Exception e) {
                e.printStackTrace();
                template = new StringBuilder().append("<p>").append("Can't render ").append(binaryResource.getName()).append("</p>").toString();
            }
        }
        return template;
    }

    private DocumentViewer selectViewerForTemplate(BinaryResource binaryResource) {
        DocumentViewer selectedDocumentViewer = null;
        for (DocumentViewer documentViewer : documentViewers) {
            if (documentViewer.canRenderViewerTemplate(vaultPath, binaryResource)) {
                selectedDocumentViewer = documentViewer;
                break;
            }
        }
        return selectedDocumentViewer;
    }

}
