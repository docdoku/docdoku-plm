/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Julien Maffre
 */
public class ComponentDTO implements Serializable{

    private String author;
    private String authorLogin;
    private String number;
    private String name = "";
    private String version;
    private int iteration;
    private String description;
    private boolean standardPart;
    private boolean assembly;
    private int partUsageLinkId;
    private List<ComponentDTO> components;
    private int amount;
    private List<InstanceAttributeDTO> attributes;
    private UserDTO checkOutUser;
    private Date checkOutDate;
    @XmlElement(nillable = true)
    int lastIterationNumber;

    public ComponentDTO() {}

    public ComponentDTO(String number) {
        this.number=number;
    }

    public String getNumber() {
        return number;
    }

    public boolean isAssembly() {
        return assembly;
    }

    public void setAssembly(boolean assembly) {
        this.assembly = assembly;
    }

    public boolean isStandardPart() {
        return standardPart;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public List<ComponentDTO> getComponents() {
        return components;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public void setComponents(List<ComponentDTO> components) {
        this.components = components;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPartUsageLinkId() {
        return partUsageLinkId;
    }

    public void setPartUsageLinkId(int partUsageLinkId) {
        this.partUsageLinkId = partUsageLinkId;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setStandardPart(boolean standardPart) {
        this.standardPart = standardPart;
    }

    public void setAttributes(List<InstanceAttributeDTO> attributes) {this.attributes = attributes;}
    public List<InstanceAttributeDTO> getAttributes() {return attributes;}

    public String getAuthor() {return author;}
    public void setAuthor(String author) {this.author = author;}

    public String getAuthorLogin() {return authorLogin;}
    public void setAuthorLogin(String authorLogin) {this.authorLogin = authorLogin;}

    public int getAmount() {return amount;}
    public void setAmount(int amount) {this.amount = amount;}

    public UserDTO getCheckOutUser() {return checkOutUser;}
    public void setCheckOutUser(UserDTO checkOutUser) {this.checkOutUser = checkOutUser;}

    public Date getCheckOutDate() {return checkOutDate;}
    public void setCheckOutDate(Date checkOutDate) {this.checkOutDate = checkOutDate;}

    public int getLastIterationNumber() {return lastIterationNumber;}
    public void setLastIterationNumber(int lastIterationNumber) {this.lastIterationNumber = lastIterationNumber;}
}
