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

import com.docdoku.core.common.UserKey;
import com.docdoku.core.meta.TagKey;

import java.io.Serializable;

/**
 * @author Florent Garin on 12/09/16.
 */
public class TagUserSubscriptionKey implements Serializable{

    private UserKey userSubscriber;
    private TagKey tag;

    public TagUserSubscriptionKey(){

    }

    public TagUserSubscriptionKey(String pWorkspaceId, String pLogin, String pLabel) {
        this(new UserKey(pWorkspaceId, pLogin), new TagKey(pWorkspaceId, pLabel));
    }

    public TagUserSubscriptionKey(UserKey userSubscriber, TagKey tag) {
        this.userSubscriber = userSubscriber;
        this.tag = tag;
    }

    public UserKey getUserSubscriber() {
        return userSubscriber;
    }

    public void setUserSubscriber(UserKey userSubscriber) {
        this.userSubscriber = userSubscriber;
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

        TagUserSubscriptionKey that = (TagUserSubscriptionKey) o;

        if (userSubscriber != null ? !userSubscriber.equals(that.userSubscriber) : that.userSubscriber != null) return false;
        return !(tag != null ? !tag.equals(that.tag) : that.tag != null);

    }

    @Override
    public int hashCode() {
        int result = userSubscriber != null ? userSubscriber.hashCode() : 0;
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }
}
