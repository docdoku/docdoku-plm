/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.api.models.utils;

import com.docdoku.api.models.*;

/**
 * This class helps to get manipulate attributes
 *
 * @Author Morgan Guimard
 */
public class AttributesHelper {

    /**
     * Create an attribute template
     *
     * @param type      : attribute type
     * @param name      : attribute name
     * @param mandatory : mandatory
     * @param locked    : lock attribute
     * @return an instance attribute template
     */
    public static InstanceAttributeTemplateDTO createInstanceAttributeTemplate(
            InstanceAttributeTemplateDTO.AttributeTypeEnum type, String name, boolean mandatory, boolean locked) {

        InstanceAttributeTemplateDTO attribute = new InstanceAttributeTemplateDTO();
        attribute.setAttributeType(type);
        attribute.setName(name);
        attribute.setMandatory(mandatory);
        attribute.setLocked(locked);
        return attribute;
    }

    /**
     * Create an attribute
     *
     * @param type:  attribute type
     * @param name:  attribute name
     * @param value: attribute value
     * @return an instance attribute
     */
    public static InstanceAttributeDTO createInstanceAttribute(
            InstanceAttributeDTO.TypeEnum type, String name, String value) {

        InstanceAttributeDTO attribute = new InstanceAttributeDTO();
        attribute.setName(name);
        attribute.setType(type);
        attribute.setValue(value);
        return attribute;
    }
}
