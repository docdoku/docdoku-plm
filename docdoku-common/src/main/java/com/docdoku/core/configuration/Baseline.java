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
package com.docdoku.core.configuration;

import com.docdoku.core.common.Workspace;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterKey;

import java.io.Serializable;
import java.util.*;
import javax.persistence.*;

/**
 * Baseline refers to a specific configuration, it could be seen as
 * "snapshots in time" of configurations. More concretely, baselines are collections
 * of items (for instance parts) at a specified iteration.
 * Within a baseline, there must not be two different iterations of the same part.
 * 
 * @author Florent Garin
 * @version 2.0, 15/05/13
 * @since   V2.0
 */
@Table(name="BASELINE")
@Entity
public class Baseline implements Serializable {


    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Workspace workspace;

    @Lob
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date creationDate;

    private String name;

    @MapsId("baselinedPartKey")
    @OneToMany(mappedBy="baseline", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private Map<BaselinedPartKey, BaselinedPart> baselinedParts=new HashMap<BaselinedPartKey, BaselinedPart>();

    public Baseline() {
    }

    public Map<BaselinedPartKey, BaselinedPart> getBaselinedParts() {
        return baselinedParts;
    }

    public void setBaselinedParts(Map<BaselinedPartKey, BaselinedPart> baselinedParts) {
        this.baselinedParts = baselinedParts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Baseline)) return false;

        Baseline baseline = (Baseline) o;

        if (id != baseline.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
