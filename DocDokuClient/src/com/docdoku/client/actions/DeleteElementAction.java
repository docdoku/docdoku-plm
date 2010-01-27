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
    
    public void actionPerformed(ActionEvent pAE) {
        MasterDocument mdoc = mOwner.getSelectedMDoc();
        WorkflowModel wfModel = mOwner.getSelectedWorkflowModel();
        FolderTreeNode folderTreeNode = mOwner.getSelectedFolder();
        MasterDocumentTemplate template=mOwner.getSelectedMDocTemplate();
        MainController controller = MainController.getInstance();
        
        try {
            if (mdoc == null && wfModel == null && template == null && folderTreeNode != null) {
                if(folderTreeNode instanceof TagTreeNode){
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner,I18N.BUNDLE.getString("DeleteElement_question_tag"),I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                        controller.delTag(folderTreeNode.getName());
                    }
                }else{
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner,I18N.BUNDLE.getString("DeleteElement_question_folder"),I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                        MasterDocumentKey[] pks=controller.delFolder(folderTreeNode.getCompletePath());
                        for(MasterDocumentKey pk:pks){
                            FileIO.rmDir(Config.getCheckOutFolder(pk));
                            FileIO.rmDir(Config.getCacheFolder(pk));
                            Prefs.removeDocNode(pk);
                        }
                    }
                }
            } else if (mdoc != null){
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner, I18N.BUNDLE.getString("DeleteElement_question_document"),I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    controller.delMDoc(mdoc);
                    FileIO.rmDir(Config.getCheckOutFolder(mdoc));
                    FileIO.rmDir(Config.getCacheFolder(mdoc));
                    Prefs.removeDocNode(mdoc);
                }
            } else if (wfModel != null){
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner, I18N.BUNDLE.getString("DeleteElement_question_workflow"),I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    controller.delWorkflowModel(wfModel);
                }
            } else if (template != null){
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner, I18N.BUNDLE.getString("DeleteElement_question_template"),I18N.BUNDLE.getString("DeleteElement_question_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
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
