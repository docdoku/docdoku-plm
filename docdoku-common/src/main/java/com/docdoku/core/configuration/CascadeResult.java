package com.docdoku.core.configuration;

import java.io.Serializable;

/**
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
    *
    * Created by Charles Fallourd on 12/10/15.
*/
public class CascadeResult implements Serializable{

    private int succeedAttempts;
    private int failedAttempts;

    public CascadeResult() {
        this.succeedAttempts = 0;
        this.failedAttempts = 0;
    }

    public void incSucceedAttempts() {
        this.succeedAttempts++;
    }

    public void incFailedAttemps() {
        this.failedAttempts++;
    }

    public int getSucceedAttempts() {
        return succeedAttempts;
    }

    public void setSucceedAttempts(int succeedAttempts) {
        this.succeedAttempts = succeedAttempts;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
}
