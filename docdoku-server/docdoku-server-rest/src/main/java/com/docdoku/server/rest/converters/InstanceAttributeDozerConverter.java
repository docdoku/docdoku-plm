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

package com.docdoku.server.rest.converters;

import com.docdoku.core.meta.*;
import com.docdoku.core.product.InstancePartNumberAttribute;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.util.DateUtils;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import com.docdoku.server.rest.dto.NameValuePairDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.DozerConverter;
import org.dozer.Mapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Florent Garin
 */
public class InstanceAttributeDozerConverter extends DozerConverter<InstanceAttribute, InstanceAttributeDTO> {

    private Mapper mapper;

    public InstanceAttributeDozerConverter() {
        super(InstanceAttribute.class, InstanceAttributeDTO.class);
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }


    @Override
    public InstanceAttributeDTO convertTo(InstanceAttribute source, InstanceAttributeDTO dto) {
        if(dto==null)
            dto=new InstanceAttributeDTO();

        InstanceAttributeDTO.Type type;
        String value = "";

        if (source instanceof InstanceBooleanAttribute) {
            type = InstanceAttributeDTO.Type.BOOLEAN;
            value = source.getValue() + "";
        } else if (source instanceof InstanceTextAttribute) {
            type = InstanceAttributeDTO.Type.TEXT;
            value = source.getValue() + "";
        } else if (source instanceof InstanceNumberAttribute) {
            type = InstanceAttributeDTO.Type.NUMBER;
            value = source.getValue() + "";
        } else if (source instanceof InstanceDateAttribute) {
            type = InstanceAttributeDTO.Type.DATE;
            Date date = ((InstanceDateAttribute) source).getDateValue();
            if (date != null) {
                value = DateUtils.format(date);
            }
        } else if (source instanceof InstanceURLAttribute) {
            type = InstanceAttributeDTO.Type.URL;
            value = source.getValue() + "";
        } else if (source instanceof InstanceListOfValuesAttribute) {
            type = InstanceAttributeDTO.Type.LOV;
            value = ((InstanceListOfValuesAttribute) source).getIndexValue() + "";

            List<NameValuePair> items = ((InstanceListOfValuesAttribute) source).getItems();
            List<NameValuePairDTO> itemsDTO = new ArrayList<>();
            for (NameValuePair item : items) {
                itemsDTO.add(mapper.map(item, NameValuePairDTO.class));
            }
            dto.setItems(itemsDTO);
        } else if (source instanceof InstanceLongTextAttribute) {
            type = InstanceAttributeDTO.Type.LONG_TEXT;
            value = source.getValue() + "";
        } else if (source instanceof InstancePartNumberAttribute) {
            type = InstanceAttributeDTO.Type.PART_NUMBER;
            InstancePartNumberAttribute ipna=(InstancePartNumberAttribute) source;
            value = ipna.getPartMasterValue()==null?"":ipna.getPartMasterValue().getNumber();
        }
        else {
            throw new IllegalArgumentException("Instance attribute not supported");
        }
        dto.setName(source.getName());
        dto.setMandatory(source.isMandatory());
        dto.setLocked(source.isLocked());
        dto.setType(type);
        dto.setValue(value);

        return dto;
    }

    @Override
    public InstanceAttribute convertFrom(InstanceAttributeDTO source, InstanceAttribute destination) {
        InstanceAttribute attr;
        switch (source.getType()) {
            case BOOLEAN:
                attr = new InstanceBooleanAttribute();
                attr.setValue(source.getValue());
                break;
            case TEXT:
                attr = new InstanceTextAttribute();
                attr.setValue(source.getValue());
                break;
            case NUMBER:
                attr = new InstanceNumberAttribute();
                attr.setValue(source.getValue());
                break;
            case DATE:
                attr = new InstanceDateAttribute();
                attr.setValue(source.getValue());
                break;
            case URL:
                attr = new InstanceURLAttribute();
                attr.setValue(source.getValue());
                break;
            case LOV:
                attr = new InstanceListOfValuesAttribute();
                List<NameValuePairDTO> itemsDTO = source.getItems();
                List<NameValuePair> items = new ArrayList<>();
                if (itemsDTO != null) {
                    for (NameValuePairDTO itemDTO : itemsDTO) {
                        items.add(mapper.map(itemDTO, NameValuePair.class));
                    }
                }
                ((InstanceListOfValuesAttribute) attr).setItems(items);
                attr.setValue(source.getValue());
                break;
            case LONG_TEXT:
                attr = new InstanceLongTextAttribute();
                attr.setValue(source.getValue());
                break;
            case PART_NUMBER:
                attr = new InstancePartNumberAttribute();
                attr.setValue(new PartMasterKey(source.getWorkspaceId(),source.getValue()));
                break;
            default:
                throw new IllegalArgumentException("Instance attribute not supported");
        }

        attr.setName(source.getName());
        attr.setLocked(source.isLocked());
        attr.setMandatory(source.isMandatory());
        return attr;
    }

}
