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

package com.docdoku.server.rest;


import java.io.File;

/**
 * @author Julien Maffre
 */
public class Range {

    private long min;
    private long max;

    public Range(String rangeFromHttpHeader) {
        parse(rangeFromHttpHeader);
    }

    Range(long minRange, long maxRange) {
        this.min = minRange;
        this.max = maxRange;
    }

    private void parse(String pRange) {

        //we assume that 'bytes=' is always on pRange (according to the HTTP/1.1 spec)
        String range = pRange.substring(6);
        String[] ranges = range.split("-");
        if (range.matches("[0-9]+-[0-9]+")) {
            System.out.println("match all");
            this.min = Math.abs(Long.parseLong(ranges[0]));
            this.max = Math.abs(Long.parseLong(ranges[1]));
        } else if (range.matches("[0-9]+-")) {
            System.out.println("match min");
            this.min = Math.abs(Long.parseLong(ranges[0]));
            this.max = -1;
        } else if (range.matches("-[0-9]+")) {
            System.out.println("match max");
            this.min = -1;
            this.max = Math.abs(Long.parseLong(ranges[1]));
        }
    }

    public long getMax() {
        return max;
    }

    public long getMin() {
        return min;
    }

    public int getlengthOfTheBytesRange() {
        return (int) (this.max - this.min + 1);
    }


    public static Range validateRangeWithFile(Range range, File file) throws RequestedRangeNotSatisfiableException {

        // Get the size of the file
        long lengthOfFile = file.length();

        long minRange;
        long maxRange;

        if (range.getMin() == -1 && range.getMax() != -1) {
            // bytes=-499 : the last 500 bytes
            // not implemented
            throw new RequestedRangeNotSatisfiableException();
        } else if (range.getMin() != -1 && range.getMax() == -1) {
            // bytes=499- : from 500th bytes to the end
            minRange = range.getMin();
            maxRange = lengthOfFile - 1;
        } else if (range.getMin() != -1 && range.getMax() != -1) {
            // bytes=499-999 : from 500th bytes to the 1000th bytes
            minRange = range.getMin();
            maxRange = range.getMax();
        } else {
            throw new RequestedRangeNotSatisfiableException();
        }

        if (minRange > maxRange) {
            throw new RequestedRangeNotSatisfiableException();
        }

        if (minRange >= lengthOfFile) {
            throw new RequestedRangeNotSatisfiableException();
        }

        if (maxRange >= lengthOfFile) {
            maxRange = (int) lengthOfFile - 1;
        }

        return new Range(minRange, maxRange);
    }

}
