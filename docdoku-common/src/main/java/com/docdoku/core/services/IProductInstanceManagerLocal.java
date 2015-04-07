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

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.configuration.*;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.security.ACL;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Taylor LABEJOF
 * @version 2.0, 26/09/14
 * @since   V2.0
 */
public interface IProductInstanceManagerLocal {
    List<ProductInstanceMaster> getProductInstanceMasters(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    List<ProductInstanceMaster> getProductInstanceMasters(ConfigurationItemKey configurationItemKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    ProductInstanceMaster getProductInstanceMaster(ProductInstanceMasterKey productInstanceMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException;
    List<ProductInstanceIteration> getProductInstanceIterations(ProductInstanceMasterKey productInstanceMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException;
    ProductInstanceIteration getProductInstanceIteration(ProductInstanceIterationKey productInstanceIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, ProductInstanceMasterNotFoundException;
    List<BaselinedPart> getProductInstanceIterationBaselinedPart(ProductInstanceIterationKey productInstanceIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, ProductInstanceMasterNotFoundException;
    List<BaselinedPart> getProductInstanceIterationPartWithReference(ProductInstanceIterationKey productInstanceIterationKey, String q, int maxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, ProductInstanceMasterNotFoundException;
    ProductInstanceMaster createProductInstance(String workspaceId, ConfigurationItemKey configurationItemKey, String serialNumber, int baselineId, Map<String, ACL.Permission> userEntries, Map<String, ACL.Permission> groupEntries, List<InstanceAttribute> attributes, DocumentIterationKey[] links, String[] documentLinkComments) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, BaselineNotFoundException, CreationException, ProductInstanceAlreadyExistsException;
    void deleteProductInstance(String workspaceId, String configurationItemId, String serialNumber) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException, ProductInstanceMasterNotFoundException;

    void updateACLForProductInstanceMaster(String workspaceId, String configurationItemId, String serialNumber, Map<String, String> userEntries, Map<String, String> groupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException;
    void removeACLFromProductInstanceMaster(String workspaceId, String configurationItemId, String serialNumber) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, ProductInstanceMasterNotFoundException;

    public BinaryResource saveFileInProductInstance(String workspaceId, ProductInstanceIterationKey pdtIterationKey, String fileName, int pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, ProductInstanceMasterNotFoundException, AccessRightException, ProductInstanceIterationNotFoundException, FileAlreadyExistsException, CreationException;
    public BinaryResource getBinaryResource(String fullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, FileNotFoundException, NotAllowedException;

    public BinaryResource renameFileInProductInstance(String pFullName, String pNewName, String serialNumber,String cId,int iteration) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, ProductInstanceMasterNotFoundException, NotAllowedException, AccessRightException, FileAlreadyExistsException, CreationException;
    public ProductInstanceMaster removeFileFromProductInstanceIteration(String workspaceId, int iteration, String fullName, ProductInstanceMaster productInstanceMaster) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException;

    public ProductInstanceMaster updateProductInstance(String workspaceId,int iteration,String iterationNote, ConfigurationItemKey configurationItemKey, String serialNumber, int baselineId, List<InstanceAttribute> attributes, DocumentIterationKey[] links, String[] documentLinkComments) throws ProductInstanceMasterNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException;

    public PathData addPathData(String workspaceId, String configurationItemId, String serialNumber, String path, List<InstanceAttribute> attributes, String description) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, UserNotActiveException;
    public PathData updatePathData(String workspaceId, String configurationItemId, String serialNumber, String path, int pathDataId, List<InstanceAttribute> attributes, String description, DocumentIterationKey[] pLinkKeys, String[] documentLinkComments) throws UserNotActiveException, WorkspaceNotFoundException, UserNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, NotAllowedException;
    public void deletePathData(String workspaceId,  String configurationItemId, String serialNumber , int pathDataId) throws UserNotActiveException, WorkspaceNotFoundException, UserNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, NotAllowedException;

    public PathData getPathData(String workspaceId,  String configurationItemId,  String serialNumber , int pathDataId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, NotAllowedException;
    public PathData getPathDataByPath(String workspaceId,  String configurationItemId, String serialNumber , String path) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, ProductInstanceMasterNotFoundException;
}
