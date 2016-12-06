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

import java.util.Date;
/**
 * A kind of {@link EffectivityConfigSpec} expressed by date and time.
 * 
 * @author Florent Garin
 * @version 1.1, 30/10/11
 * @since   V1.1
 */

public class DateBasedEffectivityConfigSpec extends EffectivityConfigSpec {

    /**
     * The date and/or time of the context.
     */
    private Date date;


    public DateBasedEffectivityConfigSpec(Date date, ConfigurationItem configurationItem) {
        super(configurationItem);
        this.date=date;
    }
    public DateBasedEffectivityConfigSpec(Date date, ProductConfiguration configuration) {
        super(configuration);
        this.date=date;
    }

    @Override
    protected boolean isEffective(Effectivity eff){
        if(eff instanceof DateBasedEffectivity){
            DateBasedEffectivity dateEff=(DateBasedEffectivity) eff;
            return isEffective(dateEff);
        }else
            return false;
    }
    private boolean isEffective(DateBasedEffectivity dateEff){
        ConfigurationItem ci = dateEff.getConfigurationItem();
        if(ci != null && !ci.equals(configurationItem))
            return false;

        if(dateEff.getStartDate().after(date))
            return false;

        if(dateEff.getEndDate()!=null && dateEff.getEndDate().before(date))
            return false;

        return true;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
