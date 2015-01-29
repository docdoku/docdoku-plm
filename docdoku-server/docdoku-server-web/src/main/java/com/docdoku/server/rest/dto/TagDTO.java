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

/**
 *
 * @author Yassine Belouad
 */
public class TagDTO implements Serializable {

    @XmlElement(nillable = true)
    private String id;
    @XmlElement(nillable = true)
    private String label;
    @XmlElement(nillable = true)
    private String workspaceId;
    
    public TagDTO(){
    }
    
    public TagDTO(String label){
        this.label=label;
    }

    public TagDTO(String label, String workspaceId) {
        this.id = label;
        this.label = label;
        this.workspaceId = workspaceId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getId() {
        id= this.label;
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
    
    
    
}
