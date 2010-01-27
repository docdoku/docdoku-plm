package com.docdoku.client.actions;

import com.docdoku.client.data.Config;
import com.docdoku.client.data.MainModel;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.entities.BinaryResource;
import com.docdoku.core.entities.MasterDocumentTemplate;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.template.CreateMDocTemplateDialog;

public class CreateMDocTemplateAction extends ClientAbstractAction {
    
    public CreateMDocTemplateAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("MDocTemplateCreation_title"), "/com/docdoku/client/resources/icons/document_notebook_new.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("MDocTemplateCreation_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("MDocTemplateCreation_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("MDocTemplateCreation_mnemonic_key")));
    }
    
    public void actionPerformed(ActionEvent pAE) {
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                final CreateMDocTemplateDialog source = (CreateMDocTemplateDialog) pAE.getSource();
                Thread worker = new Thread(new Runnable() {
                    public void run() {
                        MainController controller = MainController
                                .getInstance();
                        try {
                            // TODO the operation is split in 3 Tx may be
                            // it's
                            // better to gather all in one
                            MasterDocumentTemplate template=controller.createMDocTemplate(source.getMDocTemplateID(),source.getDocumentType(),source.getMask(), source.getAttributeTemplates(), source.isIdGenerated());
                            for(File fileToAdd:source.getFilesToAdd()){
                                try{
                                    controller.saveFile(source, template, fileToAdd);
                                    File destFolder = Config.getCacheFolder(template);
                                    destFolder.mkdirs();
                                    File destFile = new File(destFolder, fileToAdd.getName());
                                    destFile.deleteOnExit();
                                    FileIO.copyFile(fileToAdd,destFile);
                                }catch (InterruptedIOException pIIOEx) {
                                    
                                }
                            }
                            //Force reload to cache template
                            //TODO remove this call it's a dirty patch
                            controller.updateMDocTemplate(template,source.getDocumentType(), source.getMask(), source.getAttributeTemplates(), source.isIdGenerated());
                            source.dispose();
                        } catch (Exception pEx) {
                            String message = pEx.getMessage()==null?I18N.BUNDLE
                                    .getString("Error_unknown"):pEx.getMessage();
                            JOptionPane.showMessageDialog(null,
                                    message, I18N.BUNDLE
                                    .getString("Error_title"),
                                    JOptionPane.ERROR_MESSAGE);
                        }
                        ExplorerFrame.unselectElementInAllFrame();
                    }
                });
                worker.start();
            }
        };
        ActionListener editFileAction = new EditFileActionListener();
        ActionListener addAttributeAction = new AddAttributeTemplateActionListener();
        new CreateMDocTemplateDialog(mOwner, action, editFileAction, addAttributeAction);
    }
}
