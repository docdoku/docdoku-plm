/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.docdoku.server.rest.util;

import com.docdoku.core.workflow.Activity;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import org.dozer.DozerConverter;
import org.dozer.Mapper;
import org.dozer.MapperAware;

/**
 *
 * @author yassinebelouad
 */
public class VectorToListDozerConverter extends DozerConverter<Vector, List> implements MapperAware{

    private Mapper mapper;
    
    @Override
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }
    
    public VectorToListDozerConverter() {
        super(Vector.class, List.class);
    }
    
    @Override
    public List convertTo(Vector source, List destination) {
        List originalToMapped = new ArrayList();
        return originalToMapped;
    }

    @Override
    public Vector convertFrom(List b, Vector a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
