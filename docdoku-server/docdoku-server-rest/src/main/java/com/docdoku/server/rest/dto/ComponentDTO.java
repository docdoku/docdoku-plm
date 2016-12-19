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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Julien Maffre
 */
@XmlRootElement
@ApiModel(value = "ComponentDTO", description = "This class is the representation of an {@link com.docdoku.core.product.Component} entity")
public class ComponentDTO implements Serializable {

    @ApiModelProperty(value = "Part author name")
    private String author;

    @ApiModelProperty(value = "Part author login")
    private String authorLogin;

    @ApiModelProperty(value = "Part number")
    private String number;

    @ApiModelProperty(value = "Part name")
    private String name = "";

    @ApiModelProperty(value = "Part version")
    private String version;

    @ApiModelProperty(value = "Part iteration")
    private int iteration;

    @ApiModelProperty(value = "Part description")
    private String description;

    @ApiModelProperty(value = "Standard part flag")
    private boolean standardPart;

    @ApiModelProperty(value = "Assembly flag")
    private boolean assembly;

    @ApiModelProperty(value = "Substitute flag")
    private boolean substitute;

    @ApiModelProperty(value = "Component usage link id")
    private String partUsageLinkId;

    @ApiModelProperty(value = "Component usage link description")
    private String partUsageLinkReferenceDescription;

    @ApiModelProperty(value = "List of children components")
    private List<ComponentDTO> components;

    @ApiModelProperty(value = "Amount of component")
    private double amount;

    @ApiModelProperty(value = "Unit for amount")
    private String unit;

    @ApiModelProperty(value = "List of part iteration attributes")
    private List<InstanceAttributeDTO> attributes;

    @ApiModelProperty(value = "Check out user if any")
    private UserDTO checkOutUser;

    @ApiModelProperty(value = "Check out date if any")
    private Date checkOutDate;

    @ApiModelProperty(value = "Released flag")
    private boolean released;

    @ApiModelProperty(value = "Obsolete flag")
    private boolean obsolete;

    @ApiModelProperty(value = "Optional flag")
    private boolean optional;

    @ApiModelProperty(value = "Last part iteration number")
    @XmlElement(nillable = true)
    private int lastIterationNumber;

    @ApiModelProperty(value = "Denied access flag")
    @XmlElement(nillable = true)
    private boolean accessDeny;

    @ApiModelProperty(value = "Available substitutes list")
    @XmlElement(nillable = true)
    private List<String> substituteIds;

    @ApiModelProperty(value = "Hooked modifications notifications")
    private List<ModificationNotificationDTO> notifications;

    @ApiModelProperty(value = "Contains structure path data flag")
    private boolean hasPathData;

    @ApiModelProperty(value = "Is virtual component flag")
    private boolean isVirtual;

    @ApiModelProperty(value = "Component path in context")
    private String path;

    public ComponentDTO() {
    }

    public ComponentDTO(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
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

    public void setStandardPart(boolean standardPart) {
        this.standardPart = standardPart;
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

    public void setComponents(List<ComponentDTO> components) {
        this.components = components;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPartUsageLinkId() {
        return partUsageLinkId;
    }

    public void setPartUsageLinkId(String partUsageLinkId) {
        this.partUsageLinkId = partUsageLinkId;
    }

    public List<InstanceAttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<InstanceAttributeDTO> attributes) {
        this.attributes = attributes;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorLogin() {
        return authorLogin;
    }

    public void setAuthorLogin(String authorLogin) {
        this.authorLogin = authorLogin;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public UserDTO getCheckOutUser() {
        return checkOutUser;
    }

    public void setCheckOutUser(UserDTO checkOutUser) {
        this.checkOutUser = checkOutUser;
    }

    public Date getCheckOutDate() {
        return (checkOutDate != null) ? (Date) checkOutDate.clone() : null;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = (checkOutDate != null) ? (Date) checkOutDate.clone() : null;
    }

    public int getLastIterationNumber() {
        return lastIterationNumber;
    }

    public void setLastIterationNumber(int lastIterationNumber) {
        this.lastIterationNumber = lastIterationNumber;
    }

    public boolean isAccessDeny() {
        return accessDeny;
    }

    public void setAccessDeny(boolean accessDeny) {
        this.accessDeny = accessDeny;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isSubstitute() {
        return substitute;
    }

    public void setSubstitute(boolean substitute) {
        this.substitute = substitute;
    }

    public List<ModificationNotificationDTO> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<ModificationNotificationDTO> notifications) {
        this.notifications = notifications;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public String getPartUsageLinkReferenceDescription() {
        return partUsageLinkReferenceDescription;
    }

    public void setPartUsageLinkReferenceDescription(String partUsageLinkReferenceDescription) {
        this.partUsageLinkReferenceDescription = partUsageLinkReferenceDescription;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isHasPathData() {
        return hasPathData;
    }

    public void setHasPathData(boolean hasPathData) {
        this.hasPathData = hasPathData;
    }

    public boolean isVirtual() {
        return isVirtual;
    }

    public void setVirtual(boolean isVirtual) {
        this.isVirtual = isVirtual;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getSubstituteIds() {
        return substituteIds;
    }

    public void setSubstituteIds(List<String> substituteIds) {
        this.substituteIds = substituteIds;
    }
}
