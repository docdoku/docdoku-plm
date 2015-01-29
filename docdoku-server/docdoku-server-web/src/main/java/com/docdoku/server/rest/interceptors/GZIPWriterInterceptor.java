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

package com.docdoku.server.rest.interceptors;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

@Provider
@Compress
public class GZIPWriterInterceptor implements WriterInterceptor {
    private static final int DEFAULT_BUFFER_SIZE = 4096;


    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException{
        MultivaluedMap<String,Object> headers = context.getHeaders();
        Object rangeHeader = headers.getFirst("Content-Range");

        GZIPOutputStream gzipOutputStream=null;


        if(rangeHeader==null){
            headers.add("Content-Encoding", "gzip");
            headers.remove("Content-Length");
            gzipOutputStream = new GZIPOutputStream(context.getOutputStream(),DEFAULT_BUFFER_SIZE);
            context.setOutputStream(gzipOutputStream);
        }

        try {
            context.proceed();
        } finally {
            if(gzipOutputStream!=null){
                gzipOutputStream.finish();
            }
        }
    }


}
