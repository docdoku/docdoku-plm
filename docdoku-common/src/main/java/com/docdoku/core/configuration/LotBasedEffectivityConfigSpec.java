/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <a href="EffectivityConfigSpec.html">EffectivityConfigSpec</a>
 * based on a specific lot.
 * 
 * @author Florent Garin
 * @version 1.1, 30/10/11
 * @since   V1.1
 */
@Table(name="LOTBASEDEFFCS")
@Entity
public class LotBasedEffectivityConfigSpec extends EffectivityConfigSpec {

    /**
     * The lot id of the particular batch of items specified by the context.
     */
    @Column(name="LOTIDCONFIG")
    private String lotId;

    public LotBasedEffectivityConfigSpec() {
    }

    
    
    public String getLotId() {
        return lotId;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }
    
    
    
}
