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
package com.docdoku.client.ui.help;

import com.docdoku.client.backbone.ElementSelectedEvent;
import com.docdoku.client.backbone.ElementSelectedEvent.ElementType;
import com.docdoku.client.backbone.ElementSelectedListener;
import com.docdoku.client.backbone.HasElementSelectedListeners;
import com.docdoku.client.data.FolderTreeNode;
import com.docdoku.client.data.RootTreeNode;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ActionFactory;
import com.docdoku.client.ui.common.WebLink;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author Florent GARIN
 */
public class ShortcutsPanel extends JPanel {

    private WebLink mCloseLink;
    private ImageIcon mWorkflowShortcut;
    private ImageIcon mDocMShortcut;
    private ImageIcon mTemplateShortcut;
    private ImageIcon mFolderShortcut;
    private ImageIcon mFindShortcut;
    private ActionListener mCloseActionListener;
    private HasElementSelectedListeners mSelectionDispatcher;
    private WebLink mSCSearch;
    private WebLink mSCTemplate;
    private WebLink mSCWorkflow;
    private WebLink mSCFolder;
    private WebLink mSCDocM;
    private ElementSelectedListener mCreateFolderAction;
    private ElementSelectedListener mCreateDocMAction;

    public ShortcutsPanel(ActionListener pCloseActionListener, HasElementSelectedListeners pSelectionDispatcher) {
        mCloseActionListener = pCloseActionListener;
        mSelectionDispatcher = pSelectionDispatcher;
        Image img = Toolkit.getDefaultToolkit().getImage(ShortcutsPanel.class.getResource("/com/docdoku/client/resources/icons/branch_element_new_big.png"));
        mWorkflowShortcut = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(ShortcutsPanel.class.getResource("/com/docdoku/client/resources/icons/document_new_big.png"));
        mDocMShortcut = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(ShortcutsPanel.class.getResource("/com/docdoku/client/resources/icons/document_notebook_big.png"));
        mTemplateShortcut = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(ShortcutsPanel.class.getResource("/com/docdoku/client/resources/icons/folder_new_big.png"));
        mFolderShortcut = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(ShortcutsPanel.class.getResource("/com/docdoku/client/resources/icons/find_big.png"));
        mFindShortcut = new ImageIcon(img);

        createLayout();
    }

    public void setActions(final ActionFactory pActionFactory) {
        mCreateFolderAction = new ElementSelectedListener() {

            @Override
            public void elementSelected(ElementSelectedEvent pElementSelectedEvent) {
                Object selection = pElementSelectedEvent.getElement();
                ElementType type = pElementSelectedEvent.getElementType();
                if (type == ElementSelectedEvent.ElementType.FOLDER_TREE_NODE && selection != null) {
                    if (selection.getClass().equals(RootTreeNode.class) || selection.getClass().equals(FolderTreeNode.class)) {
                        try {
                            pActionFactory.getCreateFolderAction().actionPerformed(new ActionEvent(this, 0, null));
                        } finally {
                            mSelectionDispatcher.removeElementSelectedListener(this);
                        }
                    }
                }
            }
        };
        mSCFolder.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                JOptionPane.showMessageDialog(null,
                        I18N.BUNDLE.getString("Shortcuts_folder_selection"), I18N.BUNDLE.getString("CreateFolderDialog_title"), JOptionPane.INFORMATION_MESSAGE);
                mSelectionDispatcher.addElementSelectedListener(mCreateFolderAction);

            }
        });
        mCreateDocMAction = new ElementSelectedListener() {

            @Override
            public void elementSelected(ElementSelectedEvent pElementSelectedEvent) {
                Object selection = pElementSelectedEvent.getElement();
                ElementType type = pElementSelectedEvent.getElementType();
                if (type == ElementSelectedEvent.ElementType.FOLDER_TREE_NODE && selection != null) {
                    if (selection.getClass().equals(RootTreeNode.class) || selection.getClass().equals(FolderTreeNode.class)) {
                        try {
                            pActionFactory.getCreateDocMAction().actionPerformed(new ActionEvent(this, 0, null));
                        } finally {
                            mSelectionDispatcher.removeElementSelectedListener(this);
                        }
                    }
                }
            }
        };
        mSCDocM.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                JOptionPane.showMessageDialog(null,
                        I18N.BUNDLE.getString("Shortcuts_folder_selection"), I18N.BUNDLE.getString("CreateDocMDialog_title"), JOptionPane.INFORMATION_MESSAGE);
                mSelectionDispatcher.addElementSelectedListener(mCreateDocMAction);

            }
        });
        mSCSearch.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                pActionFactory.getSearchAction().actionPerformed(new ActionEvent(this, 0, null));
            }
        });
        mSCTemplate.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                pActionFactory.getCreateDocMTemplateAction().actionPerformed(new ActionEvent(this, 0, null));
            }
        });
        mSCWorkflow.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                pActionFactory.getCreateWorkflowModelAction().actionPerformed(new ActionEvent(this, 0, null));
            }
        });
    }

    private void createLayout() {
        setLayout(new BorderLayout());
        mCloseLink = new WebLink(I18N.BUNDLE.getString("Shortcuts_close"));
        mCloseLink.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                mCloseActionListener.actionPerformed(new ActionEvent(this, 0, null));
            }
        });

        add(BorderLayout.NORTH, mCloseLink);
        JPanel linksPanel = new JPanel(new GridLayout(3, 3));

        mSCFolder = new WebLink(I18N.BUNDLE.getString("Shortcut_folder"), mFolderShortcut);
        mSCFolder.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                mCloseActionListener.actionPerformed(new ActionEvent(this, 0, null));
            }
        });
        linksPanel.add(mSCFolder);

        linksPanel.add(new JPanel());

        mSCDocM = new WebLink(I18N.BUNDLE.getString("Shortcut_docm"), mDocMShortcut);
        mSCDocM.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                mCloseActionListener.actionPerformed(new ActionEvent(this, 0, null));
            }
        });
        linksPanel.add(mSCDocM);

        linksPanel.add(new JPanel());

        mSCSearch = new WebLink(I18N.BUNDLE.getString("Shortcut_search"), mFindShortcut);
        mSCSearch.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                mCloseActionListener.actionPerformed(new ActionEvent(this, 0, null));
            }
        });
        linksPanel.add(mSCSearch);

        linksPanel.add(new JPanel());

        mSCTemplate = new WebLink(I18N.BUNDLE.getString("Shortcut_template"), mTemplateShortcut);
        mSCTemplate.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                mCloseActionListener.actionPerformed(new ActionEvent(this, 0, null));
            }
        });
        linksPanel.add(mSCTemplate);

        linksPanel.add(new JPanel());

        mSCWorkflow = new WebLink(I18N.BUNDLE.getString("Shortcut_workflow"), mWorkflowShortcut);
        mSCWorkflow.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                mCloseActionListener.actionPerformed(new ActionEvent(this, 0, null));
            }
        });
        linksPanel.add(mSCWorkflow);

        add(BorderLayout.CENTER, linksPanel);
    }
}
