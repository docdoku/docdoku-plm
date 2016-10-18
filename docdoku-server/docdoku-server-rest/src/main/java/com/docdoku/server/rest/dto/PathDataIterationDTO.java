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

package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author Chadid Asmae
 */
@XmlRootElement
@ApiModel(value = "PathDataIterationDTO", description = "This class is a representation of a {@link com.docdoku.core.product.PathDataIteration} entity")
public class PathDataIterationDTO implements Serializable {

    @ApiModelProperty(value = "Product instance serial number")
    private String serialNumber;

    @ApiModelProperty(value = "Path data master id")
    private int pathDataMasterId;

    @ApiModelProperty(value = "Path data iteration number")
    private int iteration;

    @ApiModelProperty(value = "Path data iteration note")
    private String iterationNote;

    @ApiModelProperty(value = "List of part links")
    private LightPartLinkListDTO partLinksList;

    @ApiModelProperty(value = "Complete path in context")
    private String path;

    @ApiModelProperty(value = "Path data iteration attached files")
    private List<BinaryResourceDTO> attachedFiles;

    @ApiModelProperty(value = "Path data iteration linked documents")
    private Set<DocumentRevisionDTO> linkedDocuments;

    @ApiModelProperty(value = "Path data iteration attributes")
    private List<InstanceAttributeDTO> instanceAttributes;

    public PathDataIterationDTO() {
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public String getIterationNote() {
        return iterationNote;
    }

    public void setIterationNote(String iterationNote) {
        this.iterationNote = iterationNote;
    }

    public LightPartLinkListDTO getPartLinksList() {
        return partLinksList;
    }

    public void setPartLinksList(LightPartLinkListDTO partLinksList) {
        this.partLinksList = partLinksList;
    }

    public List<BinaryResourceDTO> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(List<BinaryResourceDTO> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public Set<DocumentRevisionDTO> getLinkedDocuments() {
        return linkedDocuments;
    }

    public void setLinkedDocuments(Set<DocumentRevisionDTO> linkedDocuments) {
        this.linkedDocuments = linkedDocuments;
    }

    public List<InstanceAttributeDTO> getInstanceAttributes() {
        return instanceAttributes;
    }

    public void setInstanceAttributes(List<InstanceAttributeDTO> instanceAttributes) {
        this.instanceAttributes = instanceAttributes;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPathDataMasterId() {
        return pathDataMasterId;
    }

    public void setPathDataMasterId(int pathDataMasterId) {
        this.pathDataMasterId = pathDataMasterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PathDataIterationDTO dto = (PathDataIterationDTO) o;

        if (iteration != dto.iteration) {
            return false;
        }
        if (pathDataMasterId != dto.pathDataMasterId) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = pathDataMasterId;
        result = 31 * result + iteration;
        return result;
    }
}
