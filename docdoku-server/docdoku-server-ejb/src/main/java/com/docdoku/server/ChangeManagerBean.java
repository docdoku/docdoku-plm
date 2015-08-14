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
package com.docdoku.server;

import com.docdoku.core.change.*;
import com.docdoku.core.common.User;
import com.docdoku.core.common.UserKey;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IChangeManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.*;
import com.docdoku.server.factory.ACLFactory;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Florent Garin
 */
@Local(IChangeManagerLocal.class)
@Stateless(name = "ChangeManagerBean")
public class ChangeManagerBean implements IChangeManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private IUserManagerLocal userManager;

    private static final Logger LOGGER = Logger.getLogger(ChangeManagerBean.class.getName());

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue getChangeIssue(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeIssue changeIssue = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeIssue(pId);           // Load the Change-Issue
        checkChangeItemReadAccess(changeIssue, user);                                                                   // Check if the user can access to the Change-Issue
        return changeIssue;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ChangeIssue> getChangeIssues(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        List<ChangeIssue> allChangeIssues = new ChangeItemDAO(new Locale(user.getLanguage()),
                em).findAllChangeIssues(pWorkspaceId);                    // Load all the Change-Issues
        List<ChangeIssue> visibleChangeIssues = new ArrayList<>();                                                      // Create a Change-Issues list to filter it
        for (ChangeIssue changeIssue : allChangeIssues) {
            try {
                checkChangeItemReadAccess(changeIssue, user);                                                            // Check if the user can access to this Change-Issue
                visibleChangeIssues.add(changeIssue);                                                                   // Add the Change-Issue to the list
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleChangeIssues;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ChangeIssue> getIssuesWithReference(String pWorkspaceId, String q, int maxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        List<ChangeIssue> allChangeIssues = new ChangeItemDAO(new Locale(user.getLanguage()),
                em).findAllChangeIssuesWithReferenceLike(pWorkspaceId, q, maxResults);// Load all the Change-Issues matching this pattern
        List<ChangeIssue> visibleChangeIssues = new ArrayList<>();                                                      // Create a Change-Issues list to filter it
        for (ChangeIssue changeIssue : allChangeIssues) {
            try {
                checkChangeItemReadAccess(changeIssue, user);                                                            // Check if the user can access to this Change-Issue
                visibleChangeIssues.add(changeIssue);                                                                   // Add the Change-Issue to the list
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleChangeIssues;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue createChangeIssue(String pWorkspaceId, String name, String description, String initiator, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);                                                // Check the write access to the workspace
        User assigneeUser = null;
        if (assignee != null && pWorkspaceId != null) {
            assigneeUser = em.find(User.class, new UserKey(pWorkspaceId, assignee));
        }
        ChangeIssue change = new ChangeIssue(name,                                                                      // Create the Change-Issue =>   The Change-Issue's name
                user.getWorkspace(),                                                        //                              The Change-Issue's workspace
                user,                                                                       //                              The Change-Issue's author
                assigneeUser,                                                               //                              The Change-Issue's assignee
                new Date(),                                                                 //                              The Change-Issue's creation date
                description,                                                                //                              The Change-Issue's description
                priority,                                                                   //                              The Change-Issue's priority
                category,                                                                   //                              The Change-Issue's category
                initiator);                                                                 //                              The Change-Issue's initiator
        new ChangeItemDAO(new Locale(user.getLanguage()), em).createChangeItem(change);                                 // Persist the Change-Issue
        return change;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue updateChangeIssue(int pId, String pWorkspaceId, String description, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeIssue changeIssue = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeIssue(pId);            // Load the Change-Issue
        checkChangeItemWriteAccess(changeIssue, user);                                                                  // Check the write access to the Change-Issue
        changeIssue.setDescription(description);                                                                        // Update the Change-Issue attributes
        changeIssue.setPriority(priority);
        changeIssue.setCategory(category);
        changeIssue.setAssignee(em.find(User.class, new UserKey(pWorkspaceId, assignee)));
        return changeIssue;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteChangeIssue(int pId) throws ChangeIssueNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, EntityConstraintException {
        ChangeIssue changeIssue = new ChangeItemDAO(em).loadChangeIssue(pId);                                           // Load the Change-Issue
        User user = userManager.checkWorkspaceReadAccess(changeIssue.getWorkspaceId());                                 // Check the read access to the workspace
        checkChangeItemWriteAccess(changeIssue, user);                                                                  // Check the write access to the Change-Issue

        Locale locale = new Locale(user.getLanguage());
        ChangeItemDAO changeItemDAO = new ChangeItemDAO(locale, em);

        if (changeItemDAO.hasChangeRequestsLinked(changeIssue)) {
            throw new EntityConstraintException(locale, "EntityConstraintException26");
        }

        new ChangeItemDAO(locale, em).deleteChangeItem(changeIssue);                                                    // Delete the Change-Issue
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue saveChangeIssueAffectedDocuments(String pWorkspaceId, int pId, DocumentIterationKey[] pAffectedDocuments) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException, DocumentRevisionNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Locale userLocale = new Locale(user.getLanguage());                                                             // Load the user Locale
        ChangeIssue changeIssue = new ChangeItemDAO(userLocale, em).loadChangeIssue(pId);                               // Load the Change-Issue
        checkChangeItemWriteAccess(changeIssue, user);                                                                  // Check the write access to the Change-Issue
        changeIssue.setAffectedDocuments(getDocumentIterations(pAffectedDocuments,userLocale));                         // Update the Change-Issue's affected documents list
        return changeIssue;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue saveChangeIssueAffectedParts(String pWorkspaceId, int pId, PartIterationKey[] pAffectedParts) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Locale userLocale = new Locale(user.getLanguage());                                                             // Load the user Locale
        ChangeIssue changeIssue = new ChangeItemDAO(userLocale, em).loadChangeIssue(pId);                                // Load the Change-Issue
        checkChangeItemWriteAccess(changeIssue, user);                                                                   // Check the write access to the Change-Issue

        Set<PartIteration> partIterations = new HashSet<>();
        PartRevisionDAO partRDAO = new PartRevisionDAO(userLocale, em);
        for (PartIterationKey partKey : pAffectedParts) {
            try {
                partIterations.add(partRDAO.loadPartR(partKey.getPartRevision()).getIteration(partKey.getIteration())); // Add the part iteration to the Change-Issue
            } catch (PartRevisionNotFoundException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        }

        changeIssue.setAffectedParts(partIterations);                                                                   // Update the Change-Issue's affected part list
        return changeIssue;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue saveChangeIssueTags(String pWorkspaceId, int pId, String[] tagsLabel) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Locale userLocale = new Locale(user.getLanguage());                                                             // Load the user Locale
        ChangeIssue changeIssue = new ChangeItemDAO(userLocale, em).loadChangeIssue(pId);                                // Load the Change-Issue
        checkChangeItemWriteAccess(changeIssue, user);                                                                  // Check the write access to the Change-Issue

        Set<Tag> tags = new HashSet<>();                                                                            // Create un Set of the tags
        for (String label : tagsLabel) {
            tags.add(new Tag(user.getWorkspace(), label));
        }

        TagDAO tagDAO = new TagDAO(userLocale, em);
        List<Tag> existingTags = Arrays.asList(tagDAO.findAllTags(user.getWorkspaceId()));                              // Load all the existing tags

        Set<Tag> tagsToCreate = new HashSet<>(tags);
        tagsToCreate.removeAll(existingTags);                                                                           // Get the list of new tag in the tag list

        for (Tag t : tagsToCreate) {                                                                                    // Create the missing tag
            try {
                tagDAO.createTag(t);
            } catch (CreationException | TagAlreadyExistsException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        changeIssue.setTags(tags);                                                                                      // Update the Change-Issue's tag list
        return changeIssue;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue removeChangeIssueTag(String pWorkspaceId, int pId, String tagName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeItemDAO changeItemDAO = new ChangeItemDAO(new Locale(user.getLanguage()), em);
        ChangeIssue changeIssue = changeItemDAO.loadChangeIssue(pId);                                                   // Load the Change-Issue
        checkChangeItemWriteAccess(changeIssue, user);                                                                  // Check the write access to the Change-Issue
        return (ChangeIssue) changeItemDAO.removeTag(changeIssue, tagName);                                             // Update the Change-Issue's tag list
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest getChangeRequest(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeRequest changeRequest = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeRequest(pId);     // Load the Change-Request
        checkChangeItemReadAccess(changeRequest, user);                                                                 // Check if the user can access to the Change-Request
        return filterLinkedChangeIssues(changeRequest, user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ChangeRequest> getChangeRequests(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        List<ChangeRequest> allChangeRequests = new ChangeItemDAO(new Locale(user.getLanguage()),
                em).findAllChangeRequests(pWorkspaceId);                    // Load all the Change-Requests
        List<ChangeRequest> visibleChangeRequests = new ArrayList<>();                                                  // Create a Change-Requests list to filter it

        for (ChangeRequest changeRequest : allChangeRequests) {
            try {
                checkChangeItemReadAccess(changeRequest, user);                                                          // Check if the user can access to this Change-Request
                visibleChangeRequests.add(filterLinkedChangeIssues(changeRequest, user));                               // Add the Change-Request filtered to the list
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleChangeRequests;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ChangeRequest> getRequestsWithReference(String pWorkspaceId, String q, int maxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        List<ChangeRequest> allChangeRequests = new ChangeItemDAO(new Locale(user.getLanguage()),
                em).findAllChangeRequestsWithReferenceLike(pWorkspaceId, q, maxResults);// Load all the Change-Requests matching this pattern
        List<ChangeRequest> visibleChangeRequests = new ArrayList<>();                                                  // Create a Change-Requests list to filter it
        for (ChangeRequest changeRequest : allChangeRequests) {
            try {
                checkChangeItemReadAccess(changeRequest, user);                                                          // Check if the user can access to this Change-Request
                visibleChangeRequests.add(filterLinkedChangeIssues(changeRequest, user));                               // Add the Change-Request filtered to the list
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleChangeRequests;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest createChangeRequest(String pWorkspaceId, String name, String description, int milestoneId, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);                                                // Check the write access to the workspace
        User assigneeUser = null;
        if (assignee != null && pWorkspaceId != null) {
            assigneeUser = em.find(User.class, new UserKey(pWorkspaceId, assignee));
        }
        ChangeRequest changeRequest = new ChangeRequest(name,                                                           // Create the Change-Request => The Change-Request's name
                user.getWorkspace(),                                                   //                              The Change-Request's workspace
                user,                                                                  //                              The Change-Request's author
                assigneeUser,                                                          //                              The Change-Request's Assignee
                new Date(),                                                            //                              The Change-Request's Creation Date
                description,                                                           //                              The Change-Request's Description
                priority,                                                              //                              The Change-Request's Priority
                category,                                                              //                              The Change-Request's Category
                em.find(Milestone.class, milestoneId));                                //                              The Change-Request's Milestone
        new ChangeItemDAO(new Locale(user.getLanguage()), em).createChangeItem(changeRequest);                          // Persist the Change-Request
        return changeRequest;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest updateChangeRequest(int pId, String pWorkspaceId, String description, int milestoneId, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeRequest changeRequest = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeRequest(pId);      // Load the Change-Request
        checkChangeItemWriteAccess(changeRequest, user);                                                                // Check the write access to the Change-Request
        changeRequest.setDescription(description);                                                                      // Update the Change-Request attributes
        changeRequest.setPriority(priority);
        changeRequest.setCategory(category);
        changeRequest.setAssignee(em.find(User.class, new UserKey(pWorkspaceId, assignee)));
        changeRequest.setMilestone(em.find(Milestone.class, milestoneId));
        return changeRequest;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteChangeRequest(String pWorkspaceId, int pId) throws ChangeRequestNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, EntityConstraintException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());

        ChangeItemDAO changeItemDAO = new ChangeItemDAO(userLocale, em);
        ChangeRequest changeRequest = changeItemDAO.loadChangeRequest(pId);

        if (changeItemDAO.hasChangeOrdersLinked(changeRequest)) {
            throw new EntityConstraintException(userLocale, "EntityConstraintException10");
        }

        checkChangeItemWriteAccess(changeRequest, user);
        changeItemDAO.deleteChangeItem(changeRequest);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest saveChangeRequestAffectedDocuments(String pWorkspaceId, int pId, DocumentIterationKey[] pAffectedDocuments) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException, DocumentRevisionNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Locale userLocale = new Locale(user.getLanguage());                                                             // Load the user Locale
        ChangeRequest changeRequest = new ChangeItemDAO(userLocale, em).loadChangeRequest(pId);                         // Load the Change-Request
        checkChangeItemWriteAccess(changeRequest, user);                                                                // Check the write access to the Change-Request
        changeRequest.setAffectedDocuments(getDocumentIterations(pAffectedDocuments,userLocale));
        return changeRequest;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest saveChangeRequestAffectedParts(String pWorkspaceId, int pId, PartIterationKey[] pAffectedParts) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Locale userLocale = new Locale(user.getLanguage());                                                             // Load the user Locale
        ChangeRequest changeRequest = new ChangeItemDAO(userLocale, em).loadChangeRequest(pId);                          // Load the Change-Request
        checkChangeItemWriteAccess(changeRequest, user);                                                                 // Check the write access to the Change-Request

        Set<PartIteration> partIterations = new HashSet<>();
        PartRevisionDAO partRDAO = new PartRevisionDAO(userLocale, em);
        for (PartIterationKey partKey : pAffectedParts) {
            try {
                partIterations.add(partRDAO.loadPartR(partKey.getPartRevision()).getIteration(partKey.getIteration())); // Add the part iteration to the Change-Request
            } catch (PartRevisionNotFoundException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        }
        changeRequest.setAffectedParts(partIterations);
        return changeRequest;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest saveChangeRequestAffectedIssues(String pWorkspaceId, int pId, int[] pLinkIds) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeItemDAO changeItemDAO = new ChangeItemDAO(new Locale(user.getLanguage()), em);
        ChangeRequest changeRequest = changeItemDAO.loadChangeRequest(pId);                                             // Load the Change-Request
        checkChangeItemWriteAccess(changeRequest, user);                                                                 // Check the write access to the Change-Request

        Set<ChangeIssue> changeIssues = new HashSet<>();
        for (int linkId : pLinkIds) {
            try {
                changeIssues.add(changeItemDAO.loadChangeIssue(linkId));                                                // Add the issue to the Change-Request
            } catch (ChangeIssueNotFoundException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        }
        changeRequest.setAddressedChangeIssues(changeIssues);
        return changeRequest;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest saveChangeRequestTags(String pWorkspaceId, int pId, String[] tagsLabel) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Locale userLocale = new Locale(user.getLanguage());                                                             // Load the user Locale
        ChangeRequest changeRequest = new ChangeItemDAO(userLocale, em).loadChangeRequest(pId);                          // Load the Change-Request
        checkChangeItemWriteAccess(changeRequest, user);                                                                // Check the write access to the Change-Request

        Set<Tag> tags = new HashSet<>();                                                                            // Create un Set of the tags
        for (String label : tagsLabel) {
            tags.add(new Tag(user.getWorkspace(), label));
        }

        TagDAO tagDAO = new TagDAO(userLocale, em);
        List<Tag> existingTags = Arrays.asList(tagDAO.findAllTags(user.getWorkspaceId()));                              // Load all the existing tags

        Set<Tag> tagsToCreate = new HashSet<>(tags);                                                                    // Get the list of new tag in the tag list
        tagsToCreate.removeAll(existingTags);

        for (Tag t : tagsToCreate) {                                                                                    // Create the missing tag
            try {
                tagDAO.createTag(t);
            } catch (CreationException | TagAlreadyExistsException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        changeRequest.setTags(tags);                                                                                    // Update the Change-Request's tag list
        return changeRequest;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest removeChangeRequestTag(String pWorkspaceId, int pId, String tagName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeItemDAO changeItemDAO = new ChangeItemDAO(new Locale(user.getLanguage()), em);
        ChangeIssue changeRequest = changeItemDAO.loadChangeIssue(pId);                                                 // Load the Change-Request
        checkChangeItemWriteAccess(changeRequest, user);                                                                // Check the write access to the Change-Request
        return (ChangeRequest) changeItemDAO.removeTag(changeRequest, tagName);                                         // Update the Change-Request's tag list
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder getChangeOrder(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeOrder changeOrder = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeOrder(pId);           // Load the Change-Order
        checkChangeItemReadAccess(changeOrder, user);                                                                   // Check if the user can access to the Change-Order
        return filterLinkedChangeRequests(changeOrder, user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ChangeOrder> getChangeOrders(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        List<ChangeOrder> allChangeOrders = new ChangeItemDAO(new Locale(user.getLanguage()),
                em).findAllChangeOrders(pWorkspaceId);                    // Load all the Change-Orders
        List<ChangeOrder> visibleChangeOrders = new ArrayList<>();                                                      // Create a Change-Orders list to filter it
        for (ChangeOrder changeOrder : allChangeOrders) {
            try {
                checkChangeItemReadAccess(changeOrder, user);                                                            // Check if the user can access to this Change-Order
                visibleChangeOrders.add(filterLinkedChangeRequests(changeOrder, user));                                 // Add the Change-Order filtered to the list
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleChangeOrders;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder createChangeOrder(String pWorkspaceId, String name, String description, int milestoneId, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);                                                // Check the write access to the workspace
        User assigneeUser = null;
        if (assignee != null && pWorkspaceId != null) {
            assigneeUser = em.find(User.class, new UserKey(pWorkspaceId, assignee));
        }
        ChangeOrder changeOrder = new ChangeOrder(name,                                                                 // Create the Change-Order => The Change-Order's name
                user.getWorkspace(),                                                   //                            The Change-Order's workspace
                user,                                                                  //                            The Change-Order's author
                assigneeUser,                                                          //                            The Change-Order's Assignee
                new Date(),                                                            //                            The Change-Order's Creation Date
                description,                                                           //                            The Change-Order's Description
                priority,                                                              //                            The Change-Order's Priority
                category,                                                              //                            The Change-Order's Category
                em.find(Milestone.class, milestoneId));                                //                            The Change-Order's Milestone
        new ChangeItemDAO(new Locale(user.getLanguage()), em).createChangeItem(changeOrder);                            // Persist the Change-Order
        return changeOrder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder updateChangeOrder(int pId, String pWorkspaceId, String description, int milestoneId, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeOrder changeOrder = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeOrder(pId);            // Load the Change-Order
        checkChangeItemWriteAccess(changeOrder, user);                                                                  // Check the write access to the Change-Order
        changeOrder.setDescription(description);                                                                        // Update the Change-Order attributes
        changeOrder.setPriority(priority);
        changeOrder.setCategory(category);
        changeOrder.setAssignee(em.find(User.class, new UserKey(pWorkspaceId, assignee)));
        changeOrder.setMilestone(em.find(Milestone.class, milestoneId));
        return changeOrder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteChangeOrder(int pId) throws ChangeOrderNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        ChangeOrder changeOrder = new ChangeItemDAO(em).loadChangeOrder(pId);                                           // Load the Change-Order
        User user = userManager.checkWorkspaceReadAccess(changeOrder.getWorkspaceId());                                 // Check the read access to the workspace
        checkChangeItemWriteAccess(changeOrder, user);                                                                   // Check the write access to the Change-Order
        new ChangeItemDAO(new Locale(user.getLanguage()), em).deleteChangeItem(changeOrder);                             // Delete the Change-Order
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder saveChangeOrderAffectedDocuments(String pWorkspaceId, int pId, DocumentIterationKey[] pAffectedDocuments) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException, DocumentRevisionNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Locale userLocale = new Locale(user.getLanguage());                                                             // Load the user Locale
        ChangeOrder changeOrder = new ChangeItemDAO(userLocale, em).loadChangeOrder(pId);                                // Load the Change-Order
        checkChangeItemWriteAccess(changeOrder, user);                                                                   // Check the write access to the Change-Order
        changeOrder.setAffectedDocuments(getDocumentIterations(pAffectedDocuments,userLocale));
        return changeOrder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder saveChangeOrderAffectedParts(String pWorkspaceId, int pId, PartIterationKey[] pAffectedParts) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Locale userLocale = new Locale(user.getLanguage());                                                             // Load the user Locale
        ChangeOrder changeOrder = new ChangeItemDAO(userLocale, em).loadChangeOrder(pId);                                // Load the Change-Order
        checkChangeItemWriteAccess(changeOrder, user);                                                                   // Check the write access to the Change-Order

        Set<PartIteration> partIterations = new HashSet<>();
        PartRevisionDAO partRDAO = new PartRevisionDAO(userLocale, em);
        for (PartIterationKey partKey : pAffectedParts) {
            try {
                partIterations.add(partRDAO.loadPartR(partKey.getPartRevision()).getIteration(partKey.getIteration())); // Add the part iteration to the Change-Order
            } catch (PartRevisionNotFoundException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        }
        changeOrder.setAffectedParts(partIterations);
        return changeOrder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder saveChangeOrderAffectedRequests(String pWorkspaceId, int pId, int[] pLinkIds) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeItemDAO changeItemDAO = new ChangeItemDAO(new Locale(user.getLanguage()), em);
        ChangeOrder changeOrder = changeItemDAO.loadChangeOrder(pId);                                                   // Load the Change-Order
        checkChangeItemWriteAccess(changeOrder, user);                                                                   // Check the write access to the Change-Order

        Set<ChangeRequest> changeRequests = new HashSet<>();
        for (int linkId : pLinkIds) {
            try {
                changeRequests.add(changeItemDAO.loadChangeRequest(linkId));                                            // Add the request to the Change-Order
            } catch (ChangeRequestNotFoundException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        }
        changeOrder.setAddressedChangeRequests(changeRequests);
        return changeOrder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder saveChangeOrderTags(String pWorkspaceId, int pId, String[] tagsLabel) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Locale userLocale = new Locale(user.getLanguage());                                                             // Load the user Locale
        ChangeOrder changeOrder = new ChangeItemDAO(userLocale, em).loadChangeOrder(pId);                                // Load the Change-Order
        checkChangeItemWriteAccess(changeOrder, user);                                                                   // Check the write access to the Change-Order

        Set<Tag> tags = new HashSet<>();                                                                            // Create un Set of the tags
        for (String label : tagsLabel) {
            tags.add(new Tag(user.getWorkspace(), label));
        }

        TagDAO tagDAO = new TagDAO(userLocale, em);
        List<Tag> existingTags = Arrays.asList(tagDAO.findAllTags(user.getWorkspaceId()));                              // Load all the existing tags

        Set<Tag> tagsToCreate = new HashSet<>(tags);                                                                    // Get the list of new tag in the tag list
        tagsToCreate.removeAll(existingTags);

        for (Tag t : tagsToCreate) {
            try {
                tagDAO.createTag(t);
            } catch (CreationException | TagAlreadyExistsException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        changeOrder.setTags(tags);                                                                                      // Update the Change-Order's tag list
        return changeOrder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder removeChangeOrderTag(String pWorkspaceId, int pId, String tagName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeItemDAO changeItemDAO = new ChangeItemDAO(new Locale(user.getLanguage()), em);
        ChangeIssue changeOrder = changeItemDAO.loadChangeIssue(pId);                                                   // Load the Change-Order
        checkChangeItemWriteAccess(changeOrder, user);                                                                  // Check the write access to the Change-Order
        return (ChangeOrder) changeItemDAO.removeTag(changeOrder, tagName);                                             // Update the Change-Order's tag list
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Milestone getChangeMilestone(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Milestone milestone = new MilestoneDAO(new Locale(user.getLanguage()), em).loadMilestone(pId);                  // Load the Milestone
        checkMilestoneReadAccess(milestone, user);                                                                      // Check if the user can access to the Milestone
        return milestone;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Milestone getChangeMilestoneByTitle(String pWorkspaceId, String pTitle) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Milestone milestone = new MilestoneDAO(new Locale(user.getLanguage()), em).loadMilestone(pTitle, pWorkspaceId); // Load the Milestone
        checkMilestoneReadAccess(milestone, user);                                                                      // Check if the user can access to the Milestone
        return milestone;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<Milestone> getChangeMilestones(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        List<Milestone> allMilestones = new MilestoneDAO(new Locale(user.getLanguage()),
                em).findAllMilestone(pWorkspaceId);                            // Load all the Milestones
        List<Milestone> visibleMilestones = new ArrayList<>(allMilestones);                                             // Duplicate the Milestones list to filter it
        for (Milestone milestone : allMilestones) {
            try {
                checkMilestoneReadAccess(milestone, user);                                                              // Check if the user can access to this Milestone
            } catch (AccessRightException e) {
                visibleMilestones.remove(milestone);                                                                    // If access is deny remove it from the result
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleMilestones;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Milestone createChangeMilestone(String pWorkspaceId, String title, String description, Date dueDate) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, MilestoneAlreadyExistsException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);                                                // Check the write access to the workspace
        Milestone milestone = new Milestone(title,                                                                      // Create the Milestone =>  The Milestone's title
                dueDate,                                                                    //                          The Milestone's due date
                description,                                                                //                          The Milestone's description
                user.getWorkspace());                                                       //                          The Milestone's workspace
        new MilestoneDAO(new Locale(user.getLanguage()), em).createMilestone(milestone);                                // Persist the Milestone
        return milestone;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Milestone updateChangeMilestone(int pId, String pWorkspaceId, String title, String description, Date dueDate) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Milestone milestone = new MilestoneDAO(new Locale(user.getLanguage()), em).loadMilestone(pId);                   // Load the Milestone
        checkMilestoneWriteAccess(milestone, user);                                                                      // Check the write access to the milestone
        milestone.setTitle(title);                                                                                      // Update the Milestone
        milestone.setDescription(description);
        milestone.setDueDate(dueDate);
        return milestone;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteChangeMilestone(String pWorkspaceId, int pId) throws MilestoneNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, EntityConstraintException {

        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        MilestoneDAO milestoneDAO = new MilestoneDAO(userLocale, em);

        Milestone milestone = milestoneDAO.loadMilestone(pId);

        checkMilestoneWriteAccess(milestone, user);

        int numberOfOrders = milestoneDAO.getNumberOfOrders(milestone.getId(), milestone.getWorkspaceId());

        if (numberOfOrders > 0) {
            throw new EntityConstraintException(userLocale, "EntityConstraintException8");
        }

        int numberOfRequests = milestoneDAO.getNumberOfRequests(milestone.getId(), milestone.getWorkspaceId());

        if (numberOfRequests > 0) {
            throw new EntityConstraintException(userLocale, "EntityConstraintException9");
        }

        milestoneDAO.deleteMilestone(milestone);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ChangeRequest> getChangeRequestsByMilestone(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Milestone milestone = new MilestoneDAO(new Locale(user.getLanguage()), em).loadMilestone(pId);                  // Load the Milestone
        checkMilestoneReadAccess(milestone, user);                                                                      // Check if the user can access to the Milestone
        List<ChangeRequest> affectedRequests = new MilestoneDAO(new Locale(user.getLanguage()),
                em).getAllRequests(pId, pWorkspaceId);              // Load all affected request
        List<ChangeRequest> visibleChangeRequests = new ArrayList<>();                                                  // Create a Change-Requests list to filter it
        for (ChangeRequest changeRequest : affectedRequests) {
            try {
                checkChangeItemReadAccess(changeRequest, user);                                                          // Check if the user can access to this Change-Request
                visibleChangeRequests.add(filterLinkedChangeIssues(changeRequest, user));                               // Add the Change-Request filtered to the list
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleChangeRequests;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ChangeOrder> getChangeOrdersByMilestone(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Milestone milestone = new MilestoneDAO(new Locale(user.getLanguage()), em).loadMilestone(pId);                  // Load the Milestone
        checkMilestoneReadAccess(milestone, user);                                                                      // Check if the user can access to the Milestone
        List<ChangeOrder> affectedOrders = new MilestoneDAO(new Locale(user.getLanguage()),
                em).getAllOrders(pId, pWorkspaceId);                        // Load all affected request
        List<ChangeOrder> visibleChangeOrders = new ArrayList<>();                                                      // Create a Change-Orders list to filter it
        for (ChangeOrder changeOrder : affectedOrders) {
            try {
                checkChangeItemReadAccess(changeOrder, user);                                                            // Check if the user can access to this Change-Order
                visibleChangeOrders.add(filterLinkedChangeRequests(changeOrder, user));                                 // Add the Change-Order filtered to the list
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleChangeOrders;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public int getNumberOfRequestByMilestone(String pWorkspaceId, int milestoneId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        return new MilestoneDAO(new Locale(user.getLanguage()), em).getNumberOfRequests(milestoneId, pWorkspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public int getNumberOfOrderByMilestone(String pWorkspaceId, int milestoneId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        return new MilestoneDAO(new Locale(user.getLanguage()), em).getNumberOfOrders(milestoneId, pWorkspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void updateACLForChangeIssue(String pWorkspaceId, int pId, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeIssue changeIssue = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeIssue(pId);            // Load the change item
        checkChangeItemGrantAccess(changeIssue, user);                                                                  // Check the grant access to the change item

        updateACLForChangeItem(pWorkspaceId, changeIssue, pUserEntries, pGroupEntries);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void updateACLForChangeRequest(String pWorkspaceId, int pId, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeRequest changeRequest = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeRequest(pId);      // Load the change item
        checkChangeItemGrantAccess(changeRequest, user);                                                                // Check the grant access to the change item

        updateACLForChangeItem(pWorkspaceId, changeRequest, pUserEntries, pGroupEntries);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void updateACLForChangeOrder(String pWorkspaceId, int pId, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeOrder changeOrder = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeOrder(pId);            // Load the change item
        checkChangeItemGrantAccess(changeOrder, user);                                                                  // Check the grant access to the change item

        updateACLForChangeItem(pWorkspaceId, changeOrder, pUserEntries, pGroupEntries);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void updateACLForMilestone(String pWorkspaceId, int pId, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Milestone milestone = new MilestoneDAO(new Locale(user.getLanguage()), em).loadMilestone(pId);                   // Load the milestone
        checkMilestoneWriteAccess(milestone, user);                                                                    // Check the grant access to the milestone
        ACLFactory aclFactory = new ACLFactory(em);
        if (milestone.getACL() == null) {
            // Check if already a ACL Rule
            ACL acl = aclFactory.createACL(pWorkspaceId, pUserEntries, pGroupEntries);
            milestone.setACL(acl);
        } else {                                                                                                          // Else change existing ACL Rule
            ACL acl = milestone.getACL();
            aclFactory.updateACL(pWorkspaceId,acl, pUserEntries, pGroupEntries);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeACLFromChangeIssue(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeIssue changeIssue = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeIssue(pId);            // Load the change item
        checkChangeItemGrantAccess(changeIssue, user);                                                                   // Check the grant access to the change item

        removeACLFromChangeItem(changeIssue);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeACLFromChangeRequest(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeRequest changeRequest = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeRequest(pId);      // Load the change item
        checkChangeItemGrantAccess(changeRequest, user);                                                                // Check the grant access to the change item

        removeACLFromChangeItem(changeRequest);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeACLFromChangeOrder(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        ChangeOrder changeOrder = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeOrder(pId);            // Load the change item
        checkChangeItemGrantAccess(changeOrder, user);                                                                  // Check the grant access to the change item

        removeACLFromChangeItem(changeOrder);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeACLFromMilestone(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);                                                 // Check the read access to the workspace
        Milestone milestone = new MilestoneDAO(new Locale(user.getLanguage()), em).loadMilestone(pId);                   // Load the milestone
        checkMilestoneWriteAccess(milestone, user);                                                                     // Check the grant access to the milestone

        ACL acl = milestone.getACL();
        if (acl != null) {
            new ACLDAO(em).removeACLEntries(acl);
            milestone.setACL(null);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public boolean isChangeItemWritable(ChangeItem pChangeItem) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pChangeItem.getWorkspaceId());                                 // Check the read access to the workspace
        try {
            checkChangeItemWriteAccess(pChangeItem, user);                                                               // Try to check if the user can write the Change-Item
            return true;                                                                                                // Set the writable attribute to XHR
        } catch (AccessRightException e) {
            LOGGER.log(Level.FINEST, null, e);
            return false;
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public boolean isMilestoneWritable(Milestone pMilestone) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pMilestone.getWorkspaceId());                                  // Check the read access to the workspace
        try {
            checkMilestoneWriteAccess(pMilestone, user);                                                                 // Try to check if the user can write the Milestone
            return true;
        } catch (AccessRightException e) {
            LOGGER.log(Level.FINEST, null, e);
            return false;
        }
    }

    private void updateACLForChangeItem(String pWorkspaceId, ChangeItem changeItem, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) {
        ACLFactory aclFactory = new ACLFactory(em);
        if (changeItem.getACL() == null) {                                                                           // Check if already a ACL Rule
            ACL acl = aclFactory.createACL(pWorkspaceId, pUserEntries, pGroupEntries);
            changeItem.setACL(acl);
        } else {                                                                                                         // Else change existing ACL Rule

            aclFactory.updateACL(pWorkspaceId,changeItem.getACL(), pUserEntries, pGroupEntries);
        }
    }

    private void removeACLFromChangeItem(ChangeItem changeItem) {
        ACL acl = changeItem.getACL();
        if (acl != null) {
            new ACLDAO(em).removeACLEntries(acl);
            changeItem.setACL(null);
        }
    }

    private User checkChangeItemGrantAccess(ChangeItem pChangeItem, User pUser) throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        if (pUser.isAdministrator()) {                                                                                    // Check if it is the workspace's administrator
            return pUser;
        } else if (pUser.getLogin().equals(pChangeItem.getAuthor().getLogin())) {                                          // Check if it the change item owner
            checkChangeItemWriteAccess(pChangeItem, pUser);                                                              // Check if the owner have right access to the change item
            return pUser;
        } else {
            throw new AccessRightException(new Locale(pUser.getLanguage()), pUser);
        }
    }

    private User checkChangeItemWriteAccess(ChangeItem pChangeItem, User pUser) throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        if (pUser.isAdministrator()) {                                                                                    // Check if it is the workspace's administrator
            return pUser;
        }
        if (pChangeItem.getACL() == null) {                                                                                 // Check if the item haven't ACL
            return userManager.checkWorkspaceWriteAccess(pChangeItem.getWorkspaceId());
        } else if (pChangeItem.getACL().hasWriteAccess(pUser)) {                                                            // Check if there is a write access
            return pUser;
        } else {                                                                                                          // Else throw a AccessRightException
            throw new AccessRightException(new Locale(pUser.getLanguage()), pUser);
        }
    }

    private User checkChangeItemReadAccess(ChangeItem pChangeItem, User pUser) throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        if (pUser.isAdministrator() ||                                                                                   // Check if it is the workspace's administrator
                pChangeItem.getACL() == null ||                                                                                // Check if the item haven't ACL
                pChangeItem.getACL().hasReadAccess(pUser)) {                                                                  // Check if ACL grant read access
            return pUser;
        } else {                                                                                                          // Else throw a AccessRightException
            throw new AccessRightException(new Locale(pUser.getLanguage()), pUser);
        }
    }

    private User checkMilestoneWriteAccess(Milestone pMilestone, User pUser) throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        if (pUser.isAdministrator()) {                                                                                    // Check if it is the workspace's administrator
            return pUser;
        }
        if (pMilestone.getACL() == null) {                                                                                  // Check if the item haven't ACL
            return userManager.checkWorkspaceWriteAccess(pMilestone.getWorkspaceId());
        } else if (pMilestone.getACL().hasWriteAccess(pUser)) {                                                             // Check if there is a write access
            return pUser;
        } else {                                                                                                          // Else throw a AccessRightException
            throw new AccessRightException(new Locale(pUser.getLanguage()), pUser);
        }
    }

    private User checkMilestoneReadAccess(Milestone pMilestone, User pUser) throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        if (pUser.isAdministrator() ||                                                                                   // Check if it is the workspace's administrator
                pMilestone.getACL() == null ||                                                                            // Check if the item haven't ACL
                pMilestone.getACL().hasReadAccess(pUser)) {                                                              // Check if ACL grant read access
            return pUser;
        } else {                                                                                                          // Else throw a AccessRightException
            throw new AccessRightException(new Locale(pUser.getLanguage()), pUser);
        }
    }

    private ChangeRequest filterLinkedChangeIssues(ChangeRequest changeRequest, User user) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        em.detach(changeRequest);
        Set<ChangeIssue> addressedChangeIssues = changeRequest.getAddressedChangeIssues();
        Set<ChangeIssue> visibleChangeIssues = new HashSet<>();                                                         // Create a Change-Issues list to filter it
        for (ChangeIssue changeIssue : addressedChangeIssues) {
            try {
                checkChangeItemReadAccess(changeIssue, user);                                                            // Check if the user can access to this Change-Issue
                visibleChangeIssues.add(changeIssue);                                                                   // Add the Change-Issue to the list
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        changeRequest.setAddressedChangeIssues(visibleChangeIssues);
        return changeRequest;
    }

    private ChangeOrder filterLinkedChangeRequests(ChangeOrder changeOrder, User user) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        em.detach(changeOrder);
        Set<ChangeRequest> allChangeRequests = changeOrder.getAddressedChangeRequests();
        Set<ChangeRequest> visibleChangeRequests = new HashSet<>();                                                     // Create a Change-Requests list to filter it
        for (ChangeRequest changeRequest : allChangeRequests) {
            try {
                checkChangeItemReadAccess(changeRequest, user);                                                          // Check if the user can access to this Change-Request
                visibleChangeRequests.add(filterLinkedChangeIssues(changeRequest, user));                               // Add the Change-Request filtered to the list
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        changeOrder.setAddressedChangeRequests(visibleChangeRequests);
        return changeOrder;
    }

    private Set<DocumentIteration> getDocumentIterations(DocumentIterationKey[] pAffectedDocuments, Locale userLocale) throws DocumentRevisionNotFoundException {

        Set<DocumentIteration> documentIterations = new HashSet<>();
        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(userLocale, em);

        for (DocumentIterationKey docKey : pAffectedDocuments) {

            DocumentRevision documentRevision = documentRevisionDAO.loadDocR(docKey.getDocumentRevision());
            DocumentIteration iteration;

            if(docKey.getIteration() > 0){
                iteration = documentRevision.getIteration(docKey.getIteration());
            }else{
                iteration = documentRevision.getLastCheckedInIteration();
            }

            if(iteration != null){
                documentIterations.add(iteration);
            }
        }
        return documentIterations;
    }

}