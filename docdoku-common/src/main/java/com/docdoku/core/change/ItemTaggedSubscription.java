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

package com.docdoku.core.change;

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.meta.Tag;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Subscription on the event triggered each time an item (document, part...) is tagged with
 * the monitored tag.
 * 
 * @author Florent Garin
 * @version 2.5, 06/09/16
 * @since   V2.5
 */
@Table(name="ITEMTAGGEDSUBSCRIPTION")
@Entity
public class ItemTaggedSubscription implements Serializable{



    @ManyToMany
    @JoinTable(name="TAG_USERSUBSCRIBER",
            inverseJoinColumns={
                    @JoinColumn(name="USER_LOGIN", referencedColumnName="LOGIN"),
                    @JoinColumn(name="USER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
            },
            joinColumns={
                    @JoinColumn(name="TAG_LABEL", referencedColumnName="TAG_LABEL"),
                    @JoinColumn(name="TAG_WORKSPACE_ID", referencedColumnName="TAG_WORKSPACE_ID")
            })
    private Set<User> userSubscribers=new HashSet<>();


    @ManyToMany
    @JoinTable(name="TAG_GROUPSUBSCRIBER",
            inverseJoinColumns={
                    @JoinColumn(name="USERGROUP_ID", referencedColumnName="ID"),
                    @JoinColumn(name="USERGROUP_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
            },
            joinColumns={
                    @JoinColumn(name="TAG_LABEL", referencedColumnName="TAG_LABEL"),
                    @JoinColumn(name="TAG_WORKSPACE_ID", referencedColumnName="TAG_WORKSPACE_ID")
            })
    private Set<UserGroup> groupSubscribers=new HashSet<>();


    @EmbeddedId
    private ItemTaggedSubscriptionKey id;

    @MapsId
    @JoinColumns({
            @JoinColumn(name="TAG_LABEL", referencedColumnName="LABEL"),
            @JoinColumn(name="TAG_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
        }
    )
    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true, optional = false)
    private Tag tag;




    public ItemTaggedSubscription() {
    }

    public ItemTaggedSubscription(Tag pTag, Set<User> pUserSubscribers, Set<UserGroup> pGroupSubscribers){
        this.tag=pTag;
        setUserSubscribers(pUserSubscribers);
        setGroupSubscribers(pGroupSubscribers);
        setTag(pTag);
    }

    public void setTag(Tag pTag) {
        this.tag = pTag;
        this.id=new ItemTaggedSubscriptionKey(pTag.getWorkspaceId(), pTag.getLabel());
    }

    public Tag getTag() {
        return tag;
    }

    public Set<User> getUserSubscribers() {
        return userSubscribers;
    }

    public void setUserSubscribers(Set<User> userSubscribers) {
        this.userSubscribers = userSubscribers;
    }

    public void setGroupSubscribers(Set<UserGroup> groupSubscribers) {
        this.groupSubscribers = groupSubscribers;
    }

    public Set<UserGroup> getGroupSubscribers() {
        return groupSubscribers;
    }

    public boolean addUserSubscriber(User pUser) {
        if(userSubscribers!=null)
            return userSubscribers.add(pUser);
        else
            return false;
    }

    public boolean removeUserSubscriber(User pUser) {
        if(userSubscribers!=null)
            return userSubscribers.remove(pUser);
        else
            return false;
    }

    public boolean removeGroupSubscriber(UserGroup pGroup) {
        if(groupSubscribers!=null)
            return groupSubscribers.remove(pGroup);
        else
            return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemTaggedSubscription that = (ItemTaggedSubscription) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
