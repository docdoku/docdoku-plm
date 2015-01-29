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

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * SerialNumberBasedEffectivity indicates that an item is effective while a
 * configuration item is being produced in a range of serial numbered units.
 * 
 * @author Florent Garin
 * @version 1.1, 18/10/11
 * @since   V1.1
 */
@Table(name="SERIALNUMBERBASEDEFFECTIVITY")
@Entity
public class SerialNumberBasedEffectivity extends Effectivity{

    /**
     * The serial number of the first item that the effectivity applies to.
     */
    private String startNumber;
    
    /**
     * The serial number of the last item that the effectivity applies to.
     * This value is optional.
     */
    private String endNumber;

    public SerialNumberBasedEffectivity() {
    }

    public String getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(String startNumber) {
        this.startNumber = startNumber;
    }

    
    public String getEndNumber() {
        return endNumber;
    }

    public void setEndNumber(String endNumber) {
        this.endNumber = endNumber;
    }
    
    
    
}
