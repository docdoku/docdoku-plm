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

import com.docdoku.core.*;
import com.docdoku.core.entities.BinaryResource;
import com.docdoku.core.entities.Document;
import com.docdoku.core.entities.Folder;
import com.docdoku.core.entities.keys.BasicElementKey;
import com.docdoku.core.entities.keys.MasterDocumentKey;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.MasterDocumentTemplate;
import com.docdoku.core.entities.User;
import com.docdoku.core.entities.keys.TagKey;
import com.docdoku.core.entities.keys.Version;
import com.docdoku.core.entities.WorkflowModel;
import com.docdoku.core.entities.Workspace;
import com.docdoku.core.util.Tools;
import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.*;
import java.util.*;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.InstanceAttribute;
import com.docdoku.core.entities.SearchQuery;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.StreamingDataHandler;
import java.io.InterruptedIOException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

public class MainModel {

    public class ModelUpdater {

        private ModelUpdater() {

        }

        public void createFolder(String pParentFolder, String pShortName) {
            FolderTreeNode[] path = MainModel.this.getElementsTreeModel().getPath(pParentFolder);

            FolderTreeNode[] children = {new FolderTreeNode(pParentFolder + "/" + pShortName, path[path.length - 1])};
            mCache.cacheFolderTreeNode(pParentFolder, children[0]);

            int[] childIndices = {path[path.length - 1].getFolderIndexOfChild(children[0])};

            MainModel.this.getElementsTreeModel().fireTreeNodesInserted(path,
                    childIndices, children);
        }

        public void createMDocInFolder(MasterDocument pMasterDocument) {
            mCache.cacheMDoc(pMasterDocument);
            insertIntoTables(pMasterDocument);
        }

        public void createMDocTemplate(MasterDocumentTemplate pTemplate) {
            mCache.cacheMDocTemplate(pTemplate);
            insertIntoTables(pTemplate);
        }

        public void updateMDocTemplate(MasterDocumentTemplate pTemplate) {
            mCache.cacheMDocTemplate(pTemplate);
        }

        public void saveWorkflowModel(WorkflowModel pWorkflowModel) {
            WorkflowModel previousModel = mCache.getWorkflowModel(pWorkflowModel.getId());
            mCache.cacheWorkflowModel(pWorkflowModel);
            if (previousModel == null) {
                insertIntoTables(pWorkflowModel);
            } else {
                updateTables(pWorkflowModel);
            }
        }

        public void updateMDoc(MasterDocument pMDoc) {
            mCache.cacheMDoc(pMDoc);
            MainModel.this.getElementsTreeModel().fireTagTreeStructureChanged();
        }

        public void updateUser(User pUser) {
            mCache.cacheUser(pUser);
        }

        public void delMDoc(MasterDocument pMasterDocument) {
            List<Integer> indexes = indexesOfElement(pMasterDocument);
            int i = 0;
            mCache.removeMDoc(pMasterDocument);
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        pMasterDocument.getLocation().getCompletePath())) {
                    int row = indexes.get(i++);
                    model.fireTableRowsDeleted(row, row);
                }

            }
        }

        public void delMDocTemplate(MasterDocumentTemplate pTemplate) {
            List<Integer> indexes = indexesOfElement(pTemplate);
            int i = 0;
            mCache.removeMDocTemplate(pTemplate);
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        TemplateTreeNode.TEMPLATE_MODEL_PATH)) {
                    int row = indexes.get(i++);
                    model.fireTableRowsDeleted(row, row);
                }
            }
        }

        public void moveMDoc(MasterDocument pTargetMDoc) {
            insertIntoTables(pTargetMDoc);
            mCache.cacheMDoc(pTargetMDoc);
        }

        public void renameFolder(String pCompletePath, String pNewName) {
            Folder parentFolder = new Folder(pCompletePath).getParentFolder();
            FolderTreeNode[] path = MainModel.this.getElementsTreeModel().getPath(parentFolder.getCompletePath());
            mCache.moveFolder(pCompletePath, new FolderTreeNode(parentFolder.getCompletePath() + "/" + pNewName, path[path.length - 1]));

            MainModel.this.getElementsTreeModel().fireTreeStructureChanged(path);
        }

        public void delFolder(String pCompletePath) {
            Folder parentFolder = new Folder(pCompletePath).getParentFolder();
            FolderTreeNode[] path = MainModel.this.getElementsTreeModel().getPath(parentFolder.getCompletePath());

            FolderTreeNode[] children = {new FolderTreeNode(pCompletePath,
                path[path.length - 1])
            };

            int[] childIndices = {path[path.length - 1].getFolderIndexOfChild(children[0])};
            //fireTreeStructureChanged just for the case the structure display on the screen
            //is not the one that is stored on the model
            MainModel.this.getElementsTreeModel().fireTreeStructureChanged(path);
            MainModel.this.getElementsTreeModel().fireTreeNodesRemoved(path,
                        childIndices, children);
                mCache.removeFolder(pCompletePath);
        }

        public void delTag(String pTag) {
            FolderTreeNode[] path = new FolderTreeNode[2];
            ElementsTreeModel treeModel = MainModel.this.getElementsTreeModel();
            path[0] = treeModel.getRoot();
            path[1] = treeModel.getRoot().getTagRootTreeNode();
            FolderTreeNode[] children = {new TagTreeNode(path[1].getCompletePath() + "/" + pTag,
                path[1])
            };

            int[] childIndices = {path[1].getFolderIndexOfChild(children[0])};

            MainModel.this.getElementsTreeModel().fireTreeNodesRemoved(path,
                    childIndices, children);
            mCache.removeTag(pTag);
        }

        public void delWorkflowModel(WorkflowModel pWorkflowModel) {
            List<Integer> indexes = indexesOfElement(pWorkflowModel);
            int i = 0;
            mCache.removeWorkflowModel(pWorkflowModel);
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        WorkflowModelTreeNode.WORKFLOW_MODEL_PATH)) {
                    int row = indexes.get(i++);
                    System.out.println("ROW: " + row);
                    model.fireTableRowsDeleted(row, row);

                }


            }
        }

        public void checkIn(MasterDocument pMasterDocument) {
            mCache.cacheMDoc(pMasterDocument);
            updateTables(pMasterDocument);
        }

        public void checkOut(MasterDocument pMasterDocument) {
            mCache.cacheMDoc(pMasterDocument);
            updateTables(pMasterDocument);
        }

        public void makeNewVersion(MasterDocument[] pOriginalAndNewMDoc) {
            mCache.cacheMDoc(pOriginalAndNewMDoc[0]);
            updateTables(pOriginalAndNewMDoc[0]);
            mCache.cacheMDoc(pOriginalAndNewMDoc[1]);
            insertIntoTables(pOriginalAndNewMDoc[1]);
        }

        public void undoCheckOut(MasterDocument pMasterDocument) {
            mCache.cacheMDoc(pMasterDocument);
            updateTables(pMasterDocument);
        }

        public void addStateNotification(MasterDocument pMasterDocument) {
            mCache.cacheStateSubscription(pMasterDocument.getKey());
        }

        public void removeStateNotification(MasterDocument pMasterDocument) {
            mCache.removeStateSubscription(pMasterDocument.getKey());
        }

        public void addIterationNotification(MasterDocument pMasterDocument) {
            mCache.cacheIterationSubscription(pMasterDocument.getKey());
        }

        public void removeIterationNotification(MasterDocument pMasterDocument) {
            mCache.removeIterationSubscription(pMasterDocument.getKey());
        }

        public void refreshTree() {
            MainModel.this.getElementsTreeModel().fireAllTreeNodesChanged();
        }

        public void clear() {
            mCache.clear();
            MainModel.this.getElementsTreeModel().fireAllTreeStructureChanged();
        }

        private void updateTables(MasterDocument pMasterDocument) {
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        pMasterDocument.getLocation().getCompletePath())) {
                    int row = model.getIndexOfElement(pMasterDocument);
                    model.fireTableRowsUpdated(row, row);
                }
            }
        }

        private void updateTables(WorkflowModel pWorkflowModel) {
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        WorkflowModelTreeNode.WORKFLOW_MODEL_PATH)) {
                    int row = model.getIndexOfElement(pWorkflowModel);
                    model.fireTableRowsUpdated(row, row);
                }
            }
        }

        private void insertIntoTables(WorkflowModel pWorkflowModel) {
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        WorkflowModelTreeNode.WORKFLOW_MODEL_PATH)) {
                    int row = model.getIndexOfElement(pWorkflowModel);
                    model.fireTableRowsInserted(row, row);
                }
            }
        }

        private void insertIntoTables(MasterDocumentTemplate pTemplate) {
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        TemplateTreeNode.TEMPLATE_MODEL_PATH)) {
                    int row = model.getIndexOfElement(pTemplate);
                    model.fireTableRowsInserted(row, row);
                }
            }
        }

        private void insertIntoTables(MasterDocument pMasterDocument) {
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        pMasterDocument.getLocation().getCompletePath())) {
                    int row = model.getIndexOfElement(pMasterDocument);
                    model.fireTableRowsInserted(row, row);
                }
            }
        }

        private List<Integer> indexesOfElement(MasterDocument pMasterDocument) {
            List<Integer> indexes = new LinkedList<Integer>();
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        pMasterDocument.getLocation().getCompletePath())) {
                    int row = model.getIndexOfElement(pMasterDocument);
                    indexes.add(row);
                }
            }
            return indexes;
        }

        private List<Integer> indexesOfElement(WorkflowModel pWorkflowModel) {
            List<Integer> indexes = new LinkedList<Integer>();
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        WorkflowModelTreeNode.WORKFLOW_MODEL_PATH)) {
                    int row = model.getIndexOfElement(pWorkflowModel);
                    indexes.add(row);
                }
            }
            return indexes;
        }

        private List<Integer> indexesOfElement(MasterDocumentTemplate pTemplate) {
            List<Integer> indexes = new LinkedList<Integer>();
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        TemplateTreeNode.TEMPLATE_MODEL_PATH)) {
                    int row = model.getIndexOfElement(pTemplate);
                    indexes.add(row);
                }
            }
            return indexes;
        }
    }
    private Cache mCache;
    private ElementsTreeModel mElementsTreeModel;
    private List<FolderBasedElementsTableModel> mElementTableModels;
    public final ModelUpdater updater;
    private ICommandWS mCommandService;
    private IUploadDownloadWS mFileService;
    private static MainModel sSingleton;

    public static MainModel getInstance() {
        if (sSingleton == null) {
            throw new NotInitializedException(MainModel.class.getName());
        }

        return sSingleton;
    }

    public static MainModel init(String pLogin, String pPassword, String pWorkspaceId, ICommandWS pCommandService, IUploadDownloadWS pFileService)
            throws InitializationException {
        if (sSingleton == null) {
            sSingleton = new MainModel(pLogin, pPassword, pWorkspaceId, pCommandService, pFileService);
            return sSingleton;
        } else {
            throw new AlreadyInitializedException(MainModel.class.getName());
        }
    }

    private MainModel(String pLogin, String pPassword, String pWorkspaceId, ICommandWS pCommandService, IUploadDownloadWS pFileService) throws InitializationException {
        mCommandService = pCommandService;
        mFileService = pFileService;
        mElementTableModels = new LinkedList<FolderBasedElementsTableModel>();
        try {
            Workspace workspace = mCommandService.getWorkspace(pWorkspaceId);
            mCache = new Cache(pLogin, pPassword, workspace);
        } catch (Exception pEx) {
            throw new InitializationException(
                    "Enable to retrieve the login and the workspace", pEx);
        }

        updater = new ModelUpdater();
    }


    public FolderTreeNode[] getFolderTreeNodes(FolderTreeNode pParent) {
        String completePath = pParent.getCompletePath();
        FolderTreeNode[] folderTreeNodes = mCache.getFolderTreeNodes(completePath);
        if (folderTreeNodes == null) {
            try {
                System.out.println("Retrieving folders in " + completePath);
                String[] folders = mCommandService.getFolders(completePath);
                folderTreeNodes = new FolderTreeNode[folders.length];
                for (int i = 0; i < folderTreeNodes.length; i++) {
                    folderTreeNodes[i] = new FolderTreeNode(completePath + "/" + folders[i], pParent);
                }

                mCache.cacheFolderTreeNodes(completePath, folderTreeNodes);
            } catch (WebServiceException pWSEx) {
                String message;
                Throwable t = pWSEx.getCause();
                if (t != null) {
                    message = t.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : t.getMessage();
                } else {
                    message = pWSEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pWSEx.getMessage();
                }

                showContinueOrExitDialog(message);
            } catch (Exception pEx) {
                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                showContinueOrExitDialog(message);
            }
        }
        return folderTreeNodes;
    }


    public WorkflowModel[] getWorkflowModels() {
        WorkflowModel[] models = mCache.getWorkflowModels();

        if (models == null) {
            try {
                System.out.println("Retrieving workflow models");
                models = Tools.resetParentReferences(mCommandService.getWorkflowModels(getWorkspace().getId()));
                mCache.cacheWorkflowModels(models);
            } catch (WebServiceException pWSEx) {
                String message;
                Throwable t = pWSEx.getCause();
                if (t != null) {
                    message = t.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : t.getMessage();
                } else {
                    message = pWSEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pWSEx.getMessage();
                }

                showContinueOrExitDialog(message);
            } catch (Exception pEx) {
                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                showContinueOrExitDialog(message);
            }
        }
        return models;
    }

    public MasterDocumentTemplate[] getMDocTemplates() {
        MasterDocumentTemplate[] templates = mCache.getMDocTemplates();

        if (templates == null) {
            try {
                System.out.println("Retrieving templates");
                templates = Tools.resetParentReferences(mCommandService.getMDocTemplates(getWorkspace().getId()));
                mCache.cacheMDocTemplates(templates);
            } catch (WebServiceException pWSEx) {
                String message;
                Throwable t = pWSEx.getCause();
                if (t != null) {
                    message = t.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : t.getMessage();
                } else {
                    message = pWSEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pWSEx.getMessage();
                }

                showContinueOrExitDialog(message);
            } catch (Exception pEx) {
                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                showContinueOrExitDialog(message);
            }
        }
        return templates;
    }

    public MasterDocumentTemplate getMDocTemplate(String pId) {
        MasterDocumentTemplate template = mCache.getMDocTemplate(pId);

        if (template == null) {
            try {
                System.out.println("Retrieving document template " + pId);
                template = Tools.resetParentReferences(mCommandService.getMDocTemplate(new BasicElementKey(getWorkspace().getId(), pId)));
                mCache.cacheMDocTemplate(template);
            } catch (WebServiceException pWSEx) {
                String message;
                Throwable t = pWSEx.getCause();
                if (t != null) {
                    message = t.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : t.getMessage();
                } else {
                    message = pWSEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pWSEx.getMessage();
                }

                showContinueOrExitDialog(message);
            } catch (Exception pEx) {
                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                JOptionPane.showMessageDialog(null,
                        message, I18N.BUNDLE.getString("Error_title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return template;
    }

    public WorkflowModel getWorkflowModel(String pId) {
        WorkflowModel workflowModel = mCache.getWorkflowModel(pId);

        if (workflowModel == null) {
            try {
                System.out.println("Retrieving workflow model " + pId);
                workflowModel = Tools.resetParentReferences(mCommandService.getWorkflowModel(new BasicElementKey(getWorkspace().getId(), pId)));
                mCache.cacheWorkflowModel(workflowModel);
            } catch (WebServiceException pWSEx) {
                String message;
                Throwable t = pWSEx.getCause();
                if (t != null) {
                    message = t.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : t.getMessage();
                } else {
                    message = pWSEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pWSEx.getMessage();
                }

                showContinueOrExitDialog(message);
            } catch (Exception pEx) {
                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                JOptionPane.showMessageDialog(null,
                        message, I18N.BUNDLE.getString("Error_title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return workflowModel;
    }

    public MasterDocument getMDoc(MasterDocumentKey pMDocPK) {
        MasterDocument mdoc = mCache.getMDoc(pMDocPK);

        if (mdoc == null) {
            try {
                System.out.println("Retrieving master document " + pMDocPK);
                mdoc = Tools.resetParentReferences(mCommandService.getMDoc(pMDocPK));
                mCache.cacheMDoc(mdoc);
            } catch (WebServiceException pWSEx) {
                String message;
                Throwable t = pWSEx.getCause();
                if (t != null) {
                    message = t.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : t.getMessage();
                } else {
                    message = pWSEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pWSEx.getMessage();
                }

                showContinueOrExitDialog(message);
            } catch (Exception pEx) {
                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                JOptionPane.showMessageDialog(null,
                        message, I18N.BUNDLE.getString("Error_title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return mdoc;
    }

    public MasterDocumentKey[] getStateChangeEventSubscriptions() {
        MasterDocumentKey[] subKeys = mCache.getStateSubscriptions();

        if (subKeys == null) {
            try {
                System.out.println("Retrieving state subscriptions");
                subKeys = mCommandService.getStateChangeEventSubscriptions(getWorkspace().getId());
                mCache.cacheStateSubscriptions(subKeys);
            } catch (WebServiceException pWSEx) {
                String message;
                Throwable t = pWSEx.getCause();
                if (t != null) {
                    message = t.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : t.getMessage();
                } else {
                    message = pWSEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pWSEx.getMessage();
                }

                showContinueOrExitDialog(message);
            } catch (Exception pEx) {
                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                showContinueOrExitDialog(message);
            }
        }
        return subKeys;
    }

    public boolean hasIterationChangeEventSubscription(MasterDocument pMDoc) {
        MasterDocumentKey[] subKeys = getIterationChangeEventSubscriptions();
        return Arrays.asList(subKeys).contains(pMDoc.getKey());
    }

    public boolean hasStateChangeEventSubscription(MasterDocument pMDoc) {
        MasterDocumentKey[] subKeys = getStateChangeEventSubscriptions();
        return Arrays.asList(subKeys).contains(pMDoc.getKey());
    }

    public MasterDocumentKey[] getIterationChangeEventSubscriptions() {
        MasterDocumentKey[] subKeys = mCache.getIterationSubscriptions();

        if (subKeys == null) {
            try {
                System.out.println("Retrieving iteration subscriptions");
                subKeys = mCommandService.getIterationChangeEventSubscriptions(getWorkspace().getId());
                mCache.cacheIterationSubscriptions(subKeys);
            } catch (WebServiceException pWSEx) {
                String message;
                Throwable t = pWSEx.getCause();
                if (t != null) {
                    message = t.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : t.getMessage();
                } else {
                    message = pWSEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pWSEx.getMessage();
                }

                showContinueOrExitDialog(message);
            } catch (Exception pEx) {
                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                showContinueOrExitDialog(message);
            }
        }
        return subKeys;
    }

    public MasterDocument[] getCheckedOutMDocs() {
        MasterDocument[] mdocs = mCache.getCheckedOutMDocs();

        if (mdocs == null) {
            try {
                System.out.println("Retrieving personnal checked out master documents");
                mdocs = Tools.resetParentReferences(mCommandService.getCheckedOutMDocs(getWorkspace().getId()));
                mCache.cacheCheckedOutMDocs(mdocs);
            } catch (WebServiceException pWSEx) {
                String message;
                Throwable t = pWSEx.getCause();
                if (t != null) {
                    message = t.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : t.getMessage();
                } else {
                    message = pWSEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pWSEx.getMessage();
                }

                showContinueOrExitDialog(message);
            } catch (Exception pEx) {
                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                showContinueOrExitDialog(message);
            }
        }
        return mdocs;
    }

    public MasterDocument[] findMDocsByTag(String pTag) {
        MasterDocument[] mdocs = mCache.findMDocsByTag(pTag);

        if (mdocs == null) {
            try {
                System.out.println("Searching master documents by tag " + pTag);
                mdocs = Tools.resetParentReferences(mCommandService.findMDocsByTag(new TagKey(getWorkspace().getId(), pTag)));
                mCache.cacheMDocsByTag(pTag, mdocs);
            } catch (WebServiceException pWSEx) {
                String message;
                Throwable t = pWSEx.getCause();
                if (t != null) {
                    message = t.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : t.getMessage();
                } else {
                    message = pWSEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pWSEx.getMessage();
                }

                showContinueOrExitDialog(message);
            } catch (Exception pEx) {
                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                showContinueOrExitDialog(message);
            }
        }
        return mdocs;
    }

    public MasterDocument[] findMDocsByFolder(String pCompletePath) {
        MasterDocument[] mdocs = mCache.findMDocsByFolder(pCompletePath);

        if (mdocs == null) {
            try {
                System.out.println("Searching master documents by folder " + pCompletePath);
                mdocs = Tools.resetParentReferences(mCommandService.findMDocsByFolder(pCompletePath));
                mCache.cacheMDocsByFolder(pCompletePath, mdocs);
            } catch (WebServiceException pWSEx) {
                String message;
                Throwable t = pWSEx.getCause();
                if (t != null) {
                    message = t.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : t.getMessage();
                } else {
                    message = pWSEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pWSEx.getMessage();
                }

                showContinueOrExitDialog(message);
            } catch (Exception pEx) {
                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                showContinueOrExitDialog(message);
            }
        }
        return mdocs;
    }

    public String getLogin() {
        return mCache.getLogin();
    }

    public String getPassword() {
        return mCache.getPassword();
    }

    public Workspace getWorkspace() {
        return mCache.getWorkspace();
    }

    public User getUser() {
        User user = mCache.getUser();

        if (user == null) {
            try {
                System.out.println("Retrieving personnal informations");
                user = mCommandService.whoAmI(getWorkspace().getId());
                mCache.cacheUser(user);
            } catch (WebServiceException pWSEx) {
                String message;
                Throwable t = pWSEx.getCause();
                if (t != null) {
                    message = t.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : t.getMessage();
                } else {
                    message = pWSEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pWSEx.getMessage();
                }

                showContinueOrExitDialog(message);
            } catch (Exception pEx) {
                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                JOptionPane.showMessageDialog(null,
                        message, I18N.BUNDLE.getString("Error_title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return user;
    }

    public User[] getUsers() {
        User[] users = mCache.getUsers();

        if (users == null) {
            try {
                System.out.println("Retrieving users");
                users = mCommandService.getUsers(getWorkspace().getId());
                mCache.cacheUsers(users);
            } catch (WebServiceException pWSEx) {
                String message;
                Throwable t = pWSEx.getCause();
                if (t != null) {
                    message = t.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : t.getMessage();
                } else {
                    message = pWSEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pWSEx.getMessage();
                }

                showContinueOrExitDialog(message);
            } catch (Exception pEx) {
                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                JOptionPane.showMessageDialog(null,
                        message, I18N.BUNDLE.getString("Error_title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return users;
    }

    public String getGeneratedId(String pWorkspaceId, String pMDocTemplateId) {
        String generatedId = null;
        try {
            System.out.println("Retrieving generated master document id");
            generatedId = mCommandService.generateId(pWorkspaceId, pMDocTemplateId);
        } catch (Exception pEx) {
        //TODO treat exception ?
        }
        return generatedId;
    }

    public String[] getTags() {
        String[] tags = mCache.getTags();

        if (tags == null) {
            try {
                System.out.println("Retrieving tags");
                tags = mCommandService.getTags(getWorkspace().getId());
                mCache.cacheTags(tags);
            } catch (WebServiceException pWSEx) {
                String message;
                Throwable t = pWSEx.getCause();
                if (t != null) {
                    message = t.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : t.getMessage();
                } else {
                    message = pWSEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pWSEx.getMessage();
                }

                showContinueOrExitDialog(message);
            } catch (Exception pEx) {
                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                showContinueOrExitDialog(message);
            }
        }
        return tags;
    }

    public ElementsTreeModel getElementsTreeModel() {
        if (mElementsTreeModel == null) {
            mElementsTreeModel = new ElementsTreeModel();
        }
        return mElementsTreeModel;
    }
    /*
    private void downloadFileWithServlet(Component pParent, File pLocalFile, String pURL) throws IOException {
    System.out.println("Downloading file");
    ProgressMonitorInputStream in = null;
    OutputStream out = null;
    HttpURLConnection conn = null;
    try {
    out = new BufferedOutputStream(new FileOutputStream(pLocalFile), Config.BUFFER_CAPACITY);
    URL url = new URL(pURL);
    conn = (HttpURLConnection) url.openConnection();
    conn.setUseCaches(false);
    conn.setAllowUserInteraction(true);
    conn.setRequestProperty("Connection", "Keep-Alive");
    conn.setRequestMethod("GET");
    byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64((getLogin() + ":" + getPassword()).getBytes("ISO-8859-1"));
    conn.setRequestProperty("Authorization", "Basic " + new String(encoded, "US-ASCII"));
    conn.connect();
    int code = conn.getResponseCode();
    System.out.println("Download HTTP response code: " + code);
    in = new ProgressMonitorInputStream(pParent, I18N.BUNDLE.getString("DownloadMsg_part1"), new BufferedInputStream(conn.getInputStream(), Config.BUFFER_CAPACITY));
    ProgressMonitor pm = in.getProgressMonitor();
    pm.setMaximum(conn.getContentLength());
    byte[] data = new byte[Config.CHUNK_SIZE];
    int length;
    while ((length = in.read(data)) != -1) {
    out.write(data, 0, length);
    }
    out.flush();
    } finally {
    out.close();
    in.close();
    conn.disconnect();
    }
    }
     */

    public FolderBasedElementsTableModel createElementsTableModel() {
        FolderBasedElementsTableModel elementsTableModel = new FolderBasedElementsTableModel();
        mElementTableModels.add(elementsTableModel);
        return elementsTableModel;
    }

    public List<FolderBasedElementsTableModel> getElementTableModels() {
        return mElementTableModels;
    }

    public MasterDocument[] searchMDocs(String pMDocId, String pTitle,
            Version pVersion, User pAuthor, String pType, Date pCreationDateFrom,
            Date pCreationDateTo, SearchQuery.AbstractAttributeQuery[] pAttributes, String[] pTags, String pContent) throws Exception{
        MasterDocument[] mdocs = null;
        try {
            System.out.println("Searching for master document " + pMDocId + " version " + pVersion + " title " + pTitle + " author " + pAuthor + " creation date between " + pCreationDateFrom + " and " + pCreationDateTo + " tags " + pTags + " content " + pContent);
            mdocs = Tools.resetParentReferences(mCommandService.searchMDocs(new SearchQuery(getWorkspace().getId(), pMDocId, pTitle, pVersion == null ? null : pVersion.toString(), pAuthor == null ? null : pAuthor.getLogin(),
                    pType, pCreationDateFrom, pCreationDateTo, pAttributes, pTags, pContent)));
        //TODO cache mdocs ?
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
        return mdocs;
    }

    public File getFile(Component pParent, Document pDocument, BinaryResource pBin) throws Exception {
        MasterDocument mdoc = pDocument.getMasterDocument();
        File folder = null;
        boolean readOnly;
        if (!mdoc.isCheckedOut() || !mdoc.getCheckOutUser().equals(getUser()) || !mdoc.getLastIteration().equals(pDocument)) {
            folder = new File(Config.getCacheFolder(mdoc), pDocument.getIteration() + "");
            readOnly = true;
        } else {
            folder = Config.getCheckOutFolder(mdoc);
            readOnly = false;
        }

        File localFile = new File(folder, pBin.getName());

        if (!localFile.exists()) {
            MainModel model = MainModel.getInstance();
            folder.mkdirs();
            try {
                StreamingDataHandler dh;
                try {
                    dh = (StreamingDataHandler) mFileService.downloadFromDocument(model.getWorkspace().getId(), pDocument.getMasterDocumentId(), pDocument.getMasterDocumentVersion(), pDocument.getIteration(), pBin.getName());
                } catch (Exception ex) {
                    if (ex.getCause() instanceof InterruptedIOException) {
                        throw ex;
                    }
                    //error encountered, try again without chunked mode
                    Map<String, Object> ctxt = ((BindingProvider) mFileService).getRequestContext();
                    if (ctxt.containsKey(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE)) {
                        System.out.println("Disabling chunked mode");
                        ctxt.remove(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE);
                        dh = (StreamingDataHandler) mFileService.downloadFromDocument(model.getWorkspace().getId(), pDocument.getMasterDocumentId(), pDocument.getMasterDocumentVersion(), pDocument.getIteration(), pBin.getName());
                    } else {
                        //we were already not using the chunked mode
                        //there's not much to do...
                        throw ex;
                    }
                }
                downloadFile(pParent, localFile, (int) pBin.getContentLength(), dh.readOnce());
            } catch (WebServiceException pWSEx) {
                Throwable t = pWSEx.getCause();
                if (t instanceof Exception) {
                    throw (Exception) t;
                } else {
                    throw pWSEx;
                }
            }

            /*
            String url = Config.getHTTPCodebase()
            + "files/"
            + URLEncoder.encode(model.getWorkspace().getId(),"UTF-8") + "/"
            + "documents/"
            + URLEncoder.encode(pDocument.getMasterDocumentId(),"UTF-8") + "/"
            + pDocument.getMasterDocumentVersion() + "/"
            + pDocument.getIteration() + "/"
            + URLEncoder.encode(pFileName,"UTF-8");
            downloadFileWithServlet(pParent, localFile, url);
             */
            if (readOnly) {
                localFile.setReadOnly();
                localFile.deleteOnExit();
            } else {
                Prefs.storeDocInfo(mdoc, localFile.getName(), localFile.lastModified());
            }
        }

        return localFile;
    }

    private void downloadFile(Component pParent, File pLocalFile, int contentLength, InputStream inputStream) throws IOException {
        System.out.println("Downloading file");
        ProgressMonitorInputStream in = null;
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(pLocalFile), Config.BUFFER_CAPACITY);
            in = new ProgressMonitorInputStream(pParent, I18N.BUNDLE.getString("DownloadMsg_part1"), new BufferedInputStream(inputStream, Config.BUFFER_CAPACITY));
            ProgressMonitor pm = in.getProgressMonitor();
            pm.setMaximum(contentLength);
            byte[] data = new byte[Config.CHUNK_SIZE];
            int length;
            while ((length = in.read(data)) != -1) {
                out.write(data, 0, length);
            }

            out.flush();

        } finally {
            out.close();
            in.close();
        }
    }

    public File getFile(Component pParent, MasterDocumentTemplate pTemplate, BinaryResource pBin) throws Exception {
        File folder = Config.getCacheFolder(pTemplate);
        File localFile = new File(folder, pBin.getName());

        if (!localFile.exists()) {
            MainModel model = MainModel.getInstance();
            folder.mkdirs();
            try {
                StreamingDataHandler dh;
                try {
                    dh = (StreamingDataHandler) mFileService.downloadFromTemplate(model.getWorkspace().getId(), pTemplate.getId(), pBin.getName());
                } catch (Exception ex) {
                    if (ex.getCause() instanceof InterruptedIOException) {
                        throw ex;
                    }
                    //error encountered, try again without chunked mode
                    Map<String, Object> ctxt = ((BindingProvider) mFileService).getRequestContext();
                    if (ctxt.containsKey(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE)) {
                        System.out.println("Disabling chunked mode");
                        ctxt.remove(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE);
                        dh = (StreamingDataHandler) mFileService.downloadFromTemplate(model.getWorkspace().getId(), pTemplate.getId(), pBin.getName());
                    } else {
                        //we were already not using the chunked mode
                        //there's not much to do...
                        throw ex;
                    }
                }
                downloadFile(pParent, localFile, (int) pBin.getContentLength(), dh.readOnce());

            } catch (WebServiceException pWSEx) {
                Throwable t = pWSEx.getCause();
                if (t instanceof Exception) {
                    throw (Exception) t;
                } else {
                    throw pWSEx;
                }
            }
            /*
            String url = Config.getHTTPCodebase()
            + "files/"
            + URLEncoder.encode(model.getWorkspace().getId(),"UTF-8") + "/"
            + "templates/"
            + URLEncoder.encode(pTemplate.getId(),"UTF-8") + "/"
            + URLEncoder.encode(pFileName,"UTF-8");
            folder.mkdirs();
            downloadFileWithServlet(pParent, localFile, url);
             */
            localFile.deleteOnExit();
        }
        return localFile;
    }

    private void showContinueOrExitDialog(String pMessage) {
        Object[] options = {I18N.BUNDLE.getString("Continue_label"), I18N.BUNDLE.getString("Exit_label")};
        int selectedOption = JOptionPane.showOptionDialog(
                null,
                pMessage,
                I18N.BUNDLE.getString("Error_title"),
                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                null, options, options[0]);
        if (selectedOption == 1) {
            System.exit(-1);
        }
    }
}
