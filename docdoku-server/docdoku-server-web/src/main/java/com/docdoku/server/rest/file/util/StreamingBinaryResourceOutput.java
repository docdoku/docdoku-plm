package com.docdoku.server.rest.file.util;

import com.docdoku.server.rest.exceptions.InterruptedStreamException;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StreamingBinaryResourceOutput implements StreamingOutput {
    private static final Logger LOGGER = Logger.getLogger(StreamingBinaryResourceOutput.class.getName());
    private InputStream binaryContentInputStream;
    private Range fullRange;

    public StreamingBinaryResourceOutput(InputStream binaryContentInputStream, long start, long end, long length) {
        this.binaryContentInputStream = binaryContentInputStream;
        this.fullRange = new Range(start, end, length);
    }

    @Override
    public void write(OutputStream outputStream) throws WebApplicationException, IOException {
        try {
            if(binaryContentInputStream==null){
                String message = "The file input stream is null";
                LOGGER.log(Level.SEVERE,message);
            }
            copy(binaryContentInputStream, outputStream, fullRange.start, fullRange.length,fullRange.total);
        } catch (InterruptedStreamException e) {
            LOGGER.log(Level.WARNING,"Downloading file interrupted");
            LOGGER.log(Level.FINE,"Streaming file interruption",e.getCause());
        }
    }

    private static void copy(final InputStream input, OutputStream output, long start, long length, long binaryLength) throws InterruptedStreamException, IOException {
        if(start == 0 && binaryLength == length){
            try {
                ByteStreams.copy(input, output);
            }catch (IOException e){
                throw new InterruptedStreamException(input,output,e);
            }
        }else{
            // Slice the input stream considering offset and length
            InputStream slicedInputStream = ByteStreams.slice(new InputSupplier<InputStream>() {
                public InputStream getInput() throws IOException {
                    return input;
                }
            }, start, length).getInput();

            try {
                ByteStreams.copy(slicedInputStream, output);
            }catch (IOException e){
                throw new InterruptedStreamException(slicedInputStream,output,e);
            }
        }

    }

    private static class Range {
        long start;
        long end;
        long length;
        long total;

        public Range(long start, long end, long total) {
            this.start = start;
            this.end = end;
            this.length = end - start + 1;
            this.total = total;
        }
    }
}
