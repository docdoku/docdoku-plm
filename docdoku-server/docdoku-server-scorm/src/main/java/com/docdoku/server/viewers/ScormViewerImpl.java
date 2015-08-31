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
package com.docdoku.server.viewers;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.StorageException;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.server.InternalService;
import com.docdoku.server.viewers.utils.ScormManifestParser;
import com.docdoku.server.viewers.utils.ScormOrganization;
import com.docdoku.server.viewers.utils.ScormUtil;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import javax.inject.Inject;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScormViewerImpl implements DocumentViewer {

    private static final Logger LOGGER = Logger.getLogger(ScormViewerImpl.class.getName());

    @InternalService
    @Inject
    private IDataManagerLocal dataManager;


    @Override
    public boolean canRenderViewerTemplate(BinaryResource binaryResource) {
        try {
            return dataManager.exists(binaryResource, ScormUtil.getScormSubResourceVirtualPath(ScormUtil.IMS_MANIFEST));
        } catch (StorageException e) {
            LOGGER.log(Level.INFO, null, e);
            return false;
        }
    }

    @Override
    public String renderHtmlForViewer(BinaryResource scormResource, String uuid) throws Exception {
        String manifestVirtualPath = ScormUtil.getScormSubResourceVirtualPath(ScormUtil.IMS_MANIFEST);
        ScormOrganization scormOrganization = new ScormManifestParser(dataManager.getBinarySubResourceInputStream(scormResource, manifestVirtualPath)).parse();
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("com/docdoku/server/viewers/scorm_viewer.mustache");
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("organization", scormOrganization);
        scopes.put("uriResource", ViewerUtils.getURI(scormResource, uuid));
        StringWriter templateWriter = new StringWriter();
        mustache.execute(templateWriter, scopes).flush();

        return ViewerUtils.getViewerTemplate(dataManager, scormResource, uuid, templateWriter.toString());
    }

}
