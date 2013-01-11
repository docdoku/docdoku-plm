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

import com.docdoku.client.ui.search.SearchDialog;
import com.docdoku.client.ui.search.SearchResultDialog;
import com.docdoku.client.ui.doc.EditLinksPanel;
import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentToDocumentLink;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Version;
import com.docdoku.core.document.DocumentMaster;

import com.docdoku.core.document.SearchQuery;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.Date;

public class AddLinkActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent pAE) {
        final EditLinksPanel sourcePanel = (EditLinksPanel) pAE.getSource();
        Dialog owner = (Dialog) SwingUtilities.getAncestorOfClass(Dialog.class, sourcePanel);
        ActionListener action = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent pAE) {
                SearchDialog source = (SearchDialog) pAE.getSource();
                try {
                    source.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    String id = source.getId();
                    String title = source.getDocMTitle();
                    Version version = source.getVersion();
                    User author = source.getAuthor();
                    String type = source.getDocumentType();
                    String[] tags = source.getTags();
                    SearchQuery.AbstractAttributeQuery[] attrs = source.getInstanceAttributes();
                    String content = source.getContent();
                    Date creationDateFrom = source.getCreationDateFrom();
                    Date creationDateTo = source.getCreationDateTo();
                    ActionListener action = new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent pAE) {
                            SearchResultDialog source = (SearchResultDialog) pAE.getSource();
                            for (DocumentMaster docM : source.getSelectedDocMs()) {
                                DocumentIteration fromDoc = sourcePanel.getEditedDoc();
                                sourcePanel.getLinksListModel().addElement(new DocumentToDocumentLink(fromDoc, docM.getLastIteration(), null));
                            }
                        }
                    };
                    DocumentMaster[] docMs = MainModel.getInstance().searchDocMs(id, title, version, author, type, creationDateFrom, creationDateTo, attrs, tags, content);
                    new SearchResultDialog(source, docMs, action, true);
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
