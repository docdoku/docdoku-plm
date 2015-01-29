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

import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import org.dozer.DozerConverter;
import org.dozer.Mapper;
import org.dozer.MapperAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Yassine Belouad
 */
public class MapToListDozerConverter extends DozerConverter<Map, List> implements MapperAware {

    private Mapper mapper;

    @Override
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public MapToListDozerConverter() {
        super(Map.class, List.class);
    }

    @Override
    public List convertTo(Map source, List destination) {
        if (source==null) {
            return null;
        }
        
        List<InstanceAttributeDTO> convertedList = new ArrayList<>();
        for (Object object : source.values()) {
            InstanceAttributeDTO mappedItem = mapper.map(object, InstanceAttributeDTO.class);
            convertedList.add(mappedItem);
        }
        return convertedList;
    }

    @Override
    public Map convertFrom(List source, Map destination) {
        if (source==null) {
            return null;
        }
        
        Map<String, InstanceAttribute> convertedMap = new HashMap<>();
        for (Object object : source) {
            InstanceAttribute mappedItem = mapper.map(object, InstanceAttribute.class);
            convertedMap.put(mappedItem.getName(), mappedItem);
        }
        return convertedMap;
    }
}