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
package com.docdoku.server.rest.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Created by lebeaujulien on 03/03/15.
 */
@XmlRootElement
public class ListOfValuesDTO implements Serializable {

    private String workspaceId;
    private String name;
    private WorkspaceDTO workspace;
    private List<NameValuePairDTO> values;
    private String id;

    private boolean isDeletable = true;

    public ListOfValuesDTO(){
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return name;
    }

    public void setId(String id){
        this.id = id;
    }

    public WorkspaceDTO getWorkspace() {
        return workspace;
    }

    public void setWorkspace(WorkspaceDTO workspace) {
        this.workspace = workspace;
    }

    public List<NameValuePairDTO> getValues() {
        return values;
    }

    public void setValues(List<NameValuePairDTO> values) {
        this.values = values;
    }

    public boolean isDeletable() {
        return isDeletable;
    }

    public void setDeletable(boolean isDeletable) {
        this.isDeletable = isDeletable;
    }
}
