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

package com.docdoku.client.ui.doc;

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.WebLink;
import com.docdoku.core.document.MasterDocument;
import com.docdoku.core.common.User;
import java.text.DateFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.docdoku.client.ui.workflow.ViewWorkflowDetailsDialog;
import com.docdoku.client.data.Config;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.workflow.Workflow;

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
        DateFormat format=DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.SHORT);
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

        mPermaLink = new WebLink(I18N.BUNDLE.getString("Permalink_label"),Config.getPermaLink(pWatchedMDoc));

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
