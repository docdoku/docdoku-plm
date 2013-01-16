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

package com.docdoku.client.ui.doc;

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentMaster;
import java.text.DateFormat;

import javax.swing.*;
import java.awt.*;
import com.docdoku.client.localization.I18N;

public abstract class DocPanel extends JPanel {

    private JLabel mAuthorLabel;
    private JLabel mAuthorValueLabel;
    private JLabel mIDLabel;
    private JLabel mIDValueLabel;
    private JLabel mCreationDateLabel;
    private JLabel mCreationDateValueLabel;

    public DocPanel(DocumentMaster pDocM) {
        DateFormat format=DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.SHORT);
        mAuthorLabel = new JLabel(I18N.BUNDLE.getString("Author_label"));
        mAuthorValueLabel = new JLabel(pDocM.getAuthor().getName());
        mIDLabel = new JLabel(I18N.BUNDLE.getString("ID_label"));
        mIDValueLabel = new JLabel(pDocM.toString());
        mCreationDateLabel = new JLabel(I18N.BUNDLE.getString("CreationDate_label"));
        mCreationDateValueLabel = new JLabel(format.format(pDocM.getCreationDate()));
        createLayout();
    }

    public DocPanel(DocumentIteration pDoc) {
        DateFormat format=DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.SHORT);
        mAuthorLabel = new JLabel(I18N.BUNDLE.getString("Author_label"));
        mAuthorValueLabel = new JLabel(pDoc.getAuthor().getName());
        mIDLabel = new JLabel(I18N.BUNDLE.getString("ID_label"));
        mIDValueLabel = new JLabel(pDoc.toString());
        mCreationDateLabel = new JLabel(I18N.BUNDLE.getString("ModificationDate_label"));
        mCreationDateValueLabel = new JLabel(format.format(pDoc.getCreationDate()));
        createLayout();
    }
    
    private void createLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 0;
        constraints.weightx = 0;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;

        constraints.gridx = 0;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridy = 0;
        add(mIDLabel, constraints);

        constraints.gridy = 1;
        add(mAuthorLabel, constraints);

        constraints.gridy = 2;
        add(mCreationDateLabel, constraints);

        constraints.gridy = 0;
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        add(mIDValueLabel, constraints);

        constraints.gridy = 1;
        add(mAuthorValueLabel, constraints);

        constraints.gridy = 2;
        add(mCreationDateValueLabel, constraints);
    }
}
