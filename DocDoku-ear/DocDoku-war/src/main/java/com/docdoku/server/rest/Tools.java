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
    
    public static String replaceSlashWithDots(String completePath){
        return completePath.replaceAll("/", ":");
    }
    
    public static String replaceDotsWithSlash(String completePath){
        return completePath.replaceAll(":", "/");
    }    

    public static DocumentMasterDTO createLightDocumentMasterDTO(DocumentMasterDTO docMsDTO){

       String documentAuthorName = docMsDTO.getAuthor().getName();
       docMsDTO.setAuthor(new UserDTO());
       docMsDTO.getAuthor().setName(documentAuthorName);
       
       if(docMsDTO.getCheckOutUser()!=null){
            String checkOutUserName = docMsDTO.getCheckOutUser().getName();
            docMsDTO.setCheckOutUser(new UserDTO());
            docMsDTO.getCheckOutUser().setName(checkOutUserName);
       }
       
       if (docMsDTO.getLastIteration() != null) {
           
           Date lastIterationCreationDate = docMsDTO.getLastIteration().getCreationDate();
           int lastIterationNumber = docMsDTO.getLastIteration().getIteration();
           String lastIterationAuthorName = docMsDTO.getLastIteration().getAuthor().getName();
           List<DocumentIterationDTO> iterations = new ArrayList<DocumentIterationDTO>();
           iterations.add(new DocumentIterationDTO());
           docMsDTO.setDocumentIterations(iterations);
           docMsDTO.getLastIteration().setCreationDate(lastIterationCreationDate);
           docMsDTO.getLastIteration().setIteration(lastIterationNumber);
           
           docMsDTO.getLastIteration().setAuthor(new UserDTO());
           docMsDTO.getLastIteration().getAuthor().setName(lastIterationAuthorName);
       }
       
       docMsDTO.setTags(null);
       docMsDTO.setWorkflow(null);       
       
       return docMsDTO;
   }    
}
