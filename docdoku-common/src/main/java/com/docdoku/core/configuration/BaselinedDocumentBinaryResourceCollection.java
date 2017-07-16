/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.core.configuration;

import com.docdoku.core.common.BinaryResource;

import java.io.Serializable;
import java.util.*;

/**
 * @author Elisabel Généreux
 */
public class BaselinedDocumentBinaryResourceCollection implements Serializable {

    private String rootFolderName;
    private Set<BinaryResource> attachedFiles = new HashSet<>();

    public BaselinedDocumentBinaryResourceCollection() {
    }

    public BaselinedDocumentBinaryResourceCollection(String rootFolderName) {
        this.rootFolderName = rootFolderName;
    }

    public String getRootFolderName() {
        return rootFolderName;
    }

    public void setRootFolderName(String rootFolderName) {
        this.rootFolderName = rootFolderName;
    }

    public Set<BinaryResource> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(Set<BinaryResource> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public boolean hasNoFiles() {
        return attachedFiles.isEmpty();
    }
}
