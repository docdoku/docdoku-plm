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

import com.docdoku.core.configuration.DocumentBaseline;
import com.docdoku.server.rest.dto.UserDTO;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@XmlRootElement
public class DocumentBaselineDTO implements Serializable {

    private int id;
    private String name;
    private String description;
    private Date creationDate;
    private DocumentBaseline.BaselineType type;
    private List<BaselinedDocumentDTO> baselinedDocuments;
    private UserDTO author;

    public DocumentBaselineDTO() {
    }

    public DocumentBaselineDTO(String name, String description, int id, Date creationDate, DocumentBaseline.BaselineType type, UserDTO author) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.creationDate = creationDate;
        this.type = type;
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public DocumentBaseline.BaselineType getType() {
        return type;
    }

    public void setType(DocumentBaseline.BaselineType type) {
        this.type = type;
    }

    public List<BaselinedDocumentDTO> getBaselinedDocuments() {
        return baselinedDocuments;
    }

    public void setBaselinedDocuments(List<BaselinedDocumentDTO> baselinedDocuments) {
        this.baselinedDocuments = baselinedDocuments;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }
}
