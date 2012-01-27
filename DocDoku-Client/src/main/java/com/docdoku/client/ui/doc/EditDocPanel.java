/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.core.document.Document;

import javax.swing.*;
import java.awt.*;

public class EditDocPanel extends DocPanel {

    private JLabel mCommentLabel;
    private JTextField mCommentText;

    public EditDocPanel(Document pEditedDoc) {
        super(pEditedDoc);
        mCommentText =
                new JTextField(
                        new MaxLengthDocument(255),
                        pEditedDoc.getRevisionNote(),
                        10);
        mCommentLabel = new JLabel(I18N.BUNDLE.getString("RevisionNote_label"));
        createLayout();
    }

    public String getComment() {
        return mCommentText.getText();
    }

    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("EditDocPanel_border")));
        mCommentLabel.setLabelFor(mCommentText);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mCommentText, constraints);
        
        constraints.weightx = 0;
        constraints.gridx = 0;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        add(mCommentLabel, constraints);
    }
}
