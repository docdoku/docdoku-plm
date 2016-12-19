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

package com.docdoku.server.importers;

import com.docdoku.core.product.ImportResult;
import com.docdoku.core.product.PartRevision;

import java.io.File;
import java.util.List;

/**
 * PartImporter plugin interface
 */
public interface PartImporter {
    /**
     * Determine if plugin is able to import the given file format
     *
     * @param importFileName   the file name
     * @return true if plugin can handle the import, false otherwise
     */
    boolean canImportFile(String importFileName);
    /**
     * Import the file and make requested changes
     *
     * @param workspaceId the workspace concerned by the import
     * @param file the file to import
     * @param revisionNote a revision note to apply on parts changed
     * @param autoCheckout check out the part if not checked out
     * @param autoCheckin check in the modified parts after operation
     * @param permissiveUpdate todo
     * @return an import result object
     */
    // TODO : replace java.io.File with java.io.InputStream
    ImportResult importFile(String workspaceId, File file, String revisionNote, boolean autoCheckout, boolean autoCheckin, boolean permissiveUpdate);

    /**
     * Run a dry import
     *
     * @param workspaceId the workspace concerned by the import
     * @param file the file to import
     * @param originalFileName the original file name
     * @param autoCheckout check out the part if not checked out
     * @param autoCheckin check in the modified parts after operation
     * @param permissiveUpdate todo
     * @return an import result object
     */
    // TODO : replace java.io.File with java.io.InputStream
    List<PartRevision> dryRunImport(String workspaceId, File file, String originalFileName, boolean autoCheckout, boolean autoCheckin, boolean permissiveUpdate);

}
