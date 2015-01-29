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

import java.io.Serializable;

public class ProductBaselineCreationDTO extends BaselineDTO implements Serializable {
    private String configurationItemId;
    private ProductBaseline.BaselineType type;
    private String creationMessage;

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

    public String getCreationMessage() {
        return creationMessage;
    }
    public void setCreationMessage(String creationMessage) {
        this.creationMessage = creationMessage;
    }
}