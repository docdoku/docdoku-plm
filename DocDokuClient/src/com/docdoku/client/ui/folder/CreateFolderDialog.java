package com.docdoku.client.ui.folder;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.entities.Folder;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateFolderDialog extends JDialog implements ActionListener {
    private CreateFolderPanel mCreateFolderPanel;
    private OKCancelPanel mOKCancelPanel;
    private ActionListener mAction;
    private Folder mParentFolder;

    public CreateFolderDialog(Frame pOwner, Folder pParentFolder, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("CreateFolderDialog_title"), true);
        setLocationRelativeTo(pOwner);
        mParentFolder=pParentFolder;
        mCreateFolderPanel = new CreateFolderPanel();
        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction = pAction;
        createLayout();
        createListener();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mCreateFolderPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        mOKCancelPanel.setEnabled(false);
        pack();
    }

    private void createListener() {
        DocumentListener listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent pDE) {
                mOKCancelPanel.setEnabled(true);
            }

            public void removeUpdate(DocumentEvent pDE) {
                int length = pDE.getDocument().getLength();
                if (length == 0)
                    mOKCancelPanel.setEnabled(false);
            }

            public void changedUpdate(DocumentEvent pDE) {
            }
        };
        mCreateFolderPanel.getFolderText().getDocument().addDocumentListener(listener);
    }

    public Folder getParentFolder() {
        return mParentFolder;
    }

    public String getFolder() {
        return mCreateFolderPanel.getFolder();
    }

    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
