package com.docdoku.cli.helpers;


import org.apache.commons.io.FileUtils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConsoleProgressMonitorInputStream extends FilterInputStream {

    private long maximum;
    private long totalRead;

    private int rotationChar;

    private final static char[] ROTATION = {'|','|','|','|','/','/','/','/','-','-','-','-','\\','\\','\\','\\'};

    public ConsoleProgressMonitorInputStream(long maximum, InputStream in){
        super(in);
        this.maximum=maximum;
    }

    public int read(byte b[]) throws IOException {
        int length =  super.read(b, 0, b.length);
        totalRead += length;
        int percentage = (int)((totalRead * 100.0f) / maximum);

        String percentageToPrint;
        if(percentage==100)
            percentageToPrint=""+percentage;
        else
            percentageToPrint=(percentage<10)?"  "+percentage:" "+percentage;

        if(length ==-1)
            System.out.println("\r" + 100);
        else
            System.out.print("\r" + percentageToPrint + "% Total " + FileUtils.byteCountToDisplaySize(maximum) + " " + ROTATION[rotationChar % ROTATION.length]);

        rotationChar++;
        return length;
    }


}
