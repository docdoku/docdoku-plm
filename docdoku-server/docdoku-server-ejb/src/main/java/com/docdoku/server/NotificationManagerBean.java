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

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.UserGroupKey;
import com.docdoku.core.common.UserKey;
import com.docdoku.core.exceptions.AccessRightException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.exceptions.UserNotFoundException;
import com.docdoku.core.exceptions.WorkspaceNotFoundException;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.meta.TagKey;
import com.docdoku.core.notification.TagUserGroupSubscription;
import com.docdoku.core.notification.TagUserGroupSubscriptionKey;
import com.docdoku.core.notification.TagUserSubscription;
import com.docdoku.core.notification.TagUserSubscriptionKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.INotificationManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.SubscriptionDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author Florent Garin on 07/09/16
 */
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(INotificationManagerLocal.class)
@Stateless(name = "NotificationManagerBean")
public class NotificationManagerBean implements INotificationManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private IUserManagerLocal userManager;



    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public TagUserSubscription subscribeToTagEvent(String pWorkspaceId, String pLabel, boolean pOnIterationChange, boolean pOnStateChange) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        SubscriptionDAO subDAO = new SubscriptionDAO(userLocale, em);
        TagUserSubscription subscription = new TagUserSubscription(
                em.getReference(Tag.class, new TagKey(pWorkspaceId, pLabel)),
                user,
                pOnIterationChange, pOnStateChange);
        return subDAO.saveTagUserSubscription(subscription);
    }


    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void unsubscribeToTagEvent(String pWorkspaceId, String pLabel) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        SubscriptionDAO subDAO = new SubscriptionDAO(userLocale, em);
        subDAO.removeTagUserSubscription(new TagUserSubscriptionKey(pWorkspaceId, user.getLogin(), pLabel));
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public TagUserSubscription createOrUpdateTagUserSubscription(String pWorkspaceId, String pLogin, String pLabel, boolean pOnIterationChange, boolean pOnStateChange) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        // Check if it is the workspace's administrator
        if (user.isAdministrator()) {
            Locale userLocale = new Locale(user.getLanguage());
            SubscriptionDAO subDAO = new SubscriptionDAO(userLocale, em);
            TagUserSubscription subscription = new TagUserSubscription(
                    em.getReference(Tag.class, new TagKey(pWorkspaceId, pLabel)),
                    em.getReference(User.class, new UserKey(pWorkspaceId, pLogin)),
                    pOnIterationChange, pOnStateChange);
            return subDAO.saveTagUserSubscription(subscription);
        } else {
            // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public TagUserGroupSubscription createOrUpdateTagUserGroupSubscription(String pWorkspaceId, String pId, String pLabel, boolean pOnIterationChange, boolean pOnStateChange) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        // Check if it is the workspace's administrator
        if (user.isAdministrator()) {
            Locale userLocale = new Locale(user.getLanguage());
            SubscriptionDAO subDAO = new SubscriptionDAO(userLocale, em);
            TagUserGroupSubscription subscription = new TagUserGroupSubscription(
                    em.getReference(Tag.class, new TagKey(pWorkspaceId, pLabel)),
                    em.getReference(UserGroup.class, new UserKey(pWorkspaceId, pId)),
                    pOnIterationChange, pOnStateChange);
            return subDAO.saveTagUserGroupSubscription(subscription);
        } else {
            // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeTagUserSubscription(String pWorkspaceId, String pLogin, String pLabel) throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        // Check if it is the workspace's administrator
        if (user.isAdministrator()) {
            Locale userLocale = new Locale(user.getLanguage());
            SubscriptionDAO subDAO = new SubscriptionDAO(userLocale, em);
            subDAO.removeTagUserSubscription(new TagUserSubscriptionKey(pWorkspaceId, pLogin, pLabel));
        } else {
            // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeTagUserGroupSubscription(String pWorkspaceId, String pId, String pLabel) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        // Check if it is the workspace's administrator
        if (user.isAdministrator()) {
            Locale userLocale = new Locale(user.getLanguage());
            SubscriptionDAO subDAO = new SubscriptionDAO(userLocale, em);
            subDAO.removeTagUserGroupSubscription(new TagUserGroupSubscriptionKey(pWorkspaceId, pId, pLabel));
        } else {
            // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<TagUserGroupSubscription> getTagUserGroupSubscriptionsByGroup(String pWorkspaceId, String pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        // Check if it is the workspace's administrator
        if (user.isAdministrator()) {
            Locale userLocale = new Locale(user.getLanguage());
            SubscriptionDAO subDAO = new SubscriptionDAO(userLocale, em);
            return subDAO.getTagUserGroupSubscriptionsByGroup(em.getReference(UserGroup.class,new UserGroupKey(pWorkspaceId, pId)));
        } else {
            // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<TagUserSubscription> getTagUserSubscriptionsByUser(String pWorkspaceId, String pLogin) throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        // Check if it is the workspace's administrator
        if (user.isAdministrator()) {
            Locale userLocale = new Locale(user.getLanguage());
            SubscriptionDAO subDAO = new SubscriptionDAO(userLocale, em);
            return subDAO.getTagUserSubscriptionsByUser(em.getReference(User.class,new UserKey(pWorkspaceId, pLogin)));
        } else {
            // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }
}
