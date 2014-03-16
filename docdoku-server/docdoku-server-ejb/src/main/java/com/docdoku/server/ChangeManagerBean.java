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
package com.docdoku.server;

import com.docdoku.core.change.*;
import com.docdoku.core.common.User;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.IChangeManagerLocal;
import com.docdoku.core.services.IChangeManagerWS;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.ChangeItemDAO;
import com.docdoku.server.dao.LayerDAO;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * @author Florent Garin
 */

@Local(IChangeManagerLocal.class)
@Stateless(name = "ChangeManagerBean")
public class ChangeManagerBean implements IChangeManagerWS, IChangeManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @Resource
    private SessionContext ctx;

    @EJB
    private IUserManagerLocal userManager;

    private final static Logger LOGGER = Logger.getLogger(ChangeManagerBean.class.getName());

    @PostConstruct
    private void init() {
    }


    @RolesAllowed("users")
    @Override
    public void deleteChangeIssue(int pId) throws ChangeIssueNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotFoundException {
        ChangeIssue change = new ChangeItemDAO(em).loadChangeIssue(pId);
        User user = userManager.checkWorkspaceWriteAccess(change.getWorkspaceId());
        new ChangeItemDAO(new Locale(user.getLanguage()),em).deleteChangeItem(change);
    }

    @RolesAllowed("users")
    @Override
    public List<ChangeIssue> getChangeIssues(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new ChangeItemDAO(new Locale(user.getLanguage()), em).findAllChangeIssues(pWorkspaceId);
    }

    @RolesAllowed("users")
    @Override
    public ChangeIssue createChangeIssue(String pWorkspaceId, String name, String description, String initiator, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        ChangeIssue change = new ChangeIssue(user.getWorkspace(), name, user);
        Date now = new Date();
        change.setCreationDate(now);
        change.setDescription(description);
        change.setPriority(priority);
        change.setCategory(category);
        change.setAssignee(em.getReference(User.class, assignee));
        change.setInitiator(initiator);
        new ChangeItemDAO(new Locale(user.getLanguage()), em).createChangeItem(change);
        return change;
    }

    @RolesAllowed("users")
    @Override
    public void deleteChangeRequest(int pId) throws ChangeRequestNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotFoundException {
        ChangeRequest change = new ChangeItemDAO(em).loadChangeRequest(pId);
        User user = userManager.checkWorkspaceWriteAccess(change.getWorkspaceId());
        new ChangeItemDAO(new Locale(user.getLanguage()),em).deleteChangeItem(change);
    }

    @RolesAllowed("users")
    @Override
    public List<ChangeRequest> getChangeRequests(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new ChangeItemDAO(new Locale(user.getLanguage()), em).findAllChangeRequests(pWorkspaceId);
    }

    @RolesAllowed("users")
    @Override
    public ChangeRequest createChangeRequest(String pWorkspaceId, String name, String description, int milestone, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        ChangeRequest change = new ChangeRequest(user.getWorkspace(), name, user);
        Date now = new Date();
        change.setCreationDate(now);
        change.setDescription(description);
        change.setPriority(priority);
        change.setCategory(category);
        change.setAssignee(em.getReference(User.class, assignee));
        change.setMilestone(em.getReference(Milestone.class,milestone));
        new ChangeItemDAO(new Locale(user.getLanguage()), em).createChangeItem(change);
        return change;
    }

    @RolesAllowed("users")
    @Override
    public void deleteChangeOrder(int pId) throws ChangeOrderNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotFoundException {
        ChangeOrder change = new ChangeItemDAO(em).loadChangeOrder(pId);
        User user = userManager.checkWorkspaceWriteAccess(change.getWorkspaceId());
        new ChangeItemDAO(new Locale(user.getLanguage()),em).deleteChangeItem(change);
    }

    @RolesAllowed("users")
    @Override
    public List<ChangeOrder> getChangeOrders(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new ChangeItemDAO(new Locale(user.getLanguage()), em).findAllChangeOrders(pWorkspaceId);
    }

    @RolesAllowed("users")
    @Override
    public ChangeOrder createChangeOrder(String pWorkspaceId, String name, String description, int milestone, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        ChangeOrder change = new ChangeOrder(user.getWorkspace(), name, user);
        Date now = new Date();
        change.setCreationDate(now);
        change.setDescription(description);
        change.setPriority(priority);
        change.setCategory(category);
        change.setAssignee(em.getReference(User.class, assignee));
        change.setMilestone(em.getReference(Milestone.class,milestone));
        new ChangeItemDAO(new Locale(user.getLanguage()), em).createChangeItem(change);
        return change;
    }
}
