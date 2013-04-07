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

import javax.ejb.Stateless;

@Stateless(name="DocumentViewerBean")
public class DocumentViewerBean {

    /*
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
    public String getVaultPath() {
        return vaultPath;
    }

    @Override
    public String getServletName(String binaryResourceFullName) throws Exception {
        String servletName = "default.jspf";

        DocumentViewer selectedDocumentViewer = selectViewer(binaryResourceFullName);
        if (selectedDocumentViewer != null) {
            servletName = selectedDocumentViewer.getServletName();
        }

        return servletName;
    }
    */

}
