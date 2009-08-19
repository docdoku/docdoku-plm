/*
 * MDocSearchResult.java
 * 
 * Copyright (c) 2009 Docdoku. All rights reserved.
 * 
 * This file is part of Docdoku.
 * 
 * Docdoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Docdoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Docdoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.core;

import com.docdoku.core.entities.MasterDocument;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class MDocSearchResult {

    private MasterDocument data[] ;
    private int sizeOfResults ;
    private int offsetOfChunk ;

    public MDocSearchResult(int offsetOfChunk, int sizeOfChunck, MasterDocument allData[]) {
        this.offsetOfChunk = offsetOfChunk;
        if (offsetOfChunk < allData.length){
            data = new MasterDocument[sizeOfChunck];
        }else{
            data = new MasterDocument[allData.length-offsetOfChunk] ;
        }

        int j = 0 ;
        for (int i = offsetOfChunk ; i < allData.length &&  i < offsetOfChunk + sizeOfChunck ; i++){
            data[j] = allData[i] ;
            j++ ;
        }
        sizeOfResults = allData.length ;
    }

    public MasterDocument[] getData() {
        return data;
    }

    public int getOffsetOfChunk() {
        return offsetOfChunk;
    }

    public int getSizeOfResults() {
        return sizeOfResults;
    }
}
