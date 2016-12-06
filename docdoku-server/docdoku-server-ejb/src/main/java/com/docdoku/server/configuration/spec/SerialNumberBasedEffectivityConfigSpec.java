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


package com.docdoku.server.configuration.spec;

import com.docdoku.core.configuration.ProductConfiguration;
import com.docdoku.core.product.*;
import com.docdoku.core.util.AlphanumericComparator;
import com.docdoku.core.util.Tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A kind of {@link EffectivityConfigSpec} based on serial number.
 * 
 * @author Florent Garin
 * @version 1.1, 30/10/11
 * @since   V1.1
 */
public class SerialNumberBasedEffectivityConfigSpec extends EffectivityConfigSpec {

    /**
     * The serial number of the particular item specified by the context.
     */
    private String number;

    private final static Comparator<CharSequence> STRING_COMPARATOR = new AlphanumericComparator();


    public SerialNumberBasedEffectivityConfigSpec(String number, ConfigurationItem configurationItem) {
        super(configurationItem);
        this.number=number;
    }
    public SerialNumberBasedEffectivityConfigSpec(String number, ProductConfiguration configuration) {
        super(configuration);
        this.number=number;
    }


    @Override
    protected boolean isEffective(Effectivity eff){
        if(eff instanceof SerialNumberBasedEffectivity){
            SerialNumberBasedEffectivity serialEff=(SerialNumberBasedEffectivity) eff;
            return isEffective(serialEff);
        }else
            return false;
    }
    private boolean isEffective(SerialNumberBasedEffectivity serialEff){
        ConfigurationItem ci = serialEff.getConfigurationItem();
        if(!configurationItem.equals(ci))
            return false;

        if(STRING_COMPARATOR.compare(number, serialEff.getStartNumber())<0)
            return false;

        if(serialEff.getEndNumber()!=null && STRING_COMPARATOR.compare(number, serialEff.getEndNumber())>0)
            return false;

        return true;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
    
}
