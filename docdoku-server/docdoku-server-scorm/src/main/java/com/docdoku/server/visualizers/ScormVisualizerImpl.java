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
package com.docdoku.server.visualizers;

import com.docdoku.core.util.FileIO;
import com.docdoku.server.visualizers.utils.ScormManifestParser;
import com.docdoku.server.visualizers.utils.ScormOrganization;
import com.docdoku.server.visualizers.utils.ScormUtil;

import javax.activation.FileTypeMap;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@ScormVisualizer
@Stateless
public class ScormVisualizerImpl implements DocumentVisualizer {

    @Resource(name = "vaultPath")
    private String vaultPath;

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
    public String getJspPageName() throws Exception {
        return "scormPlayer.jspf";
    }

    @Override
    public boolean canVisualize(String fileName) {
        return ScormUtil.isScormArchive(new File(vaultPath + "/" + fileName));
    }

    @Override
    public Map<String, Object> getExtraParams(String resourceFullName) throws Exception {
        ScormOrganization scormOrganization = new ScormManifestParser(ScormUtil.getManifest(resourceFullName, vaultPath)).parse();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("organization", scormOrganization);
        return params;
    }
}
