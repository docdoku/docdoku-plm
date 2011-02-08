/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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
import com.docdoku.core.document.MasterDocumentKey;
import com.docdoku.core.document.MasterDocument;
import com.docdoku.core.document.MasterDocumentTemplate;
import com.docdoku.core.common.User;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.common.Workspace;
import java.lang.ref.*;


import java.util.*;

public class Cache {
    
    private Map<MasterDocumentKey, MasterDocument> mPK2MDoc;
    private Map<String, Collection<MasterDocumentKey>> mPath2PKs;
    private Map<String, Collection<FolderTreeNode>> mPath2Folders;
    private Map<String, Collection<MasterDocumentKey>> mTag2PKs;
    private SoftReference<Collection<MasterDocumentKey>> mCheckedOutPKs;
    private SoftReference<Collection<String>> mWorkflowModels;
    private Map<String,WorkflowModel> mID2WorkflowModel;
    private SoftReference<Collection<String>> mMDocTemplates;
    private Map<String,MasterDocumentTemplate> mID2MDocTemplate;
    private SoftReference<Collection<String>> mUsers;
    private SoftReference<Collection<String>> mTags;
    private Map<String,User> mLogin2User;
    private SoftReference<Collection<MasterDocumentKey>> mIterationSubscriptions;
    private SoftReference<Collection<MasterDocumentKey>> mStateSubscriptions;
    
    private final String mLogin;
    private final String mPassword;
    private final Workspace mWorkspace;
    
    public Cache(String pLogin, String pPassword, Workspace pWorkspace) {
        mWorkspace = pWorkspace;
        mLogin = pLogin;
        mPassword = pPassword;
        
        mPK2MDoc = new SoftValueHashMap<MasterDocumentKey, MasterDocument>();
        mPath2PKs = new SoftValueHashMap<String, Collection<MasterDocumentKey>>();
        mTag2PKs = new SoftValueHashMap<String, Collection<MasterDocumentKey>>();
        mPath2Folders = new SoftValueHashMap<String, Collection<FolderTreeNode>>();
        mID2WorkflowModel = new SoftValueHashMap<String,WorkflowModel>();
        mID2MDocTemplate = new SoftValueHashMap<String,MasterDocumentTemplate>();
        mLogin2User = new SoftValueHashMap<String,User>();
    }
    
    
    public MasterDocument getMDoc(MasterDocumentKey pPK) {
        return mPK2MDoc.get(pPK);
    }
    
    public MasterDocumentKey cacheMDoc(MasterDocument pMDoc) {
        MasterDocumentKey pk =pMDoc.getKey();
        
        MasterDocument oldMDoc=mPK2MDoc.get(pk);
        //firstable we remove all references of the old mdoc
        if(oldMDoc!=null)
            removeMDoc(oldMDoc);
        
        mPK2MDoc.put(pk, pMDoc);
        
        Collection<MasterDocumentKey> pks = mPath2PKs.get(pMDoc.getLocation().getCompletePath());
        if (pks != null) {
            pks.add(pk);
        }
        
        for(Tag tag:pMDoc.getTags()){
            pks = mTag2PKs.get(tag.getLabel());
            if (pks != null) {
                pks.add(pk);
            }
        }
        
        
        if(pMDoc.isCheckedOutBy(mLogin)){
            if (mCheckedOutPKs != null) {
                Collection<MasterDocumentKey> checkedOutPKs = mCheckedOutPKs.get();
                if (checkedOutPKs != null) {
                    checkedOutPKs.add(pk);
                }
            }
        }
        
        for(Tag tag:pMDoc.getTags())
            cacheTag(tag.getLabel());
        
        System.out.println("Caching master document " + pMDoc);
        return pk;
    }
    
    public void removeMDoc(MasterDocument pMDoc) {
        Collection<MasterDocumentKey> pks = mPath2PKs.get(pMDoc.getLocation().getCompletePath());
        if (pks != null)
            pks.remove(pMDoc.getKey());
        
        for(Tag tag:pMDoc.getTags()){
            pks = mTag2PKs.get(tag.getLabel());
            if (pks != null) {
                pks.remove(pMDoc.getKey());
            }
        }
        
        if (mCheckedOutPKs != null) {
            Collection<MasterDocumentKey> checkedOutPKs = mCheckedOutPKs.get();
            if (checkedOutPKs != null) {
                checkedOutPKs.remove(pMDoc.getKey());
            }
        }
        mPK2MDoc.remove(pMDoc.getKey());
        System.out.println("Removing from cache master document " + pMDoc);
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
        Collection<MasterDocumentKey> pks = mPath2PKs.get(pCompletePath);
        if (pks != null) {
            for(MasterDocumentKey pk:pks){
                MasterDocument mdoc = mPK2MDoc.get(pk);
                if(mdoc!=null)
                  mdoc.setLocation(pNewFolderTreeNode.getFolder());

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
        Collection<MasterDocumentKey> pks = mPath2PKs.get(pCompletePath);
        if (pks != null) {
            if (mCheckedOutPKs != null) {
                Collection<MasterDocumentKey> checkedOutPKs = mCheckedOutPKs.get();
                if (checkedOutPKs != null) {
                    checkedOutPKs.removeAll(pks);
                }
            }
            Iterator<Collection<MasterDocumentKey>> ite=mTag2PKs.values().iterator();
            while(ite.hasNext()){
                Collection<MasterDocumentKey> taggedPKs =ite.next();
                if (taggedPKs != null) {
                    taggedPKs.removeAll(pks);
                }
            }
            for(MasterDocumentKey pk:pks)
                mPK2MDoc.remove(pk);
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
        Collection<MasterDocumentKey> pks = mTag2PKs.remove(pTag);
        if (pks != null) {
            for(MasterDocumentKey pk:pks){
                MasterDocument mdoc=getMDoc(pk);
                if (mdoc != null){
                    mdoc.removeTag(new Tag(mdoc.getWorkspace(),pTag));
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
    
    public void removeMDocTemplate(MasterDocumentTemplate pTemplate) {
        if (mMDocTemplates != null) {
            Collection<String> ids = mMDocTemplates.get();
            if (ids != null) {
                ids.remove(pTemplate.getId());
            }
        }
        mID2MDocTemplate.remove(pTemplate.getId());
        System.out.println("Removing from cache master document template " + pTemplate);
    }
    
    public MasterDocument[] findMDocsByFolder(String pCompletePath) {
        Collection<MasterDocumentKey> pks = mPath2PKs.get(pCompletePath);
        if (pks != null) {
            MasterDocument[] mdocs = new MasterDocument[pks.size()];
            int i = 0;
            for (MasterDocumentKey pk:pks) {
                MasterDocument mdoc=getMDoc(pk);
                if (mdoc == null)
                    return null;
                else
                    mdocs[i++]=mdoc;
            }
            return mdocs;
        }
        return null;
    }
    
    public MasterDocument[] findMDocsByTag(String pTag) {
        Collection<MasterDocumentKey> pks = mTag2PKs.get(pTag);
        if (pks != null) {
            MasterDocument[] mdocs = new MasterDocument[pks.size()];
            int i = 0;
            for (MasterDocumentKey pk:pks) {
                MasterDocument mdoc=getMDoc(pk);
                if (mdoc == null)
                    return null;
                else
                    mdocs[i++]=mdoc;
            }
            return mdocs;
        }
        return null;
    }
    
    public void cacheMDocsByFolder(String pCompletePath, MasterDocument[] pMDocs) {
        Set<MasterDocumentKey> pks = new HashSet<MasterDocumentKey>();
        for (MasterDocument mdoc:pMDocs) {
            pks.add(cacheMDoc(mdoc));
        }
        mPath2PKs.put(pCompletePath, pks);
        System.out.println("Caching master documents by folder " + pCompletePath);
    }
    
    public void cacheMDocsByTag(String pTag, MasterDocument[] pMDocs) {
        Set<MasterDocumentKey> pks = new HashSet<MasterDocumentKey>();
        for (MasterDocument mdoc:pMDocs) {
            pks.add(cacheMDoc(mdoc));
        }
        mTag2PKs.put(pTag, pks);
        System.out.println("Caching master documents by tag " + pTag);
    }
    
    public MasterDocument[] getCheckedOutMDocs() {
        if (mCheckedOutPKs != null) {
            Collection<MasterDocumentKey> pks = mCheckedOutPKs.get();
            if (pks != null) {
                MasterDocument[] mdocs = new MasterDocument[pks.size()];
                int i = 0;
                for (MasterDocumentKey pk:pks) {
                    MasterDocument mdoc=getMDoc(pk);
                    if (mdoc == null)
                        return null;
                    else
                        mdocs[i++]=mdoc;
                }
                return mdocs;
            }
        }
        return null;
    }
    
    public MasterDocumentKey[] getIterationSubscriptions() {
        if (mIterationSubscriptions != null) {
            Collection<MasterDocumentKey> subs = mIterationSubscriptions.get();
            return subs==null?null:subs.toArray(new MasterDocumentKey[subs.size()]);
        }
        return null;
    }
    
    public MasterDocumentKey[] getStateSubscriptions() {
        if (mStateSubscriptions != null) {
            Collection<MasterDocumentKey> subs = mStateSubscriptions.get();
            return subs==null?null:subs.toArray(new MasterDocumentKey[subs.size()]);
        }
        return null;
    }
    
    public void cacheIterationSubscriptions(MasterDocumentKey[] pSubKeys) {
        mIterationSubscriptions = new SoftReference<Collection<MasterDocumentKey>>(new HashSet<MasterDocumentKey>(Arrays.asList(pSubKeys)));
        System.out.println("Caching iteration subscriptions");
    }
    
    public void cacheStateSubscriptions(MasterDocumentKey[] pSubKeys) {
        mStateSubscriptions = new SoftReference<Collection<MasterDocumentKey>>(new HashSet<MasterDocumentKey>(Arrays.asList(pSubKeys)));
        System.out.println("Caching state subscriptions");
    }
    
    public void cacheCheckedOutMDocs(MasterDocument[] pMDocs) {
        Set<MasterDocumentKey> pks = new HashSet<MasterDocumentKey>();
        for (MasterDocument mdoc:pMDocs) {
            pks.add(cacheMDoc(mdoc));
        }
        
        mCheckedOutPKs = new SoftReference<Collection<MasterDocumentKey>>(pks);
        System.out.println("Caching checked out master documents");
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
    
    public MasterDocumentTemplate[] getMDocTemplates() {
        if (mMDocTemplates != null) {
            Collection<String> ids = mMDocTemplates.get();
            if (ids != null) {
                MasterDocumentTemplate[] templates = new MasterDocumentTemplate[ids.size()];
                int i = 0;
                for (String id:ids) {
                    MasterDocumentTemplate template = getMDocTemplate(id);
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
    
    public void cacheIterationSubscription(MasterDocumentKey pSubKey) {
        if (mIterationSubscriptions != null) {
            Collection<MasterDocumentKey> subKeys = mIterationSubscriptions.get();
            if (subKeys != null) {
                subKeys.add(pSubKey);
            }
        }
        System.out.println("Caching iteration subscription " + pSubKey);
    }
    
    public void cacheStateSubscription(MasterDocumentKey pSubKey) {
        if (mStateSubscriptions != null) {
            Collection<MasterDocumentKey> subKeys = mStateSubscriptions.get();
            if (subKeys != null) {
                subKeys.add(pSubKey);
            }
        }
        System.out.println("Caching state subscription " + pSubKey);
    }
    
    public void removeIterationSubscription(MasterDocumentKey pSubKey) {
        if (mIterationSubscriptions != null) {
            Collection<MasterDocumentKey> subKeys = mIterationSubscriptions.get();
            if (subKeys != null) {
                subKeys.remove(pSubKey);
            }
        }
        System.out.println("Removing from cache iteration subscription " + pSubKey);
    }
    
    public void removeStateSubscription(MasterDocumentKey pSubKey) {
        if (mStateSubscriptions != null) {
            Collection<MasterDocumentKey> subKeys = mStateSubscriptions.get();
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
        System.out.println("Caching work flow models");
    }
    
    public void cacheMDocTemplates(MasterDocumentTemplate[] pTemplates) {
        Set<String> ids = new LinkedHashSet<String>();
        for (MasterDocumentTemplate template:pTemplates)
            ids.add(cacheMDocTemplate(template));
        mMDocTemplates = new SoftReference<Collection<String>>(ids);
        System.out.println("Caching master document templates");
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
    
    public MasterDocumentTemplate getMDocTemplate(String pID) {
        return mID2MDocTemplate.get(pID);
    }
    
    public String cacheMDocTemplate(MasterDocumentTemplate pTemplate) {
        String id = pTemplate.getId();
        mID2MDocTemplate.put(id, pTemplate);
        
        if (mMDocTemplates != null) {
            Collection<String> ids = mMDocTemplates.get();
            if (ids != null) {
                ids.add(id);
            }
        }
        
        System.out.println("Caching master document template " + pTemplate);
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
        mMDocTemplates = null;
        mUsers = null;
        mTags = null;
        mIterationSubscriptions = null;
        mStateSubscriptions = null;
        mPK2MDoc.clear();
        mPath2PKs.clear();
        mTag2PKs.clear();
        mPath2Folders.clear();
        mID2WorkflowModel.clear();
        mID2MDocTemplate.clear();
        mLogin2User.clear();
        System.out.println("Cleaning out cache");
    }
}
