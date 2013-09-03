/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.android.plm.network;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import com.docdoku.android.plm.client.Session;

import java.io.*;
import java.net.*;

/**
 * Basic class for Http requests.
 * <p>Contains the data for the server connection in the form of {@code static} fields.
 * <br>Also contains some useful methods for reading and writing data on/from network connections.
 *
 * @author: martindevillers
 */
public abstract  class HttpTask<A, B, C> extends AsyncTask<A, B, C>{
    private static final String LOG_TAG = "com.docdoku.android.plm.network.HttpTask";

    public static final String ERROR_UNKNOWN = "Connection Error";
    public static final String ERROR_URL = "Url error";
    public static final String ERROR_HTTP_BAD_REQUEST = "Http Bad request";
    public static final String ERROR_HTTP_UNAUTHORIZED = "Http unauthorized";

    private final static int CHUNK_SIZE = 1024*8;
    private final static int BUFFER_CAPACITY = 1024*32;

    protected static String host;
    protected static int port;
    protected static byte[] id;

    /**
     * Checks that the server connection information is available.
     * <p>If the {@code static} fields are not set, checks if a {@link Session} instance is available in memory, and if it is,
     * fetches the server connection information from it.
     * <br>If no server connection information can be found, prints a {@code Log} message.
     */
    protected HttpTask(){
        if (id == null || host == null){
            try {
                Log.i(LOG_TAG, "attempting to retrieve server connection information from memory...");
                Session session = Session.getSession();
                host = session.getHost();
                port = session.getPort();
                id = Base64.encode((session.getUserLogin() + ":" + session.getPassword()).getBytes("ISO-8859-1"), Base64.DEFAULT);
            } catch (Session.SessionLoadException e) {
                Log.e(LOG_TAG, "Unable to get server connection information.");
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (UnsupportedEncodingException e) {
                Log.e(LOG_TAG, "Unable to get server connection information. The server information loaded from memory threw an UnsupportedEncodingException.");
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }else{
            Log.i(LOG_TAG, "All the server connection information is correctly available");
        }
    }

    /**
     * Creates a {@code URL} to the server with the specified {@code path} appended at the end.
     * @param path the path inside the server
     * @return the {@code URL} to connect to the server
     * @throws URISyntaxException if the specified path could not be converted into an ASCII {@code String}
     * @throws MalformedURLException
     */
    protected URL createURL(String path) throws URISyntaxException, MalformedURLException {
        String uriPath = path.replace(" ", "%20");
        URI uri = new URI(uriPath);
        String ASCIIPath = uri.toASCIIString();
        Log.i(LOG_TAG, "Parameters for Http connection: " +
                "\n Host: " + host +
                "\n Port: " + port +
                "\n Path: " + path);
        if (port == -1){
            return new URL("http", host, ASCIIPath);
        }
        return new URL("http", host, port, ASCIIPath);
    }

    /**
     * Transforms the Http error code into a {@code String} message more understandable by the programmer
     * @param errorCode
     * @return
     */
    protected String analyzeHttpErrorCode(int errorCode){
        switch (errorCode){
            case 400: return ERROR_HTTP_BAD_REQUEST;
            case 401: return ERROR_HTTP_UNAUTHORIZED;
        }
        return ERROR_UNKNOWN;
    }

    /**
     * Writes a {@code byte[]} to an {@code HttpURLConnection}
     * <p>Creates an {@code OutputStream} and writes the {@code InputStream} created from the {@code byte[]} onto it.
     *
     * @param connection the Http Connection, already opened
     * @param bytes the {@code byte[]} to write on the Http conection
     * @throws IOException
     */
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

    /**
     * Reads an {@code InputStream}, writing it to a {@code String}
     *
     * @param in the {@code InputStream} to read
     * @return the {@code String} read from the {@code InputStream}
     * @throws IOException
     */
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
