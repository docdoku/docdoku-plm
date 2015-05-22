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

package com.docdoku.core.services;

import com.docdoku.core.change.ModificationNotification;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.configuration.PSFilter;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.configuration.ProductInstanceMaster;
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

import javax.jws.WebService;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * The product service which is the entry point for the API related to products
 * definition and manipulation. The client of these functions must
 * be authenticated and have read or write access rights on the workspace
 * where the operations occur.
 * 
 * @author Florent Garin
 * @version 1.1, 03/10/12
 * @since   V1.1
 */
@WebService
public interface IProductManagerWS{
    
    /**
     * Searches all instances of a part and returns their paths, defined by a
     * serie of usage links, from the top of the structure to their own usage
     * link.
     * 
     * @param pKey
     * The configuration item under which context the search is made
     * 
     * @param search
     * The search pattern, matching part number
     * 
     * 
     * @return
     * The usage paths to all instances of the supplied part
     * 
     * @throws WorkspaceNotFoundException
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     *
     * Do not expose until we have a workaround to send interfaces.
     */
    // public List<PartLink[]> findPartUsages(ConfigurationItemKey pKey, PSFilter filter, String search) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException, ConfigurationItemNotFoundException ;

    /**
     * Creates a new product structure.
     * 
     * @param workspaceId
     * The workspace in which the product structure will be created
     * 
     * @param id
     * The id of the product structure which must be unique inside
     * the workspace context
     * 
     * @param description
     * The description of the product structure
     * 
     * @param designItemNumber
     * The id of the part master that will be the root of the product structure
     * 
     * @return
     * The newly created configuration item
     * 
     * @throws UserNotFoundException
     * @throws WorkspaceNotFoundException
     * @throws AccessRightException
     * @throws NotAllowedException
     * @throws ConfigurationItemAlreadyExistsException
     * @throws CreationException
     * @throws PartMasterNotFoundException
     */
    ConfigurationItem createConfigurationItem(String workspaceId, String id, String description, String designItemNumber) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ConfigurationItemAlreadyExistsException, CreationException, PartMasterNotFoundException;
    
    /**
     * Creates a new <a href="PartMaster.html">PartMaster</a>. Be aware that
     * the created item will still be in checkout state when returned.
     * Hence the calling client code has the opportunity to perform final
     * modifications on the first, iteration number 1,
     * <a href="PartIteration.html">PartIteration</a>.
     * 
     * @param workspaceId
     * The workspace in which the part master will be created
     * 
     * @param number
     * The part number of the item to create which is its id
     * inside the workspace
     * 
     * @param name
     * The user friendly name of the item
     * 
     * @param standardPart
     * Boolean indicating if the item to create is a standard part
     * 
     * @param workflowModelId
     * The id of the workflow template that will be instantiated and attached
     * to the created part master. Actually, it's the first 
     * <a href="PartRevision.html">PartRevision</a> that will hold
     * the reference to the workflow. Obviously this parameter may be null,
     * it's not mandatory to rely on workflows for product definitions. 
     * 
     * @param partRevisionDescription
     * The description of the first revision, version A, of the item.
     * This revision will be created in the same time than
     * the <a href="PartMaster.html">PartMaster</a> itself.
     *
     * @param templateId
     * The id of the template to use to instantiate the part, may be null.
     * Refers to a <a href="PartMasterTemplate.html">PartMasterTemplate</a>.
     *
     * @param roleMappings
     * Role mapping for the selected workflow model
     *
     * @param userEntries
     * ACL user entries
     *
     * @param userGroupEntries
     * ACL group entries
     *
     * @return
     * The created part master instance
     * 
     * @throws NotAllowedException
     * @throws UserNotFoundException
     * @throws WorkspaceNotFoundException
     * @throws AccessRightException
     * @throws WorkflowModelNotFoundException
     * @throws PartMasterAlreadyExistsException
     * @throws CreationException
     * @throws PartMasterTemplateNotFoundException
     * @throws FileAlreadyExistsException
     * @throws RoleNotFoundException
     */
    PartMaster createPartMaster(String workspaceId, String number, String name, boolean standardPart, String workflowModelId, String partRevisionDescription, String templateId, Map<String, String> roleMappings, ACLUserEntry[] userEntries, ACLUserGroupEntry[] userGroupEntries) throws NotAllowedException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, PartMasterAlreadyExistsException, CreationException, PartMasterTemplateNotFoundException, FileAlreadyExistsException, RoleNotFoundException;

    /**
     * Checks out the supplied part revision to allow the operating user to modify it.
     *
     * @param partRPK
     * The id of the part revision to check out
     *
     * @return
     * The part revision which is now in the checkout state
     *
     * @throws UserNotFoundException
     * @throws AccessRightException
     * @throws WorkspaceNotFoundException
     * @throws PartRevisionNotFoundException
     * @throws NotAllowedException
     * @throws FileAlreadyExistsException
     * @throws CreationException
     */
    PartRevision checkOutPart(PartRevisionKey partRPK) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException, NotAllowedException, FileAlreadyExistsException, CreationException;


    /**
     * Undoes checkout the given part revision. As a consequence its current
     * working copy, represented by its latest
     * <a href="PartIteration.html">PartIteration</a> will be deleted.
     * Thus, some modifications may be lost.
     * 
     * @param partRPK
     * The id of the part revision to undo check out
     * 
     * @return
     * The part revision which is now in the checkin state
     * 
     * @throws NotAllowedException
     * @throws PartRevisionNotFoundException
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws AccessRightException
     */
    PartRevision undoCheckOutPart(PartRevisionKey partRPK) throws NotAllowedException, PartRevisionNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException;
    
    /**
     * Checks in the supplied part revision so its latest iteration,
     * that carries the modifications realized since the checkout operation,
     * will be published and made visible to all users.
     * 
     * @param partRPK
     * The id of the part revision to check in
     * 
     * @return
     * The part revision which has just been checked in
     * 
     * @throws PartRevisionNotFoundException
     * @throws UserNotFoundException
     * @throws WorkspaceNotFoundException
     * @throws AccessRightException
     * @throws NotAllowedException
     * @throws ESServerException
     */
    PartRevision checkInPart(PartRevisionKey partRPK) throws PartRevisionNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ESServerException, EntityConstraintException, UserNotActiveException, PartMasterNotFoundException;

    /**
     * Creates the <a href="BinaryResource.html">BinaryResource</a> file,
     * which is the native CAD file associated with the part iteration passed as parameter.
     * The part must be in the checkout state and the calling user must have
     * write access rights to the part.
     *
     * @param partIPK
     * The id of the part iteration on which the file will be attached
     *
     * @param name
     * The name of the binary resource to create
     *
     * @param size
     * Number of bytes of the physical file
     *
     * @return
     * The binary resource, a BinaryResource instance, that now needs to be created
     *
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws NotAllowedException
     * @throws PartRevisionNotFoundException
     * @throws FileAlreadyExistsException
     * @throws CreationException
     */
    BinaryResource saveNativeCADInPartIteration(PartIterationKey partIPK, String name, long size) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException;

    /**
     * Creates a <a href="Geometry.html">Geometry</a> file,
     * a specialized kind of binary resource which contains CAD data, and
     * attaches it to the part iteration passed as parameter.
     * The part must be in the checkout state and the calling user must have
     * write access rights to the part.
     * 
     * @param partIPK
     * The id of the part iteration on which the file will be attached
     * 
     * @param name
     * The name of the binary resource to create
     * 
     * @param quality
     * The quality of the CAD file, starts at 0, smaller is greater
     * 
     * @param size
     * Number of bytes of the physical file
     * 
     * @return
     * The binary resource, a BinaryResource instance, that now needs to be created
     * 
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws NotAllowedException
     * @throws PartRevisionNotFoundException
     * @throws FileAlreadyExistsException
     * @throws CreationException
     */
    BinaryResource saveGeometryInPartIteration(PartIterationKey partIPK, String name, int quality, long size, double[] box) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException;
    
    /**
     * Creates a regular file, <a href="BinaryResource.html">BinaryResource</a>
     * object, and attaches it to the part iteration instance passed
     * as parameter. The part must be in the checkout state and
     * the calling user must have write access rights to the part.
     * 
     * 
     * @param partIPK
     * The id of the part iteration on which the file will be attached
     * 
     * @param name
     * The name of the binary resource to create
     * 
     * @param size
     * Number of bytes of the physical file
     * 
     * @return
     * The binary resource, a BinaryResource instance, that now needs to be created
     * 
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws NotAllowedException
     * @throws PartRevisionNotFoundException
     * @throws FileAlreadyExistsException
     * @throws CreationException
     */
    BinaryResource saveFileInPartIteration(PartIterationKey partIPK, String name, long size) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException;
    
    /**
     * Updates the specified <a href="PartIteration.html">PartIteration</a> with
     * the properties passed as parameters. The corresponding part revision
     * should be in checkout state.
     * 
     * @param key
     * The id of the part iteration to modify
     * 
     * @param iterationNote
     * A note to describe the iteration and thus the modifications made
     * to the part
     * 
     * @param source
     * The <a href="PartIteration.Source.html">PartIteration.Source</a>
     * attribute of the part
     * 
     * @param usageLinks
     * Links to other parts. Only assembly parts can define usage links
     * 
     * @param attributes
     * Custom attributes that may be added to the part
     *
     * @param linkKeys
     * Links to documents
     *
     * @param lovNames
     * Names of lovs
     * 
     * @return
     * The <a href="PartRevision.html">PartRevision</a> of the updated
     * part iteration
     * 
     * @throws UserNotFoundException
     * @throws WorkspaceNotFoundException
     * @throws AccessRightException
     * @throws NotAllowedException
     * @throws PartRevisionNotFoundException
     * @throws PartMasterNotFoundException
     */
    PartRevision updatePartIteration(PartIterationKey key, java.lang.String iterationNote, PartIteration.Source source, java.util.List<PartUsageLink> usageLinks, java.util.List<InstanceAttribute> attributes, java.util.List<InstanceAttributeTemplate> pAttributesTemplates, DocumentIterationKey[] linkKeys, String[] documentLinkComments, String[] lovNames) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, PartRevisionNotFoundException, PartMasterNotFoundException, EntityConstraintException, UserNotActiveException, ListOfValuesNotFoundException;
    
    /**
     * Retrieves all product structures that belong to the given workspace.
     * 
     * @param workspaceId
     * The workspace which is the first level context
     * where all products and parts are referenced
     * 
     * @return
     * The list of product structures
     * 
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     */
    List<ConfigurationItem> getConfigurationItems(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    ConfigurationItem getConfigurationItem(ConfigurationItemKey ciKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException;
    /**
     * Retrieves all layers of the given product structure, ie
     * <a href="ConfigurationItem.html">ConfigurationItem</a>.
     * 
     * @param key
     * The id of the configuration item
     * 
     * @return
     * The list of all layers of the current configuration item
     * 
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     */
    List<Layer> getLayers(ConfigurationItemKey key) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    
    /**
     * Retrieves a given layer.
     * 
     * @param id
     * Integer value that uniquely identifies the layer to return
     * 
     * @return
     * The layer to fetch
     * 
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws LayerNotFoundException
     */
    Layer getLayer(int id) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, LayerNotFoundException;
    
    /**
     * Creates a new layer on a given product structure.
     * 
     * @param key
     * The identifier object of the configuration item wherein the layer will
     * be created
     * 
     * @param name
     * The user friendly name of the layer
     * 
     * @return
     * The newly created layer
     * 
     * @throws UserNotFoundException
     * @throws WorkspaceNotFoundException
     * @throws AccessRightException
     * @throws ConfigurationItemNotFoundException
     */
    Layer createLayer(ConfigurationItemKey key, String name, String color) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, ConfigurationItemNotFoundException;
    
    /**
     * Creates a new marker that will be a member of the given layer.
     * 
     * @param layerId
     * The layer id on which the marker will be created
     * 
     * @param title
     * The title of the marker
     * 
     * @param description
     * The description of the marker
     * 
     * @param x
     * The x axis coordinate of the marker
     * 
     * @param y
     * The y axis coordinate of the marker
     * 
     * @param z
     * The z axis coordinate of the marker
     * 
     * @return
     * The newly created marker
     * 
     * @throws LayerNotFoundException
     * @throws UserNotFoundException
     * @throws WorkspaceNotFoundException
     * @throws AccessRightException
     */
    Marker createMarker(int layerId, String title, String description, double x, double y, double z) throws LayerNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException;

    /**
     * Returns a specific <a href="PartMaster.html">PartMaster</a>.
     *
     * @param partMPK
     * The id of the part master to get
     *
     * @return
     * The part master
     *
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws PartMasterNotFoundException
     */
    PartMaster getPartMaster(PartMasterKey partMPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartMasterNotFoundException;

    /**
     * Returns a specific <a href="PartRevision.html">PartRevision</a>.
     *
     * @param partRPK
     * The id of the part revision to get
     *
     * @return
     * The part revision
     *
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws PartRevisionNotFoundException
     * @throws AccessRightException
     */
    PartRevision getPartRevision(PartRevisionKey partRPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException;
   
    /**
     * Returns a specific <a href="PartIteration.html">PartIteration</a>.
     *
     * @param pPartIPK
     * The id of the part iteration to get
     *
     * @return
     * The part iteration
     *
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws PartIterationNotFoundException
     * @throws PartRevisionNotFoundException
     * @throws AccessRightException
     */
    PartIteration getPartIteration(PartIterationKey pPartIPK) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartIterationNotFoundException, PartRevisionNotFoundException, AccessRightException;
    
    /**
     * Finds part masters by their part number using like style query.
     *
     * @param workspaceId
     * The workspace in which part masters will be searched
     *
     * @param partNumber
     * The number of the part master to search for
     *
     * @param maxResults
     * Set the maximum number of results to retrieve
     *
     * @return
     * The list of <a href="PartMaster.html">PartMaster</a>
     *
     * @throws UserNotFoundException
     * @throws AccessRightException
     * @throws WorkspaceNotFoundException
     */
    List<PartMaster> findPartMasters(String workspaceId, String partNumber, String pPartName, int maxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException;

    /**
     * Fetches the components of the supplied part assembly
     *
     * @param partIPK
     * The id of the part iteration of which the components have to be retrieved
     *
     * @return
     * The list of <a href="PartUsageLink.html">PartUsageLink</a>
     *
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws PartIterationNotFoundException
     * @throws NotAllowedException
     */
    List<PartUsageLink> getComponents(PartIterationKey partIPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException, NotAllowedException;


    boolean partMasterExists(PartMasterKey partMasterKey)throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    /**
     * Deletes the specified configuration item. No baseline should have been defined on it.
     *
     * @param configurationItemKey
     * The configuration item to delete
     *
     * @throws UserNotFoundException
     * @throws WorkspaceNotFoundException
     * @throws AccessRightException
     * @throws NotAllowedException
     * @throws UserNotActiveException
     * @throws ConfigurationItemNotFoundException
     * @throws LayerNotFoundException
     * @throws EntityConstraintException
     */
    void deleteConfigurationItem(ConfigurationItemKey configurationItemKey) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, UserNotActiveException, ConfigurationItemNotFoundException, LayerNotFoundException, EntityConstraintException;

    void deleteLayer(String workspaceId, int layerId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, LayerNotFoundException, AccessRightException;

    /**
     * Remove the cad file from the part iteration.
     *
     * @param partIKey
     * The id of the part iteration from which the file will be deleted
     *
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws PartIterationNotFoundException
     * @throws PartRevisionNotFoundException
     */
    void removeCADFileFromPartIteration(PartIterationKey partIKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException, PartRevisionNotFoundException;

    BinaryResource renameCADFileInPartIteration(String fullName, String newName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, FileNotFoundException, FileAlreadyExistsException, CreationException, StorageException;

    PartMasterTemplate createPartMasterTemplate(String pWorkspaceId, String pId, String pPartType, String pWorkflowModelId, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, String[] lovNames, InstanceAttributeTemplate[] pAttributeInstanceTemplates, String[] instanceLovNames, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateAlreadyExistsException, UserNotFoundException, NotAllowedException, CreationException, WorkflowModelNotFoundException, ListOfValuesNotFoundException;
    BinaryResource saveFileInTemplate(PartMasterTemplateKey pPartMTemplateKey, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, PartMasterTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException, AccessRightException;
    String generateId(String pWorkspaceId, String pPartMTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, PartMasterTemplateNotFoundException;
    PartMasterTemplate[] getPartMasterTemplates(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;
    PartMasterTemplate getPartMasterTemplate(PartMasterTemplateKey pKey) throws WorkspaceNotFoundException, PartMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException;
    PartMasterTemplate updatePartMasterTemplate(PartMasterTemplateKey pKey, String pPartType, String pWorkflowModelId, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, String[] lovNames, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateNotFoundException, UserNotFoundException, WorkflowModelNotFoundException, UserNotActiveException, ListOfValuesNotFoundException;
    void deletePartMasterTemplate(PartMasterTemplateKey pKey) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException;
    PartMasterTemplate removeFileFromTemplate(String pFullName) throws WorkspaceNotFoundException, PartMasterTemplateNotFoundException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException;
    BinaryResource renameFileInTemplate(String fileFullName,String newName) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, FileNotFoundException, UserNotActiveException, FileAlreadyExistsException, CreationException, StorageException;

    List<PartMaster> getPartMasters(String pWorkspaceId, int start, int pMaxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException;
    void deletePartMaster(PartMasterKey partMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartMasterNotFoundException, EntityConstraintException, ESServerException;
    int getTotalNumberOfParts(String pWorkspaceId) throws AccessRightException, WorkspaceNotFoundException, AccountNotFoundException, UserNotFoundException, UserNotActiveException;

    long getDiskUsageForPartsInWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException;
    long getDiskUsageForPartTemplatesInWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException;

    PartRevision[] getCheckedOutPartRevisions(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException, UserNotFoundException, UserNotActiveException;
    PartRevision[] getAllCheckedOutPartRevisions(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException;

    SharedPart createSharedPart(PartRevisionKey pPartRevisionKey, String pPassword, Date pExpireDate) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException, UserNotActiveException;
    void deleteSharedPart(SharedEntityKey pSharedEntityKey) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, SharedEntityNotFoundException;

    void updatePartRevisionACL(String workspaceId, PartRevisionKey revisionKey, Map<String,String> userEntries, Map<String,String> groupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException, DocumentRevisionNotFoundException;

    List<PartRevision> getPartRevisions(String pWorkspaceId, int start, int pMaxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException;

    int getPartsInWorkspaceCount(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    void deletePartRevision(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, EntityConstraintException, ESServerException;

    PartRevision createPartRevision(PartRevisionKey revisionKey, String pDescription, String pWorkflowModelId, ACLUserEntry[] pUserEntries, ACLUserGroupEntry[] pUserGroupEntries, Map<String, String> roleMappings) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException, NotAllowedException, FileAlreadyExistsException, CreationException, RoleNotFoundException, WorkflowModelNotFoundException, PartRevisionAlreadyExistsException;

    void removeACLFromPartRevision(PartRevisionKey revisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException;

    List<PartRevision> searchPartRevisions(PartSearchQuery partSearchQuery) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ESServerException;

    List<ProductBaseline> findBaselinesWherePartRevisionHasIterations(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException;

    PartMaster findPartMasterByCADFileName(String workspaceId, String cadFileName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    Conversion getConversion(PartIterationKey partIterationKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, PartIterationNotFoundException ;

    Conversion createConversion(PartIterationKey partIterationKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, PartIterationNotFoundException, CreationException;

    void removeConversion(PartIterationKey partIterationKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, PartIterationNotFoundException;
    
    void endConversion(PartIterationKey partIterationKey, boolean succeed) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, PartIterationNotFoundException;

    List<ModificationNotification> getModificationNotifications(PartIterationKey pPartIPK) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException;

    List<PartIteration> getUsedByAsComponent(PartRevisionKey pPartRPK) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException;

    List<PartIteration> getUsedByAsSubstitute(PartRevisionKey pParRIPK) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException;

    void updateModificationNotification(String pWorkspaceId, int pModificationNotificationId, String pAcknowledgementComment) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException;

    void checkCyclicAssemblyForPartIteration(PartIteration partIteration) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException ;
    // Component filterProductStructure(ConfigurationItemKey ciKey, PSFilter filter, List<PartLink> path, Integer depth) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, PartUsageLinkNotFoundException, AccessRightException, PartMasterNotFoundException, EntityConstraintException;
    //PartLink getRootPartUsageLink(ConfigurationItemKey pKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException;
    PSFilter getPSFilter(ConfigurationItemKey ciKey, String filterType) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, BaselineNotFoundException;

    //List<PartLink> decodePath(ConfigurationItemKey ciKey, String path) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartUsageLinkNotFoundException, ConfigurationItemNotFoundException;
}
