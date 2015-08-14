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

import com.docdoku.core.change.*;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIterationKey;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Florent Garin
 */
public interface IChangeManagerLocal {
    ChangeIssue getChangeIssue(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException;
    List<ChangeIssue> getChangeIssues(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    List<ChangeIssue> getIssuesWithReference(String workspaceId, String q, int maxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    ChangeIssue createChangeIssue(String pWorkspaceId, String name, String description, String initiator, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException;
    ChangeIssue updateChangeIssue(int pId, String pWorkspaceId, String description, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException;
    void deleteChangeIssue(int pId) throws ChangeIssueNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, EntityConstraintException;
    ChangeIssue saveChangeIssueAffectedDocuments(String pWorkspaceId, int pId, DocumentIterationKey[] pAffectedDocuments) throws UserNotActiveException, UserNotFoundException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException, DocumentRevisionNotFoundException;
    ChangeIssue saveChangeIssueAffectedParts(String pWorkspaceId, int pId, PartIterationKey[] pAffectedParts) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException;
    ChangeIssue saveChangeIssueTags(String pWorkspaceId, int pId, String[] tagsLabel) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException;
    ChangeIssue removeChangeIssueTag(String workspaceId, int pId, String tagName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException;

    ChangeRequest getChangeRequest(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException;
    List<ChangeRequest> getChangeRequests(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    List<ChangeRequest> getRequestsWithReference(String workspaceId, String q, int maxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    ChangeRequest createChangeRequest(String pWorkspaceId, String name, String description, int milestone, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException;
    ChangeRequest updateChangeRequest(int pId, String pWorkspaceId, String description, int milestoneId, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException;
    void deleteChangeRequest(String pWorkspaceId, int pId) throws ChangeRequestNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, EntityConstraintException;
    ChangeRequest saveChangeRequestAffectedDocuments(String workspaceId, int requestId, DocumentIterationKey[] links) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException, DocumentRevisionNotFoundException;
    ChangeRequest saveChangeRequestAffectedParts(String workspaceId, int requestId, PartIterationKey[] links) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException;
    ChangeRequest saveChangeRequestAffectedIssues(String pWorkspaceId, int pRequestId, int[] pLinkId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException;
    ChangeRequest saveChangeRequestTags(String pWorkspaceId, int pId, String[] tagsLabel) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException;
    ChangeRequest removeChangeRequestTag(String workspaceId, int pId, String tagName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException;

    ChangeOrder getChangeOrder(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException;
    List<ChangeOrder> getChangeOrders(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    ChangeOrder createChangeOrder(String pWorkspaceId, String name, String description, int milestone, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException;
    ChangeOrder updateChangeOrder(int pId, String pWorkspaceId, String description, int milestoneId, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException;
    void deleteChangeOrder(int pId) throws ChangeOrderNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException;
    ChangeOrder saveChangeOrderAffectedDocuments(String workspaceId, int pOrderId, DocumentIterationKey[] links) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException, DocumentRevisionNotFoundException;
    ChangeOrder saveChangeOrderAffectedParts(String workspaceId, int pOrderId, PartIterationKey[] links) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException;
    ChangeOrder saveChangeOrderAffectedRequests(String pWorkspaceId, int pOrderId, int[] pLinkId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException;
    ChangeOrder saveChangeOrderTags(String pWorkspaceId, int pId, String[] tagsLabel) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException;
    ChangeOrder removeChangeOrderTag(String workspaceId, int pId, String tagName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException;

    Milestone getChangeMilestone(String pWorkspaceId, int milestoneId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException;
    Milestone getChangeMilestoneByTitle(String pWorkspaceId, String pTitle) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException;
    List<Milestone> getChangeMilestones(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    Milestone createChangeMilestone(String pWorkspaceId, String title, String description, Date dueDate) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, MilestoneAlreadyExistsException;
    Milestone updateChangeMilestone(int milestoneId, String pWorkspaceId, String title, String description, Date dueDate) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException;
    void deleteChangeMilestone(String pWorkspaceId, int milestoneId) throws MilestoneNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, EntityConstraintException;
    List<ChangeRequest> getChangeRequestsByMilestone(String pWorkspaceId, int milestoneId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException;
    List<ChangeOrder> getChangeOrdersByMilestone(String pWorkspaceId, int milestoneId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException;
    int getNumberOfRequestByMilestone(String pWorkspaceId, int milestoneId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    int getNumberOfOrderByMilestone(String pWorkspaceId, int milestoneId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    void updateACLForChangeIssue(String pWorkspaceId, int pId, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException;
    void updateACLForChangeRequest(String pWorkspaceId, int pId, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException;
    void updateACLForChangeOrder(String pWorkspaceId, int pId, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException;
    void updateACLForMilestone(String pWorkspaceId, int pId, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException;

    void removeACLFromChangeIssue(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException;
    void removeACLFromChangeRequest(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException;
    void removeACLFromChangeOrder(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException;
    void removeACLFromMilestone(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException;

    boolean isChangeItemWritable(ChangeItem pChangeItem) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    boolean isMilestoneWritable(Milestone pMilestone) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
}