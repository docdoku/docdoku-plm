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
