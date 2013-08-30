package com.docdoku.android.plm.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: martindevillers
 */
public final class Session {

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

    boolean permanentSession;
    private String userName;
    private String userLogin;
    private String password;
    private String host;
    private int port;

    private String[] downloadedWorkspaces;
    private String currentWorkspace;

    private Session(boolean permanentSession, String userName, String userLogin, String password, String host, int port){
        this.permanentSession = permanentSession;
        this.userName = userName;
        this.userLogin = userLogin;
        this.password = password;
        this.host = host;
        this.port = port;
    };

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

    public void eraseSession(Context context){
        session = null;
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_SESSION, Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
    }

    public static Session getSession() throws SessionLoadException{
        if (session == null){
            throw new SessionLoadException();
        }
        return session;
    }

    public static Session getSession(Context context) throws SessionLoadException{
        if (session == null){
            if (!loadSession(context)){
                throw new SessionLoadException();
            }
        }
        return session;
    }

    public void setDownloadedWorkspaces(Context context, String[] downloadedWorkspaces) {
        this.downloadedWorkspaces = downloadedWorkspaces;
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_SESSION, Context.MODE_PRIVATE);
        Set<String> workspacesSet = new HashSet<String>(Arrays.asList(downloadedWorkspaces));
        preferences.edit()
            .putStringSet(PREFERENCE_KEY_DOWNLOADED_WORKSPACES, workspacesSet)
            .commit();
    }

    public void setCurrentWorkspace(Context context, String currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_SESSION, Context.MODE_PRIVATE);
        preferences.edit()
            .putString(PREFERENCE_KEY_CURRENT_WORKSPACE, currentWorkspace)
            .commit();
    }

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

    public String[] getDownloadedWorkspaces(Context context) {
        if (downloadedWorkspaces == null){
            Log.i("com.docdoku.android.plm", "Loading downloaded workspaces from preferences");
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_SESSION, Context.MODE_PRIVATE);
            Set<String> workspaceSet = preferences.getStringSet(PREFERENCE_KEY_DOWNLOADED_WORKSPACES, null);
            try {
                downloadedWorkspaces = workspaceSet.toArray(new String[0]);
                Arrays.sort(downloadedWorkspaces);
            }catch (NullPointerException e){
                Log.w("com.docdoku.android.plm", "No downloaded workspaces found in preferences");
            }

        }
        return downloadedWorkspaces;
    }

    public String getCurrentWorkspace(Context context) {
        if (currentWorkspace == null){
            Log.i("com.docdoku.android.plm", "Loading current workspace from preferences");
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_SESSION, Context.MODE_PRIVATE);
            currentWorkspace = preferences.getString(PREFERENCE_KEY_CURRENT_WORKSPACE, null);
            if (currentWorkspace == null){
                Log.i("com.docdoku.android.plm", "No current workspace found in preferences");
                try{
                    if (downloadedWorkspaces == null){
                        getDownloadedWorkspaces(context);
                    }
                    Log.i("com.docdoku.android.plm", "Setting current workspace as default");
                    currentWorkspace = downloadedWorkspaces[0];
                }catch (ArrayIndexOutOfBoundsException e){
                    Log.w("com.docdoku.android.plm", "No workspaces downloaded, unable to set current workspace as default");
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
                Log.i("com.docdoku.android.plm", "Extracted port from Url: " + port);
                try{
                    return Integer.parseInt(portString);
                }catch(NumberFormatException e){
                    Log.w("com.docdoku.android.plm", "Error reading port number");
                }
            }
        }
        return -1;
    }

    public static class SessionLoadException extends Exception{

        public SessionLoadException(){
            super("No user session was loaded");
        }
    }
}
