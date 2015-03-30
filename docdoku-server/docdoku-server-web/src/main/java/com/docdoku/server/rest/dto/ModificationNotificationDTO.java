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
import java.util.Date;

public class ModificationNotificationDTO {

    private int id;

    private String impactedPartNumber;
    private String impactedPartVersion;

    private String modifiedPartNumber;
    private String modifiedPartName;
    private String modifiedPartVersion;
    private int modifiedPartIteration;

    private Date checkInDate;
    private String iterationNote;
    private UserDTO author;

    @XmlElement(nillable = true)
    private boolean acknowledged;

    private String ackComment;
    private UserDTO ackAuthor;
    private Date ackDate;

    public ModificationNotificationDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImpactedPartNumber() {
        return impactedPartNumber;
    }

    public void setImpactedPartNumber(String impactedPartNumber) {
        this.impactedPartNumber = impactedPartNumber;
    }

    public String getImpactedPartVersion() {
        return impactedPartVersion;
    }

    public void setImpactedPartVersion(String impactedPartVersion) {
        this.impactedPartVersion = impactedPartVersion;
    }

    public String getModifiedPartNumber() {
        return modifiedPartNumber;
    }

    public void setModifiedPartNumber(String modifiedPartNumber) {
        this.modifiedPartNumber = modifiedPartNumber;
    }

    public String getModifiedPartVersion() {
        return modifiedPartVersion;
    }

    public void setModifiedPartVersion(String modifiedPartVersion) {
        this.modifiedPartVersion = modifiedPartVersion;
    }

    public int getModifiedPartIteration() {
        return modifiedPartIteration;
    }

    public void setModifiedPartIteration(int modifiedPartIteration) {
        this.modifiedPartIteration = modifiedPartIteration;
    }

    public String getModifiedPartName() {
        return modifiedPartName;
    }

    public void setModifiedPartName(String modifiedPartName) {
        this.modifiedPartName = modifiedPartName;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getIterationNote() {
        return iterationNote;
    }

    public void setIterationNote(String iterationNote) {
        this.iterationNote = iterationNote;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public String getAckComment() {
        return ackComment;
    }

    public void setAckComment(String ackComment) {
        this.ackComment = ackComment;
    }

    public UserDTO getAckAuthor() {
        return ackAuthor;
    }

    public void setAckAuthor(UserDTO ackAuthor) {
        this.ackAuthor = ackAuthor;
    }

    public Date getAckDate() {
        return ackDate;
    }

    public void setAckDate(Date ackDate) {
        this.ackDate = ackDate;
    }

}
