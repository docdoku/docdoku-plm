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
import com.docdoku.server.viewers.utils.ScormManifestParser;
import com.docdoku.server.viewers.utils.ScormOrganization;
import com.docdoku.server.viewers.utils.ScormUtil;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ScormViewerImpl implements DocumentViewer {

    @Override
    public boolean canPrepareFileForViewer(File file, HttpServletRequest pRequest) {
        return false;
    }

    @Override
    public File prepareFileForViewer(HttpServletRequest pRequest, HttpServletResponse pResponse, ServletContext servletContext, File dataFile) throws Exception {
        return null;
    }

    @Override
    public boolean canRenderViewerTemplate(String vaultPath, BinaryResource binaryResource) {
        return ScormUtil.isScormArchive(new File(vaultPath, binaryResource.getFullName()));
    }

    @Override
    public String renderHtmlForViewer(String vaultPath, BinaryResource scormResource) throws Exception {
        ScormOrganization scormOrganization = new ScormManifestParser(ScormUtil.getManifest(vaultPath, scormResource.getFullName())).parse();
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("com/docdoku/server/viewers/scorm_viewer.mustache");
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("organization", scormOrganization);
        scopes.put("uriResource", ViewerUtils.getURI(scormResource));
        scopes.put("fileName", scormResource.getName());
        StringWriter templateWriter = new StringWriter();
        mustache.execute(templateWriter, scopes).flush();
        return templateWriter.toString();
    }

}
