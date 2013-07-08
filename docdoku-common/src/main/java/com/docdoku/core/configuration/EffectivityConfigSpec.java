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
import com.docdoku.core.product.PartMaster;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * A configuration specification used to select
 * a <a href="PartIteration.html">PartIteration</a>
 * of a given <a href="PartMaster.html">PartMaster</a>
 * according to its effectivities.
 * Actually the EffectivityConfigSpec determine the right
 * <a href="PartRevision.html">PartRevision</a>, we then catch its last iteration.
 * 
 * @author Florent Garin
 * @version 1.1, 30/10/11
 * @since   V1.1
 */
@Table(name="EFFECTIVITYCONFIGSPEC")
@XmlSeeAlso({DateBasedEffectivityConfigSpec.class, SerialNumberBasedEffectivityConfigSpec.class, LotBasedEffectivityConfigSpec.class})
@Entity
@NamedQuery(name="EffectivityConfigSpec.removeEffectivityConfigSpecFromConfigurationItem",query="DELETE FROM EffectivityConfigSpec ec WHERE ec.configurationItem.id = :configurationItemId AND ec.configurationItem.workspace.id = :workspaceId")

public abstract class EffectivityConfigSpec extends ConfigSpec {

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "CONFIGURATIONITEM_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "CONFIGURATIONITEM_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    protected ConfigurationItem configurationItem;

    public EffectivityConfigSpec() {
    }

    public void setConfigurationItem(ConfigurationItem configurationItem) {
        this.configurationItem = configurationItem;
    }

    public ConfigurationItem getConfigurationItem() {
        return configurationItem;
    }

    @Override
    public PartMaster filterConfigSpec(PartMaster root, int depth, EntityManager em) {
        return null;
    }
}
