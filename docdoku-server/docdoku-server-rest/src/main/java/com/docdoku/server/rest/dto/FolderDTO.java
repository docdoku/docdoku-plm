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
 * @author Yassine Belouad
 */

@XmlRootElement
@ApiModel(value = "FolderDTO", description = "This class is a representation of a {@link com.docdoku.core.document.Folder} entity")
public class FolderDTO implements Serializable {

    @ApiModelProperty(value = "Folder full path")
    private String path;

    @ApiModelProperty(value = "Folder id")
    private String id;

    @ApiModelProperty(value = "Folder name")
    private String name;

    @ApiModelProperty(value = "Folder home flag")
    private boolean home;

    public FolderDTO() {

    }

    public FolderDTO(String parentFolder, String name) {
        this.name = name.trim();
        path = parentFolder + "/" + this.name;
    }

    public static String replaceSlashWithColon(String completePathWithSlashes) {
        return completePathWithSlashes.replaceAll("/", ":");
    }

    public static String replaceColonWithSlash(String completePathWithColons) {
        return completePathWithColons.replaceAll(":", "/");
    }

    public static String extractName(String slashedCompletePath) {
        stripTrailingSlash(slashedCompletePath);
        int lastSlash = slashedCompletePath.lastIndexOf('/');
        return slashedCompletePath.substring(lastSlash, slashedCompletePath.length());
    }

    public static String extractParentFolder(String slashedCompletePath) {
        stripTrailingSlash(slashedCompletePath);
        int lastSlash = slashedCompletePath.lastIndexOf('/');
        return slashedCompletePath.substring(0, lastSlash);
    }

    private static String stripTrailingSlash(String completePath) {
        if (completePath.charAt(completePath.length() - 1) == '/') {
            return completePath.substring(0, completePath.length() - 1);
        } else {
            return completePath;
        }
    }

    public boolean isHome() {
        return home;
    }

    public void setHome(boolean home) {
        this.home = home;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
