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
import com.docdoku.client.data.Prefs;
import com.docdoku.client.data.TagTreeNode;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.entities.keys.MasterDocumentKey;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.MasterDocumentTemplate;
import com.docdoku.core.entities.WorkflowModel;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.data.FolderTreeNode;

import javax.swing.*;
import java.awt.event.*;
import java.text.MessageFormat;


public class DeleteElementAction extends ClientAbstractAction {
    
    public DeleteElementAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("DeleteElement_title"), "/com/docdoku/client/resources/icons/garbage_empty.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION,
                I18N.BUNDLE.getString("DeleteElement_short_desc"));
        putValue(Action.LONG_DESCRIPTION,
                I18N.BUNDLE.getString("DeleteElement_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("DeleteElement_mnemonic_key")));
        putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    }
    
    @Override
    public void actionPerformed(ActionEvent pAE) {
        MasterDocument mdoc = mOwner.getSelectedMDoc();
        WorkflowModel wfModel = mOwner.getSelectedWorkflowModel();
        FolderTreeNode folderTreeNode = mOwner.getSelectedFolder();
        MasterDocumentTemplate template=mOwner.getSelectedMDocTemplate();
        MainController controller = MainController.getInstance();
        
        try {
            if (mdoc == null && wfModel == null && template == null && folderTreeNode != null) {
                if(folderTreeNode instanceof TagTreeNode){
                    String questionMsg = MessageFormat.format(I18N.BUNDLE.getString("DeleteElement_question_tag"),folderTreeNode.getName());
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner,questionMsg,I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                        controller.delTag(folderTreeNode.getName());
                    }
                }else{
                    String questionMsg = MessageFormat.format(I18N.BUNDLE.getString("DeleteElement_question_folder"),folderTreeNode.getCompletePath());
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner,questionMsg,I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                        MasterDocumentKey[] pks=controller.delFolder(folderTreeNode.getCompletePath());
                        for(MasterDocumentKey pk:pks){
                            FileIO.rmDir(Config.getCheckOutFolder(pk));
                            FileIO.rmDir(Config.getCacheFolder(pk));
                            Prefs.removeDocNode(pk);
                        }
                    }
                }
            } else if (mdoc != null){
                String questionMsg = MessageFormat.format(I18N.BUNDLE.getString("DeleteElement_question_document"),mdoc);
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner,questionMsg,I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    controller.delMDoc(mdoc);
                    FileIO.rmDir(Config.getCheckOutFolder(mdoc));
                    FileIO.rmDir(Config.getCacheFolder(mdoc));
                    Prefs.removeDocNode(mdoc);
                }
            } else if (wfModel != null){
                String questionMsg = MessageFormat.format(I18N.BUNDLE.getString("DeleteElement_question_workflow"),wfModel);
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner,questionMsg,I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    controller.delWorkflowModel(wfModel);
                }
            } else if (template != null){
                String questionMsg = MessageFormat.format(I18N.BUNDLE.getString("DeleteElement_question_template"),template);               
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner, questionMsg,I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    controller.delMDocTemplate(template);
                    FileIO.rmDir(Config.getCacheFolder(template));
                }
            }
        }  catch (Exception pEx) {
            String message = pEx.getMessage()==null?I18N.BUNDLE
                    .getString("Error_unknown"):pEx.getMessage();
            JOptionPane.showMessageDialog(null,
                    message, I18N.BUNDLE
                    .getString("Error_title"),
                    JOptionPane.ERROR_MESSAGE);
        }
        ExplorerFrame.unselectElementInAllFrame();
    }
}
