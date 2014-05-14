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

package com.docdoku.server.rest.dto;

public class BaselinedPartDTO {

    private String number;
    private String version;
    private int iteration;
    private int lastIteration;
    private String lastVersion;
    private String lastReleasedVersion;

    public BaselinedPartDTO() {
    }

    public BaselinedPartDTO(String number, String version, int iteration) {
        this.number = number;
        this.version = version;
        this.iteration = iteration;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public int getIteration() {
        return iteration;
    }
    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public int getLastIteration() {
        return lastIteration;
    }
    public void setLastIteration(int lastIteration) {
        this.lastIteration = lastIteration;
    }


    public String getLastVersion() {
        return lastVersion;
    }
    public void setLastVersion(String lastVersion) {
        this.lastVersion = lastVersion;
    }

    public String getLastReleasedVersion() {return lastReleasedVersion;}
    public void setLastReleasedVersion(String lastReleasedVersion) {this.lastReleasedVersion = lastReleasedVersion;}
}
