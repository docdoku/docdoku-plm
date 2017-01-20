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

package com.docdoku.server.rest;

import com.docdoku.core.change.ModificationNotification;
import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.configuration.*;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.product.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.dto.baseline.*;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Florent Garin
 */
public class Tools {

    private Tools() {

    }

    public static String stripTrailingSlash(String completePath) {
        if (completePath == null || completePath.isEmpty()) {
            return completePath;
        }
        if (completePath.charAt(completePath.length() - 1) == '/') {
            return completePath.substring(0, completePath.length() - 1);
        } else {
            return completePath;
        }
    }

    public static String stripLeadingSlash(String completePath) {
        if (completePath.charAt(0) == '/') {
            return completePath.substring(1, completePath.length());
        } else {
            return completePath;
        }
    }

    public static DocumentRevisionDTO createLightDocumentRevisionDTO(DocumentRevisionDTO docRsDTO) {

        if (docRsDTO.getLastIteration() != null) {
            DocumentIterationDTO lastIteration = docRsDTO.getLastIteration();
            List<DocumentIterationDTO> iterations = new ArrayList<>();
            iterations.add(lastIteration);
            docRsDTO.setDocumentIterations(iterations);
        }

        docRsDTO.setTags(null);
        docRsDTO.setWorkflow(null);

        return docRsDTO;
    }


    public static ACLDTO mapACLtoACLDTO(ACL acl) {

        ACLDTO aclDTO = new ACLDTO();

        for (Map.Entry<User, ACLUserEntry> entry : acl.getUserEntries().entrySet()) {
            ACLUserEntry aclEntry = entry.getValue();
            aclDTO.addUserEntry(aclEntry.getPrincipalLogin(), aclEntry.getPermission());
        }

        for (Map.Entry<UserGroup, ACLUserGroupEntry> entry : acl.getGroupEntries().entrySet()) {
            ACLUserGroupEntry aclEntry = entry.getValue();
            aclDTO.addGroupEntry(aclEntry.getPrincipalId(), aclEntry.getPermission());
        }

        return aclDTO;

    }

    public static List<ModificationNotificationDTO> mapModificationNotificationsToModificationNotificationDTO(Collection<ModificationNotification> pNotifications) {
        return pNotifications.stream().map(Tools::mapModificationNotificationToModificationNotificationDTO).collect(Collectors.toList());
    }

    public static ModificationNotificationDTO mapModificationNotificationToModificationNotificationDTO(ModificationNotification pNotification) {
        ModificationNotificationDTO dto = new ModificationNotificationDTO();
        Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();

        UserDTO userDTO = mapper.map(pNotification.getModifiedPart().getAuthor(), UserDTO.class);
        dto.setAuthor(userDTO);

        dto.setId(pNotification.getId());

        dto.setImpactedPartNumber(pNotification.getImpactedPart().getNumber());
        dto.setImpactedPartVersion(pNotification.getImpactedPart().getVersion());

        User ackAuthor = pNotification.getAcknowledgementAuthor();
        if (ackAuthor != null) {
            UserDTO ackDTO = mapper.map(ackAuthor, UserDTO.class);
            dto.setAckAuthor(ackDTO);
        }
        dto.setAcknowledged(pNotification.isAcknowledged());
        dto.setAckComment(pNotification.getAcknowledgementComment());
        dto.setAckDate(pNotification.getAcknowledgementDate());

        dto.setCheckInDate(pNotification.getModifiedPart().getCheckInDate());
        dto.setIterationNote(pNotification.getModifiedPart().getIterationNote());

        dto.setModifiedPartIteration(pNotification.getModifiedPart().getIteration());
        dto.setModifiedPartNumber(pNotification.getModifiedPart().getPartNumber());
        dto.setModifiedPartName(pNotification.getModifiedPart().getPartName());
        dto.setModifiedPartVersion(pNotification.getModifiedPart().getPartVersion());

        return dto;
    }

    public static PartRevisionDTO mapPartRevisionToPartDTO(PartRevision partRevision) {

        Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();

        PartRevisionDTO partRevisionDTO = mapper.map(partRevision, PartRevisionDTO.class);

        partRevisionDTO.setNumber(partRevision.getPartNumber());
        partRevisionDTO.setPartKey(partRevision.getPartNumber() + "-" + partRevision.getVersion());
        partRevisionDTO.setName(partRevision.getPartMaster().getName());
        partRevisionDTO.setStandardPart(partRevision.getPartMaster().isStandardPart());
        partRevisionDTO.setType(partRevision.getPartMaster().getType());

        if (partRevision.isObsolete()) {
            partRevisionDTO.setObsoleteDate(partRevision.getObsoleteDate());
            UserDTO obsoleteUserDTO = mapper.map(partRevision.getObsoleteAuthor(), UserDTO.class);
            partRevisionDTO.setObsoleteAuthor(obsoleteUserDTO);
        }

        if (partRevision.getReleaseAuthor() != null) {
            partRevisionDTO.setReleaseDate(partRevision.getReleaseDate());
            UserDTO releaseUserDTO = mapper.map(partRevision.getReleaseAuthor(), UserDTO.class);
            partRevisionDTO.setReleaseAuthor(releaseUserDTO);
        }

        List<PartIterationDTO> partIterationDTOs = new ArrayList<>();
        for (PartIteration partIteration : partRevision.getPartIterations()) {
            partIterationDTOs.add(mapPartIterationToPartIterationDTO(partIteration));
        }
        partRevisionDTO.setPartIterations(partIterationDTOs);

        if (partRevision.isCheckedOut()) {
            partRevisionDTO.setCheckOutDate(partRevision.getCheckOutDate());
            UserDTO checkoutUserDTO = mapper.map(partRevision.getCheckOutUser(), UserDTO.class);
            partRevisionDTO.setCheckOutUser(checkoutUserDTO);
        }

        if (partRevision.hasWorkflow()) {
            partRevisionDTO.setLifeCycleState(partRevision.getWorkflow().getLifeCycleState());
            partRevisionDTO.setWorkflow(mapper.map(partRevision.getWorkflow(), WorkflowDTO.class));
        }

        ACL acl = partRevision.getACL();
        if (acl != null) {
            partRevisionDTO.setAcl(Tools.mapACLtoACLDTO(acl));
        } else {
            partRevisionDTO.setAcl(null);
        }


        return partRevisionDTO;
    }

    public static PartIterationDTO mapPartIterationToPartIterationDTO(PartIteration partIteration) {
        Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();

        List<PartUsageLinkDTO> usageLinksDTO = new ArrayList<>();
        PartIterationDTO partIterationDTO = mapper.map(partIteration, PartIterationDTO.class);

        for (PartUsageLink partUsageLink : partIteration.getComponents()) {
            PartUsageLinkDTO partUsageLinkDTO = mapper.map(partUsageLink, PartUsageLinkDTO.class);
            List<CADInstanceDTO> cadInstancesDTO = new ArrayList<>();
            for (CADInstance cadInstance : partUsageLink.getCadInstances()) {
                CADInstanceDTO cadInstanceDTO = mapper.map(cadInstance, CADInstanceDTO.class);
                cadInstanceDTO.setMatrix(cadInstance.getRotationMatrix().getValues());
                cadInstancesDTO.add(cadInstanceDTO);
            }
            List<PartSubstituteLinkDTO> substituteLinkDTOs = new ArrayList<>();
            for (PartSubstituteLink partSubstituteLink : partUsageLink.getSubstitutes()) {
                PartSubstituteLinkDTO substituteLinkDTO = mapper.map(partSubstituteLink, PartSubstituteLinkDTO.class);
                substituteLinkDTOs.add(substituteLinkDTO);

            }
            partUsageLinkDTO.setCadInstances(cadInstancesDTO);
            partUsageLinkDTO.setSubstitutes(substituteLinkDTOs);
            usageLinksDTO.add(partUsageLinkDTO);
        }
        partIterationDTO.setComponents(usageLinksDTO);
        partIterationDTO.setNumber(partIteration.getPartRevision().getPartNumber());
        partIterationDTO.setVersion(partIteration.getPartRevision().getVersion());

        if (!partIteration.getGeometries().isEmpty()) {
            partIterationDTO.setGeometryFileURI("/api/files/" + partIteration.getSortedGeometries().get(0).getFullName());
        }

        return partIterationDTO;
    }

    public static BaselinedPartDTO mapBaselinedPartToBaselinedPartDTO(BaselinedPart baselinedPart) {
        return mapPartIterationToBaselinedPart(baselinedPart.getTargetPart());
    }

    public static List<BaselinedDocumentDTO> mapBaselinedDocumentsToBaselinedDocumentDTOs(DocumentCollection documentCollection) {
        List<BaselinedDocumentDTO> baselinedDocumentDTOs = new ArrayList<>();
        Map<BaselinedDocumentKey, BaselinedDocument> baselinedDocuments = documentCollection.getBaselinedDocuments();

        for (Map.Entry<BaselinedDocumentKey, BaselinedDocument> map : baselinedDocuments.entrySet()) {
            BaselinedDocument baselinedDocument = map.getValue();
            DocumentIteration targetDocument = baselinedDocument.getTargetDocument();
            baselinedDocumentDTOs.add(new BaselinedDocumentDTO(targetDocument.getDocumentMasterId(), targetDocument.getVersion(), targetDocument.getIteration(), targetDocument.getTitle()));
        }

        return baselinedDocumentDTOs;
    }

    public static BaselinedPartDTO mapPartIterationToBaselinedPart(PartIteration partIteration) {

        BaselinedPartDTO baselinedPartDTO = new BaselinedPartDTO();
        baselinedPartDTO.setNumber(partIteration.getPartNumber());
        baselinedPartDTO.setVersion(partIteration.getVersion());
        baselinedPartDTO.setName(partIteration.getPartRevision().getPartMaster().getName());
        baselinedPartDTO.setIteration(partIteration.getIteration());

        List<BaselinedPartOptionDTO> availableIterations = new ArrayList<>();
        for (PartRevision partRevision : partIteration.getPartRevision().getPartMaster().getPartRevisions()) {
            BaselinedPartOptionDTO option = new BaselinedPartOptionDTO(partRevision.getVersion(),
                    partRevision.getLastIteration().getIteration(),
                    partRevision.isReleased());
            availableIterations.add(option);
        }
        baselinedPartDTO.setAvailableIterations(availableIterations);

        return baselinedPartDTO;
    }

    public static BaselinedPartDTO createBaselinedPartDTOFromPartList(List<PartIteration> availableParts) {
        BaselinedPartDTO baselinedPartDTO = new BaselinedPartDTO();
        PartIteration max = Collections.max(availableParts);
        baselinedPartDTO.setNumber(max.getPartNumber());
        baselinedPartDTO.setVersion(max.getVersion());
        baselinedPartDTO.setName(max.getPartRevision().getPartMaster().getName());
        baselinedPartDTO.setIteration(max.getIteration());
        List<BaselinedPartOptionDTO> availableIterations = new ArrayList<>();
        for (PartIteration partIteration : availableParts) {
            availableIterations.add(new BaselinedPartOptionDTO(partIteration.getVersion(), partIteration.getIteration(), partIteration.getPartRevision().isReleased()));
        }
        baselinedPartDTO.setAvailableIterations(availableIterations);
        return baselinedPartDTO;
    }


    public static PathChoiceDTO mapPathChoiceDTO(PathChoice choice) {
        PathChoiceDTO pathChoiceDTO = new PathChoiceDTO();
        ArrayList<ResolvedPartLinkDTO> resolvedPath = new ArrayList<>();
        for (ResolvedPartLink resolvedPartLink : choice.getResolvedPath()) {
            ResolvedPartLinkDTO resolvedPartLinkDTO = new ResolvedPartLinkDTO();
            PartIteration resolvedIteration = resolvedPartLink.getPartIteration();
            resolvedPartLinkDTO.setPartIteration(new PartIterationDTO(resolvedIteration.getWorkspaceId(), resolvedIteration.getName(), resolvedIteration.getNumber(), resolvedIteration.getVersion(), resolvedIteration.getIteration()));
            PartLink partLink = resolvedPartLink.getPartLink();
            resolvedPartLinkDTO.setPartLink(new LightPartLinkDTO(partLink.getComponent().getNumber(), partLink.getComponent().getName(), partLink.getReferenceDescription(), partLink.getFullId()));
            resolvedPath.add(resolvedPartLinkDTO);
        }
        pathChoiceDTO.setResolvedPath(resolvedPath);
        Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();
        pathChoiceDTO.setPartUsageLink(mapper.map(choice.getPartUsageLink(), PartUsageLinkDTO.class));
        return pathChoiceDTO;
    }
}