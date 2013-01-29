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

package com.docdoku.client.ui.doc;

import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Set;

public class ViewLinksPanel extends JPanel {

    private JScrollPane mLinksScrollPane;
    private JList mLinksList;
    private JButton mViewDocButton;
    private DefaultListModel mLinksListModel;

    public ViewLinksPanel(final ActionListener pDownloadAction, final ActionListener pOpenAction) {
        mLinksListModel = new DefaultListModel();

        mLinksScrollPane = new JScrollPane();
        mLinksList = new JList(mLinksListModel);
        mLinksList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        Image img =
                Toolkit.getDefaultToolkit().getImage(ViewIterationsDialog.class.getResource(
                        "/com/docdoku/client/resources/icons/view_large.png"));
        ImageIcon viewIcon = new ImageIcon(img);

        mViewDocButton = new JButton(I18N.BUNDLE.getString("ViewDocument_button"), viewIcon);
        mViewDocButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                Dialog dialog = (Dialog) SwingUtilities.getAncestorOfClass(Dialog.class, ViewLinksPanel.this);
                DocumentMaster docM = MainModel.getInstance().getDocM(getSelectedLink());
                new ViewDocDetailsDialog(dialog, docM.getLastIteration(),pDownloadAction,pOpenAction);
            }
        });
        createLayout();
        createListener();
    }

    public ViewLinksPanel(Set<DocumentLink> pLinks,ActionListener pDownloadAction, ActionListener pOpenAction) {
        this(pDownloadAction,pOpenAction);
        for(DocumentLink link:pLinks) {
            mLinksListModel.addElement(link);
        }
    }

    private DocumentMasterKey getSelectedLink() {
        DocumentLink link=(DocumentLink) mLinksList.getSelectedValue();
        DocumentIterationKey targetKey = link.getTargetDocumentKey();
        String docMId=targetKey.getDocumentMasterId();
        String docMVersion=targetKey.getDocumentMasterVersion();
        String workspaceId=targetKey.getWorkspaceId();
        return new DocumentMasterKey(workspaceId,docMId,docMVersion);
    }

    private void createListener() {
        mLinksList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent pE) {
                if (mLinksList.isSelectionEmpty()) {
                    mViewDocButton.setEnabled(false);
                } else {
                    mViewDocButton.setEnabled(true);
                }

            }
        });
    }

    private void createLayout() {
        mViewDocButton.setHorizontalAlignment(SwingConstants.LEFT);
        mViewDocButton.setEnabled(false);
        mLinksScrollPane.getViewport().add(mLinksList);
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridwidth = 1;

        constraints.gridheight = 2;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(mLinksScrollPane, constraints);

        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridheight = 1;
        constraints.gridx = 1;
        add(mViewDocButton, constraints);
    }

}