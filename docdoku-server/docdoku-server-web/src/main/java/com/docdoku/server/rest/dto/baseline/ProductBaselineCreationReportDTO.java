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

package com.docdoku.server.rest.dto.baseline;

import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.server.rest.dto.PartDTO;

import java.io.Serializable;
import java.util.Set;

public class ProductBaselineCreationReportDTO implements Serializable {
    
    private ProductBaseline productBaseline;
    private String message;
    private Set<PartDTO> conflits;

    public ProductBaselineCreationReportDTO() {
    }

    public ProductBaselineCreationReportDTO(ProductBaseline productBaseline, String message, Set<PartDTO> conflits) {
        this.productBaseline = productBaseline;
        this.message = message;
        this.conflits = conflits;
    }

    public ProductBaseline getProductBaseline() {
        return productBaseline;
    }

    public void setProductBaseline(ProductBaseline productBaseline) {
        this.productBaseline = productBaseline;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Set<PartDTO> getConflits() {
        return conflits;
    }

    public void setConflits(Set<PartDTO> conflits) {
        this.conflits = conflits;
    }
}