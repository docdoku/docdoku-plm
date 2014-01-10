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


package com.docdoku.core.change;

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Abstract parent class from which change objects are derived.
 *
 * @author Florent Garin
 * @version 2.0, 10/01/14
 * @since   V2.0
 */
@MappedSuperclass
public abstract class ChangeItem implements Serializable {


    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    protected int id;

    protected String name;

    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    protected Workspace workspace;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name="AUTHOR_LOGIN", referencedColumnName="LOGIN"),
            @JoinColumn(name="AUTHOR_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    protected User author;


    @Temporal(TemporalType.TIMESTAMP)
    protected java.util.Date creationDate;

    @Lob
    protected String description;

    public enum Priority {
        LOW, HIGH, MEDIUM, EMERGENCY
    }

    /**
     * An adaptive change maintains functionality for a different platform or
     * environment.
     * A corrective change corrects a defect.
     * A perfective change adds functionality.
     * A preventive change improves maintainability.
     */
    protected Category category;

    public enum Category {
        ADAPTIVE, CORRECTIVE, PERFECTIVE, PREVENTIVE, OTHER
    }



    public ChangeItem() {
    }



}
