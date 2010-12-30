/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

package com.docdoku.client.ui.setting;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.client.data.MainModel;
import com.docdoku.client.data.Prefs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Locale;

public class SettingDialog extends JDialog implements ActionListener {
    private JTabbedPane mTabbedPane;
    private LookAndFeelPanel mLookAndFeelPanel;
    private LocalePanel mLocalePanel;
    private OKCancelPanel mOKCancelPanel;
    private ActionListener mAction;
	

    public SettingDialog(Frame pOwner, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("Setting_title"), true);
        setLocationRelativeTo(pOwner);
        mTabbedPane = new JTabbedPane();
        boolean numberedNode = MainModel.getInstance().getElementsTreeModel().getNumbered();
        mLookAndFeelPanel = new LookAndFeelPanel(numberedNode);
        mLocalePanel = new LocalePanel(Prefs.getLocale());
        mTabbedPane.add(I18N.BUNDLE.getString("Look_label"), mLookAndFeelPanel);
        mTabbedPane.add(I18N.BUNDLE.getString("Locale_label"), mLocalePanel);

        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction = pAction;
        createLayout();
        setVisible(true);
    }

    public boolean numberedNode() {
        return mLookAndFeelPanel.numberedNode();
    }
    
    public Locale getSelectedLocale() {
        return mLocalePanel.getSelectedLocale();
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        mainPanel.add(mTabbedPane, BorderLayout.CENTER);
        setContentPane(mainPanel);
        pack();
    }

    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
