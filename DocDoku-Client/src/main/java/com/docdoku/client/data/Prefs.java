/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.client.data;

import com.docdoku.core.document.MasterDocumentKey;
import com.docdoku.core.document.MasterDocument;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Prefs {
    
    private final static Preferences USER_ROOT=Preferences.userRoot().node("com.docdoku");
    private final static Preferences LOCALE_NODE=USER_ROOT.node("locale");
    private final static Preferences DOC_NODE=USER_ROOT.node("documents");
    private final static Preferences CONNECTION_NODE=USER_ROOT.node("connection");
    
    private Prefs(){}
    
    public static boolean getNumbered(){
        return USER_ROOT.getBoolean("numbered node", false);
    }
    
    public static void setNumbered(boolean numberedNode){
        USER_ROOT.putBoolean("numbered node",numberedNode);
    }
    
    public static String getLastWorkspace(){
        return CONNECTION_NODE.get("last workspace", null);
    }
    
    public static void setLastWorkspace(String lastWorkspaceNode){
        CONNECTION_NODE.put("last workspace",lastWorkspaceNode);
    }
    
    public static String getLastLogin(){
        return CONNECTION_NODE.get("last login", null);
    }
    
    public static void setLastLogin(String lastLoginNode){
        CONNECTION_NODE.put("last login",lastLoginNode);
    }
    
    public static Locale getLocale(){
        return Locale.getDefault();
    }
    
    public static void storeDocInfo(MasterDocument pMDoc, String pKey, String pValue){
        Preferences node=DOC_NODE.node(MainModel.getInstance().getWorkspace().getId()).node(pMDoc.getId()+"-"+pMDoc.getVersion());
        node.put(pKey, pValue);
    }
    
    public static void storeDocInfo(MasterDocument pMDoc, String pKey, long pValue){
        Preferences node=DOC_NODE.node(MainModel.getInstance().getWorkspace().getId()).node(pMDoc.getId()+"-"+pMDoc.getVersion());
        node.putLong(pKey, pValue);
    }
    
    public static void removeDocInfo(MasterDocument pMDoc, String pKey){
        Preferences node=DOC_NODE.node(MainModel.getInstance().getWorkspace().getId()).node(pMDoc.getId()+"-"+pMDoc.getVersion());
        node.remove(pKey);
    }
    
    public static void removeDocNode(MasterDocument pMDoc){
        removeDocNode(pMDoc.getKey());
    }
    
    public static void removeDocNode(MasterDocumentKey pMDocPK){
        Preferences node=DOC_NODE.node(MainModel.getInstance().getWorkspace().getId()).node(pMDocPK.getId()+"-"+pMDocPK.getVersion());
        try {
            node.removeNode();
        } catch (BackingStoreException pEx) {
            System.err.println(pEx.getMessage());
        }
    }
    
    public static String getDocInfo(MasterDocument pMDoc, String pKey){
        Preferences node=DOC_NODE.node(MainModel.getInstance().getWorkspace().getId()).node(pMDoc.getId()+"-"+pMDoc.getVersion());
        return node.get(pKey, null);
    }
    
    public static long getLongDocInfo(MasterDocument pMDoc, String pKey){
        Preferences node=DOC_NODE.node(MainModel.getInstance().getWorkspace().getId()).node(pMDoc.getId()+"-"+pMDoc.getVersion());
        return node.getLong(pKey, 0);
    }
    
    public static void initLocale(){
        Locale defaultLocale=Locale.getDefault();
        String language=LOCALE_NODE.get("language",defaultLocale.getLanguage());
        String country=LOCALE_NODE.get("country",defaultLocale.getCountry());
        String variant=LOCALE_NODE.get("variant",defaultLocale.getVariant());
        Locale locale=new Locale(language,country,variant);
        Locale.setDefault(locale);
    } 
    public static void setLocale(Locale pLocale){
        LOCALE_NODE.put("language",pLocale.getLanguage());
        LOCALE_NODE.put("country",pLocale.getCountry());
        LOCALE_NODE.put("variant",pLocale.getVariant());
    }
    
}
