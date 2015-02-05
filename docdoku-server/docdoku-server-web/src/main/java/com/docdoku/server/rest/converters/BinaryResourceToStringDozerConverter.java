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

import com.docdoku.core.common.BinaryResource;
import org.dozer.DozerConverter;

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
