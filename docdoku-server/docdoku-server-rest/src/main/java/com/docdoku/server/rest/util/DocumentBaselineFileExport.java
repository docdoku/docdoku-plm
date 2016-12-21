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

package com.docdoku.server.rest.util;

/**
 * This class holds the context for a document baseline export
 * See {link com.docdoku.server.rest.writers.DocumentBaselineFileExportMessageBodyWriter} for response implementation
 *
 * @author Elisabel Généreux on 21/09/16.
 */
public class DocumentBaselineFileExport {

    /**
     * The workspace concerned
     */
    private String workspaceId;
    /**
     * The baseline identifier
     */
    private Integer baselineId;

    public DocumentBaselineFileExport() {
    }

    public DocumentBaselineFileExport(String workspaceId, Integer baselineId) {
        this.workspaceId = workspaceId;
        this.baselineId = baselineId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Integer getBaselineId() {
        return baselineId;
    }

    public void setBaselineId(Integer baselineId) {
        this.baselineId = baselineId;
    }
}
