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

package com.docdoku.core.sharing;

import java.io.Serializable;

/**
 * Identity class of {@link SharedEntity} objects.
 *
 * @author Morgan Guimard
 */

public class SharedEntityKey implements Serializable{

    private String workspace;
    private String uuid;

    public SharedEntityKey() {
    }

    public SharedEntityKey(String workspace, String uuid) {
        this.workspace = workspace;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String pWorkspace) {
        workspace = pWorkspace;
    }

    @Override
    public String toString() {
        return workspace + "-" + uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SharedEntityKey that = (SharedEntityKey) o;

        return !(uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) &&
               !(workspace != null ? !workspace.equals(that.workspace) : that.workspace != null);

    }

    @Override
    public int hashCode() {
        int result = workspace != null ? workspace.hashCode() : 0;
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        return result;
    }
}
