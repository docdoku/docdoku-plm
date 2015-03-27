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
import com.docdoku.core.product.PartLink;
import com.docdoku.core.product.PartMaster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A ConfigSpec is used to select for each {@link PartMaster}s and {@link DocumentRevision}s
 * the right {@link PartIteration} and {@link DocumentIteration} according to specific rules.
 * 
 * @author Florent Garin
 * @version 1.1, 30/10/11
 * @since   V1.1
 */

public abstract class ProductConfigSpec extends PSFilter implements Serializable{

    public ProductConfigSpec() {
    }

    // Config specs are strict and returns a single value
    // Do not override them
    public final List<PartLink> filter(List<PartLink> path) {
        PartLink partLink = filterPartLink(path);
        if(partLink != null){
            return Arrays.asList(new PartLink[]{partLink});
        }
        return new ArrayList<>();
    }
    public final List<PartIteration> filter(PartMaster partMaster) {
        PartIteration partIteration = filterPartIteration(partMaster);
        if(partIteration != null){
            return Arrays.asList(new PartIteration[]{partIteration});
        }
        return new ArrayList<>();
    }

    // All config specs must implement a strict filter
    public abstract PartIteration filterPartIteration(PartMaster partMaster);
    public abstract PartLink filterPartLink(List<PartLink> path);

}