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
package com.docdoku.server;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IFileViewerManagerLocal;
import com.docdoku.server.viewers.DocumentViewer;
import com.docdoku.server.viewers.ViewerUtils;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless(name="DocumentViewerBean")
public class DocumentViewerBean implements IFileViewerManagerLocal {

    @Inject
    private IDataManagerLocal dataManager;

    @Inject
    @Any
    private Instance<DocumentViewer> documentViewers;

    private static final Logger LOGGER = Logger.getLogger(DocumentViewerBean.class.getName());

    @Override
    public String getHtmlForViewer(BinaryResource binaryResource, String uuid) {
        String template;
        DocumentViewer documentViewerSelected = selectViewerForTemplate(binaryResource);

        try {
            if (documentViewerSelected != null) {
                template = documentViewerSelected.renderHtmlForViewer(binaryResource,uuid);
            }else{
                template = getDefaultTemplate(binaryResource,uuid);
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
            template = new StringBuilder().append("<p>").append("Can't render ").append(binaryResource.getName()).append("</p>").toString();
        }

        return template;
    }

    private DocumentViewer selectViewerForTemplate(BinaryResource binaryResource) {
        DocumentViewer selectedDocumentViewer = null;
        for (DocumentViewer documentViewer : documentViewers) {
            if (documentViewer.canRenderViewerTemplate(binaryResource)) {
                selectedDocumentViewer = documentViewer;
                break;
            }
        }
        return selectedDocumentViewer;
    }

    private String getDefaultTemplate(BinaryResource binaryResource,String uuid) throws IOException {

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("com/docdoku/server/viewers/default_viewer.mustache");
        Map<Object,Object> scopes = new HashMap<>();
        scopes.put("uriResource", ViewerUtils.getURI(binaryResource, uuid));
        scopes.put("fileName", binaryResource.getName());
        StringWriter templateWriter = new StringWriter();
        mustache.execute(templateWriter, scopes).flush();

        return ViewerUtils.getViewerTemplate(dataManager, binaryResource, uuid, templateWriter.toString());
    }

}
