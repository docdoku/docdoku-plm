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

package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Morgan Guimard
 */
@XmlRootElement
@ApiModel(value="DiskUsageSpaceDTO", description="This class provides storage information")
public class DiskUsageSpaceDTO implements Serializable {

    @ApiModelProperty(value = "Storage size for document files")
    private long documents;

    @ApiModelProperty(value = "Storage size for part files")
    private long parts;

    @ApiModelProperty(value = "Storage size for documentTemplates files")
    private long documentTemplates;

    @ApiModelProperty(value = "Storage size for partTemplates files")
    private long partTemplates;

    public DiskUsageSpaceDTO() {
    }

    public long getDocuments() {
        return documents;
    }

    public void setDocuments(long documents) {
        this.documents = documents;
    }

    public long getParts() {
        return parts;
    }

    public void setParts(long parts) {
        this.parts = parts;
    }

    public long getDocumentTemplates() {
        return documentTemplates;
    }

    public void setDocumentTemplates(long documentTemplates) {
        this.documentTemplates = documentTemplates;
    }

    public long getPartTemplates() {
        return partTemplates;
    }

    public void setPartTemplates(long partTemplates) {
        this.partTemplates = partTemplates;
    }
}
