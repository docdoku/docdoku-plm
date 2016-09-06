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

import com.docdoku.core.meta.TagKey;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Identity class of {@link ItemTaggedSubscription} objects.
 *
 * @author Florent Garin
 */
@Embeddable
public class ItemTaggedSubscriptionKey implements Serializable {

    private TagKey tag;

    public ItemTaggedSubscriptionKey() {
    }

    public ItemTaggedSubscriptionKey(TagKey pTagKey) {
        tag=pTagKey;
    }

    public ItemTaggedSubscriptionKey(String pWorkspaceId, String pLabel) {
        tag=new TagKey(pWorkspaceId,pLabel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemTaggedSubscriptionKey that = (ItemTaggedSubscriptionKey) o;

        return !(tag != null ? !tag.equals(that.tag) : that.tag != null);

    }

    @Override
    public int hashCode() {
        return tag != null ? tag.hashCode() : 0;
    }


    @Override
    public String toString() {
        return tag!=null?tag.toString():"";
    }
}

