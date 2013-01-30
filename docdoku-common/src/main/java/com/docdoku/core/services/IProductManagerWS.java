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

import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.ConfigSpec;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.Layer;
import com.docdoku.core.product.Marker;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.product.PartUsageLink;
import java.io.File;
import javax.jws.WebService;
import java.util.List;


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
     * Searchs all instances of a part and returns their paths, defined by a
     * serie of usage links, from the top of the structure to their own usage
     * link.
     * 
     * @param ciKey
     * The configuration item under which context the search is made
     * 
     * @param partMKey
     * The id of the part master to search on the structure
     * 
     * 
     * @return
     * The usage paths to all instances of the supplied part
     * 
     * @throws WorkspaceNotFoundException
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     */
    List<PartUsageLink[]> findPartUsages(ConfigurationItemKey ciKey, PartMasterKey partMKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;
    
    /**
     * Resolves the product structure identified by the supplied
     * <a href="ConfigurationItemKey.html">ConfigurationItemKey</a>.
     * The resolution is made according to the given
     * <a href="ConfigSpec.html">ConfigSpec</a> and starts at the specified
     * part usage link if any.
     * 
     * @param ciKey
     * The product structure to resolve
     * 
     * @param configSpec
     * The rules for the resolution algorithm
     * 
     * @param partUsageLink
     * The part usage link id, if null starts from the root part
     * 
     * @param depth
     * The fetch depth
     * 
     * @return
     * The resolved product
     * 
     * @throws ConfigurationItemNotFoundException
     * @throws WorkspaceNotFoundException
     * @throws NotAllowedException
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     */
    PartUsageLink filterProductStructure(ConfigurationItemKey ciKey, ConfigSpec configSpec, Integer partUsageLink, Integer depth) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, PartUsageLinkNotFoundException;
    
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
     */
    ConfigurationItem createConfigurationItem(String workspaceId, String id, String description, String designItemNumber) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ConfigurationItemAlreadyExistsException, CreationException;
    
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
     * @param partMasterDescription
     * The full item description
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
     */
    PartMaster createPartMaster(String workspaceId, String number, String name, String partMasterDescription, boolean standardPart, String workflowModelId, String partRevisionDescription) throws NotAllowedException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, PartMasterAlreadyExistsException, CreationException;
    
    /**
     * Undoes checkout the given part revision. As a consequence its current
     * working copy, represented by its latest
     * <a href="PartIteration.html">PartIteration</a> will be deleted.
     * Thus, some modifications may be lost.
     * 
     * @param partRPK
     * The id of the part revision to undo checkout
     * 
     * @return
     * The part revision which is now in the checkin state
     * 
     * @throws NotAllowedException
     * @throws PartRevisionNotFoundException
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     */
    PartRevision undoCheckOutPart(PartRevisionKey partRPK) throws NotAllowedException, PartRevisionNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    
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
     */
    PartRevision checkInPart(PartRevisionKey partRPK) throws PartRevisionNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException;
    
    /**
     * Creates a <a href="Geometry.html">Geometry</a> file,
     * a specialized kind of binary resource which contains CAD data, and
     * attachs it to the part iteration passed as parameter.
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
     * The physical file, a java.io.File instance, that now needs to be created
     * 
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws NotAllowedException
     * @throws PartRevisionNotFoundException
     * @throws FileAlreadyExistsException
     * @throws CreationException
     */
    java.io.File saveGeometryInPartIteration(PartIterationKey partIPK, String name, int quality, long size) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException;
    
    /**
     * Creates a regular file, <a href="BinaryResource.html">BinaryResource</a>
     * object, and attachs it to the part iteration instance passed
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
     * The physical file, a java.io.File instance, that now needs to be created
     * 
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws NotAllowedException
     * @throws PartRevisionNotFoundException
     * @throws FileAlreadyExistsException
     * @throws CreationException
     */
    java.io.File saveFileInPartIteration(PartIterationKey partIPK, String name, long size) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException;
    
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
     * @return
     * The <a href="PartRevision.html">PartRevision</a> of the updated
     * part iteration
     * 
     * @throws UserNotFoundException
     * @throws WorkspaceNotFoundException
     * @throws AccessRightException
     * @throws NotAllowedException
     * @throws PartRevisionNotFoundException
     */
    PartRevision updatePartIteration(PartIterationKey key, java.lang.String iterationNote, PartIteration.Source source, java.util.List<PartUsageLink> usageLinks, java.util.List<InstanceAttribute> attributes) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, PartRevisionNotFoundException;
    
    /**
     * Returns the java.io.File object that references the physical file of the
     * supplied binary resource.
     * 
     * @param fullName
     * Id of the <a href="BinaryResource.html">BinaryResource</a> of which the
     * data file will be returned
     * 
     * @return
     * The physical file of the binary resource
     * 
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws FileNotFoundException
     * @throws NotAllowedException
     */
    File getDataFile(String fullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, NotAllowedException;
    
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
    Layer createLayer(ConfigurationItemKey key, String name) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, ConfigurationItemNotFoundException;
    
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
     * Find part masters by their number
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
    List<PartMaster> findPartMasters(String workspaceId, String partNumber, int maxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException;
}
