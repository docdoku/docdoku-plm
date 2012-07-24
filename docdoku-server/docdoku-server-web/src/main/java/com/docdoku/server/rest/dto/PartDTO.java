/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

package com.docdoku.server.rest.dto;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Florent Garin
 */
public class PartDTO implements Serializable{
    
    private String workspaceId;
    private String number;
    private String name;
    private String version;
    private int iteration;
    private String description;
    private List<String> files;
    private boolean standardPart;
    
    private List<PartDTO> components;
    private List<CADInstanceDTO> instances;
    
    public PartDTO(){
        
    }
    
    public PartDTO(String workspaceId, String number) {
        this.workspaceId=workspaceId;
        this.number=number;
    }



    public String getNumber() {
        return number;
    }

    public String getWorkspaceId() {
        return workspaceId;
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

    
    public List<PartDTO> getComponents() {
        return components;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getFiles() {
        return files;
    }

    public String getName() {
        return name;
    }

    public void setComponents(List<PartDTO> components) {
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

    
    public void setFiles(List<String> files) {
        this.files = files;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public void setNumber(String number) {
        this.number = number;
    }

    public void setStandardPart(boolean standardPart) {
        this.standardPart = standardPart;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public List<CADInstanceDTO> getInstances() {
        return instances;
    }

    public void setInstances(List<CADInstanceDTO> instances) {
        this.instances = instances;
    }
    
}
