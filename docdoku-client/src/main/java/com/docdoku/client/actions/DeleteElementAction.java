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
import com.docdoku.client.data.Prefs;
import com.docdoku.client.data.TagTreeNode;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.workflow.WorkflowModel;
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
        DocumentMaster docM = mOwner.getSelectedDocM();
        WorkflowModel wfModel = mOwner.getSelectedWorkflowModel();
        FolderTreeNode folderTreeNode = mOwner.getSelectedFolder();
        DocumentMasterTemplate template=mOwner.getSelectedDocMTemplate();
        MainController controller = MainController.getInstance();
        
        try {
            if (docM == null && wfModel == null && template == null && folderTreeNode != null) {
                if(folderTreeNode instanceof TagTreeNode){
                    String questionMsg = MessageFormat.format(I18N.BUNDLE.getString("DeleteElement_question_tag"),folderTreeNode.getName());
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner,questionMsg,I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                        controller.delTag(folderTreeNode.getName());
                    }
                }else{
                    String questionMsg = MessageFormat.format(I18N.BUNDLE.getString("DeleteElement_question_folder"),folderTreeNode.getCompletePath());
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner,questionMsg,I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                        DocumentMasterKey[] pks=controller.delFolder(folderTreeNode.getCompletePath());
                        for(DocumentMasterKey pk:pks){
                            FileIO.rmDir(Config.getCheckOutFolder(pk));
                            FileIO.rmDir(Config.getCacheFolder(pk));
                            Prefs.removeDocNode(pk);
                        }
                    }
                }
            } else if (docM != null){
                String questionMsg = MessageFormat.format(I18N.BUNDLE.getString("DeleteElement_question_document"),docM);
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner,questionMsg,I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    controller.delDocM(docM);
                    FileIO.rmDir(Config.getCheckOutFolder(docM));
                    FileIO.rmDir(Config.getCacheFolder(docM));
                    Prefs.removeDocNode(docM);
                }
            } else if (wfModel != null){
                String questionMsg = MessageFormat.format(I18N.BUNDLE.getString("DeleteElement_question_workflow"),wfModel);
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner,questionMsg,I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    controller.delWorkflowModel(wfModel);
                }
            } else if (template != null){
                String questionMsg = MessageFormat.format(I18N.BUNDLE.getString("DeleteElement_question_template"),template);               
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner, questionMsg,I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    controller.delDocMTemplate(template);
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
