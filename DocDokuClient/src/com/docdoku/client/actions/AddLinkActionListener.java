package com.docdoku.client.actions;

import com.docdoku.client.ui.search.SearchDialog;
import com.docdoku.client.ui.search.SearchResultDialog;
import com.docdoku.client.ui.doc.EditLinksPanel;
import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.Document;
import com.docdoku.core.entities.DocumentToDocumentLink;
import com.docdoku.core.entities.InstanceAttribute;
import com.docdoku.core.entities.User;
import com.docdoku.core.entities.keys.Version;
import com.docdoku.core.entities.MasterDocument;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.Date;

public class AddLinkActionListener implements ActionListener {

    public void actionPerformed(ActionEvent pAE) {
        final EditLinksPanel sourcePanel = (EditLinksPanel) pAE.getSource();
        Dialog owner = (Dialog) SwingUtilities.getAncestorOfClass(Dialog.class, sourcePanel);
        ActionListener action = new ActionListener() {

            public void actionPerformed(ActionEvent pAE) {
                SearchDialog source = (SearchDialog) pAE.getSource();
                try {
                    source.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    String id = source.getId();
                    String title = source.getMDocTitle();
                    Version version = source.getVersion();
                    User author = source.getAuthor();
                    String type = source.getType();
                    String[] tags = source.getTags();
                    InstanceAttribute[] attrs = source.getInstanceAttributes();
                    String content = source.getContent();
                    Date creationDateFrom = source.getCreationDateFrom();
                    Date creationDateTo = source.getCreationDateTo();
                    ActionListener action = new ActionListener() {

                        public void actionPerformed(ActionEvent pAE) {
                            SearchResultDialog source = (SearchResultDialog) pAE.getSource();
                            for (MasterDocument mdoc : source.getSelectedMDocs()) {
                                Document fromDoc = sourcePanel.getEditedDoc();
                                sourcePanel.getLinksListModel().addElement(new DocumentToDocumentLink(fromDoc, mdoc.getLastIteration(), null));
                            }
                        }
                    };
                    MasterDocument[] mdocs = MainModel.getInstance().searchMDocs(id, title, version, author, type, creationDateFrom, creationDateTo, attrs, tags, content);
                    new SearchResultDialog(source, mdocs, action, true);
                } catch (Exception pEx) {
                    String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                    JOptionPane.showMessageDialog(null,
                            message, I18N.BUNDLE.getString("Error_title"),
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    source.setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        new SearchDialog(owner, action);
    }
}
