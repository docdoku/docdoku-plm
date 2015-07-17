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
    private boolean substitute;
    private String partUsageLinkId;
    private String partUsageLinkReferenceDescription;
    private List<ComponentDTO> components;
    private double amount;
    private String unit;
    private List<InstanceAttributeDTO> attributes;
    private UserDTO checkOutUser;
    private Date checkOutDate;
    private boolean released;
    private boolean obsolete;
    private boolean optional;
    @XmlElement(nillable = true)
    private int lastIterationNumber;
    @XmlElement(nillable = true)
    private boolean accessDeny;
    @XmlElement(nillable = true)
    private List<String> substituteIds;
    private List<ModificationNotificationDTO> notifications;
    private boolean hasPathData;
    private boolean isVirtual;
    private String path;


    public ComponentDTO() {

    }

    public ComponentDTO(String number) {
        this.number=number;
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

    public void setAttributes(List<InstanceAttributeDTO> attributes) {
        this.attributes = attributes;
    }
    public List<InstanceAttributeDTO> getAttributes() {
        return attributes;
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
        return (checkOutDate!=null) ? (Date) checkOutDate.clone() : null;
    }
    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = (checkOutDate!=null) ? (Date) checkOutDate.clone() : null;
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
