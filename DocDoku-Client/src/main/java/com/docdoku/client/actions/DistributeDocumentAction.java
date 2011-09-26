/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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
import com.docdoku.core.document.MasterDocument;
import com.docdoku.client.ui.ExplorerFrame;

import javax.swing.*;

import java.awt.event.*;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.Document;
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
            MasterDocument mdoc = mOwner.getSelectedMDoc();
            String subject=mdoc +"";
            String body = Config.getPermaLink(mdoc) + "%0D%0A" + I18N.BUNDLE.getString("Distribution_message");
            body = body.replace(" ","%20");
            
            Document doc = mdoc.getLastIteration();
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
