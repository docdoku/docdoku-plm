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

package com.docdoku.client.ui;

import com.docdoku.client.data.Config;
import com.docdoku.client.ui.common.ButtonMenu;
import com.docdoku.client.ui.common.OKButton;
import java.net.URI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import java.awt.Desktop;

public class ExplorerMenu extends JMenuBar {
    
    private ButtonMenu mNewDoc;
    private ButtonMenu mNewFolder;
    private ButtonMenu mNewWorkflow;
    private ButtonMenu mNewTemplate;
    private ButtonMenu mNewWin;
    private ButtonMenu mExport;
    private ButtonMenu mClose;
    private JMenuItem mQuit;
    
    private ButtonMenu mCheckIn;
    private ButtonMenu mCheckOut;
    private ButtonMenu mUndoCheckOut;
    private ButtonMenu mDeleteElement;
    private ButtonMenu mEditDoc;
    private ButtonMenu mVersionDocM;
    private ButtonMenu mApprove;
    private ButtonMenu mReject;

    private ButtonMenu mDistributeDocument;
    private ButtonMenu mNotification;
    private ButtonMenu mManageTags;
    
    private ButtonMenu mRefresh;
    private ButtonMenu mViewDoc;
    private ButtonMenu mViewIterations;
    private ButtonMenu mSearch;
    
    private ButtonMenu mUser;
    private ButtonMenu mSetting;
    
    private ButtonMenu mShortcuts;
    private JMenuItem mAbout;
    
    public ExplorerMenu(JLabel pStatusLabel) {
        JMenu file = new JMenu(I18N.BUNDLE.getString("FileMenu_title"));
        JMenu action = new JMenu(I18N.BUNDLE.getString("ActionsMenu_title"));
        JMenu view = new JMenu(I18N.BUNDLE.getString("ViewMenu_title"));
        JMenu conf = new JMenu(I18N.BUNDLE.getString("ConfigurationMenu_title"));
        JMenu help = new JMenu(I18N.BUNDLE.getString("QuestionMenu_title"));

        //workaround for synthetica plaf that doesn't apply insets
        //on menu item
        file.setMargin(GUIConstants.MENU_INSETS);
        action.setMargin(GUIConstants.MENU_INSETS);
        view.setMargin(GUIConstants.MENU_INSETS);
        conf.setMargin(GUIConstants.MENU_INSETS);
        help.setMargin(GUIConstants.MENU_INSETS);
        
        
        add(file);
        add(action);
        add(view);
        add(conf);
        add(help);
        
        file.setMnemonic(I18N.getCharBundle("FileMenu_mnemonic_key"));
        action.setMnemonic(I18N.getCharBundle("ActionsMenu_mnemonic_key"));
        view.setMnemonic(I18N.getCharBundle("ViewMenu_mnemonic_key"));
        conf.setMnemonic(I18N.getCharBundle("ConfigurationMenu_mnemonic_key"));
        help.setMnemonic(I18N.getCharBundle("QuestionMenu_mnemonic_key"));
        
        mNewWin = new ButtonMenu(pStatusLabel);
        
        JMenu newSubMenu = new JMenu(I18N.BUNDLE.getString("NewMenuOption_title"));
        newSubMenu.setMnemonic(I18N.getCharBundle("NewMenuOption_mnemonic_key"));
        
        mNewDoc = new ButtonMenu(pStatusLabel);
        mNewFolder = new ButtonMenu(pStatusLabel);
        mNewWorkflow = new ButtonMenu(pStatusLabel);
        mNewTemplate = new ButtonMenu(pStatusLabel);
        newSubMenu.add(mNewDoc);
        newSubMenu.add(mNewFolder);
        newSubMenu.add(mNewTemplate);
        newSubMenu.add(mNewWorkflow);
        
        mClose = new ButtonMenu(pStatusLabel);
        mExport = new ButtonMenu(pStatusLabel);
        
        Image img =
                Toolkit.getDefaultToolkit().getImage(ExplorerMenu.class.getResource("/com/docdoku/client/resources/icons/exit.png"));
        ImageIcon exitIcon = new ImageIcon(img);
        mQuit = new JMenuItem(I18N.BUNDLE.getString("ExitMenuOption_title"), exitIcon);
        mQuit.setMnemonic(I18N.getCharBundle("ExitMenuOption_mnemonic_key"));
        
        file.add(mNewWin);
        file.add(newSubMenu);
        file.addSeparator();
        file.add(mExport);
        file.addSeparator();
        file.add(mClose);
        file.addSeparator();
        file.add(mQuit);
        
        mCheckIn = new ButtonMenu(pStatusLabel);
        mCheckOut = new ButtonMenu(pStatusLabel);
        mUndoCheckOut = new ButtonMenu(pStatusLabel);
        mDeleteElement = new ButtonMenu(pStatusLabel);
        mEditDoc = new ButtonMenu(pStatusLabel);
        mVersionDocM = new ButtonMenu(pStatusLabel);
        mApprove = new ButtonMenu(pStatusLabel);
        mReject = new ButtonMenu(pStatusLabel);
        mDistributeDocument = new ButtonMenu(pStatusLabel);
        mNotification = new ButtonMenu(pStatusLabel);
        mManageTags = new ButtonMenu(pStatusLabel);
        
        action.add(mCheckIn);
        action.add(mCheckOut);
        action.add(mUndoCheckOut);
        action.addSeparator();
        action.add(mDeleteElement);
        action.add(mEditDoc);
        action.add(mVersionDocM);
        action.addSeparator();
        action.add(mApprove);
        action.add(mReject);
        action.addSeparator();
        action.add(mDistributeDocument);
        action.add(mNotification);
        action.add(mManageTags);
        
        mRefresh = new ButtonMenu(pStatusLabel);
        mViewDoc = new ButtonMenu(pStatusLabel);
        mViewIterations = new ButtonMenu(pStatusLabel);
        mSearch = new ButtonMenu(pStatusLabel);
        
        view.add(mRefresh);
        view.addSeparator();
        view.add(mViewDoc);
        view.add(mViewIterations);
        view.addSeparator();
        view.add(mSearch);
        
        mUser = new ButtonMenu(pStatusLabel);
        mSetting = new ButtonMenu(pStatusLabel);
        conf.add(mUser);
        conf.add(mSetting);
        
        img = Toolkit.getDefaultToolkit().getImage(ExplorerMenu.class.getResource("/com/docdoku/client/resources/icons/about.png"));
        ImageIcon aboutIcon = new ImageIcon(img);
        mAbout = new JMenuItem(I18N.BUNDLE.getString("AboutMenuOption_title"), aboutIcon);
        mAbout.setMnemonic(I18N.getCharBundle("AboutMenuOption_mnemonic_key"));
        
        img = Toolkit.getDefaultToolkit().getImage(ExplorerMenu.class.getResource("/com/docdoku/client/resources/icons/help.png"));
        ImageIcon helpIcon = new ImageIcon(img);
                
        mShortcuts = new ButtonMenu(pStatusLabel);
        
        help.add(mShortcuts);
        help.add(mAbout);
    }
    
    public void setActions(ActionFactory pActionFactory) {
        mNewDoc.setAction(pActionFactory.getCreateDocMAction());
        mNewFolder.setAction(pActionFactory.getCreateFolderAction());
        mNewWorkflow.setAction(pActionFactory.getCreateWorkflowModelAction());
        mNewTemplate.setAction(pActionFactory.getCreateDocMTemplateAction());
        mNewWin.setAction(pActionFactory.getNewWinAction());
        mExport.setAction(pActionFactory.getExportAction());
        mClose.setAction(pActionFactory.getCloseWinAction());
        
        mQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pAE) {
                System.exit(0);
            }
        });
        
        mCheckIn.setAction(pActionFactory.getCheckInAction());
        mCheckOut.setAction(pActionFactory.getCheckOutAction());
        mUndoCheckOut.setAction(pActionFactory.getUndoCheckOutAction());
        mDeleteElement.setAction(pActionFactory.getDeleteElementAction());
        mEditDoc.setAction(pActionFactory.getEditElementAction());
        mVersionDocM.setAction(pActionFactory.getCreateVersionAction());
        mApprove.setAction(pActionFactory.getApproveAction());
        mReject.setAction(pActionFactory.getRejectAction());
        mDistributeDocument.setAction(pActionFactory.getDistributeDocumentAction());
        mNotification.setAction(pActionFactory.getNotificationAction());
        mManageTags.setAction(pActionFactory.getManageTagsAction());
        
        mRefresh.setAction(pActionFactory.getRefreshAction());
        mViewDoc.setAction(pActionFactory.getViewElementAction());
        mViewIterations.setAction(pActionFactory.getViewIterationsAction());
        mSearch.setAction(pActionFactory.getSearchAction());
        mUser.setAction(pActionFactory.getEditUserAction());
        mSetting.setAction(pActionFactory.getSettingAction());
        
        mShortcuts.setAction(pActionFactory.getDisplayShortcutsAction());
        mAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pAE) {
                String aboutMessage = I18N.BUNDLE.getString("About_copyright");
                
                final OKButton option = new OKButton(I18N.BUNDLE.getString("Ok_button"));
                option.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Window topWindow =
                                (Window) option.getTopLevelAncestor();
                        topWindow.dispose();
                    }
                });
                
                Object[] options = {option};
                JOptionPane.showOptionDialog(
                        null,
                        aboutMessage,
                        I18N.BUNDLE.getString("AboutMenuOption_title"),
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]);
            }
        });
    }
}
