/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.docdoku.core.product;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * An Effectivity is an abstract subclass which is
 * a kind of qualification object.
 * 
 * Effectivities are applied to <code>PartRevision</code> objects.
 * 
 * 
 * @author Florent Garin
 * @version 1.1, 14/10/11
 * @since   V1.1
 */
@XmlSeeAlso({DateBasedEffectivity.class, SerialNumberBasedEffectivity.class, LotBasedEffectivity.class})
@Inheritance()
@Entity
public abstract class Effectivity implements Serializable{

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;

    protected String name;
    
    private String description;
    
    @ManyToOne(optional=true, fetch= FetchType.EAGER)
    private ConfigurationItem configurationItem;
    
    /**
     * Used to restrict the effectivity to a given product definition usage.
     * However, the case when we need to manage different versions of a part
     * in the same product structure may rarely happen.
     */
    @ManyToOne(optional=true, fetch= FetchType.EAGER)
    private PartUsageLink usageLink;
    
    
    
    public Effectivity() {
    }

    public Effectivity(String pName) {
        name = pName;
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
