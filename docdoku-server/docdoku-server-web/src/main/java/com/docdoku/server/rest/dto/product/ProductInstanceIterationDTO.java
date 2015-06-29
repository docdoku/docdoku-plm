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

package com.docdoku.server.rest.dto.product;

import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.dto.baseline.BaselineDTO;
import com.docdoku.server.rest.dto.baseline.BaselinedPartDTO;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement
public class ProductInstanceIterationDTO {
    private String serialNumber;
    private int iteration;
    private String iterationNote;
    private String configurationItemId;
    private String updateAuthor;
    private String updateAuthorName;
    private Date updateDate;
    private List<BaselinedPartDTO> baselinedParts;
    private List<String> substituteLinks;
    private List<String> optionalUsageLinks;
    private UserDTO author;
    private Date creationDate;
    private List<PartMinimalListDTO> substitutesParts;
    private List<PartMinimalListDTO> optionalsParts;
    private List<PathToPathLinkDTO> pathToPathLinks;

    private BaselineDTO basedOn;
    private List<PathDataMasterDTO> pathDataMasterList;
    private List<PartMinimalListDTO> usedByPaths;
    private List<InstanceAttributeDTO> instanceAttributes = new ArrayList<>();
    private Set<DocumentRevisionDTO> linkedDocuments = new HashSet<>();
    private List<String> attachedFiles;

    public ProductInstanceIterationDTO() {
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

    public String getConfigurationItemId() {
        return configurationItemId;
    }
    public void setConfigurationItemId(String configurationItemId) {
        this.configurationItemId = configurationItemId;
    }

    public String getUpdateAuthor() {
        return updateAuthor;
    }
    public void setUpdateAuthor(String updateAuthor) {
        this.updateAuthor = updateAuthor;
    }

    public String getUpdateAuthorName() {
        return updateAuthorName;
    }
    public void setUpdateAuthorName(String updateAuthorName) {
        this.updateAuthorName = updateAuthorName;
    }

    public Date getUpdateDate() {
        return updateDate;
    }
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public List<BaselinedPartDTO> getBaselinedParts() {
        return baselinedParts;
    }

    public void setBaselinedParts(List<BaselinedPartDTO> baselinedParts) {
        this.baselinedParts = baselinedParts;
    }

    public List<String> getOptionalUsageLinks() {
        return optionalUsageLinks;
    }

    public void setOptionalUsageLinks(List<String> optionalUsageLinks) {
        this.optionalUsageLinks = optionalUsageLinks;
    }

    public List<InstanceAttributeDTO> getInstanceAttributes() {
        return instanceAttributes;
    }

    public void setInstanceAttributes(List<InstanceAttributeDTO> instanceAttributes) {
        this.instanceAttributes = instanceAttributes;
    }

    public Set<DocumentRevisionDTO> getLinkedDocuments() {
        return linkedDocuments;
    }

    public void setLinkedDocuments(Set<DocumentRevisionDTO> linkedDocuments) {
        this.linkedDocuments = linkedDocuments;
    }

    public List<String> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(List<String> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public BaselineDTO getBasedOn() {
        return basedOn;
    }

    public void setBasedOn(BaselineDTO basedOn) {
        this.basedOn = basedOn;
    }

    public List<PathDataMasterDTO> getPathDataMasterList() {
        return pathDataMasterList;
    }

    public void setPathDataMasterList(List<PathDataMasterDTO> pathDataMasterList) {
        this.pathDataMasterList = pathDataMasterList;
    }

    public List<String> getSubstituteLinks() {
        return substituteLinks;
    }

    public void setSubstituteLinks(List<String> substituteLinks) {
        this.substituteLinks = substituteLinks;
    }

    public List<PartMinimalListDTO> getSubstitutesParts() {
        return substitutesParts;
    }

    public void setSubstitutesParts(List<PartMinimalListDTO> substitutesParts) {
        this.substitutesParts = substitutesParts;
    }

    public List<PartMinimalListDTO> getOptionalsParts() {
        return optionalsParts;
    }

    public void setOptionalsParts(List<PartMinimalListDTO> optionalsParts) {
        this.optionalsParts = optionalsParts;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }


    public List<PartMinimalListDTO> getUsedByPaths() {
        return usedByPaths;
    }

    public void setUsedByPaths(List<PartMinimalListDTO> usedByPaths) {
        this.usedByPaths = usedByPaths;
    }

    public List<PathToPathLinkDTO> getPathToPathLinks() {
        return this.pathToPathLinks;
    }
    public void setPathToPathLinks(List<PathToPathLinkDTO> pathToPathLinks) {
        this.pathToPathLinks = pathToPathLinks;
    }
}