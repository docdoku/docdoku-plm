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

package com.docdoku.server.validation;

import com.docdoku.core.meta.InstanceAttribute;

import java.util.*;

/**
 * Created by kelto on 15/07/15.
 */

public class AttributesConsistencyUtils {

    private AttributesConsistencyUtils() {
    }

    public static boolean hasValidChange(List<InstanceAttribute> pAttributes, boolean attributesLocked, List<InstanceAttribute> currentAttrs) {
        if (attributesLocked) {
            //Check attributes haven't changed
            return currentAttrs.size() == pAttributes.size() && checkAttributesEquality(currentAttrs, pAttributes);
        } else {
            return lockedAttributesConsistency(currentAttrs,pAttributes);
        }
    }

    private static boolean checkAttributesEquality(List<InstanceAttribute> currentAttrs, List<InstanceAttribute> pAttributes) {
        for (int i = 0; i < currentAttrs.size(); i++) {
            InstanceAttribute currentAttr = currentAttrs.get(i);
            InstanceAttribute newAttr = pAttributes.get(i);
            if (newAttr == null
                    || !newAttr.getName().equals(currentAttr.getName())
                    || !newAttr.getClass().equals(currentAttr.getClass())) {
                // Attribute has been swapped with a new attributes or his type has changed
                return false;
            }
            if(!checkValidAttribute(newAttr)){
                return false;
            }
        }
        return true;
    }

    private static boolean checkValidAttribute(InstanceAttribute newAttr) {
        if(newAttr.isMandatory()) {
            if(!newAttr.isLocked()) {
                // An attribute mandatory must be locked
                return false;
            }
            if(newAttr.getValue() == null || newAttr.getValue().toString().isEmpty()) {
                return false;
            }
        }
        // once an attribute properties has been defined, it can not be changed.
        //return newAttr.isLocked() == currentAttr.isLocked() && newAttr.isMandatory() == currentAttr.isMandatory();
        return true;
    }

    private static boolean lockedAttributesConsistency(List<InstanceAttribute> currentAttrs, List<InstanceAttribute> pAttributes) {
        Map<String,List<InstanceAttribute>> pMapAttributes = getMappedAttributes(pAttributes);
        if(pMapAttributes == null) {
            // the map was not constructed because an attribute was not valid.
            return false;
        }
        for(InstanceAttribute attribute : currentAttrs) {
            if(attribute.isLocked()) {
                List<InstanceAttribute> attributes = pMapAttributes.get(attribute.getName());
                if(!existSameProperties(attribute,attributes)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean existSameProperties(InstanceAttribute attribute, List<InstanceAttribute> attributes) {
        for(Iterator<InstanceAttribute> iterator = attributes.iterator(); iterator.hasNext();) {
            InstanceAttribute attr = iterator.next();
            if(attribute.isLocked() == attr.isLocked()
                    && attribute.isMandatory() == attr.isMandatory()) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    private static Map<String,List<InstanceAttribute>> getMappedAttributes(List<InstanceAttribute> attributes) {
        Map<String,List<InstanceAttribute>> map = new HashMap<>();
        for(InstanceAttribute attribute : attributes) {
            if(!checkValidAttribute(attribute)) {
                // if an attribute is not valid, we return null since the map is not valid.
                return null;
            }
            if(map.get(attribute.getName()) == null) {
                map.put(attribute.getName(), new ArrayList<>());
            }
            map.get(attribute.getName()).add(attribute);
        }

        return map;
    }

}
