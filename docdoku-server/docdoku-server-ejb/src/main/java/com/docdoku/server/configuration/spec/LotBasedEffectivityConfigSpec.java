/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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


package com.docdoku.server.configuration.spec;

import com.docdoku.core.configuration.ProductConfiguration;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.Effectivity;
import com.docdoku.core.product.LotBasedEffectivity;
import com.docdoku.core.util.AlphanumericComparator;

import java.util.Comparator;

/**
 * A kind of {@link EffectivityConfigSpec} based on a specific lot.
 * 
 * @author Florent Garin
 * @version 1.1, 30/10/11
 * @since   V1.1
 */
public class LotBasedEffectivityConfigSpec extends EffectivityConfigSpec {

    /**
     * The lot id of the particular batch of items specified by the context.
     */
    private String lotId;

    private final static Comparator<CharSequence> STRING_COMPARATOR = new AlphanumericComparator();

    public LotBasedEffectivityConfigSpec(String lotId, ConfigurationItem configurationItem) {
        super(configurationItem);
        this.lotId=lotId;
    }
    public LotBasedEffectivityConfigSpec(String lotId, ProductConfiguration configuration) {
        super(configuration);
        this.lotId=lotId;
    }


    @Override
    protected boolean isEffective(Effectivity eff){
        if(eff instanceof LotBasedEffectivity){
            LotBasedEffectivity lotEff=(LotBasedEffectivity) eff;
            return isEffective(lotEff);
        }else
            return false;
    }
    private boolean isEffective(LotBasedEffectivity lotEff){
        ConfigurationItem ci = lotEff.getConfigurationItem();
        if(!configurationItem.equals(ci))
            return false;

        if(STRING_COMPARATOR.compare(lotId, lotEff.getStartLotId())<0)
            return false;

        if(lotEff.getEndLotId()!=null && STRING_COMPARATOR.compare(lotId, lotEff.getEndLotId())>0)
            return false;

        return true;
    }


    public String getLotId() {
        return lotId;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }
    
}
