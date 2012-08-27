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

import com.docdoku.core.product.ConfigSpec;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.Layer;
import com.docdoku.core.product.Marker;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import java.io.File;
import java.util.List;


/**
 *
 * @author Florent Garin
 */
public interface IProductManagerLocal{
		
    PartMaster filterProductStructure(ConfigurationItemKey pKey, ConfigSpec configSpec) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException;
    ConfigurationItem createConfigurationItem(String pWorkspaceId, String pId, String pDescription, String pDesignItemNumber) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ConfigurationItemAlreadyExistsException, CreationException;
    PartMaster createPartMaster(String pWorkspaceId, String pNumber, String pName, String pPartMasterDescription, boolean pStandardPart, String pWorkflowModelId, String pPartRevisionDescription) throws NotAllowedException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, PartMasterAlreadyExistsException, CreationException;
    PartRevision undoCheckOut(com.docdoku.core.product.PartRevisionKey pPartRPK) throws com.docdoku.core.services.NotAllowedException, com.docdoku.core.services.PartRevisionNotFoundException, com.docdoku.core.services.UserNotFoundException, com.docdoku.core.services.UserNotActiveException, com.docdoku.core.services.WorkspaceNotFoundException;
    PartRevision checkIn(com.docdoku.core.product.PartRevisionKey pPartRPK) throws com.docdoku.core.services.PartRevisionNotFoundException, com.docdoku.core.services.UserNotFoundException, com.docdoku.core.services.WorkspaceNotFoundException, com.docdoku.core.services.AccessRightException, com.docdoku.core.services.NotAllowedException;
    java.io.File saveGeometryInPartIteration(com.docdoku.core.product.PartIterationKey pPartIPK, java.lang.String pName, int quality, long pSize) throws com.docdoku.core.services.UserNotFoundException, com.docdoku.core.services.UserNotActiveException, com.docdoku.core.services.WorkspaceNotFoundException, com.docdoku.core.services.NotAllowedException, com.docdoku.core.services.PartRevisionNotFoundException, com.docdoku.core.services.FileAlreadyExistsException, com.docdoku.core.services.CreationException;
    PartRevision updatePartIteration(com.docdoku.core.product.PartIterationKey pKey, java.lang.String pIterationNote, com.docdoku.core.product.PartIteration.Source source, java.util.List<com.docdoku.core.product.PartUsageLink> pUsageLinks, java.util.List<com.docdoku.core.meta.InstanceAttribute> pAttributes) throws com.docdoku.core.services.UserNotFoundException, com.docdoku.core.services.WorkspaceNotFoundException, com.docdoku.core.services.AccessRightException, com.docdoku.core.services.NotAllowedException, com.docdoku.core.services.PartRevisionNotFoundException;
    File getDataFile(String pFullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, NotAllowedException;
    List<ConfigurationItem> getConfigurationItems(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    List<Layer> getLayers(ConfigurationItemKey pKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    Layer getLayer(int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, LayerNotFoundException;
    Layer createLayer(ConfigurationItemKey pKey, String pName) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, ConfigurationItemNotFoundException;
    Marker createMarker(int pLayerId, String pTitle, String pDescription, double pX, double pY, double pZ) throws LayerNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException;
}
