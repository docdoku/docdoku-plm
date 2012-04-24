/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.server.rest.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.dozer.DozerConverter;
import org.dozer.Mapper;
import org.dozer.MapperAware;

/**
 *
 * @author Yassine Belouad
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
