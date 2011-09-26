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
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Florent GARIN
 */
public class ShortcutsPanel extends JPanel {

    private WebLink mCloseLink;
    private ImageIcon mWorkflowShortcut;
    private ImageIcon mMDocShortcut;
    private ImageIcon mTemplateShortcut;
    private ImageIcon mFolderShortcut;
    private ImageIcon mFindShortcut;
    private ActionListener mCloseActionListener;
    private HasElementSelectedListeners mSelectionDispatcher;
    private WebLink mSCSearch;
    private WebLink mSCTemplate;
    private WebLink mSCWorkflow;
    private WebLink mSCFolder;
    private WebLink mSCMDoc;
    private ElementSelectedListener mCreateFolderAction;
    private ElementSelectedListener mCreateMDocAction;

    public ShortcutsPanel(ActionListener pCloseActionListener, HasElementSelectedListeners pSelectionDispatcher) {
        mCloseActionListener = pCloseActionListener;
        mSelectionDispatcher = pSelectionDispatcher;
        Image img = Toolkit.getDefaultToolkit().getImage(ShortcutsPanel.class.getResource("/com/docdoku/client/resources/icons/branch_element_new_big.png"));
        mWorkflowShortcut = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(ShortcutsPanel.class.getResource("/com/docdoku/client/resources/icons/document_new_big.png"));
        mMDocShortcut = new ImageIcon(img);

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
        mCreateMDocAction = new ElementSelectedListener() {

            @Override
            public void elementSelected(ElementSelectedEvent pElementSelectedEvent) {
                Object selection = pElementSelectedEvent.getElement();
                ElementType type = pElementSelectedEvent.getElementType();
                if (type == ElementSelectedEvent.ElementType.FOLDER_TREE_NODE && selection != null) {
                    if (selection.getClass().equals(RootTreeNode.class) || selection.getClass().equals(FolderTreeNode.class)) {
                        try {
                            pActionFactory.getCreateMDocAction().actionPerformed(new ActionEvent(this, 0, null));
                        } finally {
                            mSelectionDispatcher.removeElementSelectedListener(this);
                        }
                    }
                }
            }
        };
        mSCMDoc.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                JOptionPane.showMessageDialog(null,
                        I18N.BUNDLE.getString("Shortcuts_folder_selection"), I18N.BUNDLE.getString("CreateMDocDialog_title"), JOptionPane.INFORMATION_MESSAGE);
                mSelectionDispatcher.addElementSelectedListener(mCreateMDocAction);

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
                pActionFactory.getCreateMDocTemplateAction().actionPerformed(new ActionEvent(this, 0, null));
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

        mSCMDoc = new WebLink(I18N.BUNDLE.getString("Shortcut_mdoc"), mMDocShortcut);
        mSCMDoc.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                mCloseActionListener.actionPerformed(new ActionEvent(this, 0, null));
            }
        });
        linksPanel.add(mSCMDoc);

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
