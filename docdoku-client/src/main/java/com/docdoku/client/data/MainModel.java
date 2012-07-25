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

import com.docdoku.core.services.IUploadDownloadWS;
import com.docdoku.core.services.IDocumentManagerWS;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.Folder;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.common.User;
import com.docdoku.core.document.TagKey;
import com.docdoku.core.common.Version;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.common.Workspace;
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
import com.docdoku.core.document.DocumentMasterTemplateKey;
import com.docdoku.core.document.SearchQuery;
import com.docdoku.core.workflow.WorkflowModelKey;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.StreamingDataHandler;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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

        public void createDocMInFolder(DocumentMaster pDocumentMaster) {
            mCache.cacheDocM(pDocumentMaster);
            insertIntoTables(pDocumentMaster);
        }

        public void createDocMTemplate(DocumentMasterTemplate pTemplate) {
            mCache.cacheDocMTemplate(pTemplate);
            insertIntoTables(pTemplate);
        }

        public void updateDocMTemplate(DocumentMasterTemplate pTemplate) {
            mCache.cacheDocMTemplate(pTemplate);
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

        public void updateDocM(DocumentMaster pDocM) {
            mCache.cacheDocM(pDocM);
            MainModel.this.getElementsTreeModel().fireTagTreeStructureChanged();
        }

        public void updateUser(User pUser) {
            mCache.cacheUser(pUser);
        }

        public void delDocM(DocumentMaster pDocumentMaster) {
            List<Integer> indexes = indexesOfElement(pDocumentMaster);
            int i = 0;
            mCache.removeDocM(pDocumentMaster);
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        pDocumentMaster.getLocation().getCompletePath())) {
                    int row = indexes.get(i++);
                    model.fireTableRowsDeleted(row, row);
                }

            }
        }

        public void delDocMTemplate(DocumentMasterTemplate pTemplate) {
            List<Integer> indexes = indexesOfElement(pTemplate);
            int i = 0;
            mCache.removeDocMTemplate(pTemplate);
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

        public void moveDocM(DocumentMaster pTargetDocM) {
            insertIntoTables(pTargetDocM);
            mCache.cacheDocM(pTargetDocM);
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
                    model.fireTableRowsDeleted(row, row);

                }


            }
        }

        public void checkIn(DocumentMaster pDocumentMaster) {
            mCache.cacheDocM(pDocumentMaster);
            updateTables(pDocumentMaster);
        }

        public void checkOut(DocumentMaster pDocumentMaster) {
            mCache.cacheDocM(pDocumentMaster);
            updateTables(pDocumentMaster);
        }

        public void makeNewVersion(DocumentMaster[] pOriginalAndNewDocM) {
            mCache.cacheDocM(pOriginalAndNewDocM[0]);
            updateTables(pOriginalAndNewDocM[0]);
            mCache.cacheDocM(pOriginalAndNewDocM[1]);
            insertIntoTables(pOriginalAndNewDocM[1]);
        }

        public void undoCheckOut(DocumentMaster pDocumentMaster) {
            mCache.cacheDocM(pDocumentMaster);
            updateTables(pDocumentMaster);
        }

        public void addStateNotification(DocumentMaster pDocumentMaster) {
            mCache.cacheStateSubscription(pDocumentMaster.getKey());
        }

        public void removeStateNotification(DocumentMaster pDocumentMaster) {
            mCache.removeStateSubscription(pDocumentMaster.getKey());
        }

        public void addIterationNotification(DocumentMaster pDocumentMaster) {
            mCache.cacheIterationSubscription(pDocumentMaster.getKey());
        }

        public void removeIterationNotification(DocumentMaster pDocumentMaster) {
            mCache.removeIterationSubscription(pDocumentMaster.getKey());
        }

        public void refreshTree() {
            MainModel.this.getElementsTreeModel().fireAllTreeNodesChanged();
        }

        public void clear() {
            mCache.clear();
            MainModel.this.getElementsTreeModel().fireAllTreeStructureChanged();
        }

        private void updateTables(DocumentMaster pDocumentMaster) {
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        pDocumentMaster.getLocation().getCompletePath())) {
                    int row = model.getIndexOfElement(pDocumentMaster);
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

        private void insertIntoTables(DocumentMasterTemplate pTemplate) {
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

        private void insertIntoTables(DocumentMaster pDocumentMaster) {
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        pDocumentMaster.getLocation().getCompletePath())) {
                    int row = model.getIndexOfElement(pDocumentMaster);
                    model.fireTableRowsInserted(row, row);
                }
            }
        }

        private List<Integer> indexesOfElement(DocumentMaster pDocumentMaster) {
            List<Integer> indexes = new LinkedList<Integer>();
            Iterator iti = getElementTableModels().iterator();
            while (iti.hasNext()) {
                FolderBasedElementsTableModel model = (FolderBasedElementsTableModel) iti.next();
                String completePath = model.getFolderCompletePath();
                if (completePath != null && completePath.equals(
                        pDocumentMaster.getLocation().getCompletePath())) {
                    int row = model.getIndexOfElement(pDocumentMaster);
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

        private List<Integer> indexesOfElement(DocumentMasterTemplate pTemplate) {
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
    private IDocumentManagerWS mDocumentService;
    private IUploadDownloadWS mFileService;
    private static MainModel sSingleton;

    public static MainModel getInstance() {
        if (sSingleton == null) {
            throw new NotInitializedException(MainModel.class.getName());
        }

        return sSingleton;
    }

    public static MainModel init(String pLogin, String pPassword, String pWorkspaceId, IDocumentManagerWS pDocumentService, IUploadDownloadWS pFileService)
            throws InitializationException {
        if (sSingleton == null) {
            sSingleton = new MainModel(pLogin, pPassword, pWorkspaceId, pDocumentService, pFileService);
            return sSingleton;
        } else {
            throw new AlreadyInitializedException(MainModel.class.getName());
        }
    }

    private MainModel(String pLogin, String pPassword, String pWorkspaceId, IDocumentManagerWS pDocumentService, IUploadDownloadWS pFileService) throws InitializationException {
        mDocumentService = pDocumentService;
        mFileService = pFileService;
        mElementTableModels = new LinkedList<FolderBasedElementsTableModel>();
        try {
            Workspace workspace = mDocumentService.getWorkspace(pWorkspaceId);
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
                String[] folders = mDocumentService.getFolders(completePath);
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
                models = Tools.resetParentReferences(mDocumentService.getWorkflowModels(getWorkspace().getId()));
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

    public DocumentMasterTemplate[] getDocMTemplates() {
        DocumentMasterTemplate[] templates = mCache.getDocMTemplates();

        if (templates == null) {
            try {
                System.out.println("Retrieving templates");
                templates = Tools.resetParentReferences(mDocumentService.getDocumentMasterTemplates(getWorkspace().getId()));
                mCache.cacheDocMTemplates(templates);
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

    public DocumentMasterTemplate getDocMTemplate(String pId) {
        DocumentMasterTemplate template = mCache.getDocMTemplate(pId);

        if (template == null) {
            try {
                System.out.println("Retrieving document template " + pId);
                template = Tools.resetParentReferences(mDocumentService.getDocumentMasterTemplate(new DocumentMasterTemplateKey(getWorkspace().getId(), pId)));
                mCache.cacheDocMTemplate(template);
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
                workflowModel = Tools.resetParentReferences(mDocumentService.getWorkflowModel(new WorkflowModelKey(getWorkspace().getId(), pId)));
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

    public DocumentMaster getDocM(DocumentMasterKey pDocMPK) {
        DocumentMaster docM = mCache.getDocM(pDocMPK);

        if (docM == null) {
            try {
                System.out.println("Retrieving document master " + pDocMPK);
                docM = Tools.resetParentReferences(mDocumentService.getDocumentMaster(pDocMPK));
                mCache.cacheDocM(docM);
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
        return docM;
    }

    public DocumentMasterKey[] getStateChangeEventSubscriptions() {
        DocumentMasterKey[] subKeys = mCache.getStateSubscriptions();

        if (subKeys == null) {
            try {
                System.out.println("Retrieving state subscriptions");
                subKeys = mDocumentService.getStateChangeEventSubscriptions(getWorkspace().getId());
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

    public boolean hasIterationChangeEventSubscription(DocumentMaster pDocM) {
        DocumentMasterKey[] subKeys = getIterationChangeEventSubscriptions();
        return Arrays.asList(subKeys).contains(pDocM.getKey());
    }

    public boolean hasStateChangeEventSubscription(DocumentMaster pDocM) {
        DocumentMasterKey[] subKeys = getStateChangeEventSubscriptions();
        return Arrays.asList(subKeys).contains(pDocM.getKey());
    }

    public DocumentMasterKey[] getIterationChangeEventSubscriptions() {
        DocumentMasterKey[] subKeys = mCache.getIterationSubscriptions();

        if (subKeys == null) {
            try {
                System.out.println("Retrieving iteration subscriptions");
                subKeys = mDocumentService.getIterationChangeEventSubscriptions(getWorkspace().getId());
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

    public DocumentMaster[] getCheckedOutDocMs() {
        DocumentMaster[] docMs = mCache.getCheckedOutDocMs();

        if (docMs == null) {
            try {
                System.out.println("Retrieving personnal checked out document masters");
                docMs = Tools.resetParentReferences(mDocumentService.getCheckedOutDocumentMasters(getWorkspace().getId()));
                mCache.cacheCheckedOutDocMs(docMs);
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
        return docMs;
    }

    public DocumentMaster[] findDocMsByTag(String pTag) {
        DocumentMaster[] docMs = mCache.findDocMsByTag(pTag);

        if (docMs == null) {
            try {
                System.out.println("Searching document masters by tag " + pTag);
                docMs = Tools.resetParentReferences(mDocumentService.findDocumentMastersByTag(new TagKey(getWorkspace().getId(), pTag)));
                mCache.cacheDocMsByTag(pTag, docMs);
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
        return docMs;
    }

    public DocumentMaster[] findDocMsByFolder(String pCompletePath) {
        DocumentMaster[] docMs = mCache.findDocMsByFolder(pCompletePath);

        if (docMs == null) {
            try {
                System.out.println("Searching document masters by folder " + pCompletePath);
                docMs = Tools.resetParentReferences(mDocumentService.findDocumentMastersByFolder(pCompletePath));
                mCache.cacheDocMsByFolder(pCompletePath, docMs);
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
        return docMs;
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
                user = mDocumentService.whoAmI(getWorkspace().getId());
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
                users = mDocumentService.getUsers(getWorkspace().getId());
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

    public String getGeneratedId(String pWorkspaceId, String pDocMTemplateId) {
        String generatedId = null;
        try {
            System.out.println("Retrieving generated document master id");
            generatedId = mDocumentService.generateId(pWorkspaceId, pDocMTemplateId);
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
                tags = mDocumentService.getTags(getWorkspace().getId());
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

    private void downloadFileWithServlet(Component pParent, File pLocalFile, String pURL) throws IOException {
        System.out.println("Downloading file from servlet");
        ProgressMonitorInputStream in = null;
        OutputStream out = null;
        HttpURLConnection conn = null;
        try {
            //Hack for NTLM proxy
            //perform a head method to negociate the NTLM proxy authentication
            performHeadHTTPMethod(pURL);
            
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

    public FolderBasedElementsTableModel createElementsTableModel() {
        FolderBasedElementsTableModel elementsTableModel = new FolderBasedElementsTableModel();
        mElementTableModels.add(elementsTableModel);
        return elementsTableModel;
    }

    public List<FolderBasedElementsTableModel> getElementTableModels() {
        return mElementTableModels;
    }

    public DocumentMaster[] searchDocMs(String pDocMId, String pTitle,
            Version pVersion, User pAuthor, String pType, Date pCreationDateFrom,
            Date pCreationDateTo, SearchQuery.AbstractAttributeQuery[] pAttributes, String[] pTags, String pContent) throws Exception {
        DocumentMaster[] docMs = null;
        try {
            System.out.println("Searching for document master " + pDocMId + " version " + pVersion + " title " + pTitle + " author " + pAuthor + " creation date between " + pCreationDateFrom + " and " + pCreationDateTo + " tags " + pTags + " content " + pContent);
            docMs = Tools.resetParentReferences(mDocumentService.searchDocumentMasters(new SearchQuery(getWorkspace().getId(), pDocMId, pTitle, pVersion == null ? null : pVersion.toString(), pAuthor == null ? null : pAuthor.getLogin(),
                    pType, pCreationDateFrom, pCreationDateTo, pAttributes, pTags, pContent)));
            //TODO cache docMs ?
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
        return docMs;
    }

    public File getFile(Component pParent, DocumentIteration pDocument, BinaryResource pBin) throws Exception {
        DocumentMaster docM = pDocument.getDocumentMaster();
        File folder = null;
        boolean readOnly;
        if (!docM.isCheckedOut() || !docM.getCheckOutUser().equals(getUser()) || !docM.getLastIteration().equals(pDocument)) {
            folder = new File(Config.getCacheFolder(docM), pDocument.getIteration() + "");
            readOnly = true;
        } else {
            folder = Config.getCheckOutFolder(docM);
            readOnly = false;
        }

        File localFile = new File(folder, pBin.getName());

        if (!localFile.exists()) {
            folder.mkdirs();
            try {
                Map<String, Object> ctxt = ((BindingProvider) mFileService).getRequestContext();
                try {
                    if (ctxt.containsKey(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE)) {
                        StreamingDataHandler dh = (StreamingDataHandler) mFileService.downloadFromDocument(getWorkspace().getId(), pDocument.getDocumentMasterId(), pDocument.getDocumentMasterVersion(), pDocument.getIteration(), pBin.getName());
                        downloadFile(pParent, localFile, (int) pBin.getContentLength(), dh.readOnce());
                    } else {
                        //workaround mode
                        downloadFileWithServlet(pParent, localFile, getServletURL(pDocument, pBin.getName()));
                    }
                } catch (Exception ex) {
                    if (ex.getCause() instanceof InterruptedIOException) {
                        throw ex;
                    }
                    if (ctxt.containsKey(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE)) {
                        System.out.println("Disabling chunked mode");
                        ctxt.remove(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE);
                        downloadFileWithServlet(pParent, localFile, getServletURL(pDocument, pBin.getName()));
                    } else {
                        //we were already not using the chunked mode
                        //there's not much to do...
                        throw ex;
                    }

                }
            } catch (WebServiceException pWSEx) {
                Throwable t = pWSEx.getCause();
                if (t instanceof Exception) {
                    throw (Exception) t;
                } else {
                    throw pWSEx;
                }
            }


            if (readOnly) {
                localFile.setReadOnly();
                localFile.deleteOnExit();
            } else {
                Prefs.storeDocInfo(docM, localFile.getName(), localFile.lastModified());
            }
        }

        return localFile;
    }

    public File getFile(Component pParent, DocumentMasterTemplate pTemplate, BinaryResource pBin) throws Exception {
        File folder = Config.getCacheFolder(pTemplate);
        File localFile = new File(folder, pBin.getName());

        if (!localFile.exists()) {
            MainModel model = MainModel.getInstance();
            folder.mkdirs();
            try {
                Map<String, Object> ctxt = ((BindingProvider) mFileService).getRequestContext();
                try {
                    if (ctxt.containsKey(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE)) {
                        StreamingDataHandler dh = (StreamingDataHandler) mFileService.downloadFromTemplate(model.getWorkspace().getId(), pTemplate.getId(), pBin.getName());
                        downloadFile(pParent, localFile, (int) pBin.getContentLength(), dh.readOnce());
                    } else {
                        //workaround mode
                        downloadFileWithServlet(pParent, localFile, getServletURL(pTemplate, pBin.getName()));
                    }

                } catch (Exception ex) {
                    if (ex.getCause() instanceof InterruptedIOException) {
                        throw ex;
                    }
                    if (ctxt.containsKey(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE)) {
                        System.out.println("Disabling chunked mode");
                        ctxt.remove(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE);
                        downloadFileWithServlet(pParent, localFile, getServletURL(pTemplate, pBin.getName()));
                    } else {
                        //we were already not using the chunked mode
                        //there's not much to do...
                        throw ex;
                    }  
                }
            } catch (WebServiceException pWSEx) {
                Throwable t = pWSEx.getCause();
                if (t instanceof Exception) {
                    throw (Exception) t;
                } else {
                    throw pWSEx;
                }
            }
            localFile.deleteOnExit();
        }
        return localFile;
    }


    private String getServletURL(DocumentIteration pDocument, String pRemoteFileName) throws UnsupportedEncodingException {
        return Config.getHTTPCodebase()
                + "files/"
                + URLEncoder.encode(getWorkspace().getId(), "UTF-8") + "/"
                + "documents/"
                + URLEncoder.encode(pDocument.getDocumentMasterId(), "UTF-8") + "/"
                + pDocument.getDocumentMasterVersion() + "/"
                + pDocument.getIteration() + "/"
                + URLEncoder.encode(pRemoteFileName, "UTF-8");


    }

    private String getServletURL(DocumentMasterTemplate pTemplate, String pRemoteFileName) throws UnsupportedEncodingException {
        return Config.getHTTPCodebase()
                + "files/"
                + URLEncoder.encode(getWorkspace().getId(), "UTF-8")
                + "/" + "templates/"
                + URLEncoder.encode(pTemplate.getId(), "UTF-8")
                + "/" + URLEncoder.encode(pRemoteFileName, "UTF-8");
    }

    private void performHeadHTTPMethod(String pURL) throws MalformedURLException, IOException {
        URL url = new URL(pURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(true);
        conn.setRequestProperty("Connection", "Keep-Alive");
        byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64((getLogin() + ":" + getPassword()).getBytes("ISO-8859-1"));
        conn.setRequestProperty("Authorization", "Basic " + new String(encoded, "US-ASCII"));
        conn.setRequestMethod("HEAD");
        conn.connect();
        int code = conn.getResponseCode();
        System.out.println("Head HTTP response code: " + code);
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
