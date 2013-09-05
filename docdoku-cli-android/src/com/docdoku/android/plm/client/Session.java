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

package com.docdoku.android.plm.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Singleton containing the data for the current session used by the user.
 *
 * @author: martindevillers
 */
public final class Session {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.Session";

    private static final String PREFERENCES_SESSION = "session";
    private static final String PREFERENCE_KEY_USER_NAME = "user name";
    private static final String PREFERENCE_KEY_USER_LOGIN = "user login";
    private static final String PREFERENCE_KEY_PASSWORD = "password";
    private static final String PREFERENCE_KEY_SERVER_HOST = "server host";
    private static final String PREFERENCE_KEY_SERVER_PORT = "server port";
    private static final String PREFERENCE_KEY_AUTO_CONNECT = "auto connect";
    private static final String PREFERENCE_KEY_DOWNLOADED_WORKSPACES = "downloaded workspaces";
    private static final String PREFERENCE_KEY_CURRENT_WORKSPACE = "current workspace";

    private static Session session;

    private final boolean permanentSession;
    private String userName;
    private final String userLogin;
    private final String password;
    private final String host;
    private final int port;

    private String[] downloadedWorkspaces;
    private String currentWorkspace;

    private Session(boolean permanentSession, String userName, String userLogin, String password, String host, int port){
        this.permanentSession = permanentSession;
        this.userName = userName;
        this.userLogin = userLogin;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    /**
     * Sets the current <code>Session</code> with the specified parameters. If the <code>autoConnect</code> parameter's value is <code>true</code>,
     * the <code>Session</code> data is stored in the application's <code>SharedPreferences</code>. The <code>url</code> is used to
     * extract the <code>host</code> and <code>port</code> to access the server.
     *
     * @param context The <code>Context</code>, used to access the <code>SharedPreferences</code>
     * @param autoConnect Whether the auto connect option was selected
     * @param userName the user's name
     * @param userLogin the user's login
     * @param password the user's password
     * @param url the provided url
     * @return the <code>Session</code> that was generated
     */
    public static Session initSession(Context context, boolean autoConnect, String userName, String userLogin, String password, String url){
        String host = extractHostFromUrl(url);
        int port = extractPortFromUrl(url);
        session = new Session(autoConnect, userName, userLogin, password, host, port);
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_SESSION, Context.MODE_PRIVATE);
        preferences.edit()
                .putBoolean(PREFERENCE_KEY_AUTO_CONNECT, autoConnect)
                .putString(PREFERENCE_KEY_USER_NAME, userName)
                .putString(PREFERENCE_KEY_USER_LOGIN, userLogin)
                .putString(PREFERENCE_KEY_PASSWORD, password)
                .putString(PREFERENCE_KEY_SERVER_HOST, host)
                .putInt(PREFERENCE_KEY_SERVER_PORT, port)
                .commit();
        return session;
    }

    /**
     * Attempts to load a <code>Session</code> from the <code>SharedPreferences</code>
     *
     * @param context The <code>Context</code> to access the <code>SharedPreferences</code>
     * @return whether a <code>Session</code> was found in the <code>SharedPreferences</code>
     */
    public static boolean loadSession(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_SESSION, Context.MODE_PRIVATE);
        boolean autoConnect = preferences.getBoolean(PREFERENCE_KEY_AUTO_CONNECT, false);
        if (autoConnect){
            String userName = preferences.getString(PREFERENCE_KEY_USER_NAME, null);
            String userLogin = preferences.getString(PREFERENCE_KEY_USER_LOGIN, null);
            String password = preferences.getString(PREFERENCE_KEY_PASSWORD, null);
            String host = preferences.getString(PREFERENCE_KEY_SERVER_HOST, null);
            int port = preferences.getInt(PREFERENCE_KEY_SERVER_PORT, -1);
            session = new Session(true, userName, userLogin, password, host, port);
            return true;
        }else{
            return false;
        }
    }

    /**
     * Erases the current <code>Session</code> from memory and from the <code>SharedPreferences</code>
     *
     * @param context The <code>Context</code> to access the <code>SharedPreferences</code>
     */
    public void eraseSession(Context context){
        session = null;
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_SESSION, Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
    }

    /**
     * Gets the current <code>Session</code> instance
     *
     * @return the current <code>Session</code>
     * @throws SessionLoadException if no <code>Session</code> instance is available
     */
    public static Session getSession() throws SessionLoadException{
        if (session == null){
            throw new SessionLoadException();
        }
        return session;
    }

    /**
     * Gets the current <code>Session</code> instance, loading it from the <code>SharedPreferences</code> if none is available in memory.
     *
     * @param context The <code>Context</code> to access the <code>SharedPreferences</code>
     * @return The current <code>Session</code>
     * @throws SessionLoadException if no <code>Session</code> instance is available
     */
    public static Session getSession(Context context) throws SessionLoadException{
        if (session == null){
            if (!loadSession(context)){
                throw new SessionLoadException();
            }
        }
        return session;
    }

    /**
     * Sets the user's name in the current <code>Session</code>
     *
     * @param userName the user's name
     */
    public void setUserName(String userName){
        this.userName = userName;
    }

    /**
     * Sets the workspaces available to the current user for the current <code>Session</code>, and stores them in the <code>SharedPreferences</code>
     *
     * @param context The <code>Context</code> to access the <code>SharedPreferences</code>
     * @param downloadedWorkspaces The workspaces available
     */
    public void setDownloadedWorkspaces(Context context, String[] downloadedWorkspaces) {
        this.downloadedWorkspaces = downloadedWorkspaces;
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_SESSION, Context.MODE_PRIVATE);
        Set<String> workspacesSet = new HashSet<String>(Arrays.asList(downloadedWorkspaces));
        preferences.edit()
            .putStringSet(PREFERENCE_KEY_DOWNLOADED_WORKSPACES, workspacesSet)
            .commit();
    }

    /**
     * Sets the workspace that the current user is consulting for the current <code>Session</code>, and stores it in the <code>SharedPreferences</code>
     *
     * @param context The <code>Context</code> to access the <code>SharedPreferences</code>
     * @param currentWorkspace The worspace currently in use
     */
    public void setCurrentWorkspace(Context context, String currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_SESSION, Context.MODE_PRIVATE);
        preferences.edit()
            .putString(PREFERENCE_KEY_CURRENT_WORKSPACE, currentWorkspace)
            .commit();
    }

    /**
     * If auto connect was enabled for the current session
     *
     * @return
     */
    public boolean isPermanentSession(){
        return permanentSession;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    /**
     * Gets the downloaded workspaces for the current <code>Session</code>, and attempts to load them from the <code>SharedPreferences</code>
     * if none is available in memory.
     *
     * @param context The <code>Context</code> to access the <code>SharedPreferences</code>
     * @return The workspace array
     */
    public String[] getDownloadedWorkspaces(Context context) {
        if (downloadedWorkspaces == null){
            Log.i(LOG_TAG, "Loading downloaded workspaces from preferences");
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_SESSION, Context.MODE_PRIVATE);
            Set<String> workspaceSet = preferences.getStringSet(PREFERENCE_KEY_DOWNLOADED_WORKSPACES, null);
            try {
                downloadedWorkspaces = workspaceSet.toArray(new String[workspaceSet.size()]);
                Arrays.sort(downloadedWorkspaces);
            }catch (NullPointerException e){
                Log.w(LOG_TAG, "No downloaded workspaces found in preferences");
            }

        }
        return downloadedWorkspaces;
    }

    /**
     * Gets the workspaces last used by the current <code>Session</code>'s user, and attempts to load it from the <code>SharedPreferences</code>
     * if none is available in memory.
     *
     * @param context The <code>Context</code> to access the <code>SharedPreferences</code>
     * @return The workspace's name
     */
    public String getCurrentWorkspace(Context context) {
        if (currentWorkspace == null){
            Log.i(LOG_TAG, "Loading current workspace from preferences");
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_SESSION, Context.MODE_PRIVATE);
            currentWorkspace = preferences.getString(PREFERENCE_KEY_CURRENT_WORKSPACE, null);
            if (currentWorkspace == null){
                Log.i(LOG_TAG, "No current workspace found in preferences");
                try{
                    if (downloadedWorkspaces == null){
                        getDownloadedWorkspaces(context);
                    }
                    Log.i(LOG_TAG, "Setting current workspace as default");
                    currentWorkspace = downloadedWorkspaces[0];
                }catch (ArrayIndexOutOfBoundsException e){
                    Log.w(LOG_TAG, "No workspaces downloaded, unable to set current workspace as default");
                }
            }
        }
        return currentWorkspace;
    }

    private static String extractHostFromUrl(String url){
        String finalUrl = url;
        if (finalUrl != null && finalUrl.length()>0){
            if (finalUrl.length()>7 && finalUrl.substring(0,7).equals("http://")){
                finalUrl = finalUrl.substring(7, finalUrl.length());
            }
            int semicolonIndex = finalUrl.indexOf(':');
            if (semicolonIndex != -1){
                finalUrl = finalUrl.substring(0, semicolonIndex);
            }
            if (finalUrl.charAt(finalUrl.length()-1) == '/'){
                finalUrl = finalUrl.substring(0, finalUrl.length()-1);
            }
        }
        return finalUrl;
    }

    private static int extractPortFromUrl(String url){
        String finalUrl = url;
        if (finalUrl != null && finalUrl.length()>0){
            if (finalUrl.length()>7 && finalUrl.substring(0,7).equals("http://")){
                finalUrl = finalUrl.substring(7, finalUrl.length());
            }
            int semicolonIndex = finalUrl.indexOf(':');
            if (semicolonIndex != -1){
                String portString = finalUrl.substring(semicolonIndex+1);
                int port = Integer.parseInt(portString);
                Log.i(LOG_TAG, "Extracted port from Url: " + port);
                try{
                    return Integer.parseInt(portString);
                }catch(NumberFormatException e){
                    Log.w(LOG_TAG, "Error reading port number");
                }
            }
        }
        return -1;
    }

    /**
     * Exception indicating that the class was unable to load a <code>Session</code>
     */
    public static class SessionLoadException extends Exception{

        public SessionLoadException(){
            super("No user session was loaded");
        }
    }
}
