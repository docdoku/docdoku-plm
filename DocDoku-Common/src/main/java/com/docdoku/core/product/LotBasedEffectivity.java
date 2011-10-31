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

import javax.persistence.Entity;

/**
 * LotBasedEffectivity indicates that an item is effective while a
 * configuration item is being produced in a specified lot.
 * 
 * @author Florent Garin
 * @version 1.1, 18/10/11
 * @since   V1.1
 */
@Entity
public class LotBasedEffectivity extends Effectivity{

    /**
     * The identification of the first batch of items
     * that the effectivity applies to.
     */
    private String startLotId;
    

    /**
     * The identification of the last batch of items
     * that the effectivity applies to.
     * This value is optional.
     */
    private String endLotId;

    public LotBasedEffectivity() {
    }

    public String getStartLotId() {
        return startLotId;
    }

    public void setStartLotId(String startLotId) {
        this.startLotId = startLotId;
    }

    
    public String getEndLotId() {
        return endLotId;
    }

    public void setEndLotId(String endLotId) {
        this.endLotId = endLotId;
    }
    
    
}
