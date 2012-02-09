/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.docdoku.server.rest.dto;

import java.io.Serializable;

/**
 *
 * @author yassinebelouad
 */
public class FolderDTO implements Serializable{
    
    private String completePath;

    
    public FolderDTO() {
    
    }
    
    public FolderDTO(String parentFolder, String name) {
        completePath=parentFolder+"/"+name;
    }


    public String getCompletePath() {
        return completePath;
    }

    public void setCompletePath(String completePath) {
        this.completePath = completePath;
    }

    
}
