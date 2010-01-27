package com.docdoku.client.ui.tag;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.*;
import com.docdoku.core.entities.MasterDocument;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ManageTagsDialog extends JDialog implements ActionListener {
    private ManageTagsPanel mManageTagsPanel;
    private OKCancelPanel mOKCancelPanel;
    private ActionListener mAction;
    private MasterDocument mEditedMDoc;

    public ManageTagsDialog(Frame pOwner, MasterDocument pEditedMDoc, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("ManageTagsDialog_title"), true);
        setLocationRelativeTo(pOwner);
        mEditedMDoc=pEditedMDoc;
        mManageTagsPanel = new ManageTagsPanel(mEditedMDoc.getTags());
        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction = pAction;
        createLayout();
        createListener();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mManageTagsPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        mOKCancelPanel.setEnabled(true);
        pack();
    }

    private void createListener() {
        
    }

    public MasterDocument getMDoc() {
        return mEditedMDoc;
    }

    public String[] getTags() {
        String[] tags = new String[mManageTagsPanel.getTagsListModel().getSize()];
        for(int i=0;i<mManageTagsPanel.getTagsListModel().getSize();i++)
            tags[i]=(String)mManageTagsPanel.getTagsListModel().get(i);

        return tags;
    }
    
    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
