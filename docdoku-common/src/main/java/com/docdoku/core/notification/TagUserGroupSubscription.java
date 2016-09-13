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

package com.docdoku.core.notification;

import com.docdoku.core.common.UserGroup;
import com.docdoku.core.meta.Tag;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Subscription based on tag and applicable to a {@link UserGroup}.
 * Thus each time an item (document, part...)
 * is tagged or untagged with the monitored tag a notification is sent
 * to all members of the user group.
 *
 * In addition to these notifications, optionally the group can also
 * be informed when the targeted item has changed (a new iteration has been
 * created) and/or its state has evolved.
 * 
 * @author Florent Garin
 * @version 2.5, 06/09/16
 * @since   V2.5
 */
@Table(name="TAGUSERGROUPSUBSCRIPTION")
@IdClass(TagUserGroupSubscriptionKey.class)
@NamedQueries({
        @NamedQuery(name="TagUserGroupSubscription.findIterationChangeSubscribersByTags", query="SELECT distinct(u) FROM TagUserGroupSubscription s JOIN s.groupSubscriber g JOIN g.users u WHERE s.tag.workspaceId = :workspaceId AND s.onIterationChange = true AND s.tag.label IN (:tags)"),
        @NamedQuery(name="TagUserGroupSubscription.findStateChangeSubscribersByTags", query="SELECT distinct(u) FROM TagUserGroupSubscription s JOIN s.groupSubscriber g JOIN g.users u WHERE s.tag.workspaceId = :workspaceId AND s.onStateChange = true AND s.tag.label IN (:tags)"),
        @NamedQuery(name="TagUserGroupSubscription.findTagUserGroupSubscriptionsByGroup", query="SELECT s FROM TagUserGroupSubscription s WHERE s.groupSubscriber = :groupSubscriber")
})
@Entity
public class TagUserGroupSubscription implements Serializable{


    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "MEMBER_ID", referencedColumnName = "ID"),
            @JoinColumn(name = "MEMBER_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private UserGroup groupSubscriber;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name="TAG_LABEL", referencedColumnName="LABEL"),
            @JoinColumn(name="TAG_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
        }
    )
    private Tag tag;

    private boolean onIterationChange;
    private boolean onStateChange;


    public TagUserGroupSubscription() {
    }


    public TagUserGroupSubscription(Tag pTag, UserGroup pSubscriber){
        this(pTag, pSubscriber, false, false);
    }

    public TagUserGroupSubscription(Tag pTag, UserGroup pSubscriber, boolean pOnIterationChange, boolean pOnStateChange){
        setTag(pTag);
        setGroupSubscriber(pSubscriber);
        setOnIterationChange(pOnIterationChange);
        setOnStateChange(pOnStateChange);
    }

    public void setTag(Tag pTag) {
        this.tag = pTag;
    }

    public Tag getTag() {
        return tag;
    }

    public UserGroup getGroupSubscriber() {
        return groupSubscriber;
    }

    public void setGroupSubscriber(UserGroup groupSubscriber) {
        this.groupSubscriber = groupSubscriber;
    }

    public void setOnIterationChange(boolean onIterationChange) {
        this.onIterationChange = onIterationChange;
    }

    public boolean isOnIterationChange() {
        return onIterationChange;
    }

    public void setOnStateChange(boolean onStateChange) {
        this.onStateChange = onStateChange;
    }

    public boolean isOnStateChange() {
        return onStateChange;
    }


}
