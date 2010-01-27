package com.docdoku.client.ui;

import com.docdoku.client.data.Config;
import com.docdoku.client.ui.common.ButtonMenu;
import com.docdoku.client.ui.common.OKButton;
import java.net.URI;
import java.net.URL;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.docdoku.client.localization.I18N;
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
    private ButtonMenu mVersionMDoc;
    private ButtonMenu mApprove;
    private ButtonMenu mReject;
    private ButtonMenu mNotification;
    private ButtonMenu mManageTags;
    
    private ButtonMenu mRefresh;
    private ButtonMenu mViewDoc;
    private ButtonMenu mViewIterations;
    private ButtonMenu mSearch;
    
    private ButtonMenu mUser;
    private ButtonMenu mSetting;
    
    private JMenuItem mHelpOnline;
    private JMenuItem mAbout;
    
    public ExplorerMenu(JLabel pStatusLabel) {
        JMenu file = new JMenu(I18N.BUNDLE.getString("FileMenu_title"));
        JMenu action = new JMenu(I18N.BUNDLE.getString("ActionsMenu_title"));
        JMenu view = new JMenu(I18N.BUNDLE.getString("ViewMenu_title"));
        JMenu conf = new JMenu(I18N.BUNDLE.getString("ConfigurationMenu_title"));
        JMenu help = new JMenu(I18N.BUNDLE.getString("QuestionMenu_title"));
        
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
        mVersionMDoc = new ButtonMenu(pStatusLabel);
        mApprove = new ButtonMenu(pStatusLabel);
        mReject = new ButtonMenu(pStatusLabel);
        mNotification = new ButtonMenu(pStatusLabel);
        mManageTags = new ButtonMenu(pStatusLabel);
        
        action.add(mCheckIn);
        action.add(mCheckOut);
        action.add(mUndoCheckOut);
        action.addSeparator();
        action.add(mDeleteElement);
        action.add(mEditDoc);
        action.add(mVersionMDoc);
        action.addSeparator();
        action.add(mApprove);
        action.add(mReject);
        action.addSeparator();
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
        mHelpOnline = new JMenuItem(I18N.BUNDLE.getString("HeplMenuOption_title"), helpIcon);
        mHelpOnline.setMnemonic(I18N.getCharBundle("HeplMenuOption_mnemonic_key"));
        
        help.add(mHelpOnline);
        help.add(mAbout);
    }
    
    public void setActions(ActionFactory pActionFactory) {
        mNewDoc.setAction(pActionFactory.getCreateMDocAction());
        mNewFolder.setAction(pActionFactory.getCreateFolderAction());
        mNewWorkflow.setAction(pActionFactory.getCreateWorkflowModelAction());
        mNewTemplate.setAction(pActionFactory.getCreateMDocTemplateAction());
        mNewWin.setAction(pActionFactory.getNewWinAction());
        mExport.setAction(pActionFactory.getExportAction());
        mClose.setAction(pActionFactory.getCloseWinAction());
        
        mQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                System.exit(0);
            }
        });
        
        mCheckIn.setAction(pActionFactory.getCheckInAction());
        mCheckOut.setAction(pActionFactory.getCheckOutAction());
        mUndoCheckOut.setAction(pActionFactory.getUndoCheckOutAction());
        mDeleteElement.setAction(pActionFactory.getDeleteElementAction());
        mEditDoc.setAction(pActionFactory.getEditElementAction());
        mVersionMDoc.setAction(pActionFactory.getCreateVersionAction());
        mApprove.setAction(pActionFactory.getApproveAction());
        mReject.setAction(pActionFactory.getRejectAction());
        mNotification.setAction(pActionFactory.getNotificationAction());
        mManageTags.setAction(pActionFactory.getManageTagsAction());
        
        mRefresh.setAction(pActionFactory.getRefreshAction());
        mViewDoc.setAction(pActionFactory.getViewElementAction());
        mViewIterations.setAction(pActionFactory.getViewIterationsAction());
        mSearch.setAction(pActionFactory.getSearchAction());
        mUser.setAction(pActionFactory.getEditUserAction());
        mSetting.setAction(pActionFactory.getSettingAction());
        
        mHelpOnline.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                try {
                    String page = "help";
                    Desktop.getDesktop().browse(new URI(Config.getHTTPCodebase().toString()+page));
                } catch (Exception pEx) {
                    String message = pEx.getMessage()==null?I18N.BUNDLE
                            .getString("Error_unknown"):pEx.getMessage();
                    JOptionPane.showMessageDialog(null,
                            message, I18N.BUNDLE
                            .getString("Error_title"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        mAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                String aboutMessage = I18N.BUNDLE.getString("About_copyright");
                
                final OKButton option = new OKButton(I18N.BUNDLE.getString("Ok_button"));
                option.addActionListener(new ActionListener() {
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
