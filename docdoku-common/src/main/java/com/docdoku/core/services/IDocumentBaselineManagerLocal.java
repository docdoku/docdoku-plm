/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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

import com.docdoku.core.configuration.ConfigSpec;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.document.baseline.DocumentBaseline;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.TagKey;
import com.docdoku.core.query.DocumentSearchQuery;

import java.util.List;

/**
 *
 * @author Taylor LABEJOF
 * @version 2.0, 26/08/14
 * @since   V2.0
 */
public interface IDocumentBaselineManagerLocal {
    /**
     * Create a new <a href="DocumentBaseline.html">DocumentBaseline</a>s to snap the latest configuration of folder and documents
     * @param workspaceId The id of the workspace to snap
     * @param name The name of the new baseline
     * @param description The description of the new baseline
     * @return The new <a href="DocumentBaseline.html">DocumentBaseline</a>
     * @throws UserNotFoundException If no user is connected to this workspace
     * @throws AccessRightException If you cann't access to this workspace
     * @throws WorkspaceNotFoundException If the workspace cann't be found
     * @throws FolderNotFoundException If a folder of the configuration cann't be find
     * @throws UserNotActiveException If the connected user is disable
     * @throws DocumentRevisionNotFoundException If a document revision of the configuration cann't be find
     */
    DocumentBaseline createBaseline(String workspaceId, String name, String description) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, FolderNotFoundException, UserNotActiveException, DocumentRevisionNotFoundException;
    /**
     * Get all <a href="DocumentBaseline.html">DocumentBaseline</a>s of a specific workspace
     * @param workspaceId Id of the specific workspace
     * @return The list of <a href="DocumentBaseline.html">DocumentBaseline</a>s of the specif workspace
     * @throws UserNotFoundException If no user is connected to this workspace
     * @throws UserNotActiveException If the connected user is disable
     * @throws WorkspaceNotFoundException If the workspace cann't be found
     */
    List<DocumentBaseline> getBaselines(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    /**
     * Delete a specific <a href="DocumentBaseline.html">DocumentBaseline</a>.
     * @param baselineId The id of the baseline to deleted
     * @throws UserNotFoundException If no user is connected to this workspace
     * @throws AccessRightException If you cann't access to this workspace
     * @throws WorkspaceNotFoundException If the workspace cann't be found
     * @throws BaselineNotFoundException If the baseline cann't be found
     * @throws UserNotActiveException If the connected user is disable
     */
    void deleteBaseline(int baselineId) throws BaselineNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException;
    /**
     * Get a specific <a href="DocumentBaseline.html">DocumentBaseline</a>.
     * @param baselineId The id of the require baseline.
     * @return The require <a href="DocumentBaseline.html">DocumentBaseline</a>.
     * @throws BaselineNotFoundException If the baseline cann't be found
     * @throws UserNotFoundException If no user is connected to this workspace
     * @throws UserNotActiveException If the connected user is disable
     * @throws WorkspaceNotFoundException If the workspace cann't be found
     */
    DocumentBaseline getBaseline(int baselineId) throws BaselineNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    /**
     * Get the <a href="LatestConfigSpec.html">LatestConfigSpec</a> for a specific workspace
     * @param workspaceId The specific workspace
     * @return The LatestConfigSpec of the specific workspace
     * @throws UserNotFoundException If no user is connected to this workspace
     * @throws UserNotActiveException If the connected user is disable
     * @throws WorkspaceNotFoundException If the workspace cann't be found
     */
    ConfigSpec getLatestConfigSpec(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    /**
     * Get the <a href="BaselineConfigSpec.html">BaselineConfigSpec</a> for a specific baseline
     * @param baselineId The specific baseline
     * @return The LatestConfigSpec of the specific workspace
     * @throws UserNotFoundException If no user is connected to this workspace
     * @throws UserNotActiveException If the connected user is disable
     * @throws WorkspaceNotFoundException If the workspace cann't be found
     * @throws BaselineNotFoundException If the baseline cann't be found
     */
    ConfigSpec getConfigSpecForBaseline(int baselineId) throws BaselineNotFoundException, WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException;

    /**
     * Get the list of folder filtered by a configuration specification
     * @param workspaceId Workspace of the confspec
     * @param cs The current confSpec
     * @param completePath The complete path of the parent folder
     * @return The first level of subfolder filtered by a confSpec
     */
    String[] getFilteredFolders(String workspaceId, ConfigSpec cs, String completePath) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    /**
     * Get the list of documents filtered by a configuration specification and a tag
     * @param workspaceId Workspace of the confspec
     * @param cs The current confSpec
     * @param start Start with the start'th result
     * @param pMaxResults Number of result max
     * @return All documents with the tag filtered by a confSpec
     */
    DocumentRevision[] getFilteredDocuments(String workspaceId, ConfigSpec cs, int start, int pMaxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException;

    /**
     * Get the list of documents filtered by a configuration specification and a folder
     * @param workspaceId Workspace of the confspec
     * @param cs The current confSpec
     * @param completePath The complete path of the folder
     * @return All documents of the folder filtered by a confSpec
     */
    DocumentRevision[] getFilteredDocumentsByFolder(String workspaceId, ConfigSpec cs, String completePath) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    /**
     * Get the list of documents filtered by a configuration specification and a tag
     * @param workspaceId Workspace of the confspec
     * @param cs The current confSpec
     * @param tagKey The key of a specific tag
     * @return All documents with the tag filtered by a confSpec
     */
    DocumentRevision[] getFilteredDocumentsByTag(String workspaceId, ConfigSpec cs, TagKey tagKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException;

    /**
     * Get the list of documents filtered by a configuration specification and a query
     * @param workspaceId Workspace of the confspec
     * @param cs The current confSpec
     * @param pQuery The search query
     * @return All documents with the tag filtered by a confSpec
     */
    DocumentRevision[] searchFilteredDocuments(String workspaceId, ConfigSpec cs, DocumentSearchQuery pQuery) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, ESServerException;

    /**
     * Get a document revision filtered by a configuration specification
     * @param documentRevisionKey The document revision wanted
     * @param configSpec The current confSpec
     * @return The document revision without the iteration following the baselined document
     */
    DocumentRevision getFilteredDocumentRevision(DocumentRevisionKey documentRevisionKey, ConfigSpec configSpec) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, DocumentRevisionNotFoundException, UserNotActiveException;
}
