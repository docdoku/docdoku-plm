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

package com.docdoku.client.ui.doc;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.core.document.Document;

import javax.swing.*;
import java.awt.*;

public class ViewDocPanel extends DocPanel {

    private JLabel mRevisionNoteValueLabel;
    private JLabel mRevisionNoteLabel;

    public ViewDocPanel(Document pWatchedDoc) {
        super(pWatchedDoc);
        mRevisionNoteValueLabel = new JLabel(pWatchedDoc.getRevisionNote());
        mRevisionNoteLabel = new JLabel(I18N.BUNDLE.getString("RevisionNote_label"));
        createLayout();
    }

    private void createLayout() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 1;
        constraints.gridy = 3;
        add(mRevisionNoteValueLabel, constraints);
        
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridx = 0;
        add(mRevisionNoteLabel, constraints);
    }
}
