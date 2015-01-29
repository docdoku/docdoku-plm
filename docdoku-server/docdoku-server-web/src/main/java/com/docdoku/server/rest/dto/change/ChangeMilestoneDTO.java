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

package com.docdoku.server.rest.dto.change;

import com.docdoku.server.rest.dto.ACLDTO;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@XmlRootElement
public class ChangeMilestoneDTO implements Serializable {

    private int id;
    private String title;
    private Date dueDate;
    private String description;
    private String workspaceId;
    private int numberOfRequests;
    private int numberOfOrders;
    private ACLDTO acl;
    private boolean writable;

    public ChangeMilestoneDTO() {

    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDueDate() {
        return dueDate;
    }
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }
    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public int getNumberOfRequests() {
        return numberOfRequests;
    }
    public void setNumberOfRequests(int numberOfRequests) {
        this.numberOfRequests = numberOfRequests;
    }

    public int getNumberOfOrders() {
        return numberOfOrders;
    }
    public void setNumberOfOrders(int numberOfOrders) {
        this.numberOfOrders = numberOfOrders;
    }

    public ACLDTO getAcl() {
        return acl;
    }
    public void setAcl(ACLDTO acl) {
        this.acl = acl;
    }

    public boolean isWritable() {
        return writable;
    }
    public void setWritable(boolean writable) {
        this.writable = writable;
    }
}
