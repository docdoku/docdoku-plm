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

import com.docdoku.core.common.User;
import com.docdoku.core.meta.Tag;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Subscription based on tag and applicable to a {@link User}.
 * Thus each time an item (document, part...)
 * is tagged or untagged with the monitored tag a notification is sent
 * to the user.
 *
 * In addition to these notifications, optionally the user can also
 * be informed when the targeted item has changed (a new iteration has been
 * created) and/or its state has evolved.
 * 
 * @author Florent Garin
 * @version 2.5, 06/09/16
 * @since   V2.5
 */
@Table(name="TAGUSERSUBSCRIPTION")
@IdClass(TagUserSubscriptionKey.class)
@NamedQueries({
        @NamedQuery(name="TagUserSubscription.findIterationChangeSubscribersByTags", query="SELECT distinct(u) FROM TagUserSubscription s JOIN s.userSubscriber u WHERE s.tag.workspaceId = :workspaceId AND s.onIterationChange = true AND s.tag.label IN :tags"),
        @NamedQuery(name="TagUserSubscription.findStateChangeSubscribersByTags", query="SELECT distinct(u) FROM TagUserSubscription s JOIN s.userSubscriber u WHERE s.tag.workspaceId = :workspaceId AND s.onStateChange = true AND s.tag.label IN :tags"),
        @NamedQuery(name="TagUserSubscription.findSubscribersByTags", query="SELECT distinct(u) FROM TagUserSubscription s JOIN s.userSubscriber u WHERE s.tag.workspaceId = :workspaceId AND s.tag.label IN :tags"),
        @NamedQuery(name="TagUserSubscription.findTagUserSubscriptionsByUser", query="SELECT s FROM TagUserSubscription s WHERE s.userSubscriber = :userSubscriber"),
        @NamedQuery(name="TagUserSubscription.deleteTagUserSubscriptionsFromTag", query="DELETE FROM TagUserSubscription s WHERE s.tag = :tag"),
        @NamedQuery(name="TagUserSubscription.deleteTagUserSubscriptionsFromUser", query="DELETE FROM TagUserSubscription s WHERE s.userSubscriber = :userSubscriber")
})
@Entity
public class TagUserSubscription implements Serializable{


    @Id
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name="SUBSCRIBER_LOGIN", referencedColumnName="LOGIN"),
            @JoinColumn(name="SUBSCRIBER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private User userSubscriber;

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


    public TagUserSubscription() {
    }


    public TagUserSubscription(Tag pTag, User pSubscriber){
        this(pTag, pSubscriber, false, false);
    }

    public TagUserSubscription(Tag pTag, User pSubscriber, boolean pOnIterationChange, boolean pOnStateChange){
        setTag(pTag);
        setUserSubscriber(pSubscriber);
        setOnIterationChange(pOnIterationChange);
        setOnStateChange(pOnStateChange);
    }

    public void setTag(Tag pTag) {
        this.tag = pTag;
    }

    public Tag getTag() {
        return tag;
    }

    public User getUserSubscriber() {
        return userSubscriber;
    }

    public void setUserSubscriber(User userSubscriber) {
        this.userSubscriber = userSubscriber;
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
