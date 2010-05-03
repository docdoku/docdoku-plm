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
package com.docdoku.client.actions;

import com.docdoku.client.data.Config;
import com.docdoku.client.data.MainModel;
import com.docdoku.client.data.Prefs;
import com.docdoku.client.ui.template.EditMDocTemplateDialog;
import com.docdoku.core.entities.Document;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.entities.BinaryResource;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.MasterDocumentTemplate;
import com.docdoku.core.entities.WorkflowModel;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.workflow.WorkflowModelFrame;
import com.docdoku.client.ui.doc.EditDocDialog;
import com.docdoku.client.localization.I18N;

import com.docdoku.core.entities.InstanceAttribute;
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
        final MasterDocument mdoc = mOwner.getSelectedMDoc();
        MasterDocumentTemplate template = mOwner.getSelectedMDocTemplate();
        WorkflowModel wfModel = mOwner.getSelectedWorkflowModel();

        if (mdoc != null) {
            final ActionListener okAction = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent pAE) {
                    final EditDocDialog source = (EditDocDialog) pAE.getSource();
                    Thread worker = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            MainController controller = MainController.getInstance();
                            Document doc = source.getEditedDoc();
                            try {
                                // TODO the operation is split in 3 Tx may be
                                // it's better to gather all in one
                                for (BinaryResource fileToRemove : source.getFilesToRemove()) {
                                    controller.removeFileFromDocument(fileToRemove);
                                    new File(Config.getCheckOutFolder(doc.getMasterDocument()), fileToRemove.getName()).delete();
                                    Prefs.removeDocInfo(doc.getMasterDocument(), fileToRemove.getName());
                                }

                                for (File fileToAdd : source.getFilesToAdd()) {
                                    try {
                                        controller.saveFile(source, doc, fileToAdd);
                                        File destFolder = Config.getCheckOutFolder(doc.getMasterDocument());
                                        destFolder.mkdirs();
                                        File destFile = new File(destFolder, fileToAdd.getName());

                                        FileIO.copyFile(fileToAdd, destFile);
                                        Prefs.storeDocInfo(mdoc, destFile.getName(), destFile.lastModified());
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
            if (mdoc.isCheckedOut()) {
                new EditDocDialog(mOwner, mdoc.getLastIteration(), okAction,
                        editAction, addAttributeAction, addLinkAction);
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
                            final MasterDocument newMDoc = MainController.getInstance().checkOut(mdoc);
                            FileIO.rmDir(Config.getCheckOutFolder(newMDoc));
                            for (BinaryResource remoteFile : newMDoc.getLastIteration().getAttachedFiles()) {
                                try {
                                    MainModel.getInstance().getFile(mOwner, newMDoc.getLastIteration(), remoteFile);
                                } catch (InterruptedIOException pIIOEx) {
                                }
                            }
                            SwingUtilities.invokeAndWait(new Runnable() {

                                @Override
                                public void run() {
                                    new EditDocDialog(mOwner, newMDoc.getLastIteration(), okAction,
                                    editAction, addAttributeAction, addLinkAction);
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
                    final EditMDocTemplateDialog source = (EditMDocTemplateDialog) pAE.getSource();
                    Thread worker = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            MainController controller = MainController.getInstance();
                            MasterDocumentTemplate template = source.getEditedTemplate();
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

                                controller.updateMDocTemplate(template, source.getDocumentType(), source.getMask(), source.getAttributeTemplates(), source.isIdGenerated());
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
            ActionListener editFileAction = new EditFileActionListener();
            ActionListener addAttributeTemplateAction = new AddAttributeTemplateActionListener();
            new EditMDocTemplateDialog(mOwner, action, editFileAction, addAttributeTemplateAction, template);
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
