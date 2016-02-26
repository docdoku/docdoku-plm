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

import org.apache.commons.io.FileUtils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class ConsoleProgressMonitorInputStream extends FilterInputStream {

    private long maximum;
    private long totalRead;
    private PrintStream OUTPUT_STREAM = System.out;
    private int rotationChar;

    private static final char[] ROTATION = {'|','|','|','|','/','/','/','/','-','-','-','-','\\','\\','\\','\\'};

    public ConsoleProgressMonitorInputStream(long maximum, InputStream in){
        super(in);
        this.maximum=maximum;
    }

    @Override
    public int read(byte b[]) throws IOException {
        int length =  super.read(b, 0, b.length);
        totalRead += length;

        int percentage = (int)((totalRead * 100.0f) / maximum);

        String percentageToPrint;
        if(percentage==100) {
            percentageToPrint = "" + percentage;
        } else {
            percentageToPrint = (percentage < 10) ? "  " + percentage : " " + percentage;
        }

        if(length ==-1) {
            OUTPUT_STREAM.println("\r" + "100%");
        }else {
            if(maximum!=-1) {
                OUTPUT_STREAM.print("\r" + percentageToPrint + "% Total " + FileUtils.byteCountToDisplaySize(totalRead) + " " + ROTATION[rotationChar % ROTATION.length] + "      ");
            }else{
                OUTPUT_STREAM.print("\r" + "     Total " + FileUtils.byteCountToDisplaySize(totalRead) + " " + ROTATION[rotationChar % ROTATION.length] + "      ");
            }
        }

        rotationChar++;
        return length;
    }


}
