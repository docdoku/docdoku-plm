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
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PathToPathLink;
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
    ProductInstanceMaster createProductInstance(String workspaceId, ConfigurationItemKey configurationItemKey, String serialNumber, int baselineId, Map<String, ACL.Permission> userEntries, Map<String, ACL.Permission> groupEntries, List<InstanceAttribute> attributes, DocumentRevisionKey[] links, String[] documentLinkComments) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, BaselineNotFoundException, CreationException, ProductInstanceAlreadyExistsException, NotAllowedException, EntityConstraintException, UserNotActiveException, PathToPathLinkAlreadyExistsException, PartMasterNotFoundException, ProductInstanceMasterNotFoundException;
    void deleteProductInstance(String workspaceId, String configurationItemId, String serialNumber) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException, ProductInstanceMasterNotFoundException;

    void updateACLForProductInstanceMaster(String workspaceId, String configurationItemId, String serialNumber, Map<String, String> userEntries, Map<String, String> groupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException;
    void removeACLFromProductInstanceMaster(String workspaceId, String configurationItemId, String serialNumber) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, ProductInstanceMasterNotFoundException;

    public BinaryResource saveFileInProductInstance(String workspaceId, ProductInstanceIterationKey pdtIterationKey, String fileName, int pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, ProductInstanceMasterNotFoundException, AccessRightException, ProductInstanceIterationNotFoundException, FileAlreadyExistsException, CreationException;
    public BinaryResource getBinaryResource(String fullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, FileNotFoundException, NotAllowedException;
    public BinaryResource renameFileInProductInstance(String pFullName, String pNewName, String serialNumber,String cId,int iteration) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, ProductInstanceMasterNotFoundException, NotAllowedException, AccessRightException, FileAlreadyExistsException, CreationException, StorageException;
    public ProductInstanceMaster removeFileFromProductInstanceIteration(String workspaceId, int iteration, String fullName,ProductInstanceMasterKey productInstanceMasterKey) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException, FileNotFoundException, ProductInstanceMasterNotFoundException;

    public ProductInstanceMaster updateProductInstance(String workspaceId,int iteration,String iterationNote, ConfigurationItemKey configurationItemKey, String serialNumber, int baselineId, List<InstanceAttribute> attributes, DocumentRevisionKey[] links, String[] documentLinkComments) throws ProductInstanceMasterNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, UserNotActiveException, BaselineNotFoundException;
    public ProductInstanceMaster rebaseProductInstance(String workspaceId, String serialNumber, ConfigurationItemKey configurationItemKey, int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, BaselineNotFoundException, NotAllowedException, ConfigurationItemNotFoundException, PathToPathLinkAlreadyExistsException, PartMasterNotFoundException, CreationException, EntityConstraintException;

    public PathDataMaster addNewPathDataIteration(String workspaceId, String configurationItemId, String serialNumber, int pathDataId, String path, List<InstanceAttribute> attributes, String description, DocumentRevisionKey[] links, String[] documentLinkComments) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, UserNotActiveException, NotAllowedException, PathDataAlreadyExistsException, FileAlreadyExistsException, CreationException;
    public PathDataMaster updatePathData(String workspaceId, String configurationItemId, String serialNumber, int pathDataMasterId, int iteration, List<InstanceAttribute> attributes, String description, DocumentRevisionKey[] pLinkKeys, String[] documentLinkComments) throws UserNotActiveException, WorkspaceNotFoundException, UserNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, NotAllowedException;
    public void deletePathData(String workspaceId, String configurationItemId, String serialNumber, int pathDataId) throws UserNotActiveException, WorkspaceNotFoundException, UserNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, NotAllowedException;

    public PathDataMaster getPathDataByPath(String workspaceId, String configurationItemId, String serialNumber, String pathAsString) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, ProductInstanceMasterNotFoundException;

    public BinaryResource saveFileInPathData(String workspaceId, String configurationItemId,  String serialNumber, int pathDataId,int iteration, String fileName, int pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, AccessRightException, ProductInstanceMasterNotFoundException, FileAlreadyExistsException, CreationException;
    public BinaryResource getPathDataBinaryResource(String fullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, FileNotFoundException, AccessRightException;
    public BinaryResource renameFileInPathData(String workspaceId, String configurationItemId, String serialNumber, int pathDataId,int iteration, String pFullName, String pNewName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, ProductInstanceMasterNotFoundException, NotAllowedException, AccessRightException, FileAlreadyExistsException, CreationException;
    public ProductInstanceMaster removeFileFromPathData(String workspaceId, String configurationItemId,  String serialNumber, int pathDataId,int iteration, String fullName, ProductInstanceMaster productInstanceMaster) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException, NotAllowedException, FileNotFoundException;


    BinaryResource saveFileInPathDataIteration(String workspaceId, String configurationItemId, String serialNumber, int pathDataId, int iteration, String fileName, int i) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, AccessRightException, ProductInstanceMasterNotFoundException, FileAlreadyExistsException, CreationException;

    PathDataMaster createPathDataMaster(String workspaceId, String configurationItemId, String serialNumber, String path, List<InstanceAttribute> attributes, String iterationNote) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException;

    PathToPathLink getPathToPathLink(String workspaceId, String configurationItemId, String serialNumber, int pathToPathLinkId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, PathToPathLinkNotFoundException;
    List<String> getPathToPathLinkTypes(String workspaceId, String configurationItemId, String serialNumber) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException;
    List<PathToPathLink> getPathToPathLinks(String workspaceId, String configurationItemId, String serialNumber) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException;

    List<PathToPathLink> getPathToPathLinkFromSourceAndTarget(String workspaceId, String configurationItemId, String serialNumber, String sourcePath, String targetPath) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException;
    List<PathToPathLink> getRootPathToPathLinks(String workspaceId, String configurationItemId, String serialNumber, String type) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException;

    List<ProductInstanceMaster> getProductInstanceMasters(PartRevision pPartRevision) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

}
