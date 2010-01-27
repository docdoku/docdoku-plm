package com.docdoku.client.ui.search;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.entities.InstanceAttribute;
import com.docdoku.core.entities.User;
import com.docdoku.core.entities.keys.Version;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class SearchDialog extends JDialog implements ActionListener {
    private SearchPanel mSearchPanel;
    private SearchAdvancedPanel mSearchAdvancedPanel;
    private SearchAttributesPanel mSearchAttributesPanel;
    private OKCancelPanel mOKCancelPanel;
    private JTabbedPane mTabbedPane;
    private ActionListener mAction;

    public SearchDialog(Frame pOwner, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("Search_title"), true);
        init(pOwner, pAction);
    }

    public SearchDialog(Dialog pOwner, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("Search_title"), true);
        init(pOwner, pAction);
    }

    private void init(Window pOwner, ActionListener pAction) {
        setLocationRelativeTo(pOwner);
        mSearchPanel = new SearchPanel();
        mSearchAdvancedPanel = new SearchAdvancedPanel();
        mSearchAttributesPanel = new SearchAttributesPanel();
        mOKCancelPanel = new OKCancelPanel(this, this);
        mTabbedPane = new JTabbedPane();
        mAction = pAction;
        createLayout();
        createListener();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("Search_border")));
        mTabbedPane.add(I18N.BUNDLE.getString("Main_border"), mSearchPanel);
        mTabbedPane.add(I18N.BUNDLE.getString("Attributes_border"), mSearchAttributesPanel);
        mTabbedPane.add(I18N.BUNDLE.getString("Advanced_border"), mSearchAdvancedPanel);
        mainPanel.add(mTabbedPane, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        //mOKCancelPanel.setEnabled(true);
        pack();
    }

    private void createListener() {

    }

    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }

    public String getId() {
        return mSearchPanel.getId();
    }

    public String getMDocTitle() {
        return mSearchPanel.getTitle();
    }
    
    public String[] getTags() {
        return mSearchAdvancedPanel.getTags();
    }

    public String getContent() {
        return mSearchAdvancedPanel.getContent();
    }
    
    public Version getVersion() {
        return mSearchPanel.getVersion();
    }

    public String getType() {
        return mSearchPanel.getType();
    }
    
    public User getAuthor() {
        return mSearchPanel.getAuthor();
    }

    public InstanceAttribute[] getInstanceAttributes(){
        return mSearchAttributesPanel.getInstanceAttributes();
    }
    
    public Date getCreationDateFrom() {
        return mSearchPanel.getCreationDateFrom();
    }

    public Date getCreationDateTo() {
        return mSearchPanel.getCreationDateTo();
    }
}
