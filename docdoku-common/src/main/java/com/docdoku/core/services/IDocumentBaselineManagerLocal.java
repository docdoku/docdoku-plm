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

import com.docdoku.core.configuration.DocumentBaseline;
import com.docdoku.core.exceptions.*;

import java.util.List;

/**
 *
 * @author Taylor LABEJOF
 * @version 2.0, 26/08/14
 * @since   V2.0
 */
public interface IDocumentBaselineManagerLocal {
    /**
     * Create a new {@link com.docdoku.core.configuration.DocumentBaseline} to snap the latest configuration of folder and documents
     * @param workspaceId The id of the workspace to snap
     * @param name The name of the new baseline
     * @param type The type of the new baseline (latest or release)
     * @param description The description of the new baseline
     * @return The new {@link com.docdoku.core.configuration.DocumentBaseline}
     * @throws com.docdoku.core.exceptions.UserNotFoundException If no user is connected to this workspace
     * @throws com.docdoku.core.exceptions.AccessRightException If you cann't access to this workspace
     * @throws com.docdoku.core.exceptions.WorkspaceNotFoundException If the workspace cann't be found
     * @throws com.docdoku.core.exceptions.FolderNotFoundException If a folder of the configuration cann't be find
     * @throws com.docdoku.core.exceptions.UserNotActiveException If the connected user is disable
     * @throws com.docdoku.core.exceptions.DocumentRevisionNotFoundException If a document revision of the configuration cann't be find
     */
    DocumentBaseline createBaseline(String workspaceId, String name, DocumentBaseline.BaselineType type, String description) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, FolderNotFoundException, UserNotActiveException, DocumentRevisionNotFoundException;
    /**
     * Get all {@link com.docdoku.core.configuration.DocumentBaseline}s of a specific workspace
     * @param workspaceId Id of the specific workspace
     * @return The list of {@link com.docdoku.core.configuration.DocumentBaseline}s of the specif workspace
     * @throws com.docdoku.core.exceptions.UserNotFoundException If no user is connected to this workspace
     * @throws com.docdoku.core.exceptions.UserNotActiveException If the connected user is disable
     * @throws com.docdoku.core.exceptions.WorkspaceNotFoundException If the workspace cann't be found
     */
    List<DocumentBaseline> getBaselines(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    /**
     * Delete a specific {@link com.docdoku.core.configuration.DocumentBaseline}.
     * @param baselineId The id of the baseline to deleted
     * @throws com.docdoku.core.exceptions.UserNotFoundException If no user is connected to this workspace
     * @throws com.docdoku.core.exceptions.AccessRightException If you cann't access to this workspace
     * @throws com.docdoku.core.exceptions.WorkspaceNotFoundException If the workspace cann't be found
     * @throws com.docdoku.core.exceptions.BaselineNotFoundException If the baseline cann't be found
     * @throws com.docdoku.core.exceptions.UserNotActiveException If the connected user is disable
     */
    void deleteBaseline(int baselineId) throws BaselineNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException;
    /**
     * Get a specific {@link com.docdoku.core.configuration.DocumentBaseline}.
     * @param baselineId The id of the require baseline.
     * @return The require {@link com.docdoku.core.configuration.DocumentBaseline}.
     * @throws com.docdoku.core.exceptions.BaselineNotFoundException If the baseline cann't be found
     * @throws com.docdoku.core.exceptions.UserNotFoundException If no user is connected to this workspace
     * @throws com.docdoku.core.exceptions.UserNotActiveException If the connected user is disable
     * @throws com.docdoku.core.exceptions.WorkspaceNotFoundException If the workspace cann't be found
     */
    DocumentBaseline getBaseline(int baselineId) throws BaselineNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
 }
