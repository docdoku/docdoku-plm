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

package com.docdoku.server.rest.converters;

import com.docdoku.core.meta.*;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import com.docdoku.server.rest.dto.NameValuePairDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.DozerConverter;
import org.dozer.Mapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Florent Garin
 */
public class InstanceAttributeDozerConverter extends DozerConverter<InstanceAttribute, InstanceAttributeDTO> {

    public InstanceAttributeDozerConverter() {
        super(InstanceAttribute.class, InstanceAttributeDTO.class);
    }


    @Override
    public InstanceAttributeDTO convertTo(InstanceAttribute source, InstanceAttributeDTO bdestination) {
        Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();
        InstanceAttributeDTO.Type type;
        String value = "";
        List<NameValuePairDTO> itemsDTO = null;
        if (source instanceof InstanceBooleanAttribute) {
            type = InstanceAttributeDTO.Type.BOOLEAN;
            value=source.getValue()+"";
        } else if (source instanceof InstanceTextAttribute) {
            type = InstanceAttributeDTO.Type.TEXT;
            value=source.getValue()+"";
        } else if (source instanceof InstanceNumberAttribute) {
            type = InstanceAttributeDTO.Type.NUMBER;
            value=source.getValue()+"";
        } else if (source instanceof InstanceDateAttribute) {
            type = InstanceAttributeDTO.Type.DATE;
            Date date = ((InstanceDateAttribute)source).getDateValue();
            if(date != null) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                value = df.format(date);
            }
        } else if (source instanceof InstanceURLAttribute) {
            type = InstanceAttributeDTO.Type.URL;
            value=source.getValue()+"";
        } else if(source instanceof InstanceListOfValuesAttribute){
            type = InstanceAttributeDTO.Type.LOV;
            value = ((InstanceListOfValuesAttribute) source).getIndexValue()+"";

            List<NameValuePair> items = null;
            items = ((InstanceListOfValuesAttribute) source).getItems();
            itemsDTO = new ArrayList<>();
            for (NameValuePair item : items){
                itemsDTO.add(mapper.map(item, NameValuePairDTO.class));
            }
        }
        else {
            throw new IllegalArgumentException("Instance attribute not supported");
        }
        InstanceAttributeDTO dto = new InstanceAttributeDTO(source.getName(), type, value, source.isMandatory(), source.isLocked());
        if (itemsDTO != null){
            dto.setItems(itemsDTO);
        }
        return dto;
    }

    @Override
    public InstanceAttribute convertFrom(InstanceAttributeDTO source, InstanceAttribute destination) {
        switch(source.getType()){
            case BOOLEAN:
                return new InstanceBooleanAttribute(source.getName(), Boolean.parseBoolean(source.getValue()), source.isMandatory());
            case DATE:
                return new InstanceDateAttribute(source.getName(), new Date(Long.parseLong(source.getValue())), source.isMandatory());
            case NUMBER:
                return new InstanceNumberAttribute(source.getName(), Float.parseFloat(source.getValue()), source.isMandatory());
            case TEXT:
                return new InstanceTextAttribute(source.getName(), source.getValue(), source.isMandatory());
            case URL:
                return new InstanceURLAttribute(source.getName(), source.getValue(), source.isMandatory());
                      
        }
        throw new IllegalArgumentException("Instance attribute not supported");
    }

}
