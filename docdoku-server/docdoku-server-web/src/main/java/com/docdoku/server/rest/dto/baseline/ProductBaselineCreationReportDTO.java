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

import com.docdoku.server.rest.dto.PartDTO;

import java.io.Serializable;
import java.util.List;

public class ProductBaselineCreationReportDTO implements Serializable {
    
    private ProductBaselineDTO productBaseline;
    private String message;
    private List<PartDTO> conflits;

    public ProductBaselineCreationReportDTO() {
    }

    public ProductBaselineDTO getProductBaseline() {
        return productBaseline;
    }

    public void setProductBaseline(ProductBaselineDTO productBaseline) {
        this.productBaseline = productBaseline;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<PartDTO> getConflits() {
        return conflits;
    }

    public void setConflits(List<PartDTO> conflits) {
        this.conflits = conflits;
    }
}