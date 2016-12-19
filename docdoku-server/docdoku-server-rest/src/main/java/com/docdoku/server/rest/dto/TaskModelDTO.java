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

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Emmanuel Nhan
 */
@XmlRootElement
@ApiModel(value="TaskModelDTO", description="This class is a representation of a {@link com.docdoku.core.workflow.TaskModel} entity")
public class TaskModelDTO implements Serializable {

    @ApiModelProperty(value = "Task model num")
    private int num;

    @ApiModelProperty(value = "Task model title")
    private String title;

    @ApiModelProperty(value = "Task model instructions")
    private String instructions;

    @ApiModelProperty(value = "Task model assigned role")
    private RoleDTO role;

    @ApiModelProperty(value = "Task model duration")
    private int duration;

    public TaskModelDTO() {
    }

    public TaskModelDTO(int num, String title, String instructions, RoleDTO role, int duration) {
        this.num = num;
        this.title = title;
        this.instructions = instructions;
        this.role = role;
        this.duration = duration;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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

    public RoleDTO getRole() {
        return role;
    }

    public void setRole(RoleDTO role) {
        this.role = role;
    }
}
