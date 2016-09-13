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

import com.docdoku.core.common.UserGroupKey;
import com.docdoku.core.meta.TagKey;

import java.io.Serializable;

/**
 * @author Florent Garin on 12/09/16.
 */
public class TagUserGroupSubscriptionKey implements Serializable{

    private UserGroupKey groupSubscriber;
    private TagKey tag;

    public TagUserGroupSubscriptionKey(){

    }

    public TagUserGroupSubscriptionKey(String pWorkspaceId, String pId, String pLabel) {
        this(new UserGroupKey(pWorkspaceId, pId), new TagKey(pWorkspaceId, pLabel));
    }
    public TagUserGroupSubscriptionKey(UserGroupKey groupSubscriber, TagKey tag) {
        this.groupSubscriber = groupSubscriber;
        this.tag = tag;
    }

    public UserGroupKey getGroupSubscriber() {
        return groupSubscriber;
    }

    public void setGroupSubscriber(UserGroupKey groupSubscriber) {
        this.groupSubscriber = groupSubscriber;
    }

    public TagKey getTag() {
        return tag;
    }

    public void setTag(TagKey tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TagUserGroupSubscriptionKey that = (TagUserGroupSubscriptionKey) o;

        if (groupSubscriber != null ? !groupSubscriber.equals(that.groupSubscriber) : that.groupSubscriber != null) return false;
        return !(tag != null ? !tag.equals(that.tag) : that.tag != null);

    }

    @Override
    public int hashCode() {
        int result = groupSubscriber != null ? groupSubscriber.hashCode() : 0;
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }
}
