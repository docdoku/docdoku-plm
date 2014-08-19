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

package com.docdoku.core.services;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.configuration.*;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.product.*;
import com.docdoku.core.query.PartSearchQuery;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.sharing.SharedEntityKey;
import com.docdoku.core.sharing.SharedPart;
import com.docdoku.core.workflow.TaskKey;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Florent Garin
 */
public interface IProductManagerLocal{
    
    List<PartUsageLink[]> findPartUsages(ConfigurationItemKey pKey, PartMasterKey pPartMKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;
    PartUsageLink filterProductStructure(ConfigurationItemKey pKey, ConfigSpec configSpec, Integer partUsageLink, Integer depth) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, PartUsageLinkNotFoundException, AccessRightException;

    ConfigurationItem createConfigurationItem(String pWorkspaceId, String pId, String pDescription, String pDesignItemNumber) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ConfigurationItemAlreadyExistsException, CreationException, PartMasterNotFoundException;
    PartMaster createPartMaster(String pWorkspaceId, String pNumber, String pName, boolean pStandardPart, String pWorkflowModelId, String pPartRevisionDescription, String templateId, Map<String, String> roleMappings, ACLUserEntry[] userEntries, ACLUserGroupEntry[] userGroupEntries) throws NotAllowedException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, PartMasterAlreadyExistsException, CreationException, PartMasterTemplateNotFoundException, FileAlreadyExistsException, RoleNotFoundException;
    PartRevision checkOutPart(PartRevisionKey pPartRPK) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException, NotAllowedException, FileAlreadyExistsException, CreationException;
    PartRevision undoCheckOutPart(PartRevisionKey pPartRPK) throws NotAllowedException, PartRevisionNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException;
    PartRevision checkInPart(PartRevisionKey pPartRPK) throws PartRevisionNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ESServerException;
    BinaryResource saveNativeCADInPartIteration(PartIterationKey pPartIPK, String pName, long pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException;
    BinaryResource saveGeometryInPartIteration(PartIterationKey pPartIPK, String pName, int quality, long pSize, double radius) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException;
    BinaryResource saveFileInPartIteration(PartIterationKey pPartIPK, String pName, long pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException;
    PartRevision updatePartIteration(PartIterationKey pKey, java.lang.String pIterationNote, PartIteration.Source source, java.util.List<PartUsageLink> pUsageLinks, java.util.List<InstanceAttribute> pAttributes, DocumentIterationKey[] pLinkKeys) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, PartRevisionNotFoundException, PartMasterNotFoundException;
    BinaryResource getBinaryResource(String pFullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, NotAllowedException;
    List<ConfigurationItem> getConfigurationItems(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    List<Layer> getLayers(ConfigurationItemKey pKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    Layer getLayer(int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, LayerNotFoundException;
    Layer createLayer(ConfigurationItemKey pKey, String pName, String color) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, ConfigurationItemNotFoundException;
    Layer updateLayer(ConfigurationItemKey pKey, int pId, String pName, String color) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, ConfigurationItemNotFoundException, LayerNotFoundException, UserNotActiveException;
    Marker createMarker(int pLayerId, String pTitle, String pDescription, double pX, double pY, double pZ) throws LayerNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException;
    void deleteMarker(int pLayerId, int pMarkerId) throws WorkspaceNotFoundException, UserNotActiveException, LayerNotFoundException, UserNotFoundException, AccessRightException, MarkerNotFoundException;
    List<PartMaster> findPartMasters(String pWorkspaceId, String pPartNumber, int pMaxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException;
    PartRevision getPartRevision(PartRevisionKey pPartRPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException;
    PartMaster getPartMaster(PartMasterKey pPartMPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartMasterNotFoundException;
    List<PartUsageLink> getComponents(PartIterationKey pPartIPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException, NotAllowedException;
    boolean partMasterExists(PartMasterKey partMasterKey)throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    void deleteConfigurationItem(ConfigurationItemKey configurationItemKey) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, UserNotActiveException, ConfigurationItemNotFoundException, LayerNotFoundException, EntityConstraintException;
    void deleteLayer(String workspaceId, int layerId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, LayerNotFoundException, AccessRightException;
    void removeCADFileFromPartIteration(PartIterationKey partIKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException;

    PartMasterTemplate createPartMasterTemplate(String pWorkspaceId, String pId, String pPartType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateAlreadyExistsException, UserNotFoundException, NotAllowedException, CreationException;
    BinaryResource saveFileInTemplate(PartMasterTemplateKey pPartMTemplateKey, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, PartMasterTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException ;
    String generateId(String pWorkspaceId, String pPartMTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, PartMasterTemplateNotFoundException;
    PartMasterTemplate[] getPartMasterTemplates(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;
    PartMasterTemplate getPartMasterTemplate(PartMasterTemplateKey pKey) throws WorkspaceNotFoundException, PartMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException;
    PartMasterTemplate updatePartMasterTemplate(PartMasterTemplateKey pKey, String pPartType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateNotFoundException, UserNotFoundException;
    void deletePartMasterTemplate(PartMasterTemplateKey pKey) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateNotFoundException, UserNotFoundException;
    PartMasterTemplate removeFileFromTemplate(String pFullName) throws WorkspaceNotFoundException, PartMasterTemplateNotFoundException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException;

    List<PartMaster> getPartMasters(String pWorkspaceId, int start, int pMaxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException;

    void deletePartMaster(PartMasterKey partMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartMasterNotFoundException, EntityConstraintException, ESServerException;
    int getTotalNumberOfParts(String pWorkspaceId) throws AccessRightException, WorkspaceNotFoundException, AccountNotFoundException, UserNotFoundException, UserNotActiveException;

    long getDiskUsageForPartsInWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException;
    long getDiskUsageForPartTemplatesInWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException;

    PartRevision[] getAllCheckedOutPartRevisions(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException;

    PartRevision approveTaskOnPart(String pWorkspaceId, TaskKey pTaskKey, String pComment, String pSignature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException;

    PartRevision rejectTaskOnPart(String pWorkspaceId, TaskKey pTaskKey, String pComment, String pSignature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException;

    SharedPart createSharedPart(PartRevisionKey pPartRevisionKey, String pPassword, Date pExpireDate) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException, UserNotActiveException;
    void deleteSharedPart(SharedEntityKey pSharedEntityKey) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, SharedEntityNotFoundException;

    void updatePartRevisionACL(String workspaceId, PartRevisionKey revisionKey, Map<String,String> userEntries, Map<String,String> groupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException, DocumentRevisionNotFoundException;

    List<PartRevision> getPartRevisions(String pWorkspaceId, int start, int pMaxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException;

    int getPartsInWorkspaceCount(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    void deletePartRevision(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, EntityConstraintException, ESServerException;

    int getNumberOfIteration(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException;

    BaselineCreation createBaseline(ConfigurationItemKey configurationItemKey, String name, Baseline.BaselineType type, String description) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, ConfigurationItemNotReleasedException, PartIterationNotFoundException, UserNotActiveException, NotAllowedException;
    List<Baseline> getBaselines(ConfigurationItemKey configurationItemKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    ConfigSpec getLatestConfigSpec(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    ConfigSpec getConfigSpecForBaseline(int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException;
    void deleteBaseline(int baselineId) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, BaselineNotFoundException;
    Baseline getBaseline(int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException;
    Baseline getBaselineById(int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    Baseline duplicateBaseline(int baselineId, String name, Baseline.BaselineType type, String description) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, BaselineNotFoundException;
    void updateBaseline(ConfigurationItemKey configurationItemKey, int baselineId, String name, Baseline.BaselineType type, String description, List<PartIterationKey> partIterationKeys) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException, BaselineNotFoundException, ConfigurationItemNotReleasedException;
    List<BaselinedPart> getBaselinedPartWithReference(int baselineId, String q, int maxResults) throws BaselineNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;


    PartRevision createPartRevision(PartRevisionKey revisionKey, String pDescription, String pWorkflowModelId, ACLUserEntry[] pUserEntries, ACLUserGroupEntry[] pUserGroupEntries, Map<String, String> roleMappings) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException, NotAllowedException, FileAlreadyExistsException, CreationException, RoleNotFoundException, WorkflowModelNotFoundException, PartRevisionAlreadyExistsException;

    void removeACLFromPartRevision(PartRevisionKey revisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException;

    void setRadiusForPartIteration(PartIterationKey partIPK, Float radius) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException;

    List<PartRevision> searchPartRevisions(PartSearchQuery partSearchQuery) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ESServerException;

    List<Baseline> findBaselinesWherePartRevisionHasIterations(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException;


    PartMaster findPartMasterByCADFileName(String workspaceId, String cadFileName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    PartRevision[] getPartRevisionsWithReference(String pWorkspaceId, String reference, int maxResults) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    PartRevision releasePartRevision(PartRevisionKey pRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException, NotAllowedException;

    List<Baseline> getAllBaselines(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    List<ProductInstanceMaster> getProductInstanceMasters(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    List<ProductInstanceMaster> getProductInstanceMasters(ConfigurationItemKey configurationItemKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    ProductInstanceMaster getProductInstanceMaster(ProductInstanceMasterKey productInstanceMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException;
    List<ProductInstanceIteration> getProductInstanceIterations(ProductInstanceMasterKey productInstanceMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    ProductInstanceIteration getProductInstanceIteration(ProductInstanceIterationKey productInstanceIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, ProductInstanceMasterNotFoundException;
    List<BaselinedPart> getProductInstanceIterationBaselinedPart(ProductInstanceIterationKey productInstanceIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, ProductInstanceMasterNotFoundException;
    List<BaselinedPart> getProductInstanceIterationPartWithReference(ProductInstanceIterationKey productInstanceIterationKey, String q, int maxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, ProductInstanceMasterNotFoundException;
    ProductInstanceMaster createProductInstance(ConfigurationItemKey configurationItemKey, String serialNumber, int baselineId) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, BaselineNotFoundException, CreationException, ProductInstanceAlreadyExistsException;
    ProductInstanceIteration updateProductInstance(ConfigurationItemKey configurationItemKey, String serialNumber, String iterationNote, List<PartIterationKey> partIterationKeys) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, PartIterationNotFoundException;
    void deleteProductInstance(String workspaceId, String configurationItemId, String serialNumber) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException, ProductInstanceMasterNotFoundException;

}
