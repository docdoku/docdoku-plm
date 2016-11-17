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

package com.docdoku.core.admin;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author Morgan Guimard
 */
@Table(name = "PLATFORMOPTIONS")
@Entity
public class PlatformOptions implements Serializable {

    public static final int UNIQUE_ID = 1 ;

    @Id
    private int id = UNIQUE_ID;

    private OperationSecurityStrategy registrationStrategy;

    private OperationSecurityStrategy workspaceCreationStrategy;

    public PlatformOptions() {
    }

    public int getId() {
        return id;
    }

    public OperationSecurityStrategy getRegistrationStrategy() {
        return registrationStrategy;
    }

    public void setRegistrationStrategy(OperationSecurityStrategy registrationStrategy) {
        this.registrationStrategy = registrationStrategy;
    }

    public OperationSecurityStrategy getWorkspaceCreationStrategy() {
        return workspaceCreationStrategy;
    }

    public void setWorkspaceCreationStrategy(OperationSecurityStrategy workspaceCreationStrategy) {
        this.workspaceCreationStrategy = workspaceCreationStrategy;
    }

    public void setDefaults() {
        registrationStrategy = OperationSecurityStrategy.NONE;
        workspaceCreationStrategy = OperationSecurityStrategy.NONE;
    }

}
