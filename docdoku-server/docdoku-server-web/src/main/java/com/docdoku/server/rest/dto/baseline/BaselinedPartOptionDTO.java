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

package com.docdoku.server.rest.dto.baseline;

public class BaselinedPartOptionDTO {
    private String version;
    private int lastIteration;
    private boolean released;

    public BaselinedPartOptionDTO() {
    }

    public BaselinedPartOptionDTO(String version, int lastIteration, boolean released) {
        this.version = version;
        this.lastIteration = lastIteration;
        this.released = released;
    }

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public int getLastIteration() {
        return lastIteration;
    }
    public void setLastIteration(int lastIteration) {
        this.lastIteration = lastIteration;
    }

    public boolean isReleased() {
        return released;
    }
    public void setReleased(boolean released) {
        this.released = released;
    }
}