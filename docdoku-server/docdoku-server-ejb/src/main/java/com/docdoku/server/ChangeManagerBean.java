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
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
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

    @Inject
    private IUserManagerLocal userManager;

    private static final Logger LOGGER = Logger.getLogger(ChangeManagerBean.class.getName());

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue getChangeIssue(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeIssue changeIssue = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeIssue(pId);
        checkChangeItemReadAccess(changeIssue, user);
        return changeIssue;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ChangeIssue> getChangeIssues(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<ChangeIssue> allChangeIssues = new ChangeItemDAO(new Locale(user.getLanguage()),
                em).findAllChangeIssues(pWorkspaceId);
        List<ChangeIssue> visibleChangeIssues = new ArrayList<>();
        for (ChangeIssue changeIssue : allChangeIssues) {
            try {
                checkChangeItemReadAccess(changeIssue, user);
                visibleChangeIssues.add(changeIssue);
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleChangeIssues;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ChangeIssue> getIssuesWithReference(String pWorkspaceId, String q, int maxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<ChangeIssue> allChangeIssues = new ChangeItemDAO(new Locale(user.getLanguage()),
                em).findAllChangeIssuesWithReferenceLike(pWorkspaceId, q, maxResults);
        List<ChangeIssue> visibleChangeIssues = new ArrayList<>();
        for (ChangeIssue changeIssue : allChangeIssues) {
            try {
                checkChangeItemReadAccess(changeIssue, user);
                visibleChangeIssues.add(changeIssue);
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleChangeIssues;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue createChangeIssue(String pWorkspaceId, String name, String description, String initiator, ChangeItemPriority priority, String assignee, ChangeItemCategory category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        User assigneeUser = null;
        if (assignee != null && pWorkspaceId != null) {
            assigneeUser = em.find(User.class, new UserKey(pWorkspaceId, assignee));
        }
        ChangeIssue change = new ChangeIssue(name,
                user.getWorkspace(),
                user,
                assigneeUser,
                new Date(),
                description,
                priority,
                category,
                initiator);
        new ChangeItemDAO(new Locale(user.getLanguage()), em).createChangeItem(change);
        return change;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue updateChangeIssue(int pId, String pWorkspaceId, String description, ChangeItemPriority priority, String assignee, ChangeItemCategory category) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeIssue changeIssue = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeIssue(pId);
        checkChangeItemWriteAccess(changeIssue, user);
        changeIssue.setDescription(description);
        changeIssue.setPriority(priority);
        changeIssue.setCategory(category);
        changeIssue.setAssignee(em.find(User.class, new UserKey(pWorkspaceId, assignee)));
        return changeIssue;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteChangeIssue(int pId) throws ChangeIssueNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, EntityConstraintException, WorkspaceNotEnabledException {
        ChangeIssue changeIssue = new ChangeItemDAO(em).loadChangeIssue(pId);
        User user = userManager.checkWorkspaceReadAccess(changeIssue.getWorkspaceId());
        checkChangeItemWriteAccess(changeIssue, user);

        Locale locale = new Locale(user.getLanguage());
        ChangeItemDAO changeItemDAO = new ChangeItemDAO(locale, em);

        if (changeItemDAO.hasChangeRequestsLinked(changeIssue)) {
            throw new EntityConstraintException(locale, "EntityConstraintException26");
        }

        new ChangeItemDAO(locale, em).deleteChangeItem(changeIssue);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue saveChangeIssueAffectedDocuments(String pWorkspaceId, int pId, DocumentIterationKey[] pAffectedDocuments) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException, DocumentRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ChangeIssue changeIssue = new ChangeItemDAO(userLocale, em).loadChangeIssue(pId);
        checkChangeItemWriteAccess(changeIssue, user);
        changeIssue.setAffectedDocuments(getDocumentIterations(pAffectedDocuments, userLocale));
        return changeIssue;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue saveChangeIssueAffectedParts(String pWorkspaceId, int pId, PartIterationKey[] pAffectedParts) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ChangeIssue changeIssue = new ChangeItemDAO(userLocale, em).loadChangeIssue(pId);
        checkChangeItemWriteAccess(changeIssue, user);

        Set<PartIteration> partIterations = new HashSet<>();
        PartRevisionDAO partRDAO = new PartRevisionDAO(userLocale, em);
        for (PartIterationKey partKey : pAffectedParts) {
            try {
                partIterations.add(partRDAO.loadPartR(partKey.getPartRevision()).getIteration(partKey.getIteration()));
            } catch (PartRevisionNotFoundException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        }

        changeIssue.setAffectedParts(partIterations);
        return changeIssue;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue saveChangeIssueTags(String pWorkspaceId, int pId, String[] tagsLabel) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ChangeIssue changeIssue = new ChangeItemDAO(userLocale, em).loadChangeIssue(pId);
        checkChangeItemWriteAccess(changeIssue, user);

        Set<Tag> tags = new HashSet<>();
        for (String label : tagsLabel) {
            tags.add(new Tag(user.getWorkspace(), label));
        }

        TagDAO tagDAO = new TagDAO(userLocale, em);
        List<Tag> existingTags = Arrays.asList(tagDAO.findAllTags(user.getWorkspaceId()));

        Set<Tag> tagsToCreate = new HashSet<>(tags);
        tagsToCreate.removeAll(existingTags);

        for (Tag t : tagsToCreate) {
            try {
                tagDAO.createTag(t);
            } catch (CreationException | TagAlreadyExistsException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        changeIssue.setTags(tags);
        return changeIssue;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue removeChangeIssueTag(String pWorkspaceId, int pId, String tagName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeItemDAO changeItemDAO = new ChangeItemDAO(new Locale(user.getLanguage()), em);
        ChangeIssue changeIssue = changeItemDAO.loadChangeIssue(pId);
        checkChangeItemWriteAccess(changeIssue, user);
        return (ChangeIssue) changeItemDAO.removeTag(changeIssue, tagName);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest getChangeRequest(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeRequest changeRequest = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeRequest(pId);
        checkChangeItemReadAccess(changeRequest, user);
        return filterLinkedChangeIssues(changeRequest, user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ChangeRequest> getChangeRequests(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<ChangeRequest> allChangeRequests = new ChangeItemDAO(new Locale(user.getLanguage()),
                em).findAllChangeRequests(pWorkspaceId);
        List<ChangeRequest> visibleChangeRequests = new ArrayList<>();

        for (ChangeRequest changeRequest : allChangeRequests) {
            try {
                checkChangeItemReadAccess(changeRequest, user);
                visibleChangeRequests.add(filterLinkedChangeIssues(changeRequest, user));
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleChangeRequests;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ChangeRequest> getRequestsWithReference(String pWorkspaceId, String q, int maxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<ChangeRequest> allChangeRequests = new ChangeItemDAO(new Locale(user.getLanguage()),
                em).findAllChangeRequestsWithReferenceLike(pWorkspaceId, q, maxResults);
        List<ChangeRequest> visibleChangeRequests = new ArrayList<>();
        for (ChangeRequest changeRequest : allChangeRequests) {
            try {
                checkChangeItemReadAccess(changeRequest, user);
                visibleChangeRequests.add(filterLinkedChangeIssues(changeRequest, user));
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleChangeRequests;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest createChangeRequest(String pWorkspaceId, String name, String description, int milestoneId, ChangeItemPriority priority, String assignee, ChangeItemCategory category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        User assigneeUser = null;
        if (assignee != null && pWorkspaceId != null) {
            assigneeUser = em.find(User.class, new UserKey(pWorkspaceId, assignee));
        }
        ChangeRequest changeRequest = new ChangeRequest(name,
                user.getWorkspace(),
                user,
                assigneeUser,
                new Date(),
                description,
                priority,
                category,
                em.find(Milestone.class, milestoneId));
        new ChangeItemDAO(new Locale(user.getLanguage()), em).createChangeItem(changeRequest);
        return changeRequest;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest updateChangeRequest(int pId, String pWorkspaceId, String description, int milestoneId, ChangeItemPriority priority, String assignee, ChangeItemCategory category) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeRequest changeRequest = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeRequest(pId);
        checkChangeItemWriteAccess(changeRequest, user);
        changeRequest.setDescription(description);
        changeRequest.setPriority(priority);
        changeRequest.setCategory(category);
        changeRequest.setAssignee(em.find(User.class, new UserKey(pWorkspaceId, assignee)));
        changeRequest.setMilestone(em.find(Milestone.class, milestoneId));
        return changeRequest;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteChangeRequest(String pWorkspaceId, int pId) throws ChangeRequestNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, EntityConstraintException, WorkspaceNotEnabledException {
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
    public ChangeRequest saveChangeRequestAffectedDocuments(String pWorkspaceId, int pId, DocumentIterationKey[] pAffectedDocuments) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException, DocumentRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ChangeRequest changeRequest = new ChangeItemDAO(userLocale, em).loadChangeRequest(pId);
        checkChangeItemWriteAccess(changeRequest, user);
        changeRequest.setAffectedDocuments(getDocumentIterations(pAffectedDocuments, userLocale));
        return changeRequest;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest saveChangeRequestAffectedParts(String pWorkspaceId, int pId, PartIterationKey[] pAffectedParts) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ChangeRequest changeRequest = new ChangeItemDAO(userLocale, em).loadChangeRequest(pId);
        checkChangeItemWriteAccess(changeRequest, user);

        Set<PartIteration> partIterations = new HashSet<>();
        PartRevisionDAO partRDAO = new PartRevisionDAO(userLocale, em);
        for (PartIterationKey partKey : pAffectedParts) {
            try {
                partIterations.add(partRDAO.loadPartR(partKey.getPartRevision()).getIteration(partKey.getIteration()));
            } catch (PartRevisionNotFoundException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        }
        changeRequest.setAffectedParts(partIterations);
        return changeRequest;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest saveChangeRequestAffectedIssues(String pWorkspaceId, int pId, int[] pLinkIds) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeItemDAO changeItemDAO = new ChangeItemDAO(new Locale(user.getLanguage()), em);
        ChangeRequest changeRequest = changeItemDAO.loadChangeRequest(pId);
        checkChangeItemWriteAccess(changeRequest, user);

        Set<ChangeIssue> changeIssues = new HashSet<>();
        for (int linkId : pLinkIds) {
            try {
                changeIssues.add(changeItemDAO.loadChangeIssue(linkId));
            } catch (ChangeIssueNotFoundException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        }
        changeRequest.setAddressedChangeIssues(changeIssues);
        return changeRequest;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest saveChangeRequestTags(String pWorkspaceId, int pId, String[] tagsLabel) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ChangeRequest changeRequest = new ChangeItemDAO(userLocale, em).loadChangeRequest(pId);
        checkChangeItemWriteAccess(changeRequest, user);

        Set<Tag> tags = new HashSet<>();
        for (String label : tagsLabel) {
            tags.add(new Tag(user.getWorkspace(), label));
        }

        TagDAO tagDAO = new TagDAO(userLocale, em);
        List<Tag> existingTags = Arrays.asList(tagDAO.findAllTags(user.getWorkspaceId()));

        Set<Tag> tagsToCreate = new HashSet<>(tags);
        tagsToCreate.removeAll(existingTags);

        for (Tag t : tagsToCreate) {
            try {
                tagDAO.createTag(t);
            } catch (CreationException | TagAlreadyExistsException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        changeRequest.setTags(tags);
        return changeRequest;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest removeChangeRequestTag(String pWorkspaceId, int pId, String tagName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeItemDAO changeItemDAO = new ChangeItemDAO(new Locale(user.getLanguage()), em);
        ChangeIssue changeRequest = changeItemDAO.loadChangeIssue(pId);
        checkChangeItemWriteAccess(changeRequest, user);
        return (ChangeRequest) changeItemDAO.removeTag(changeRequest, tagName);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder getChangeOrder(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeOrder changeOrder = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeOrder(pId);
        checkChangeItemReadAccess(changeOrder, user);
        return filterLinkedChangeRequests(changeOrder, user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ChangeOrder> getChangeOrders(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<ChangeOrder> allChangeOrders = new ChangeItemDAO(new Locale(user.getLanguage()),
                em).findAllChangeOrders(pWorkspaceId);
        List<ChangeOrder> visibleChangeOrders = new ArrayList<>();
        for (ChangeOrder changeOrder : allChangeOrders) {
            try {
                checkChangeItemReadAccess(changeOrder, user);
                visibleChangeOrders.add(filterLinkedChangeRequests(changeOrder, user));
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleChangeOrders;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder createChangeOrder(String pWorkspaceId, String name, String description, int milestoneId, ChangeItemPriority priority, String assignee, ChangeItemCategory category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        User assigneeUser = null;
        if (assignee != null && pWorkspaceId != null) {
            assigneeUser = em.find(User.class, new UserKey(pWorkspaceId, assignee));
        }
        ChangeOrder changeOrder = new ChangeOrder(name,
                user.getWorkspace(),
                user,
                assigneeUser,
                new Date(),
                description,
                priority,
                category,
                em.find(Milestone.class, milestoneId));
        new ChangeItemDAO(new Locale(user.getLanguage()), em).createChangeItem(changeOrder);
        return changeOrder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder updateChangeOrder(int pId, String pWorkspaceId, String description, int milestoneId, ChangeItemPriority priority, String assignee, ChangeItemCategory category) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeOrder changeOrder = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeOrder(pId);
        checkChangeItemWriteAccess(changeOrder, user);
        changeOrder.setDescription(description);
        changeOrder.setPriority(priority);
        changeOrder.setCategory(category);
        changeOrder.setAssignee(em.find(User.class, new UserKey(pWorkspaceId, assignee)));
        changeOrder.setMilestone(em.find(Milestone.class, milestoneId));
        return changeOrder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteChangeOrder(int pId) throws ChangeOrderNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        ChangeOrder changeOrder = new ChangeItemDAO(em).loadChangeOrder(pId);
        User user = userManager.checkWorkspaceReadAccess(changeOrder.getWorkspaceId());
        checkChangeItemWriteAccess(changeOrder, user);
        new ChangeItemDAO(new Locale(user.getLanguage()), em).deleteChangeItem(changeOrder);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder saveChangeOrderAffectedDocuments(String pWorkspaceId, int pId, DocumentIterationKey[] pAffectedDocuments) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException, DocumentRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ChangeOrder changeOrder = new ChangeItemDAO(userLocale, em).loadChangeOrder(pId);
        checkChangeItemWriteAccess(changeOrder, user);
        changeOrder.setAffectedDocuments(getDocumentIterations(pAffectedDocuments, userLocale));
        return changeOrder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder saveChangeOrderAffectedParts(String pWorkspaceId, int pId, PartIterationKey[] pAffectedParts) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ChangeOrder changeOrder = new ChangeItemDAO(userLocale, em).loadChangeOrder(pId);
        checkChangeItemWriteAccess(changeOrder, user);

        Set<PartIteration> partIterations = new HashSet<>();
        PartRevisionDAO partRDAO = new PartRevisionDAO(userLocale, em);
        for (PartIterationKey partKey : pAffectedParts) {
            try {
                partIterations.add(partRDAO.loadPartR(partKey.getPartRevision()).getIteration(partKey.getIteration()));
            } catch (PartRevisionNotFoundException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        }
        changeOrder.setAffectedParts(partIterations);
        return changeOrder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder saveChangeOrderAffectedRequests(String pWorkspaceId, int pId, int[] pLinkIds) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeItemDAO changeItemDAO = new ChangeItemDAO(new Locale(user.getLanguage()), em);
        ChangeOrder changeOrder = changeItemDAO.loadChangeOrder(pId);
        checkChangeItemWriteAccess(changeOrder, user);

        Set<ChangeRequest> changeRequests = new HashSet<>();
        for (int linkId : pLinkIds) {
            try {
                changeRequests.add(changeItemDAO.loadChangeRequest(linkId));
            } catch (ChangeRequestNotFoundException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        }
        changeOrder.setAddressedChangeRequests(changeRequests);
        return changeOrder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder saveChangeOrderTags(String pWorkspaceId, int pId, String[] tagsLabel) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ChangeOrder changeOrder = new ChangeItemDAO(userLocale, em).loadChangeOrder(pId);
        checkChangeItemWriteAccess(changeOrder, user);

        Set<Tag> tags = new HashSet<>();
        for (String label : tagsLabel) {
            tags.add(new Tag(user.getWorkspace(), label));
        }

        TagDAO tagDAO = new TagDAO(userLocale, em);
        List<Tag> existingTags = Arrays.asList(tagDAO.findAllTags(user.getWorkspaceId()));

        Set<Tag> tagsToCreate = new HashSet<>(tags);
        tagsToCreate.removeAll(existingTags);

        for (Tag t : tagsToCreate) {
            try {
                tagDAO.createTag(t);
            } catch (CreationException | TagAlreadyExistsException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        changeOrder.setTags(tags);
        return changeOrder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder removeChangeOrderTag(String pWorkspaceId, int pId, String tagName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeItemDAO changeItemDAO = new ChangeItemDAO(new Locale(user.getLanguage()), em);
        ChangeIssue changeOrder = changeItemDAO.loadChangeIssue(pId);
        checkChangeItemWriteAccess(changeOrder, user);
        return (ChangeOrder) changeItemDAO.removeTag(changeOrder, tagName);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Milestone getMilestone(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Milestone milestone = new MilestoneDAO(new Locale(user.getLanguage()), em).loadMilestone(pId);
        checkMilestoneReadAccess(milestone, user);
        return milestone;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Milestone getMilestoneByTitle(String pWorkspaceId, String pTitle) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Milestone milestone = new MilestoneDAO(new Locale(user.getLanguage()), em).loadMilestone(pTitle, pWorkspaceId);
        checkMilestoneReadAccess(milestone, user);
        return milestone;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<Milestone> getMilestones(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<Milestone> allMilestones = new MilestoneDAO(new Locale(user.getLanguage()),
                em).findAllMilestone(pWorkspaceId);
        List<Milestone> visibleMilestones = new ArrayList<>(allMilestones);
        for (Milestone milestone : allMilestones) {
            try {
                checkMilestoneReadAccess(milestone, user);
            } catch (AccessRightException e) {
                visibleMilestones.remove(milestone);
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleMilestones;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Milestone createMilestone(String pWorkspaceId, String title, String description, Date dueDate) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, MilestoneAlreadyExistsException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        Milestone milestone = new Milestone(title,
                dueDate,
                description,
                user.getWorkspace());
        new MilestoneDAO(new Locale(user.getLanguage()), em).createMilestone(milestone);
        return milestone;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Milestone updateMilestone(int pId, String pWorkspaceId, String title, String description, Date dueDate) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Milestone milestone = new MilestoneDAO(new Locale(user.getLanguage()), em).loadMilestone(pId);
        checkMilestoneWriteAccess(milestone, user);
        milestone.setTitle(title);
        milestone.setDescription(description);
        milestone.setDueDate(dueDate);
        return milestone;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteMilestone(String pWorkspaceId, int pId) throws MilestoneNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, EntityConstraintException, WorkspaceNotEnabledException {

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
    public List<ChangeRequest> getChangeRequestsByMilestone(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Milestone milestone = new MilestoneDAO(new Locale(user.getLanguage()), em).loadMilestone(pId);
        checkMilestoneReadAccess(milestone, user);
        List<ChangeRequest> affectedRequests = new MilestoneDAO(new Locale(user.getLanguage()),
                em).getAllRequests(pId, pWorkspaceId);
        List<ChangeRequest> visibleChangeRequests = new ArrayList<>();
        for (ChangeRequest changeRequest : affectedRequests) {
            try {
                checkChangeItemReadAccess(changeRequest, user);
                visibleChangeRequests.add(filterLinkedChangeIssues(changeRequest, user));
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleChangeRequests;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ChangeOrder> getChangeOrdersByMilestone(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Milestone milestone = new MilestoneDAO(new Locale(user.getLanguage()), em).loadMilestone(pId);
        checkMilestoneReadAccess(milestone, user);
        List<ChangeOrder> affectedOrders = new MilestoneDAO(new Locale(user.getLanguage()),
                em).getAllOrders(pId, pWorkspaceId);
        List<ChangeOrder> visibleChangeOrders = new ArrayList<>();
        for (ChangeOrder changeOrder : affectedOrders) {
            try {
                checkChangeItemReadAccess(changeOrder, user);
                visibleChangeOrders.add(filterLinkedChangeRequests(changeOrder, user));
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
        return visibleChangeOrders;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public int getNumberOfRequestByMilestone(String pWorkspaceId, int milestoneId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new MilestoneDAO(new Locale(user.getLanguage()), em).getNumberOfRequests(milestoneId, pWorkspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public int getNumberOfOrderByMilestone(String pWorkspaceId, int milestoneId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new MilestoneDAO(new Locale(user.getLanguage()), em).getNumberOfOrders(milestoneId, pWorkspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue updateACLForChangeIssue(String pWorkspaceId, int pId, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeIssue changeIssue = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeIssue(pId);
        checkChangeItemGrantAccess(changeIssue, user);

        updateACLForChangeItem(pWorkspaceId, changeIssue, pUserEntries, pGroupEntries);
        return changeIssue;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest updateACLForChangeRequest(String pWorkspaceId, int pId, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeRequest changeRequest = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeRequest(pId);
        checkChangeItemGrantAccess(changeRequest, user);

        updateACLForChangeItem(pWorkspaceId, changeRequest, pUserEntries, pGroupEntries);
        return changeRequest;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder updateACLForChangeOrder(String pWorkspaceId, int pId, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeOrder changeOrder = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeOrder(pId);
        checkChangeItemGrantAccess(changeOrder, user);

        updateACLForChangeItem(pWorkspaceId, changeOrder, pUserEntries, pGroupEntries);
        return changeOrder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Milestone updateACLForMilestone(String pWorkspaceId, int pId, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Milestone milestone = new MilestoneDAO(new Locale(user.getLanguage()), em).loadMilestone(pId);
        checkMilestoneWriteAccess(milestone, user);
        ACLFactory aclFactory = new ACLFactory(em);
        if (milestone.getACL() == null) {
            // Check if already a ACL Rule
            ACL acl = aclFactory.createACL(pWorkspaceId, pUserEntries, pGroupEntries);
            milestone.setACL(acl);
        } else {
            ACL acl = milestone.getACL();
            aclFactory.updateACL(pWorkspaceId, acl, pUserEntries, pGroupEntries);
        }
        return milestone;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeIssue removeACLFromChangeIssue(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeIssueNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeIssue changeIssue = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeIssue(pId);
        checkChangeItemGrantAccess(changeIssue, user);

        removeACLFromChangeItem(changeIssue);
        return changeIssue;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeRequest removeACLFromChangeRequest(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeRequestNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeRequest changeRequest = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeRequest(pId);
        checkChangeItemGrantAccess(changeRequest, user);

        removeACLFromChangeItem(changeRequest);
        return changeRequest;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ChangeOrder removeACLFromChangeOrder(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ChangeOrderNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        ChangeOrder changeOrder = new ChangeItemDAO(new Locale(user.getLanguage()), em).loadChangeOrder(pId);
        checkChangeItemGrantAccess(changeOrder, user);

        removeACLFromChangeItem(changeOrder);
        return changeOrder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Milestone removeACLFromMilestone(String pWorkspaceId, int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, MilestoneNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Milestone milestone = new MilestoneDAO(new Locale(user.getLanguage()), em).loadMilestone(pId);
        checkMilestoneWriteAccess(milestone, user);

        ACL acl = milestone.getACL();
        if (acl != null) {
            new ACLDAO(em).removeACLEntries(acl);
            milestone.setACL(null);
        }

        return milestone;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public boolean isChangeItemWritable(ChangeItem pChangeItem) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pChangeItem.getWorkspaceId());
        try {
            checkChangeItemWriteAccess(pChangeItem, user);
            return true;
        } catch (AccessRightException e) {
            LOGGER.log(Level.FINEST, null, e);
            return false;
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public boolean isMilestoneWritable(Milestone pMilestone) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pMilestone.getWorkspaceId());
        try {
            checkMilestoneWriteAccess(pMilestone, user);
            return true;
        } catch (AccessRightException e) {
            LOGGER.log(Level.FINEST, null, e);
            return false;
        }
    }

    private void updateACLForChangeItem(String pWorkspaceId, ChangeItem changeItem, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) {
        ACLFactory aclFactory = new ACLFactory(em);
        if (changeItem.getACL() == null) {
            ACL acl = aclFactory.createACL(pWorkspaceId, pUserEntries, pGroupEntries);
            changeItem.setACL(acl);
        } else {

            aclFactory.updateACL(pWorkspaceId, changeItem.getACL(), pUserEntries, pGroupEntries);
        }
    }

    private void removeACLFromChangeItem(ChangeItem changeItem) {
        ACL acl = changeItem.getACL();
        if (acl != null) {
            new ACLDAO(em).removeACLEntries(acl);
            changeItem.setACL(null);
        }
    }

    private User checkChangeItemGrantAccess(ChangeItem pChangeItem, User pUser) throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        if (pUser.isAdministrator()) {
            return pUser;
        } else if (pUser.getLogin().equals(pChangeItem.getAuthor().getLogin())) {
            checkChangeItemWriteAccess(pChangeItem, pUser);
            return pUser;
        } else {
            throw new AccessRightException(new Locale(pUser.getLanguage()), pUser);
        }
    }

    private User checkChangeItemWriteAccess(ChangeItem pChangeItem, User pUser) throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        if (pUser.isAdministrator()) {
            return pUser;
        }
        if (pChangeItem.getACL() == null) {
            return userManager.checkWorkspaceWriteAccess(pChangeItem.getWorkspaceId());
        } else if (pChangeItem.getACL().hasWriteAccess(pUser)) {
            return pUser;
        } else {
            throw new AccessRightException(new Locale(pUser.getLanguage()), pUser);
        }
    }

    private User checkChangeItemReadAccess(ChangeItem pChangeItem, User pUser) throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        if (pUser.isAdministrator() ||
                pChangeItem.getACL() == null ||
                pChangeItem.getACL().hasReadAccess(pUser)) {
            return pUser;
        } else {
            throw new AccessRightException(new Locale(pUser.getLanguage()), pUser);
        }
    }

    private User checkMilestoneWriteAccess(Milestone pMilestone, User pUser) throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        if (pUser.isAdministrator()) {
            return pUser;
        }
        if (pMilestone.getACL() == null) {
            return userManager.checkWorkspaceWriteAccess(pMilestone.getWorkspaceId());
        } else if (pMilestone.getACL().hasWriteAccess(pUser)) {
            return pUser;
        } else {
            throw new AccessRightException(new Locale(pUser.getLanguage()), pUser);
        }
    }

    private User checkMilestoneReadAccess(Milestone pMilestone, User pUser) throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        if (pUser.isAdministrator() ||
                pMilestone.getACL() == null ||
                pMilestone.getACL().hasReadAccess(pUser)) {
            return pUser;
        } else {
            throw new AccessRightException(new Locale(pUser.getLanguage()), pUser);
        }
    }

    private ChangeRequest filterLinkedChangeIssues(ChangeRequest changeRequest, User user) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        em.detach(changeRequest);
        Set<ChangeIssue> addressedChangeIssues = changeRequest.getAddressedChangeIssues();
        Set<ChangeIssue> visibleChangeIssues = new HashSet<>();
        for (ChangeIssue changeIssue : addressedChangeIssues) {
            try {
                checkChangeItemReadAccess(changeIssue, user);
                visibleChangeIssues.add(changeIssue);
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
        Set<ChangeRequest> visibleChangeRequests = new HashSet<>();
        for (ChangeRequest changeRequest : allChangeRequests) {
            try {
                checkChangeItemReadAccess(changeRequest, user);
                visibleChangeRequests.add(filterLinkedChangeIssues(changeRequest, user));
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

            if (docKey.getIteration() > 0) {
                iteration = documentRevision.getIteration(docKey.getIteration());
            } else {
                iteration = documentRevision.getLastCheckedInIteration();
            }

            if (iteration != null) {
                documentIterations.add(iteration);
            }
        }
        return documentIterations;
    }

}