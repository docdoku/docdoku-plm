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

import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentVisualizerManagerLocal;
import com.docdoku.server.visualizers.DocumentVisualizer;

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
import java.util.Map;

@Stateless(name="DocumentVisualizerBean")
public class DocumentVisualizerBean implements IDocumentVisualizerManagerLocal{

    @EJB
    private IDocumentManagerLocal documentService;

    @Inject
    @Any
    private Instance<DocumentVisualizer> documentVisualizers;

    @Resource(name = "vaultPath")
    private String vaultPath;

    @Override
    public String getJspPageName(String resourceFullName) throws Exception {
        String resourceFile = null;
        DocumentVisualizer selectedDocumentVisualizer = selectVisualizer(resourceFullName);

        if (selectedDocumentVisualizer != null) {
            resourceFile = selectedDocumentVisualizer.getJspPageName();
        } else {
            resourceFile = "default.jspf";
        }
        return resourceFile;
    }

    private DocumentVisualizer selectVisualizer(String resourceFullName) {
        DocumentVisualizer selectedDocumentVisualizer = null;
        for (DocumentVisualizer documentVisualizer : documentVisualizers) {
            if (documentVisualizer.canVisualize(resourceFullName)) {
                selectedDocumentVisualizer = documentVisualizer;
                break;
            }
        }
        return selectedDocumentVisualizer;
    }

    @Override
    public Map<String, Object> getExtraParams(String resourceFullName) throws Exception {
        DocumentVisualizer selectedDocumentVisualizer = selectVisualizer(resourceFullName);
        Map<String, Object> extraParams  = null;
        if (selectedDocumentVisualizer != null) {
            extraParams = selectedDocumentVisualizer.getExtraParams(resourceFullName);
        }
        return extraParams;
    }

    @Override
    public File getFileForViewer(HttpServletRequest pRequest, HttpServletResponse pResponse, ServletContext servletContext, File dataFile) throws Exception {
        DocumentVisualizer selectedDocumentVisualizer = null;
        for (DocumentVisualizer documentVisualizer : documentVisualizers) {
            if (documentVisualizer.canGetResourceForViewer(dataFile,pRequest)) {
                selectedDocumentVisualizer = documentVisualizer;
                break;
            }
        }

        if (selectedDocumentVisualizer != null) {
            return selectedDocumentVisualizer.getFileForViewer(pRequest, pResponse, servletContext,dataFile);
        }
        return null;
    }

    @Override
    public String getVaultPath() {
        return vaultPath;
    }

}
