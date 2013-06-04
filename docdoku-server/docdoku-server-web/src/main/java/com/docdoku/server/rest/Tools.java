/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.server.rest;

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.configuration.Baseline;
import com.docdoku.core.configuration.BaselinedPart;
import com.docdoku.core.configuration.BaselinedPartKey;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartUsageLink;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.server.rest.dto.*;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Florent Garin
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
       docMsDTO.setAcl(null);

       return docMsDTO;
   }


    public static ACLDTO mapACLtoACLDTO(ACL acl) {

        ACLDTO aclDTO = new ACLDTO();

        for (Map.Entry<User,ACLUserEntry> entry : acl.getUserEntries().entrySet()) {
            ACLUserEntry aclEntry = entry.getValue();
            aclDTO.addUserEntry(aclEntry.getPrincipalLogin(),aclEntry.getPermission());
        }

        for (Map.Entry<UserGroup,ACLUserGroupEntry> entry : acl.getGroupEntries().entrySet()) {
            ACLUserGroupEntry aclEntry = entry.getValue();
            aclDTO.addGroupEntry(aclEntry.getPrincipalId(),aclEntry.getPermission());
        }

        return aclDTO;

    }

    public static PartDTO mapPartRevisionToPartDTO(PartRevision partRevision){

        Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();

        PartDTO partDTO = mapper.map(partRevision.getPartMaster(),PartDTO.class);
        partDTO.setNumber(partRevision.getPartNumber());
        partDTO.setPartKey(partRevision.getPartNumber() + "-" + partRevision.getVersion());
        partDTO.setName(partRevision.getPartMaster().getName());
        partDTO.setStandardPart(partRevision.getPartMaster().isStandardPart());
        partDTO.setVersion(partRevision.getVersion());

        List<PartIterationDTO> partIterationDTOs = new ArrayList<PartIterationDTO>();
        for(PartIteration partIteration : partRevision.getPartIterations()){
            partIterationDTOs.add(mapPartIterationToPartIterationDTO(partIteration));
        }
        partDTO.setPartIterations(partIterationDTOs);

        if(partRevision.isCheckedOut()){
            partDTO.setCheckOutDate(partRevision.getCheckOutDate());
            UserDTO checkoutUserDTO = mapper.map(partRevision.getCheckOutUser(),UserDTO.class);
            partDTO.setCheckOutUser(checkoutUserDTO);
        }

        if(partRevision.hasWorkflow()){
            partDTO.setLifeCycleState(partRevision.getWorkflow().getLifeCycleState());
        }

        ACL acl = partRevision.getACL();
        if(acl != null){
            partDTO.setAcl(Tools.mapACLtoACLDTO(acl));
        }else{
            partDTO.setAcl(null);
        }

        return partDTO;
    }

    public static PartIterationDTO mapPartIterationToPartIterationDTO(PartIteration partIteration){
        Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();

        List<PartUsageLinkDTO> usageLinksDTO = new ArrayList<PartUsageLinkDTO>();
        PartIterationDTO partIterationDTO = mapper.map(partIteration, PartIterationDTO.class);
        for(PartUsageLink partUsageLink : partIteration.getComponents()){
            PartUsageLinkDTO partUsageLinkDTO = mapper.map(partUsageLink, PartUsageLinkDTO.class);
            usageLinksDTO.add(partUsageLinkDTO);
        }
        partIterationDTO.setComponents(usageLinksDTO);
        partIterationDTO.setNumber(partIteration.getPartRevision().getPartNumber());
        partIterationDTO.setVersion(partIteration.getPartRevision().getVersion());

        return partIterationDTO;
    }

    public static List<BaselinedPartDTO> mapBaselinedPartsToBaselinedPartDTO(Baseline baseline){
        List<BaselinedPartDTO> baselinedPartDTOs = new ArrayList<BaselinedPartDTO>();
        Set<Map.Entry<BaselinedPartKey,BaselinedPart>> entries = baseline.getBaselinedParts().entrySet();

        for(Map.Entry<BaselinedPartKey,BaselinedPart> entry : entries){
            baselinedPartDTOs.add(mapBaselinedPartToBaselinedPartDTO(entry.getValue()));
        }

        return baselinedPartDTOs;
    }

    public static BaselinedPartDTO mapBaselinedPartToBaselinedPartDTO(BaselinedPart baselinedPart){
        BaselinedPartDTO baselinedPartDTO = new BaselinedPartDTO();
        PartIteration partI = baselinedPart.getTargetPart();
        baselinedPartDTO.setNumber(partI.getPartNumber());
        baselinedPartDTO.setVersion(partI.getPartVersion());
        baselinedPartDTO.setIteration(partI.getIteration());
        baselinedPartDTO.setLastIteration(partI.getPartRevision().getLastIteration().getIteration());
        return baselinedPartDTO;
    }
}