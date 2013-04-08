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
import com.docdoku.server.viewers.utils.ScormManifestParser;
import com.docdoku.server.viewers.utils.ScormOrganization;
import com.docdoku.server.viewers.utils.ScormUtil;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import viewers.DocumentViewer;

import javax.activation.FileTypeMap;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ScormViewerImpl implements DocumentViewer {

    private String vaultPath = "/var/lib/docdoku/vault";

    @Override
    public boolean canGetResourceForViewer(File file, HttpServletRequest pRequest) {
        return file.getAbsolutePath().contains(File.separator + "scorm" + File.separator);
    }

    @Override
    public File getFileForViewer(HttpServletRequest pRequest, HttpServletResponse pResponse, ServletContext servletContext, File dataFile) throws Exception {
        String contentType;

        if (FileIO.getExtension(dataFile).contains("htm")) {
            contentType = "text/html";
        } else {
            contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(dataFile);
        }
        pResponse.setContentType(contentType);
        return dataFile;
    }

    @Override
    public boolean canVisualize(String fileName) {
        return ScormUtil.isScormArchive(new File(vaultPath + File.separator + fileName));
    }

    @Override
    public String getHtmlForViewer(BinaryResource scormResource) {
        try {

            ScormOrganization scormOrganization = new ScormManifestParser(ScormUtil.getManifest(scormResource.getFullName(), vaultPath)).parse();

            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile("scorm_viewer.mustache");

            Map<String, Object> scopes = new HashMap<String, Object>();
            scopes.put("organization", scormOrganization);
            scopes.put("context", "/context/");
            scopes.put("filePath", "/context/" + "/files/" + scormResource.getFullName());
            scopes.put("fileName", scormResource.getName());
            scopes.put("index", 0);

            StringWriter templateWriter = new StringWriter();
            mustache.execute(templateWriter, scopes).flush();

            return templateWriter.toString();

        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
            return "error in getHtmlForViewer for scorm";
        }

    }

}
