package com.docdoku.android.plm.network;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.*;

/**
 * @author: martindevillers
 */
public abstract  class HttpTask<A, B, C> extends AsyncTask<A, B, C>{

    public static final String ERROR_UNKNOWN = "Connection Error";
    public static final String ERROR_URL = "Url error";
    public static final String ERROR_HTTP_BAD_REQUEST = "Http Bad request";
    public static final String ERROR_HTTP_UNAUTHORIZED = "Http unauthorized";

    private final static int CHUNK_SIZE = 1024*8;
    private final static int BUFFER_CAPACITY = 1024*32;

    protected static String host;
    protected static int port;
    protected static byte[] id;

    protected HttpTask(){
        if (id == null || host == null){
            Log.e("com.docdoku.android.plm", "Server connection information is missing.");
            //TODO recuperate server connection information
        }else{
            Log.i("com.docdoku.android.plm", "All the server connection information is correctly available");
        }
    }

    protected URL createURL(String path) throws URISyntaxException, MalformedURLException {
        String uriPath = path.replace(" ", "%20");
        URI uri = new URI(uriPath);
        String ASCIIPath = uri.toASCIIString();
        Log.i("com.docdoku.android.plm", "Parameters for Http connection: " +
                "\n Host: " + host +
                "\n Port: " + port +
                "\n Path: " + path);
        if (port == -1){
            return new URL("http", host, ASCIIPath);
        }
        return new URL("http", "192.168.0.12", port, ASCIIPath);
    }

    protected String analyzeHttpErrorCode(int errorCode){
        switch (errorCode){
            case 400: return ERROR_HTTP_BAD_REQUEST;
            case 401: return ERROR_HTTP_UNAUTHORIZED;
        }
        return ERROR_UNKNOWN;
    }

    protected void writeBytesToConnection(HttpURLConnection connection, byte[] bytes) throws IOException {
        OutputStream out = new BufferedOutputStream(connection.getOutputStream(), BUFFER_CAPACITY);
        InputStream inputStream = new ByteArrayInputStream(bytes);
        byte[] data = new byte[CHUNK_SIZE];
        int length;
        while ((length = inputStream.read(data)) != -1) {
            out.write(data, 0, length);
        }
        out.flush();
    }

    protected String inputStreamToString(InputStream in) throws IOException {
        String string;
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader bf = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bf.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        string = sb.toString();
        reader.close();
        bf.close();
        return string;
    }
}
