package com.docdoku.client.ui.search;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.entities.MasterDocument;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.docdoku.client.data.SearchMDocsResultTableModel;
import com.docdoku.client.ui.common.ElementsScrollPane;

public class SearchResultDialog extends JDialog implements ActionListener {
    private ElementsScrollPane mSearchResultPanel;
    private OKCancelPanel mOKCancelPanel;
    private ActionListener mAction;
    private MasterDocument[] mSelectedMDocs;

    public SearchResultDialog(JDialog pOwner, MasterDocument[] pMDocs, ActionListener pAction, boolean pMultipleSelection) {
        super(pOwner, I18N.BUNDLE.getString("SearchResult_title"), true);
        setLocationRelativeTo(pOwner);
        mSearchResultPanel = new ElementsScrollPane(new SearchMDocsResultTableModel(pMDocs),null);
        
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

    public MasterDocument[] getSelectedMDocs() {
        return mSelectedMDocs;
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
                        mSelectedMDocs = new MasterDocument[selectedElements.length];
                        for (int i = 0; i < selectedElements.length; i++) {
                            mSelectedMDocs[i] = (MasterDocument) selectedElements[i];
                        }
                    }
                });
    }

    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
