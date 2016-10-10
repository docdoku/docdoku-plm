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
import com.docdoku.core.product.PartNumberAttributeTemplate;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import com.docdoku.server.rest.dto.InstanceAttributeTemplateDTO;
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
public class InstanceAttributeTemplateDozerConverter extends DozerConverter<InstanceAttributeTemplate, InstanceAttributeTemplateDTO> {

    private Mapper mapper;

    public InstanceAttributeTemplateDozerConverter() {
        super(InstanceAttributeTemplate.class, InstanceAttributeTemplateDTO.class);
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }


    @Override
    public InstanceAttributeTemplateDTO convertTo(InstanceAttributeTemplate instanceAttributeTemplate, InstanceAttributeTemplateDTO dto) {
        if(dto==null)
            dto=new InstanceAttributeTemplateDTO();

        dto.setLocked(instanceAttributeTemplate.isLocked());
        dto.setName(instanceAttributeTemplate.getName());
        dto.setMandatory(instanceAttributeTemplate.isMandatory());
        if(instanceAttributeTemplate instanceof DefaultAttributeTemplate){
            DefaultAttributeTemplate defaultIA = (DefaultAttributeTemplate)instanceAttributeTemplate;
            dto.setAttributeType(InstanceAttributeTemplateDTO.AttributeType.valueOf(defaultIA.getAttributeType().name()));

        }else if(instanceAttributeTemplate instanceof ListOfValuesAttributeTemplate){
            ListOfValuesAttributeTemplate lovIA=(ListOfValuesAttributeTemplate)instanceAttributeTemplate;
            dto.setLovName(lovIA.getLovName());
            dto.setAttributeType(InstanceAttributeTemplateDTO.AttributeType.LOV);
        }else if(instanceAttributeTemplate instanceof PartNumberAttributeTemplate){
            dto.setAttributeType(InstanceAttributeTemplateDTO.AttributeType.PART_NUMBER);
        }
        return dto;
    }

    @Override
    public InstanceAttributeTemplate convertFrom(InstanceAttributeTemplateDTO dto, InstanceAttributeTemplate instanceAttributeTemplate) {

        InstanceAttributeTemplate data;
        if (InstanceAttributeTemplateDTO.AttributeType.LOV.equals(dto.getAttributeType())) {
            data = new ListOfValuesAttributeTemplate();
        }else if (InstanceAttributeTemplateDTO.AttributeType.PART_NUMBER.equals(dto.getAttributeType())){
            data = new PartNumberAttributeTemplate();
        }
        else {
            DefaultAttributeTemplate defaultIA = new DefaultAttributeTemplate();
            defaultIA.setAttributeType(DefaultAttributeTemplate.AttributeType.valueOf(dto.getAttributeType().name()));
            data = defaultIA;
        }

        data.setName(dto.getName());
        data.setMandatory(dto.isMandatory());
        data.setLocked(dto.isLocked());
        return data;


    }
}
