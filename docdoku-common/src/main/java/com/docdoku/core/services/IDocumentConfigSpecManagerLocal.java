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

import com.docdoku.core.configuration.DocumentConfigSpec;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.TagKey;
import com.docdoku.core.query.DocumentSearchQuery;

/**
 *
 * @author Taylor LABEJOF
 * @version 2.0, 26/08/14
 * @since   V2.0
 */
public interface IDocumentConfigSpecManagerLocal {
    /**
     * Get the {@link com.docdoku.core.configuration.DocumentConfigSpec} for a specific workspace
     * @param workspaceId The specific workspace
     * @return The LatestConfigSpec of the specific workspace
     * @throws UserNotFoundException If no user is connected to this workspace
     * @throws UserNotActiveException If the connected user is disable
     * @throws WorkspaceNotFoundException If the workspace cann't be found
     */
    DocumentConfigSpec getLatestConfigSpec(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    /**
     * Get the {@link com.docdoku.core.configuration.DocumentConfigSpec} for a specific baseline
     * @param baselineId The specific baseline
     * @return The LatestConfigSpec of the specific workspace
     * @throws UserNotFoundException If no user is connected to this workspace
     * @throws UserNotActiveException If the connected user is disable
     * @throws WorkspaceNotFoundException If the workspace cann't be found
     * @throws BaselineNotFoundException If the baseline cann't be found
     */
    DocumentConfigSpec getConfigSpecForBaseline(int baselineId) throws BaselineNotFoundException, WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException;

    /**
     * Get the list of folder filtered by a configuration specification
     * @param workspaceId Workspace of the confspec
     * @param cs The current confSpec
     * @param completePath The complete path of the parent folder
     * @return The first level of subfolder filtered by a confSpec
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     */
    String[] getFilteredFolders(String workspaceId, DocumentConfigSpec cs, String completePath) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    /**
     * Get the list of documents filtered by a configuration specification and a tag
     * @param workspaceId Workspace of the confspec
     * @param cs The current confSpec
     * @param start Start with the start'th result
     * @param pMaxResults Number of result max
     * @return All documents with the tag filtered by a confSpec
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws DocumentRevisionNotFoundException
     */
    DocumentRevision[] getFilteredDocuments(String workspaceId, DocumentConfigSpec cs, int start, int pMaxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException;

    /**
     * Get the list of documents filtered by a configuration specification and a folder
     * @param workspaceId Workspace of the confspec
     * @param cs The current confSpec
     * @param completePath The complete path of the folder
     * @return All documents of the folder filtered by a confSpec
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     */
    DocumentRevision[] getFilteredDocumentsByFolder(String workspaceId, DocumentConfigSpec cs, String completePath) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    /**
     * Get the list of documents filtered by a configuration specification and a tag
     * @param workspaceId Workspace of the confspec
     * @param cs The current confSpec
     * @param tagKey The key of a specific tag
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws DocumentRevisionNotFoundException
     * @return All documents with the tag filtered by a confSpec
     */
    DocumentRevision[] getFilteredDocumentsByTag(String workspaceId, DocumentConfigSpec cs, TagKey tagKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException;

    /**
     * Get the list of documents filtered by a configuration specification and a query
     * @param workspaceId Workspace of the confspec
     * @param cs The current confSpec
     * @param pQuery The search query
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     * @throws WorkspaceNotFoundException
     * @throws DocumentRevisionNotFoundException
     * @throws ESServerException
     * @return All documents with the tag filtered by a confSpec
     */
    DocumentRevision[] searchFilteredDocuments(String workspaceId, DocumentConfigSpec cs, DocumentSearchQuery pQuery) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, ESServerException;

    /**
     * Get a document revision filtered by a configuration specification
     * @param documentRevisionKey The document revision wanted
     * @param configSpec The current confSpec
     * @throws AccessRightException
     * @throws NotAllowedException
     * @throws WorkspaceNotFoundException
     * @throws UserNotFoundException
     * @throws DocumentRevisionNotFoundException
     * @throws UserNotActiveException
     * @return The document revision without the iteration following the baselined document
     */
    DocumentRevision getFilteredDocumentRevision(DocumentRevisionKey documentRevisionKey, DocumentConfigSpec configSpec) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, DocumentRevisionNotFoundException, UserNotActiveException;
}