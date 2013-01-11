/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.common.BinaryResource;
import org.dozer.DozerConverter;

import java.util.Date;

/**
 * @author Florent Garin
 */
public class BinaryResourceToStringDozerConverter extends DozerConverter<BinaryResource, String> {

    public BinaryResourceToStringDozerConverter() {
        super(BinaryResource.class, String.class);
    }

    @Override
    public String convertTo(BinaryResource source, String destination) {
        return (source != null) ? source.getFullName() : null;
    }

    @Override
    public BinaryResource convertFrom(String source, BinaryResource destination) {
        if (source != null) {
            BinaryResource bin = new BinaryResource();
            bin.setFullName(source);
            return bin;
        } else {
            return null;
        }
    }

}
