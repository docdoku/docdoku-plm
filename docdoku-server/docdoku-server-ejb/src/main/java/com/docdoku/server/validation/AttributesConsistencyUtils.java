package com.docdoku.server.validation;

import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;
import com.docdoku.server.dao.LOVDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by kelto on 15/07/15.
 */
public class AttributesConsistencyUtils {

    public static boolean hasValidChange(List<InstanceAttribute> pAttributes, boolean attributesLocked, List<InstanceAttribute> currentAttrs) {
        if (attributesLocked) {
            //Check attributes haven't changed
            return currentAttrs.size() == pAttributes.size() && checkAttributesEquality(currentAttrs, pAttributes);
        }
        return true;
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
        }
        return true;
    }


    public static void updateAttributes(List<InstanceAttribute> pAttributes, List<InstanceAttribute> currentAttrs) throws NotAllowedException {
        // Update attributes

        for (int i = 0; i < currentAttrs.size(); i++) {
            InstanceAttribute currentAttr = currentAttrs.get(i);

            if (i < pAttributes.size()) {
                InstanceAttribute newAttr = pAttributes.get(i);
                if (currentAttr.getClass() != newAttr.getClass() || newAttr.getClass() == InstanceListOfValuesAttribute.class) {
                    currentAttrs.set(i, newAttr);
                } else {
                    currentAttrs.get(i).setName(newAttr.getName());
                    currentAttrs.get(i).setValue(newAttr.getValue());
                    currentAttrs.get(i).setMandatory(newAttr.isMandatory());
                    currentAttrs.get(i).setLocked(newAttr.isLocked());
                }
            } else {
                //no more attribute to add remove all of them still end of iteration
                currentAttrs.remove(currentAttrs.size() - 1);
            }
        }
        for (int i = currentAttrs.size(); i < pAttributes.size(); i++) {
            InstanceAttribute newAttr = pAttributes.get(i);
            currentAttrs.add(newAttr);
        }
    }
}
