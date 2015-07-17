package com.docdoku.server.validation;

import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;
import com.docdoku.server.dao.LOVDAO;

import java.util.*;

/**
 * Created by kelto on 15/07/15.
 */
public class AttributesConsistencyUtils {

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
