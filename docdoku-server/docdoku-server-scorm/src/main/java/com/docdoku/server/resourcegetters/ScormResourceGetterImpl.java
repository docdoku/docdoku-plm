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
import com.docdoku.server.viewers.utils.ScormUtil;

import java.io.File;

public class ScormResourceGetterImpl implements DocumentResourceGetter {

    @Override
    public File getDataFile(String resourceFullName, String subResourceName, String vaultPath) throws Exception {
        File resource = new File(vaultPath + File.separator + resourceFullName);
        return new File(resource.getAbsolutePath().replace(resource.getName(),"scorm/") + FileIO.getFileNameWithoutExtension(resource) + "/" + subResourceName);
    }

    @Override
    public boolean canGetResource(String resourceFullName, String subResourceName, String vaultPath) {
        if (subResourceName != null && !subResourceName.isEmpty()) {
            File resource = new File(vaultPath + File.separator + resourceFullName);
            return ScormUtil.isScormArchive(resource);
        } else {
            return false;
        }
    }
}
