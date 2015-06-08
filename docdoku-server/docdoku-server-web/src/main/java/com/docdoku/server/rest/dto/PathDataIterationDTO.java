package com.docdoku.server.rest.dto;/*
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

import java.util.List;
import java.util.Set;

/**
 * @author: Chadid Asmae
 */
public class PathDataIterationDTO {
    private String serialNumber;
    private int partMasterId;
    private int iteration;
    private String noteIteration;
    private PartMinimalListDTO partsPath;
    private String path;
    private List<String> attachedFiles;
    private Set<DocumentRevisionDTO> linkedDocuments;
    private List<InstanceAttributeDTO> instanceAttributes;

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

    public String getNoteIteration() {
        return noteIteration;
    }

    public void setNoteIteration(String noteIteration) {
        this.noteIteration = noteIteration;
    }

    public PartMinimalListDTO getPartsPath() {
        return partsPath;
    }

    public void setPartsPath(PartMinimalListDTO partsPath) {
        this.partsPath = partsPath;
    }

    public List<String> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(List<String> attachedFiles) {
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

    public int getPartMasterId() {
        return partMasterId;
    }

    public void setPartMasterId(int partMasterId) {
        this.partMasterId = partMasterId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PathDataIterationDTO dto = (PathDataIterationDTO) o;

        if (partMasterId != dto.partMasterId) return false;
        if (!partsPath.equals(dto.partsPath)) return false;
        if (path != null ? !path.equals(dto.path) : dto.path != null) return false;
        if (!serialNumber.equals(dto.serialNumber)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = partMasterId;
        result = 31 * result + (partsPath != null ? partsPath.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }
}
