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

package com.docdoku.server.rest.util;

import com.docdoku.core.meta.*;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import com.docdoku.server.rest.dto.NameValuePairDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import java.util.ArrayList;
import java.util.List;

/*
 *
 * @author Asmae CHADID on 25/03/15.
 */
public class InstanceAttributeFactory {

    private Mapper mapper;

    public InstanceAttributeFactory() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    public List<InstanceAttribute> createInstanceAttributes(List<InstanceAttributeDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }
        List<InstanceAttribute> data = new ArrayList<>();
        for (InstanceAttributeDTO dto : dtos) {
            data.add(createInstanceAttribute(dto));
        }

        return data;
    }

    public InstanceAttribute createInstanceAttribute(InstanceAttributeDTO dto) {
        InstanceAttribute attr;
        switch (dto.getType()) {
            case BOOLEAN:
                attr = new InstanceBooleanAttribute();
                break;
            case TEXT:
                attr = new InstanceTextAttribute();
                break;
            case NUMBER:
                attr = new InstanceNumberAttribute();
                break;
            case DATE:
                attr = new InstanceDateAttribute();
                break;
            case URL:
                attr = new InstanceURLAttribute();
                break;
            case LOV :
                attr = new InstanceListOfValuesAttribute();
                List<NameValuePairDTO> itemsDTO = dto.getItems();
                List<NameValuePair> items = new ArrayList<>();
                if (itemsDTO!= null){
                    for (NameValuePairDTO itemDTO : itemsDTO){
                        items.add(mapper.map(itemDTO, NameValuePair.class));
                    }
                }
                ((InstanceListOfValuesAttribute) attr).setItems(items);
                break;
            default:
                throw new IllegalArgumentException("Instance attribute not supported");
        }

        attr.setName(dto.getName());
        attr.setValue(dto.getValue());
        attr.setLocked(dto.isLocked());
        attr.setMandatory(dto.isMandatory());
        return attr;
    }
}
