package com.docdoku.client.ui.doc;

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.WebLink;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.User;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.docdoku.client.ui.workflow.ViewWorkflowDetailsDialog;
import com.docdoku.client.data.Config;
import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.Workflow;

public class ViewMDocPanel extends DocPanel {

    private JLabel mTitleLabel;
    private JLabel mTitleValueLabel;
    private JLabel mTypeLabel;
    private JLabel mTypeValueLabel;
    private JLabel mCheckOutUserLabel;
    private JLabel mCheckOutUserValueLabel;
    private JLabel mCheckedDateLabel;
    private JLabel mCheckedDateValueLabel;
    private JLabel mLifeCycleStateLabel;
    private JLabel mLifeCycleStateValueLabel;
    private JLabel mTagsLabel;
    private JLabel mTagsValueLabel;
    private WebLink mWorkFlowLink;
    private WebLink mPermaLink;

    public ViewMDocPanel(final MasterDocument pWatchedMDoc) {
        super(pWatchedMDoc);
        DateFormat format=DateFormat.getInstance();
        mTitleLabel = new JLabel(I18N.BUNDLE.getString("Title_label"));
        mTitleValueLabel = new JLabel(pWatchedMDoc.getTitle());
        mTypeLabel = new JLabel(I18N.BUNDLE.getString("Type_label"));
        mTypeValueLabel = new JLabel(pWatchedMDoc.getType());
        mCheckOutUserLabel = new JLabel(I18N.BUNDLE.getString("CheckoutUser_label"));
        User checkOutUser = pWatchedMDoc.getCheckOutUser();
        mCheckOutUserValueLabel = new JLabel(checkOutUser==null?"":checkOutUser.getName());
        mCheckedDateLabel = new JLabel(I18N.BUNDLE.getString("CheckoutDate_label"));
        mCheckedDateValueLabel = new JLabel(pWatchedMDoc.getCheckOutDate()!=null?format.format(pWatchedMDoc.getCheckOutDate()):"");
        mLifeCycleStateLabel = new JLabel(I18N.BUNDLE.getString("LifeCycleState_label"));
        mLifeCycleStateValueLabel =
                new JLabel(pWatchedMDoc.getLifeCycleState());

        String file = null;
        try {
            file = "documents/"
                    + URLEncoder.encode(MainModel.getInstance().getWorkspace().getId(),"UTF-8") + "/"
                    + URLEncoder.encode(pWatchedMDoc.getId(),"UTF-8") + "/"
                    + pWatchedMDoc.getVersion();
        } catch (UnsupportedEncodingException pEx) {
            System.err.println(pEx.getMessage());
        }
        mPermaLink = new WebLink(I18N.BUNDLE.getString("Permalink_label"),Config.getHTTPCodebase().toString() + file);

        final Workflow workflow = pWatchedMDoc.getWorkflow();
        if (workflow != null) {
            mWorkFlowLink = new WebLink(I18N.BUNDLE.getString("Details_label"));
            mWorkFlowLink.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent pEvent) {
                    Dialog owner = (Dialog) SwingUtilities.getAncestorOfClass(Dialog.class, ViewMDocPanel.this);
                    new ViewWorkflowDetailsDialog(owner, workflow);
                }
            });
        }
        mTagsLabel = new JLabel(I18N.BUNDLE.getString("Tags_label"));
        mTagsValueLabel = new JLabel(pWatchedMDoc.getTags().size()!=0?pWatchedMDoc.getTags().toString():"");
        createLayout();

    }

    private void createLayout() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 0;
        constraints.weightx = 0;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;

        constraints.gridx = 0;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        
        constraints.gridy = 3;
        add(mTypeLabel, constraints);

        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mTitleLabel, constraints);
        
        add(mCheckOutUserLabel, constraints);

        add(mCheckedDateLabel, constraints);

        add(mLifeCycleStateLabel, constraints);
        
        add(mTagsLabel, constraints);

        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.WEST;
             
        constraints.gridy = 3;
        add(mTypeValueLabel, constraints);

        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mTitleValueLabel, constraints);

        add(mCheckOutUserValueLabel, constraints);

        add(mCheckedDateValueLabel, constraints);

        add(mLifeCycleStateValueLabel, constraints);

        add(mTagsValueLabel, constraints);
        
        if (mWorkFlowLink != null) {
            constraints.gridy = 7;
            constraints.gridx = 2;
            add(mWorkFlowLink, constraints);
        }
        
        constraints.gridy = 0;
        constraints.gridx = 2;
        add(mPermaLink, constraints);
    }
}
