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

import com.docdoku.server.rest.dto.ACLDTO;
import com.docdoku.server.rest.dto.LightPartLinkListDTO;
import com.docdoku.server.rest.dto.UserDTO;

import javax.xml.bind.annotation.XmlElement;
import java.util.Date;
import java.util.List;

public class ProductConfigurationDTO{

    @XmlElement(nillable = true)
    private int id;
    private String configurationItemId;
    private String name;
    private String description;
    private List<String> substituteLinks;
    private List<String> optionalUsageLinks;
    private Date creationDate;
    private UserDTO author;

    private List<LightPartLinkListDTO> substitutesParts;
    private List<LightPartLinkListDTO> optionalsParts;

    private ACLDTO acl;

    public ProductConfigurationDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getConfigurationItemId() {
        return configurationItemId;
    }

    public void setConfigurationItemId(String configurationItemId) {
        this.configurationItemId = configurationItemId;
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

    public List<String> getSubstituteLinks() {
        return substituteLinks;
    }

    public void setSubstituteLinks(List<String> substituteLinks) {
        this.substituteLinks = substituteLinks;
    }

    public List<String> getOptionalUsageLinks() {
        return optionalUsageLinks;
    }

    public void setOptionalUsageLinks(List<String> optionalUsageLinks) {
        this.optionalUsageLinks = optionalUsageLinks;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public ACLDTO getAcl() {
        return acl;
    }

    public void setAcl(ACLDTO acl) {
        this.acl = acl;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }

    public List<LightPartLinkListDTO> getSubstitutesParts() {
        return substitutesParts;
    }

    public void setSubstitutesParts(List<LightPartLinkListDTO> substitutesParts) {
        this.substitutesParts = substitutesParts;
    }

    public List<LightPartLinkListDTO> getOptionalsParts() {
        return optionalsParts;
    }

    public void setOptionalsParts(List<LightPartLinkListDTO> optionalsParts) {
        this.optionalsParts = optionalsParts;
    }
}