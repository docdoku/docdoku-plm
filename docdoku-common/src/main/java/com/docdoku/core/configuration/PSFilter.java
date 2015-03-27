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

import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartLink;
import com.docdoku.core.product.PartMaster;

import java.io.Serializable;
import java.util.List;

/**
 * A PSFilter is used to select for each {@link com.docdoku.core.product.PartMaster}s
 * the right {@link com.docdoku.core.product.PartIteration}s
 *
 * @author Morgan Guimard
 */

public abstract class PSFilter implements Serializable{
    public PSFilter() {
    }
    public abstract List<PartIteration> filter(PartMaster partMaster);
    public abstract List<PartLink> filter(List<PartLink> path);
}