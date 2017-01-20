/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import com.docdoku.core.configuration.ProductStructureFilter;
import com.docdoku.core.product.ConfigurationItemKey;

/**
 * This class holds the context for a product export
 * See {link com.docdoku.server.rest.writers.DocumentBaselineFileExportMessageBodyWriter} for response implementation
 *
 * @author morgan on 29/04/15.
 */
public class ProductFileExport {

    private ConfigurationItemKey configurationItemKey;
    private ProductStructureFilter psFilter;

    private String serialNumber;
    private Integer baselineId;

    private boolean exportNativeCADFile;
    private boolean exportDocumentLinks;


    public ProductFileExport() {
    }

    public ProductStructureFilter getPsFilter() {
        return psFilter;
    }

    public void setPsFilter(ProductStructureFilter psFilter) {
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

    public boolean isExportNativeCADFile() {
        return exportNativeCADFile;
    }

    public void setExportNativeCADFile(boolean exportNativeCADFile) {
        this.exportNativeCADFile = exportNativeCADFile;
    }

    public boolean isExportDocumentLinks() {
        return exportDocumentLinks;
    }

    public void setExportDocumentLinks(boolean exportDocumentLinks) {
        this.exportDocumentLinks = exportDocumentLinks;
    }
}
