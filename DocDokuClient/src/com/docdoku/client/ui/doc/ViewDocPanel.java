package com.docdoku.client.ui.doc;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.core.entities.Document;

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
