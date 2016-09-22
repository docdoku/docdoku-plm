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
package com.docdoku.server.listeners.notifications;


import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.INotificationManagerLocal;
import com.docdoku.server.events.*;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

/**
 * @author Florent Garin
 */
@Named
@RequestScoped
public class SubscriptionManager {

    @Inject
    private INotificationManagerLocal notificationService;

    @Inject
    private IMailerLocal mailer;

    private void onRemoveTag(@Observes @Removed TagEvent event) throws UserNotFoundException, AccessRightException, UserNotActiveException, TagNotFoundException, WorkspaceNotFoundException {
        Tag tag = event.getObservedTag();
        notificationService.removeAllTagSubscriptions(tag.getWorkspaceId(),tag.getLabel());
    }

    private void onRemoveUser(@Observes @Removed UserEvent event) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException {
        User user=event.getObservedUser();
        notificationService.removeAllSubscriptions(user.getWorkspaceId(), user.getLogin());
        notificationService.removeAllTagUserSubscriptions(user.getWorkspaceId(), user.getLogin());
    }

    private void onRemoveUserGroup(@Observes @Removed UserGroupEvent event) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, UserGroupNotFoundException {
        UserGroup group=event.getObservedUserGroup();
        notificationService.removeAllTagUserGroupSubscriptions(group.getWorkspaceId(), group.getId());
    }

    private void onTagItem(@Observes @Tagged TagEvent event){
        Tag t = event.getObservedTag();
        Collection<User> subscribers = notificationService.getSubscribersForTag(t.getWorkspaceId(), t.getLabel());
        DocumentRevision doc =  event.getTaggableDocument();
        PartRevision part = event.getTaggablePart();
        if(doc !=null)
            mailer.sendTaggedNotification(subscribers, doc, event.getObservedTag());
        else if(part !=null)
            mailer.sendTaggedNotification(subscribers, part, event.getObservedTag());
    }

    private void onUntagItem(@Observes @Untagged TagEvent event){
        Tag t = event.getObservedTag();
        Collection<User> subscribers = notificationService.getSubscribersForTag(t.getWorkspaceId(),t.getLabel());
        DocumentRevision doc =  event.getTaggableDocument();
        PartRevision part = event.getTaggablePart();
        if(doc !=null)
            mailer.sendUntaggedNotification(subscribers, doc, event.getObservedTag());
        else if(part != null)
            mailer.sendUntaggedNotification(subscribers, part, event.getObservedTag());
    }


}
