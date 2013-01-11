/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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
import com.docdoku.core.util.FileIO;
import com.docdoku.core.document.DocumentMasterTemplate;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InterruptedIOException;

import javax.swing.Action;
import javax.swing.JOptionPane;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.template.CreateDocMTemplateDialog;

public class CreateDocMTemplateAction extends ClientAbstractAction {
    
    public CreateDocMTemplateAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("DocMTemplateCreation_title"), "/com/docdoku/client/resources/icons/document_notebook_new.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("DocMTemplateCreation_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("DocMTemplateCreation_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("DocMTemplateCreation_mnemonic_key")));
    }

    @Override
    public void actionPerformed(ActionEvent pAE) {
        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pAE) {
                final CreateDocMTemplateDialog source = (CreateDocMTemplateDialog) pAE.getSource();
                Thread worker = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainController controller = MainController
                                .getInstance();
                        try {
                            // TODO the operation is split in 3 Tx may be
                            // it's
                            // better to gather all in one
                            DocumentMasterTemplate template=controller.createDocMTemplate(source.getDocMTemplateId(),source.getDocumentType(),source.getMask(), source.getAttributeTemplates(), source.isIdGenerated());
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
                            controller.updateDocMTemplate(template,source.getDocumentType(), source.getMask(), source.getAttributeTemplates(), source.isIdGenerated());
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
        ActionListener scanAction = new ScanActionListener();
        ActionListener addAttributeAction = new AddAttributeTemplateActionListener();
        new CreateDocMTemplateDialog(mOwner, action, editFileAction, scanAction, addAttributeAction);
    }
}
