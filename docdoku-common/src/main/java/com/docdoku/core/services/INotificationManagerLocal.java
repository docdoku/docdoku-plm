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


import com.docdoku.core.common.User;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.notification.TagUserGroupSubscription;
import com.docdoku.core.notification.TagUserSubscription;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Florent Garin
 */
public interface INotificationManagerLocal {

    TagUserSubscription subscribeToTagEvent(String pWorkspaceId, String pLabel, boolean pOnIterationChange, boolean pOnStateChange) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, TagNotFoundException;
    void unsubscribeToTagEvent(String pWorkspaceId, String pLabel) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    TagUserSubscription createOrUpdateTagUserSubscription(String pWorkspaceId, String pLogin, String pLabel, boolean pOnIterationChange, boolean pOnStateChange) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, TagNotFoundException;
    void removeTagUserSubscription(String pWorkspaceId, String pLogin, String pLabel) throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    void removeAllTagSubscriptions(String pWorkspaceId, String pLabel) throws TagNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    void removeAllTagUserSubscriptions(String pWorkspaceId, String pLogin) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException;
    void removeAllTagUserGroupSubscriptions(String pWorkspaceId, String pGroupId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, UserGroupNotFoundException, AccessRightException;

    TagUserGroupSubscription createOrUpdateTagUserGroupSubscription(String pWorkspaceId, String pId, String pLabel, boolean pOnIterationChange, boolean pOnStateChange) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, TagNotFoundException, UserGroupNotFoundException;
    void removeTagUserGroupSubscription(String pWorkspaceId, String pId, String pLabel) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException;

    List<TagUserGroupSubscription> getTagUserGroupSubscriptionsByGroup(String pWorkspaceId, String pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, UserGroupNotFoundException;
    List<TagUserSubscription> getTagUserSubscriptionsByUser(String pWorkspaceId, String pLogin) throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    Collection<User> getSubscribersForTag(String pWorkspaceId, String pLabel);
}
