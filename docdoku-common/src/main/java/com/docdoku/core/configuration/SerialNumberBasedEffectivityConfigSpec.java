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

import com.docdoku.core.configuration.EffectivityConfigSpec;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * A kind of <a href="EffectivityConfigSpec.html">EffectivityConfigSpec</a>
 * based on serial number.
 * 
 * @author Florent Garin
 * @version 1.1, 30/10/11
 * @since   V1.1
 */
@Table(name="SERIALNUMBERBASEDEFFCS")
@Entity
public class SerialNumberBasedEffectivityConfigSpec extends EffectivityConfigSpec {

    /**
     * The serial number of the particular item specified by the context.
     */
    @Column(name="NUMBERCONFIG")
    private String number;

    public SerialNumberBasedEffectivityConfigSpec() {
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
    
    
    
}
