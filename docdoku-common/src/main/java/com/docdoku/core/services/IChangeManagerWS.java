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

import com.docdoku.core.change.ChangeIssue;
import com.docdoku.core.change.ChangeItem;
import com.docdoku.core.change.ChangeOrder;
import com.docdoku.core.change.ChangeRequest;
import com.docdoku.core.exceptions.*;

import java.util.List;

/**
 *
 * @author Florent Garin
 */
public interface IChangeManagerWS {

    void deleteChangeIssue(int pId) throws ChangeIssueNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotFoundException;
    List<ChangeIssue> getChangeIssues(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    ChangeIssue createChangeIssue(String pWorkspaceId, String name, String description, String initiator, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException;

    void deleteChangeRequest(int pId) throws ChangeRequestNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotFoundException;
    List<ChangeRequest> getChangeRequests(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    ChangeRequest createChangeRequest(String pWorkspaceId, String name, String description, int milestone, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException;

    void deleteChangeOrder(int pId) throws ChangeOrderNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotFoundException;
    List<ChangeOrder> getChangeOrders(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    ChangeOrder createChangeOrder(String pWorkspaceId, String name, String description, int milestone, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException;

}
