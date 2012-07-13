/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

import com.docdoku.core.document.Folder;
import com.docdoku.core.document.Tag;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.common.User;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.common.Workspace;
import java.lang.ref.*;


import java.util.*;

public class Cache {
    
    private Map<DocumentMasterKey, DocumentMaster> mPK2DocM;
    private Map<String, Collection<DocumentMasterKey>> mPath2PKs;
    private Map<String, Collection<FolderTreeNode>> mPath2Folders;
    private Map<String, Collection<DocumentMasterKey>> mTag2PKs;
    private SoftReference<Collection<DocumentMasterKey>> mCheckedOutPKs;
    private SoftReference<Collection<String>> mWorkflowModels;
    private Map<String,WorkflowModel> mID2WorkflowModel;
    private SoftReference<Collection<String>> mDocMTemplates;
    private Map<String,DocumentMasterTemplate> mID2DocMTemplate;
    private SoftReference<Collection<String>> mUsers;
    private SoftReference<Collection<String>> mTags;
    private Map<String,User> mLogin2User;
    private SoftReference<Collection<DocumentMasterKey>> mIterationSubscriptions;
    private SoftReference<Collection<DocumentMasterKey>> mStateSubscriptions;
    
    private final String mLogin;
    private final String mPassword;
    private final Workspace mWorkspace;
    
    public Cache(String pLogin, String pPassword, Workspace pWorkspace) {
        mWorkspace = pWorkspace;
        mLogin = pLogin;
        mPassword = pPassword;
        
        mPK2DocM = new SoftValueHashMap<DocumentMasterKey, DocumentMaster>();
        mPath2PKs = new SoftValueHashMap<String, Collection<DocumentMasterKey>>();
        mTag2PKs = new SoftValueHashMap<String, Collection<DocumentMasterKey>>();
        mPath2Folders = new SoftValueHashMap<String, Collection<FolderTreeNode>>();
        mID2WorkflowModel = new SoftValueHashMap<String,WorkflowModel>();
        mID2DocMTemplate = new SoftValueHashMap<String,DocumentMasterTemplate>();
        mLogin2User = new SoftValueHashMap<String,User>();
    }
    
    
    public DocumentMaster getDocM(DocumentMasterKey pPK) {
        return mPK2DocM.get(pPK);
    }
    
    public DocumentMasterKey cacheDocM(DocumentMaster pDocM) {
        DocumentMasterKey pk =pDocM.getKey();
        
        DocumentMaster oldDocM=mPK2DocM.get(pk);
        //firstable we remove all references of the old docM
        if(oldDocM!=null)
            removeDocM(oldDocM);
        
        mPK2DocM.put(pk, pDocM);
        
        Collection<DocumentMasterKey> pks = mPath2PKs.get(pDocM.getLocation().getCompletePath());
        if (pks != null) {
            pks.add(pk);
        }
        
        for(Tag tag:pDocM.getTags()){
            pks = mTag2PKs.get(tag.getLabel());
            if (pks != null) {
                pks.add(pk);
            }
        }
        
        
        if(pDocM.isCheckedOutBy(mLogin)){
            if (mCheckedOutPKs != null) {
                Collection<DocumentMasterKey> checkedOutPKs = mCheckedOutPKs.get();
                if (checkedOutPKs != null) {
                    checkedOutPKs.add(pk);
                }
            }
        }
        
        for(Tag tag:pDocM.getTags())
            cacheTag(tag.getLabel());
        
        System.out.println("Caching document master " + pDocM);
        return pk;
    }
    
    public void removeDocM(DocumentMaster pDocM) {
        Collection<DocumentMasterKey> pks = mPath2PKs.get(pDocM.getLocation().getCompletePath());
        if (pks != null)
            pks.remove(pDocM.getKey());
        
        for(Tag tag:pDocM.getTags()){
            pks = mTag2PKs.get(tag.getLabel());
            if (pks != null) {
                pks.remove(pDocM.getKey());
            }
        }
        
        if (mCheckedOutPKs != null) {
            Collection<DocumentMasterKey> checkedOutPKs = mCheckedOutPKs.get();
            if (checkedOutPKs != null) {
                checkedOutPKs.remove(pDocM.getKey());
            }
        }
        mPK2DocM.remove(pDocM.getKey());
        System.out.println("Removing from cache document master " + pDocM);
    }

    public void moveFolder(String pCompletePath, FolderTreeNode pNewFolderTreeNode){
        //to prevent ConcurrentModificationException, we iterate from another Collection
        Collection<FolderTreeNode> subFolders = new HashSet<FolderTreeNode>();
        Collection<FolderTreeNode> folders = mPath2Folders.get(pCompletePath);
        if(folders!=null)
            subFolders.addAll(folders);
        Collection<FolderTreeNode> parentFolderSubFolders = mPath2Folders.get(pNewFolderTreeNode.getFolder().getParentFolder().getCompletePath());

        if (parentFolderSubFolders != null) {
            parentFolderSubFolders.remove(new FolderTreeNode(pCompletePath, null));
            parentFolderSubFolders.add(pNewFolderTreeNode);
        }
        Collection<DocumentMasterKey> pks = mPath2PKs.get(pCompletePath);
        if (pks != null) {
            for(DocumentMasterKey pk:pks){
                DocumentMaster docM = mPK2DocM.get(pk);
                if(docM!=null)
                  docM.setLocation(pNewFolderTreeNode.getFolder());

            }
        }
        mPath2PKs.remove(pCompletePath);
        mPath2PKs.put(pNewFolderTreeNode.getCompletePath(), pks);

        if(subFolders != null){
            mPath2Folders.put(pNewFolderTreeNode.getCompletePath(),new LinkedHashSet<FolderTreeNode>());
            for(FolderTreeNode subFolder:subFolders){
                moveFolder(subFolder.getCompletePath(), new FolderTreeNode(new Folder(pNewFolderTreeNode.getCompletePath(),subFolder.getName()),pNewFolderTreeNode));
            }
        }

        mPath2Folders.remove(pCompletePath);
        System.out.println("Updating cache, moving folder "
                + pCompletePath + " to " + pNewFolderTreeNode.getCompletePath());

    }
    public void removeFolder(String pCompletePath) {
        Folder folder = new Folder(pCompletePath);
        
        //to prevent ConcurrentModificationException, we iterate from another Collection
        Collection<FolderTreeNode> subFolders = new HashSet<FolderTreeNode>();
        Collection<FolderTreeNode> folders = mPath2Folders.get(pCompletePath);
        if(folders!=null)
            subFolders.addAll(folders);
        Collection<FolderTreeNode> parentFolderSubFolders = mPath2Folders.get(folder.getParentFolder().getCompletePath());
        
        if (parentFolderSubFolders != null) {
            parentFolderSubFolders.remove(new FolderTreeNode(pCompletePath, null));
        }
        Collection<DocumentMasterKey> pks = mPath2PKs.get(pCompletePath);
        if (pks != null) {
            if (mCheckedOutPKs != null) {
                Collection<DocumentMasterKey> checkedOutPKs = mCheckedOutPKs.get();
                if (checkedOutPKs != null) {
                    checkedOutPKs.removeAll(pks);
                }
            }
            Iterator<Collection<DocumentMasterKey>> ite=mTag2PKs.values().iterator();
            while(ite.hasNext()){
                Collection<DocumentMasterKey> taggedPKs =ite.next();
                if (taggedPKs != null) {
                    taggedPKs.removeAll(pks);
                }
            }
            for(DocumentMasterKey pk:pks)
                mPK2DocM.remove(pk);
        }
        mPath2PKs.remove(pCompletePath);
        
        if(subFolders != null){
            for(FolderTreeNode subFolder:subFolders){
                removeFolder(subFolder.getCompletePath());
            }
        }
        
        mPath2Folders.remove(pCompletePath);
        System.out.println("Updating cache, removing folder "
                + pCompletePath);
    }
    
    public void removeTag(String pTag) {
        if (mTags != null) {
            Collection<String> tags = mTags.get();
            if (tags != null) {
                tags.remove(pTag);
            }
        }
        Collection<DocumentMasterKey> pks = mTag2PKs.remove(pTag);
        if (pks != null) {
            for(DocumentMasterKey pk:pks){
                DocumentMaster docM=getDocM(pk);
                if (docM != null){
                    docM.removeTag(new Tag(docM.getWorkspace(),pTag));
                }
            }
                
        }
        System.out.println("Removing from cache tag " + pTag);
    }
    
    public void removeWorkflowModel(WorkflowModel pWorkflowModel) {
        if (mWorkflowModels != null) {
            Collection<String> ids = mWorkflowModels.get();
            if (ids != null) {
                ids.remove(pWorkflowModel.getId());
            }
        }
        mID2WorkflowModel.remove(pWorkflowModel.getId());
        System.out.println("Removing from cache workflow model " + pWorkflowModel);
    }
    
    public void removeDocMTemplate(DocumentMasterTemplate pTemplate) {
        if (mDocMTemplates != null) {
            Collection<String> ids = mDocMTemplates.get();
            if (ids != null) {
                ids.remove(pTemplate.getId());
            }
        }
        mID2DocMTemplate.remove(pTemplate.getId());
        System.out.println("Removing from cache document master template " + pTemplate);
    }
    
    public DocumentMaster[] findDocMsByFolder(String pCompletePath) {
        Collection<DocumentMasterKey> pks = mPath2PKs.get(pCompletePath);
        if (pks != null) {
            DocumentMaster[] docMs = new DocumentMaster[pks.size()];
            int i = 0;
            for (DocumentMasterKey pk:pks) {
                DocumentMaster docM=getDocM(pk);
                if (docM == null)
                    return null;
                else
                    docMs[i++]=docM;
            }
            return docMs;
        }
        return null;
    }
    
    public DocumentMaster[] findDocMsByTag(String pTag) {
        Collection<DocumentMasterKey> pks = mTag2PKs.get(pTag);
        if (pks != null) {
            DocumentMaster[] docMs = new DocumentMaster[pks.size()];
            int i = 0;
            for (DocumentMasterKey pk:pks) {
                DocumentMaster docM=getDocM(pk);
                if (docM == null)
                    return null;
                else
                    docMs[i++]=docM;
            }
            return docMs;
        }
        return null;
    }
    
    public void cacheDocMsByFolder(String pCompletePath, DocumentMaster[] pDocMs) {
        Set<DocumentMasterKey> pks = new HashSet<DocumentMasterKey>();
        for (DocumentMaster docM:pDocMs) {
            pks.add(cacheDocM(docM));
        }
        mPath2PKs.put(pCompletePath, pks);
        System.out.println("Caching document masters by folder " + pCompletePath);
    }
    
    public void cacheDocMsByTag(String pTag, DocumentMaster[] pDocMs) {
        Set<DocumentMasterKey> pks = new HashSet<DocumentMasterKey>();
        for (DocumentMaster docM:pDocMs) {
            pks.add(cacheDocM(docM));
        }
        mTag2PKs.put(pTag, pks);
        System.out.println("Caching document masters by tag " + pTag);
    }
    
    public DocumentMaster[] getCheckedOutDocMs() {
        if (mCheckedOutPKs != null) {
            Collection<DocumentMasterKey> pks = mCheckedOutPKs.get();
            if (pks != null) {
                DocumentMaster[] docMs = new DocumentMaster[pks.size()];
                int i = 0;
                for (DocumentMasterKey pk:pks) {
                    DocumentMaster docM=getDocM(pk);
                    if (docM == null)
                        return null;
                    else
                        docMs[i++]=docM;
                }
                return docMs;
            }
        }
        return null;
    }
    
    public DocumentMasterKey[] getIterationSubscriptions() {
        if (mIterationSubscriptions != null) {
            Collection<DocumentMasterKey> subs = mIterationSubscriptions.get();
            return subs==null?null:subs.toArray(new DocumentMasterKey[subs.size()]);
        }
        return null;
    }
    
    public DocumentMasterKey[] getStateSubscriptions() {
        if (mStateSubscriptions != null) {
            Collection<DocumentMasterKey> subs = mStateSubscriptions.get();
            return subs==null?null:subs.toArray(new DocumentMasterKey[subs.size()]);
        }
        return null;
    }
    
    public void cacheIterationSubscriptions(DocumentMasterKey[] pSubKeys) {
        mIterationSubscriptions = new SoftReference<Collection<DocumentMasterKey>>(new HashSet<DocumentMasterKey>(Arrays.asList(pSubKeys)));
        System.out.println("Caching iteration subscriptions");
    }
    
    public void cacheStateSubscriptions(DocumentMasterKey[] pSubKeys) {
        mStateSubscriptions = new SoftReference<Collection<DocumentMasterKey>>(new HashSet<DocumentMasterKey>(Arrays.asList(pSubKeys)));
        System.out.println("Caching state subscriptions");
    }
    
    public void cacheCheckedOutDocMs(DocumentMaster[] pDocMs) {
        Set<DocumentMasterKey> pks = new HashSet<DocumentMasterKey>();
        for (DocumentMaster docM:pDocMs) {
            pks.add(cacheDocM(docM));
        }
        
        mCheckedOutPKs = new SoftReference<Collection<DocumentMasterKey>>(pks);
        System.out.println("Caching checked out document masters");
    }
    
    public WorkflowModel[] getWorkflowModels() {
        if (mWorkflowModels != null) {
            Collection<String> ids = mWorkflowModels.get();
            if (ids != null) {
                WorkflowModel[] models = new WorkflowModel[ids.size()];
                int i = 0;
                for (String id:ids) {
                    WorkflowModel model = getWorkflowModel(id);
                    if (model == null)
                        return null;
                    else
                        models[i++] = model;
                }
                return models;
            }
        }
        return null;
    }
    
    public DocumentMasterTemplate[] getDocMTemplates() {
        if (mDocMTemplates != null) {
            Collection<String> ids = mDocMTemplates.get();
            if (ids != null) {
                DocumentMasterTemplate[] templates = new DocumentMasterTemplate[ids.size()];
                int i = 0;
                for (String id:ids) {
                    DocumentMasterTemplate template = getDocMTemplate(id);
                    if (template == null)
                        return null;
                    else
                        templates[i++]=template;
                }
                return templates;
            }
        }
        return null;
    }
    
    public User[] getUsers() {
        if (mUsers != null) {
            Collection<String> logins = mUsers.get();
            if (logins != null) {
                User[] users = new User[logins.size()];
                int i = 0;
                for (String login:logins) {
                    User user =getUser(login);
                    if (users == null)
                        return null;
                    else
                        users[i++]=user;
                }
                return users;
            }
        }
        return null;
    }
    
    public String[] getTags() {
        Collection<String> tags = null;
        if (mTags != null) {
            tags = mTags.get();
        }
        return tags==null?null:tags.toArray(new String[tags.size()]);
    }
    
    public User getUser(String pLogin) {
        return mLogin2User.get(pLogin);
    }
 
    
    public void cacheTags(String[] pTags) {
        mTags = new SoftReference<Collection<String>>(new HashSet<String>(Arrays.asList(pTags)));
        System.out.println("Caching tags");
    }
    
    public String cacheUser(User pUser) {
        String login = pUser.getLogin();
        mLogin2User.put(login, pUser);
        System.out.println("Caching user " + pUser);
        return login;
    }
    
    public void cacheTag(String pTag) {
        if (mTags != null) {
            Collection<String> tags = mTags.get();
            if (tags != null) {
                tags.add(pTag);
            }
        }
        System.out.println("Caching tag " + pTag);
    }
    
    public void cacheIterationSubscription(DocumentMasterKey pSubKey) {
        if (mIterationSubscriptions != null) {
            Collection<DocumentMasterKey> subKeys = mIterationSubscriptions.get();
            if (subKeys != null) {
                subKeys.add(pSubKey);
            }
        }
        System.out.println("Caching iteration subscription " + pSubKey);
    }
    
    public void cacheStateSubscription(DocumentMasterKey pSubKey) {
        if (mStateSubscriptions != null) {
            Collection<DocumentMasterKey> subKeys = mStateSubscriptions.get();
            if (subKeys != null) {
                subKeys.add(pSubKey);
            }
        }
        System.out.println("Caching state subscription " + pSubKey);
    }
    
    public void removeIterationSubscription(DocumentMasterKey pSubKey) {
        if (mIterationSubscriptions != null) {
            Collection<DocumentMasterKey> subKeys = mIterationSubscriptions.get();
            if (subKeys != null) {
                subKeys.remove(pSubKey);
            }
        }
        System.out.println("Removing from cache iteration subscription " + pSubKey);
    }
    
    public void removeStateSubscription(DocumentMasterKey pSubKey) {
        if (mStateSubscriptions != null) {
            Collection<DocumentMasterKey> subKeys = mStateSubscriptions.get();
            if (subKeys != null) {
                subKeys.remove(pSubKey);
            }
        }
        System.out.println("Removing from cache state subscription " + pSubKey);
    }
    
    
    public void cacheWorkflowModels(WorkflowModel[] pModels) {
        Set<String> ids = new LinkedHashSet<String>();
        for (WorkflowModel model:pModels)
            ids.add(cacheWorkflowModel(model));
        mWorkflowModels = new SoftReference<Collection<String>>(ids);
        System.out.println("Caching workflow models");
    }
    
    public void cacheDocMTemplates(DocumentMasterTemplate[] pTemplates) {
        Set<String> ids = new LinkedHashSet<String>();
        for (DocumentMasterTemplate template:pTemplates)
            ids.add(cacheDocMTemplate(template));
        mDocMTemplates = new SoftReference<Collection<String>>(ids);
        System.out.println("Caching document master templates");
    }
    
    public void cacheUsers(User[] pUsers) {
        Set<String> logins = new LinkedHashSet<String>();
        for (User user:pUsers)
            logins.add(cacheUser(user));
        mUsers = new SoftReference<Collection<String>>(logins);
        System.out.println("Caching users");
    }
    
    public WorkflowModel getWorkflowModel(String pID) {
        return mID2WorkflowModel.get(pID);
    }
    
    public DocumentMasterTemplate getDocMTemplate(String pID) {
        return mID2DocMTemplate.get(pID);
    }
    
    public String cacheDocMTemplate(DocumentMasterTemplate pTemplate) {
        String id = pTemplate.getId();
        mID2DocMTemplate.put(id, pTemplate);
        
        if (mDocMTemplates != null) {
            Collection<String> ids = mDocMTemplates.get();
            if (ids != null) {
                ids.add(id);
            }
        }
        
        System.out.println("Caching document master template " + pTemplate);
        return id;
    }
    
    public String cacheWorkflowModel(WorkflowModel pWorkflowModel) {
        String id = pWorkflowModel.getId();
        mID2WorkflowModel.put(id, pWorkflowModel);
        
        if (mWorkflowModels != null) {
            Collection<String> ids = mWorkflowModels.get();
            if (ids != null) {
                ids.add(id);
            }
        }
        
        System.out.println("Caching workflow model " + pWorkflowModel);
        return id;
    }
    
    public FolderTreeNode[] getFolderTreeNodes(String pCompletePath) {
        Collection<FolderTreeNode> folders = mPath2Folders.get(pCompletePath);
        if (folders == null)
            return null;
        else {
            FolderTreeNode[] folderTreeNodes =
                    new FolderTreeNode[folders.size()];
            return folders.toArray(folderTreeNodes);
        }
    }
    
    public void cacheFolderTreeNodes(String pCompletePath,
            FolderTreeNode[] pFolderTreeNodes) {
        Set<FolderTreeNode> folders = new LinkedHashSet<FolderTreeNode>();
        for (int i = 0; i < pFolderTreeNodes.length; i++) {
            folders.add(pFolderTreeNodes[i]);
        }
        mPath2Folders.put(pCompletePath, folders);
        System.out.println("Caching folders in " + pCompletePath);
    }
    
    public void cacheFolderTreeNode(String pCompletePath,
            FolderTreeNode pFolderTreeNode) {
        Collection<FolderTreeNode> folders = mPath2Folders.get(pCompletePath);
        if (folders != null) {
            folders.add(pFolderTreeNode);
        }
        System.out.println("Updating cache, adding folder "
                + pFolderTreeNode
                + " in folder "
                + pCompletePath);
    }
    
    public String getLogin() {
        return mLogin;
    }
    
    public String getPassword(){
        return mPassword;
    }
    
    public Workspace getWorkspace() {
        return mWorkspace;
    }
    
    public User getUser() {
        return getUser(mLogin);
    }
    
    public void clear() {
        mCheckedOutPKs = null;
        mWorkflowModels = null;
        mDocMTemplates = null;
        mUsers = null;
        mTags = null;
        mIterationSubscriptions = null;
        mStateSubscriptions = null;
        mPK2DocM.clear();
        mPath2PKs.clear();
        mTag2PKs.clear();
        mPath2Folders.clear();
        mID2WorkflowModel.clear();
        mID2DocMTemplate.clear();
        mLogin2User.clear();
        System.out.println("Cleaning out cache");
    }
}
