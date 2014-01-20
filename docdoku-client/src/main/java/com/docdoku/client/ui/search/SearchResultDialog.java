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

package com.docdoku.client.ui.search;

import com.docdoku.client.data.SearchDocMsResultTableModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.ElementsScrollPane;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.document.DocumentMaster;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchResultDialog extends JDialog implements ActionListener {
    private ElementsScrollPane mSearchResultPanel;
    private OKCancelPanel mOKCancelPanel;
    private ActionListener mAction;
    private DocumentMaster[] mSelectedDocMs;

    public SearchResultDialog(JDialog pOwner, DocumentMaster[] pDocMs, ActionListener pAction, boolean pMultipleSelection) {
        super(pOwner, I18N.BUNDLE.getString("SearchResult_title"), true);
        setLocationRelativeTo(pOwner);
        mSearchResultPanel = new ElementsScrollPane(new SearchDocMsResultTableModel(pDocMs),null);
        
        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction = pAction;
        createLayout();
        createListener();
        setMultipleSelection(pMultipleSelection);
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        mSearchResultPanel.setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("SearchResult_border")));
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mSearchResultPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        mOKCancelPanel.setEnabled(false);
        pack();
    }

    public DocumentMaster[] getSelectedDocMs() {
        return mSelectedDocMs;
    }

    private void setMultipleSelection(boolean pMulti){
        if(pMulti)
            mSearchResultPanel.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        else
            mSearchResultPanel.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    private void createListener() {
        mSearchResultPanel
                .getSelectionModel()
                .addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent pE) {
                        Object[] selectedElements = mSearchResultPanel.getSelectedElements();
                        mOKCancelPanel.setEnabled(selectedElements.length>0);
                        mSelectedDocMs = new DocumentMaster[selectedElements.length];
                        for (int i = 0; i < selectedElements.length; i++) {
                            mSelectedDocMs[i] = (DocumentMaster) selectedElements[i];
                        }
                    }
                });
    }

    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
