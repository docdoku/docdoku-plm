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

package com.docdoku.server.rest.dto.baseline;

import com.docdoku.core.configuration.ProductBaseline;

import java.util.Date;
import java.util.List;

public class ProductBaselineDTO extends BaselineDTO {

    private String configurationItemId;
    private ProductBaseline.BaselineType type;
    private List<BaselinedPartDTO> baselinedParts;

    public ProductBaselineDTO() {
    }

    public ProductBaselineDTO(int id, String name, String description, Date creationDate) {
        super(id, name, description, creationDate);
    }

    public List<BaselinedPartDTO> getBaselinedParts() {
        return baselinedParts;
    }
    public void setBaselinedParts(List<BaselinedPartDTO> baselinedParts) {
        this.baselinedParts = baselinedParts;
    }

    public ProductBaseline.BaselineType getType() {
        return type;
    }
    public void setType(ProductBaseline.BaselineType type) {
        this.type = type;
    }

    public String getConfigurationItemId() {
        return configurationItemId;
    }
    public void setConfigurationItemId(String configurationItemId) {
        this.configurationItemId = configurationItemId;
    }
}