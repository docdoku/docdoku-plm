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

package com.docdoku.cli.helpers;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class JSONProgressMonitorInputStream extends FilterInputStream {

    private long maximum;
    private long totalRead;
    private int oldPercentage=-1;
    private PrintStream OUTPUT_STREAM = System.out;

    public JSONProgressMonitorInputStream(long maximum, InputStream in){
        super(in);
        this.maximum=maximum;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int length =  super.read(b, 0, b.length);
        totalRead += length;
        int percentage = (int)((totalRead * 100.0f) / maximum);

        if(percentage > oldPercentage) {
            OUTPUT_STREAM.println("{\"progress\":" + percentage + "}");
        }

        oldPercentage = percentage ;
        return length;
    }


}
