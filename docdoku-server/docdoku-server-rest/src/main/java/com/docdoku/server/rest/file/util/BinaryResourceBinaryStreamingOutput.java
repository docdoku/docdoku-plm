/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.server.rest.file.util;

import com.docdoku.server.rest.exceptions.InterruptedStreamException;
import com.google.common.io.ByteStreams;

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
            if (binaryContentInputStream == null) {
                LOGGER.log(Level.SEVERE, "The file input stream is null");
            } else {
                copy(binaryContentInputStream, outputStream, fullRange.start, fullRange.length);
            }
        } catch (InterruptedStreamException e) {
            LOGGER.log(Level.WARNING, "Downloading file interrupted");
            LOGGER.log(Level.FINE, "Streaming file interruption", e);
        }
    }

    private void copy(final InputStream input, OutputStream output, long start, long length) throws InterruptedStreamException {
        // Slice the input stream considering offset and length
        try (InputStream in = input) {
            if (length > 0) {
                in.skip(start);
                byte[] data = new byte[1024 * 8];
                long remaining = length;
                int nr;
                while (remaining > 0) {
                    nr = in.read(data);
                    if (nr < 0) {
                        break;
                    }
                    remaining -= nr;
                    output.write(data, 0, nr);
                }
            }
            else{
                ByteStreams.copy(in, output);
            }
        } catch (IOException e) {
            // may be caused by a client side cancel
            LOGGER.log(Level.FINE, "A downloading stream was interrupted.", e);
            throw new InterruptedStreamException();
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
