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
package com.docdoku.client.actions;

import com.docdoku.client.data.Config;
import com.docdoku.client.data.MainModel;
import com.docdoku.client.data.Prefs;
import com.docdoku.client.ui.template.EditDocMTemplateDialog;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.workflow.WorkflowModelFrame;
import com.docdoku.client.ui.doc.EditDocDialog;
import com.docdoku.client.localization.I18N;

import com.docdoku.core.meta.InstanceAttribute;
import java.awt.Cursor;
import javax.swing.*;

import java.awt.event.*;
import java.util.*;
import java.io.*;

public class EditElementAction extends ClientAbstractAction {

    public EditElementAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("EditElement_title"),
                "/com/docdoku/client/resources/icons/edit.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("EditElement_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("EditElement_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("EditElement_mnemonic_key")));
        setLargeIcon("/com/docdoku/client/resources/icons/edit_large.png");
    }

    @Override
    public void actionPerformed(ActionEvent pAE) {
        final DocumentMaster docM = mOwner.getSelectedDocM();
        DocumentMasterTemplate template = mOwner.getSelectedDocMTemplate();
        WorkflowModel wfModel = mOwner.getSelectedWorkflowModel();

        if (docM != null) {
            final ActionListener okAction = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent pAE) {
                    final EditDocDialog source = (EditDocDialog) pAE.getSource();
                    Thread worker = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            MainController controller = MainController.getInstance();
                            DocumentIteration doc = source.getEditedDoc();
                            try {
                                // TODO the operation is split in 3 Tx may be
                                // it's better to gather all in one
                                for (BinaryResource fileToRemove : source.getFilesToRemove()) {
                                    controller.removeFileFromDocument(fileToRemove);
                                    new File(Config.getCheckOutFolder(doc.getDocumentMaster()), fileToRemove.getName()).delete();
                                    Prefs.removeDocInfo(doc.getDocumentMaster(), fileToRemove.getName());
                                }

                                for (File fileToAdd : source.getFilesToAdd()) {
                                    try {
                                        controller.saveFile(source, doc, fileToAdd);
                                        File destFolder = Config.getCheckOutFolder(doc.getDocumentMaster());
                                        destFolder.mkdirs();
                                        File destFile = new File(destFolder, fileToAdd.getName());

                                        FileIO.copyFile(fileToAdd, destFile);
                                        Prefs.storeDocInfo(docM, destFile.getName(), destFile.lastModified());
                                    } catch (InterruptedIOException pIIOEx) {
                                    }
                                }
                                Map<String, InstanceAttribute> editedAttrs = source.getAttributes();
                                InstanceAttribute[] attrs = new InstanceAttribute[editedAttrs.size()];
                                attrs = editedAttrs.values().toArray(attrs);

                                controller.updateDoc(doc, source.getComment(), attrs, source.getLinks());
                                source.dispose();
                            } catch (Exception pEx) {
                                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                                JOptionPane.showMessageDialog(null,
                                        message, I18N.BUNDLE.getString("Error_title"),
                                        JOptionPane.ERROR_MESSAGE);
                            }
                            ExplorerFrame.unselectElementInAllFrame();
                        }
                    });
                    worker.start();
                }
            };
            final ActionListener editAction = new EditFileActionListener();
            final ActionListener addAttributeAction = new AddAttributeActionListener();
            final ActionListener addLinkAction = new AddLinkActionListener();
            final ActionListener scanAction = new ScanActionListener();
            if (docM.isCheckedOut()) {
                new EditDocDialog(mOwner, docM.getLastIteration(), okAction,
                        editAction, scanAction, addAttributeAction, addLinkAction);
            } else if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                    mOwner,
                    I18N.BUNDLE.getString("EditElement_question"),
                    I18N.BUNDLE.getString("EditElement_question_title"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE)) {

                Thread worker = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            mOwner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            final DocumentMaster newDocM = MainController.getInstance().checkOutDocument(docM);
                            FileIO.rmDir(Config.getCheckOutFolder(newDocM));
                            for (BinaryResource remoteFile : newDocM.getLastIteration().getAttachedFiles()) {
                                try {
                                    MainModel.getInstance().getFile(mOwner, newDocM.getLastIteration(), remoteFile);
                                } catch (InterruptedIOException pIIOEx) {
                                }
                            }
                            SwingUtilities.invokeAndWait(new Runnable() {

                                @Override
                                public void run() {
                                    new EditDocDialog(mOwner, newDocM.getLastIteration(), okAction,
                                    editAction, scanAction, addAttributeAction, addLinkAction);
                                }
                            });
                            
                        } catch (Exception pEx) {
                            String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                            JOptionPane.showMessageDialog(null,
                                    message, I18N.BUNDLE.getString("Error_title"),
                                    JOptionPane.ERROR_MESSAGE);
                        } finally {
                            mOwner.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }
                        ExplorerFrame.unselectElementInAllFrame();
                    }
                });
                worker.start();
            }
        } else if (template != null) {
            ActionListener action = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent pAE) {
                    final EditDocMTemplateDialog source = (EditDocMTemplateDialog) pAE.getSource();
                    Thread worker = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            MainController controller = MainController.getInstance();
                            DocumentMasterTemplate template = source.getEditedTemplate();
                            try {
                                // TODO the operation is split in 3 Tx may be
                                // it's better to gather all in one
                                for (BinaryResource fileToRemove : source.getFilesToRemove()) {
                                    controller.removeFileFromTemplate(fileToRemove);
                                    new File(Config.getCacheFolder(template), fileToRemove.getName()).delete();
                                }

                                for (File fileToAdd : source.getFilesToAdd()) {
                                    try {
                                        controller.saveFile(source, template, fileToAdd);
                                        File destFolder = Config.getCacheFolder(template);
                                        destFolder.mkdirs();
                                        File destFile = new File(destFolder, fileToAdd.getName());
                                        destFile.deleteOnExit();

                                        FileIO.copyFile(fileToAdd, destFile);
                                    } catch (InterruptedIOException pIIOEx) {
                                    }
                                }

                                for (Map.Entry<BinaryResource, Long> pair : source.getFilesToUpdate().entrySet()) {
                                    try {
                                        File localFile = new File(Config.getCacheFolder(template), pair.getKey().getName());
                                        long creationDate = pair.getValue();
                                        if (localFile.lastModified() > creationDate) {
                                            controller.saveFile(source, template, localFile);
                                        }
                                    } catch (InterruptedIOException pIIOEx) {
                                    }
                                }

                                controller.updateDocMTemplate(template, source.getDocumentType(), source.getMask(), source.getAttributeTemplates(), source.isIdGenerated());
                                source.dispose();
                            } catch (Exception pEx) {
                                String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                                JOptionPane.showMessageDialog(null,
                                        message, I18N.BUNDLE.getString("Error_title"),
                                        JOptionPane.ERROR_MESSAGE);
                            }
                            ExplorerFrame.unselectElementInAllFrame();
                        }
                    });
                    worker.start();
                }
            };
            final ActionListener editFileAction = new EditFileActionListener();
            final ActionListener scanAction = new ScanActionListener();
            final ActionListener addAttributeTemplateAction = new AddAttributeTemplateActionListener();
            new EditDocMTemplateDialog(mOwner, action, editFileAction, scanAction, addAttributeTemplateAction, template);
        } else if (wfModel != null) {
            final WorkflowModel clonedModel = wfModel.clone();
            final ActionListener saveAsWorkflowModelAction = new SaveAsWorkflowModelActionListener();
            final ActionListener editParallelActivityModelAction = new EditParallelActivityModelActionListener();
            final ActionListener editSerialActivityModelAction = new EditSerialActivityModelActionListener();
            final ActionListener deleteParallelActivityModelAction = new DeleteParallelActivityModelActionListener();
            final ActionListener deleteSerialActivityModelAction = new DeleteSerialActivityModelActionListener();
            final ActionListener editLifeCycleStateAction = new EditLifeCycleStateActionListener();
            final MouseListener horizontalSeparatorMouseListener = new HorizontalSeparatorMouseListener();
            new WorkflowModelFrame(clonedModel, saveAsWorkflowModelAction,
                    editParallelActivityModelAction,
                    editSerialActivityModelAction,
                    deleteParallelActivityModelAction,
                    deleteSerialActivityModelAction, editLifeCycleStateAction,
                    horizontalSeparatorMouseListener);
        }
    }
}
