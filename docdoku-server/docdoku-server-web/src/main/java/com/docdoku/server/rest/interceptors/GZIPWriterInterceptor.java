package com.docdoku.server.rest.interceptors;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

@Provider
@Compress
public class GZIPWriterInterceptor implements WriterInterceptor {
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException{
        MultivaluedMap<String,Object> headers = context.getHeaders();
        Object rangeHeader = headers.getFirst("Accept-Ranges");

        OutputStream old=null;
        GZIPOutputStream gzipOutputStream=null;

        // GZip none streaming content
        if(rangeHeader==null){
            headers.add("Content-Encoding", "gzip");
            old = context.getOutputStream();
            gzipOutputStream = new GZIPOutputStream(old,DEFAULT_BUFFER_SIZE);
            context.setOutputStream(gzipOutputStream);
        }

        try {
            context.proceed();
        } finally {
            if(old!=null){
                gzipOutputStream.finish();
                context.setOutputStream(old);
            }
        }
    }
}
