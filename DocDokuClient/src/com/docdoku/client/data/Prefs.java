package com.docdoku.client.data;

import com.docdoku.core.entities.keys.MasterDocumentKey;
import com.docdoku.core.entities.MasterDocument;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Prefs {
    
    private final static Preferences USER_ROOT=Preferences.userRoot().node("com.docdoku");
    private final static Preferences LOCALE_NODE=USER_ROOT.node("locale");
    private final static Preferences DOC_NODE=USER_ROOT.node("documents");
    private Prefs(){}
    
    public static boolean getNumbered(){
        return USER_ROOT.getBoolean("numbered node", false);
    }
    
    public static void setNumbered(boolean numberedNode){
        USER_ROOT.putBoolean("numbered node",numberedNode);
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
