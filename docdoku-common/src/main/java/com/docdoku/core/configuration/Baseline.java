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

import com.docdoku.core.product.ConfigurationItem;

import javax.persistence.*;
import javax.persistence.criteria.Fetch;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
@NamedQueries({
        @NamedQuery(name="Baseline.findByConfigurationItemId", query="SELECT b FROM Baseline b WHERE b.configurationItem.id LIKE :ciId"),
        @NamedQuery(name="Baseline.findByBaselineId", query="SELECT b FROM Baseline b WHERE b.id = :baselineId"),
        @NamedQuery(name="Baseline.findByConfigurationItemIdAndBaselineId", query="SELECT b FROM Baseline b WHERE b.configurationItem.id LIKE :ciId AND b.id = :baselineId")
})
public class Baseline implements Serializable {


    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "CONFIGURATIONITEM_ID", referencedColumnName = "ID"),
            @JoinColumn(name = "CONFIGURATIONITEM_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private ConfigurationItem configurationItem;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date creationDate;

    @MapsId("baselinedPartKey")
    @OneToMany(mappedBy="baseline", cascade=CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true)
    private Map<BaselinedPartKey, BaselinedPart> baselinedParts=new HashMap<BaselinedPartKey, BaselinedPart>();

    public Baseline() {
    }

    public Baseline(ConfigurationItem configurationItem, String name, String description) {
        this.configurationItem = configurationItem;
        this.name = name;
        this.description = description;
        this.creationDate = new Date();
    }

    public Map<BaselinedPartKey, BaselinedPart> getBaselinedParts() {
        return baselinedParts;
    }

    public void addBaselinedPart(BaselinedPart baselinedPart){
        baselinedParts.put(baselinedPart.getBaselinedPartKey(),baselinedPart);
    }

    public BaselinedPart getBaselinedPart(BaselinedPartKey baselinedPartKey){
        return baselinedParts.get(baselinedPartKey);
    }

    public boolean hasBasedLinedPart(BaselinedPartKey baselinedPartKey){
        return baselinedParts.get(baselinedPartKey) != null;
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

    public int getId() {
        return id;
    }

    public ConfigurationItem getConfigurationItem() {
        return configurationItem;
    }

    public void setConfigurationItem(ConfigurationItem configurationItem) {
        this.configurationItem = configurationItem;
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
