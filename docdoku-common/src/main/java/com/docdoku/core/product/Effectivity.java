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
package com.docdoku.core.product;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;

/**
 * An Effectivity is an abstract subclass which is
 * a kind of qualification object.
 * 
 * Effectivities are primarily applied to {@link PartRevision} objects.
 * 
 * 
 * @author Florent Garin
 * @version 1.1, 14/10/11
 * @since   V1.1
 */
@Table(name="EFFECTIVITY")
@XmlSeeAlso({DateBasedEffectivity.class, SerialNumberBasedEffectivity.class, LotBasedEffectivity.class})
@Inheritance()
@Entity
@NamedQuery(name="Effectivity.removeEffectivitiesFromConfigurationItem",query="DELETE FROM Effectivity e WHERE e.configurationItem.id = :configurationItemId AND e.configurationItem.workspace.id = :workspaceId")

public abstract class Effectivity implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;
    protected String name;

    @Lob
    private String description;
    
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "CONFIGURATIONITEM_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "CONFIGURATIONITEM_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private ConfigurationItem configurationItem;
    
    /**
     * Used to restrict the effectivity to a given product definition usage.
     * However, the case when we need to manage different versions of a part
     * in the same product structure may rarely happen.
     */
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private PartUsageLink usageLink;

    public Effectivity() {
    }

    public Effectivity(String pName) {
        name = pName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /*
     * May be optional for a DateBasedEffectivity.
     */
    public void setConfigurationItem(ConfigurationItem configurationItem) {
        this.configurationItem = configurationItem;
    }

    public ConfigurationItem getConfigurationItem() {
        return configurationItem;
    }

    public PartUsageLink getUsageLink() {
        return usageLink;
    }

    public void setUsageLink(PartUsageLink usageLink) {
        this.usageLink = usageLink;
    }
}
