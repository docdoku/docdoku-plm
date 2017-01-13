/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import com.docdoku.core.exceptions.*;
import com.docdoku.core.workflow.TaskKey;
import com.docdoku.core.workflow.TaskWrapper;

/**
 *
 * @author Morgan Guimard
 */
public interface ITaskManagerLocal {
    TaskWrapper[] getAssignedTasksForGivenUser(String workspaceId, String userLogin) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException;
    TaskWrapper[] getInProgressTasksForGivenUser(String workspaceId, String userLogin) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException;
    TaskWrapper getTask(String workspaceId, TaskKey taskKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, TaskNotFoundException, AccessRightException, WorkspaceNotEnabledException;

    void processTask(String workspaceId, TaskKey taskKey, String action, String comment, String signature) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, WorkflowNotFoundException, AccessRightException, DocumentRevisionNotFoundException, WorkspaceNotEnabledException;

    void checkTask(String workspaceId, TaskKey taskKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, WorkspaceNotEnabledException, TaskNotFoundException, WorkflowNotFoundException, NotAllowedException;
}
