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

import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.workflow.TaskKey;
import com.docdoku.core.workflow.Workflow;

/**
 *
 * @author Taylor LABEJOF
 * @version 2.0, 15/10/14
 * @since   V2.0
 */
public interface IPartWorkflowManagerLocal {
    Workflow getCurentWorkflow(PartRevisionKey documentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException;
    Workflow[] getAbortedWorkflow(PartRevisionKey documentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException;

    PartRevision[] getPartRevisionsWithAssignedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;
    PartRevision[] getPartRevisionsWithOpenedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    PartRevision approveTaskOnPart(String pWorkspaceId, TaskKey pTaskKey, String pComment, String pSignature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, WorkflowNotFoundException;
    PartRevision rejectTaskOnPart(String pWorkspaceId, TaskKey pTaskKey, String pComment, String pSignature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, WorkflowNotFoundException;
}