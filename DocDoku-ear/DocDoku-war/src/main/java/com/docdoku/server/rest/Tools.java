/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.docdoku.server.rest;

import com.docdoku.server.rest.dto.DocumentIterationDTO;
import com.docdoku.server.rest.dto.DocumentMasterDTO;
import com.docdoku.server.rest.dto.UserDTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author flo
 */
public class Tools {
    
    private Tools(){};
    
    public static String stripTrailingSlash(String completePath){
        if(completePath.charAt(completePath.length()-1)=='/')
            return completePath.substring(0,completePath.length()-1);
        else
            return completePath;
    }
    
    public static String stripLeadingSlash(String completePath){
        if(completePath.charAt(0)=='/')
            return completePath.substring(1,completePath.length());
        else
            return completePath;
    }
    
    public static String replaceSlashWithColon(String completePath){
        return completePath.replaceAll("/", ":");
    }
    
    public static String replaceColonWithSlash(String completePath){
        return completePath.replaceAll(":", "/");
    }    

    public static DocumentMasterDTO createLightDocumentMasterDTO(DocumentMasterDTO docMsDTO){
       
       if (docMsDTO.getLastIteration() != null) {      
           DocumentIterationDTO lastIteration = docMsDTO.getLastIteration();
           List<DocumentIterationDTO> iterations = new ArrayList<DocumentIterationDTO>();
           iterations.add(lastIteration);
           docMsDTO.setDocumentIterations(iterations);
       }
       
       docMsDTO.setTags(null);
       docMsDTO.setWorkflow(null);       
       
       return docMsDTO;
   }    
}
