/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.gwt.explorer.shared;

import java.io.Serializable;
import java.util.Date;

public class TaskDTO implements Serializable {

    public enum Status {

        NOT_STARTED, IN_PROGRESS, APPROVED, REJECTED
    };
    private String closureComment;
    private String title;
    private String instructions;
    private int targetIteration;
    private Date closureDate;
    private UserDTO worker;
    private Status status;

    // 0 = approved
    // 1 = in progress
    // 2 = not started
    // 3 = rejected
    //private int status ;
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
        return closureDate;
    }

    public void setClosureDate(java.util.Date date) {
        this.closureDate = date;
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
}
