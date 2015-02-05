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

import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;

/**
 * A ConfigSpec is used to select for each {@link PartMaster}s and {@link DocumentRevision}s
 * the right {@link PartIteration} and {@link DocumentIteration} according to specific rules.
 * 
 * @author Florent Garin
 * @version 1.1, 30/10/11
 * @since   V1.1
 */
@Table(name="CONFIGSPEC")
@XmlSeeAlso({EffectivityConfigSpec.class, LatestReleasedConfigSpec.class, LatestConfigSpec.class, BaselineConfigSpec.class})
@Inheritance()
@Entity
public abstract class ConfigSpec implements Serializable{

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;

    public ConfigSpec() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public abstract PartIteration filterConfigSpec(PartMaster partMaster);

    public abstract DocumentIteration filterConfigSpec(DocumentRevision documentRevision);

}
