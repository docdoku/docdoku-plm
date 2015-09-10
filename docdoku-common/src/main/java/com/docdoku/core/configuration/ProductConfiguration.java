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
package com.docdoku.core.configuration;

import com.docdoku.core.common.User;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.security.ACL;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Baseline refers to a specific configuration, it could be seen as
 * "snapshots in time" of configurations. More concretely, baselines are collections
 * of items (like parts) at a specified iteration.
 * Within a baseline, there must not be two different iterations of the same part.
 * 
 * @author Florent Garin
 * @version 2.0, 15/05/13
 * @since V2.0
 */
@Table(name="PRODUCTCONFIGURATION")
@Entity
@NamedQueries({
        @NamedQuery(name="ProductConfiguration.findByWorkspace",query="SELECT p FROM ProductConfiguration p WHERE p.configurationItem.workspace.id = :workspaceId"),
        @NamedQuery(name="ProductConfiguration.findByConfigurationItem",query="SELECT p FROM ProductConfiguration p WHERE  p.configurationItem.workspace.id = :workspaceId AND  p.configurationItem.id = :configurationItemId")
})
public class ProductConfiguration implements Serializable {

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
    private Date creationDate;

    @OneToOne(orphanRemoval = true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private ACL acl;

    /**
     * Set of substitute links (actually their path from the root node)
     * that have been included into the baseline.
     * Only selected substitute links are stored as part usage links are considered as the default
     * choices for baselines.
     *
     * Paths are strings made of ordered lists of usage link ids joined by "-".
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "PRDCFG_SUBSTITUTELINK",
        joinColumns= {
            @JoinColumn(name = "PRODUCTBASELINE_ID", referencedColumnName = "ID")
        }
    )
    private Set<String> substituteLinks=new HashSet<>();

    /**
     * Set of optional usage links (actually their path from the root node)
     * that have been included into the baseline.
     *
     * Paths are strings made of ordered lists of usage link ids joined by "-".
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "PRDCFG_OPTIONALLINK",
        joinColumns={
            @JoinColumn(name = "PRODUCTBASELINE_ID", referencedColumnName="ID")
        }
    )
    private Set<String> optionalUsageLinks=new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
            @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;

    public ProductConfiguration() {
    }

    public ProductConfiguration(User user,ConfigurationItem configurationItem, String name, String description, ACL acl) {
        this.author = user;
        this.configurationItem = configurationItem;
        this.name = name;
        this.description = description;
        this.creationDate = new Date();
        this.acl = acl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ConfigurationItem getConfigurationItem() {
        return configurationItem;
    }

    public void setConfigurationItem(ConfigurationItem configurationItem) {
        this.configurationItem = configurationItem;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public ACL getAcl() {
        return acl;
    }

    public void setAcl(ACL acl) {
        this.acl = acl;
    }

    public Set<String> getSubstituteLinks() {
        return substituteLinks;
    }

    public void setSubstituteLinks(Set<String> substituteLinks) {
        this.substituteLinks = substituteLinks;
    }

    public Set<String> getOptionalUsageLinks() {
        return optionalUsageLinks;
    }

    public void setOptionalUsageLinks(Set<String> optionalUsageLinks) {
        this.optionalUsageLinks = optionalUsageLinks;
    }

    public boolean hasSubstituteLink(String link){
        return substituteLinks.contains(link);
    }

    public boolean isLinkOptional(String link){
        return optionalUsageLinks.contains(link);
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductConfiguration)) {
            return false;
        }

        ProductConfiguration productBaseline = (ProductConfiguration) o;
        return id == productBaseline.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
