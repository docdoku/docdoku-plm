package com.docdoku.server.rest.file.util;

import com.docdoku.server.rest.exceptions.InterruptedStreamException;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BinaryResourceBinaryStreamingOutput implements StreamingOutput {
    private static final Logger LOGGER = Logger.getLogger(BinaryResourceBinaryStreamingOutput.class.getName());
    private final InputStream binaryContentInputStream;
    private final Range fullRange;

    public BinaryResourceBinaryStreamingOutput(InputStream binaryContentInputStream, long start, long end, long length) {
        this.binaryContentInputStream = binaryContentInputStream;
        this.fullRange = new Range(start, end, length);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        try {
            if(binaryContentInputStream==null){
                LOGGER.log(Level.SEVERE,"The file input stream is null");
            }
            copy(binaryContentInputStream, outputStream, fullRange.start, fullRange.length,fullRange.total);
        } catch (InterruptedStreamException e) {
            LOGGER.log(Level.WARNING,"Downloading file interrupted");
            LOGGER.log(Level.FINE,"Streaming file interruption",e);
        }
    }

    private void copy(final InputStream input, OutputStream output, long start, long length, long binaryLength) throws InterruptedStreamException{
        if(start == 0 && binaryLength == length){
            try {
                ByteStreams.copy(input, output);
            }catch (IOException e){
                // may cause by a client side cancel
                LOGGER.log(Level.FINE,"A downloading stream was interrupted.",e);
                throw new InterruptedStreamException();
            }finally {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "A input stream was not clearly close.", e);
                }
            }
        }else{
            // Slice the input stream considering offset and length

            try (InputStream slicedInputStream = ByteStreams.slice(new InputSupplier<InputStream>() {
                public InputStream getInput(){
                    return input;
                }
            }, start, length).getInput()) {
                ByteStreams.copy(slicedInputStream, output);
            } catch (IOException e) {
                // may cause by a client side cancel
                LOGGER.log(Level.FINE,"A downloading stream was interrupted.",e);
                throw new InterruptedStreamException();
            }
        }

    }

    private static class Range {
        long start;
        long length;
        long total;

        public Range(long start, long end, long total) {
            this.start = start;
            this.length = end - start + 1;
            this.total = total;
        }
    }
}
