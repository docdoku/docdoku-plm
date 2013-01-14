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
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.client.ui.ExplorerFrame;

import javax.swing.*;

import java.awt.event.*;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIteration;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.io.File;
import java.net.URI;

public class DistributeDocumentAction extends ClientAbstractAction {
    
    public DistributeDocumentAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("DistributeDocument_title"), "/com/docdoku/client/resources/icons/mail_attachment.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("DistributeDocument_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("DistributeDocument_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("DistributeDocument_mnemonic_key")));
    }

    @Override
    public void actionPerformed(ActionEvent pAE) {
        try {
            DocumentMaster docM = mOwner.getSelectedDocM();
            String subject=docM +"";
            String body = Config.getPermaLink(docM) + "%0D%0A" + I18N.BUNDLE.getString("Distribution_message");
            body = body.replace(" ","%20");
            
            DocumentIteration doc = docM.getLastIteration();
            if(doc!=null && !doc.getAttachedFiles().isEmpty()){
                FileTransferable ft = new FileTransferable();

                for(BinaryResource remoteFile:doc.getAttachedFiles()){
                    File localFile = MainModel.getInstance().getFile(null,doc,remoteFile);
                    ft.addFile(localFile);
                }

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(ft, null);

            }
            Desktop.getDesktop().mail(URI.create("mailto:?subject=" + subject + "&body="+body));
        }catch (Exception pEx) {
            String message = pEx.getMessage()==null?I18N.BUNDLE
                    .getString("Error_unknown"):pEx.getMessage();
            JOptionPane.showMessageDialog(null,
                    message, I18N.BUNDLE
                    .getString("Error_title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
