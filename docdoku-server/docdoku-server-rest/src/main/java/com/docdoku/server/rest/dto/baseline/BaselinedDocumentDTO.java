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

package com.docdoku.server.rest.dto.baseline;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement
@ApiModel(value="BaselinedDocumentDTO", description="This class is the representation of a {@link com.docdoku.core.configuration.BaselinedDocument} entity")
public class BaselinedDocumentDTO implements Serializable {

    @ApiModelProperty(value = "Document master id")
    private String documentMasterId;

    @ApiModelProperty(value = "Document title")
    private String title;

    @ApiModelProperty(value = "Document version")
    private String version;

    @ApiModelProperty(value = "Document iteration")
    private int iteration;

    @ApiModelProperty(value = "Document available iterations")
    private List<BaselinedDocumentOptionDTO> availableIterations;

    public BaselinedDocumentDTO() {
    }

    public BaselinedDocumentDTO(String documentMasterId, String version, int iteration, String title) {
        this.documentMasterId = documentMasterId;
        this.version = version;
        this.title = title;
        this.iteration = iteration;
    }

    public String getDocumentMasterId() {
        return documentMasterId;
    }

    public void setDocumentMasterId(String documentMasterId) {
        this.documentMasterId = documentMasterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public List<BaselinedDocumentOptionDTO> getAvailableIterations() {
        return availableIterations;
    }

    public void setAvailableIterations(List<BaselinedDocumentOptionDTO> availableIterations) {
        this.availableIterations = availableIterations;
    }
}