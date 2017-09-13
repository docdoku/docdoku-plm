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

package com.docdoku.core.admin;

import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that wraps setting options of a particular workspace.
 *
 * @author Morgan Guimard
 * @version 2.5, 04/09/17
 * @since V2.5
 */
@Table(name = "WORKSPACEOPTIONS")
@Entity
public class WorkspaceOptions implements Serializable {

    @Id
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    private Workspace workspace;

    @OrderColumn(name = "PARTCOLUMN_ORDER")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "WORKSPACE_PARTTABLECOLUMN",
            joinColumns = {
                    @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
            }
    )
    @Column(name="TABLECOLUMN")
    private List<String> partTableColumns;

    @OrderColumn(name = "DOCUMENTCOLUMN_ORDER")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "WORKSPACE_DOCUMENTTABLECOLUMN",
            joinColumns = {
                    @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
            }
    )
    @Column(name="TABLECOLUMN")
    private List<String> documentTableColumns;

    public WorkspaceOptions() {
    }

    public WorkspaceOptions(Workspace workspace, List<String> partTableColumns, List<String> documentTableColumns) {
        this.workspace = workspace;
        this.partTableColumns = partTableColumns;
        this.documentTableColumns = documentTableColumns;
    }

    public List<String> getDocumentTableColumns() {
        return documentTableColumns;
    }

    public void setDocumentTableColumns(List<String> documentTableColumns) {
        this.documentTableColumns = documentTableColumns;
    }

    public List<String> getPartTableColumns() {
        return partTableColumns;
    }

    public void setPartTableColumns(List<String> partTableColumns) {
        this.partTableColumns = partTableColumns;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void setDefaults() {
        partTableColumns=new ArrayList<>();
        documentTableColumns=new ArrayList<>();
    }
}
