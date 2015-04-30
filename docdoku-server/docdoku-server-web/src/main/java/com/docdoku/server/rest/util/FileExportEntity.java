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

package com.docdoku.server.rest.util;

import com.docdoku.core.configuration.PSFilter;
import com.docdoku.core.product.ConfigurationItemKey;

/**
 * Created by morgan on 29/04/15.
 */
public class FileExportEntity {

    private ConfigurationItemKey configurationItemKey;
    private PSFilter psFilter;

    private String serialNumber;
    private Integer baselineId;

    public FileExportEntity() {
    }

    public PSFilter getPsFilter() {
        return psFilter;
    }

    public void setPsFilter(PSFilter psFilter) {
        this.psFilter = psFilter;
    }

    public ConfigurationItemKey getConfigurationItemKey() {
        return configurationItemKey;
    }

    public void setConfigurationItemKey(ConfigurationItemKey configurationItemKey) {
        this.configurationItemKey = configurationItemKey;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Integer getBaselineId() {
        return baselineId;
    }

    public void setBaselineId(Integer baselineId) {
        this.baselineId = baselineId;
    }
}
