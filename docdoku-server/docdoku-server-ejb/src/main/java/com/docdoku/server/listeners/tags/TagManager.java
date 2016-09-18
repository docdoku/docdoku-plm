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
package com.docdoku.server.listeners.tags;


import com.docdoku.core.common.User;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.INotificationManagerLocal;
import com.docdoku.server.events.Removed;
import com.docdoku.server.events.TagEvent;
import com.docdoku.server.events.Tagged;
import com.docdoku.server.events.Untagged;

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
public class TagManager {

    @Inject
    private INotificationManagerLocal notificationService;

    @Inject
    private IMailerLocal mailer;

    private void onRemoveTag(@Observes @Removed TagEvent event) throws UserNotFoundException, AccessRightException, UserNotActiveException, TagNotFoundException, WorkspaceNotFoundException {
        Tag tag = event.getObservedTag();
        notificationService.removeAllTagSubscriptions(tag.getWorkspaceId(),tag.getLabel());
    }

    private void onTagItem(@Observes @Tagged TagEvent event){
        Tag t = event.getObservedTag();
        Collection<User> subscribers = notificationService.getSubscribersForTag(t.getWorkspaceId(),t.getLabel());
        mailer.sendTaggedNotification(subscribers, event.getTaggableDocument(), event.getObservedTag());
    }

    private void onUntagItem(@Observes @Untagged TagEvent event){
        Tag t = event.getObservedTag();
        Collection<User> subscribers = notificationService.getSubscribersForTag(t.getWorkspaceId(),t.getLabel());
        mailer.sendUntaggedNotification(subscribers, event.getTaggableDocument(), event.getObservedTag());
    }


}
