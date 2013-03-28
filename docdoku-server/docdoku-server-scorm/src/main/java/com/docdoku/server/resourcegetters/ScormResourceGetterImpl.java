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
package com.docdoku.server.resourcegetters;

import com.docdoku.core.util.FileIO;

import javax.activation.FileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@ScormResourceGetter
public class ScormResourceGetterImpl implements DocumentResourceGetter {

    @Override
    public File getDataFile(String resourceFullName, String subResourceName, String vaultPath) throws Exception {
        File resource = new File(vaultPath + "/" + resourceFullName);
        return new File(resource.getAbsolutePath().replace(resource.getName(),"scorm/") + FileIO.getFileNameWithoutExtension(resource) + "/" + subResourceName);
    }

    @Override
    public boolean canGetResource(String resourceFullName, String subResourceName, String vaultPath) {
        File resource = new File(vaultPath + "/" + resourceFullName);
        if (resource.exists() && FileIO.isArchiveFile(resourceFullName) && subResourceName.length() > 0) {
            File archive = new File(resource.getAbsolutePath().replace(resource.getName(),"scorm/") + FileIO.getFileNameWithoutExtension(resource));
            return new File(archive, "imsmanifest.xml").exists();
        }
        return false;
    }

    @Override
    public boolean canGetResourceForViewer(File file) {
        return file.getAbsolutePath().contains("scorm/");
    }

    @Override
    public File getFileForViewer(HttpServletRequest pRequest, HttpServletResponse pResponse, File dataFile) throws Exception {
        String contentType;

        if (FileIO.getExtension(dataFile).contains("htm")) {
            contentType = "text/html";
        } else {
            contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(dataFile);
        }

        pResponse.setContentType(contentType);
        return dataFile;
    }
}
