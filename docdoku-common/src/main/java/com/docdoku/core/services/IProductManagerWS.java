/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
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
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.product.PartUsageLink;
import java.io.File;
import javax.jws.WebService;
import java.util.List;


/**
 * The product service which is the entry point for the API related for products
 * definition and manipulation.
 * 
 * @author Florent Garin
 * @version 1.1, 03/10/12
 * @since   V1.1
 */
@WebService
public interface IProductManagerWS{
    
    
    /**
     * Resolves the product structure identified by the supplied
     * <a href="ConfigurationItemKey.html">ConfigurationItemKey</a>.
     * The resolution is made according to the given
     * <a href="ConfigSpec.html">ConfigSpec</a>.
     * 
     * @param ciKey
     * The product to resolve
     * 
     * @param configSpec
     * The rules for the resolution algorithm
     * 
     * @return
     * The resolved product, actually its root part master
     * 
     * @throws ConfigurationItemNotFoundException
     * @throws WorkspaceNotFoundException
     * @throws NotAllowedException
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     */
    PartMaster filterProductStructure(ConfigurationItemKey ciKey, ConfigSpec configSpec) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException;
    
    /**
     * Creates a new product structure.
     * 
     * @param workspaceId
     * The workspace in which the product structure will be created
     * 
     * @param id
     * The id of the product structure which is unique inside
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
     * Creates a <a href="PartMaster.html">PartMaster</a>. Be aware that
     * the created item will still be in checked out state when returned.
     * Hence the calling client code has the opportunity to perform final
     * modification on the first <a href="PartIteration.html">PartIteration</a>.
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
     * the link to the workflow. Obviously this parameter may be null, it's not
     * mandatory to rely on workflows for product definitions. 
     * 
     * @param partRevisionDescription
     * The description of the first revision, version A, of the item
     * which is created in the same time than
     * the <a href="PartMaster.html">PartMaster</a>.
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
    PartRevision undoCheckOut(PartRevisionKey partRPK) throws NotAllowedException, PartRevisionNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    
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
    PartRevision checkIn(PartRevisionKey partRPK) throws PartRevisionNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException;
    
    /**
     * 
     * @param partIPK
     * @param name
     * @param quality
     * @param size
     * @return
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
     * 
     * @param partIPK
     * @param name
     * @param size
     * @return
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws NotAllowedException
     * @throws PartRevisionNotFoundException
     * @throws FileAlreadyExistsException
     * @throws CreationException
     */
    java.io.File saveFileInPartIteration(PartIterationKey pPartIPK, String name, long size) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException;
    
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
     * 
     * @param fullName
     * @return
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws FileNotFoundException
     * @throws NotAllowedException
     */
    File getDataFile(String fullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, NotAllowedException;
    
    /**
     * Retrieves all product structures that belong to the given workspace
     * 
     * @param workspaceId
     * The workspace which is the first level context
     * where all producte and parts are referenced
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
     * <a href="ConfigurationItem.html">ConfigurationItem</a>
     * 
     * @param key
     * The id of the configuration item
     * 
     * @return
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     */
    List<Layer> getLayers(ConfigurationItemKey key) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    
    /**
     * 
     * @param id
     * @return
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws LayerNotFoundException
     */
    Layer getLayer(int id) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, LayerNotFoundException;
    
    /**
     * 
     * @param key
     * @param name
     * @return
     * @throws UserNotFoundException
     * @throws WorkspaceNotFoundException
     * @throws AccessRightException
     * @throws ConfigurationItemNotFoundException
     */
    Layer createLayer(ConfigurationItemKey key, String name) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, ConfigurationItemNotFoundException;
    
    /**
     * 
     * @param layerId
     * @param title
     * @param description
     * @param x
     * @param y
     * @param z
     * @return
     * @throws LayerNotFoundException
     * @throws UserNotFoundException
     * @throws WorkspaceNotFoundException
     * @throws AccessRightException
     */
    Marker createMarker(int layerId, String title, String description, double x, double y, double z) throws LayerNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException;
}
