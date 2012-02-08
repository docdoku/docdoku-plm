/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.docdoku.server.rest.util;

import java.util.*;
import org.dozer.DozerConverter;
import org.dozer.Mapper;
import org.dozer.MapperAware;

/**
 *
 * @author yassinebelouad
 */

public class SetToMapDozerConverter extends DozerConverter<HashSet, Map> implements MapperAware{

    private Mapper mapper;
    
    @Override
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public SetToMapDozerConverter() {
        super(HashSet.class, Map.class);
    }

    @Override
    public Map convertTo(HashSet source, Map destination) {
    Map originalToMapped = new HashMap();
    for (Object item : source) {
      String mappedItem = mapper.map(item, String.class);
      originalToMapped.put(item, mappedItem);
    }
    return originalToMapped;
    }

    @Override
    public HashSet convertFrom(Map b, HashSet a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}