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

package com.docdoku.gwt.explorer.shared;

import java.io.Serializable;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public abstract class ExplorerServiceResponse<T extends Serializable> implements Serializable{
    
    private int totalSize ;
    private int chunckOffset ;
    private T data[] ;

    public ExplorerServiceResponse() {
    }

    public T[] getData() {
        return data;
    }

    public void setData(T[] data) {
        this.data = data;
    }

    public void setChunckOffset(int chunckOffset) {
        this.chunckOffset = chunckOffset;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }


    public int getChunckOffset() {
        return chunckOffset;
    }
    
    public int getTotalSize() {
        return totalSize;
    }
}
