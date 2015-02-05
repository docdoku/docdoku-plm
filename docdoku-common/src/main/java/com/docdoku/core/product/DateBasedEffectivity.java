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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * DateBasedEffectivity indicates that an item is effective while a
 * configuration item is being produced during a date range.
 * 
 * @author Florent Garin
 * @version 1.1, 18/10/11
 * @since   V1.1
 */
@Table(name="DATEBASEDEFFECTIVITY")
@Entity
public class DateBasedEffectivity extends Effectivity{

    /**
     * The date and/or time when the effectivity starts.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    
    /**
     * The date and/or time when the effectivity ends.
     * If a value for this attribute is not set, 
     * then the effectivity has no defined end.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    public DateBasedEffectivity() {
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
}
