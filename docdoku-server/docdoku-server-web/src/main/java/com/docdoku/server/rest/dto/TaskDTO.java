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

import java.io.Serializable;
import java.util.Date;

public class TaskDTO implements Serializable {

    public enum Status {
        NOT_STARTED, IN_PROGRESS, APPROVED, REJECTED, NOT_TO_BE_DONE
    }
    private String closureComment;
    private String title;
    private String instructions;
    private int targetIteration;
    private Date closureDate;
    private String signature;
    private UserDTO worker;
    private Status status;

    public TaskDTO() {
    }

    public String getClosureComment() {
        return closureComment;
    }
    public void setClosureComment(String closureComment) {
        this.closureComment = closureComment;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstructions() {
        return instructions;
    }
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getTargetIteration() {
        return targetIteration;
    }
    public void setTargetIteration(int targetIteration) {
        this.targetIteration = targetIteration;
    }

    public Date getClosureDate() {
        return (closureDate!=null) ? (Date) closureDate.clone() : null;
    }
    public void setClosureDate(Date date) {
        this.closureDate = (date!=null) ? (Date) date.clone() : null;
    }

    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    public UserDTO getWorker() {
        return worker;
    }
    public void setWorker(UserDTO worker) {
        this.worker = worker;
    }

    public String getSignature() {
        return signature;
    }
    public void setSignature(String signature) {
        this.signature = signature;
    }
}
