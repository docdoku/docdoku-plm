/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.client.ui.search;

import com.docdoku.client.ui.common.HelpButton;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;

public class SearchAdvancedPanel extends JPanel {

    
    private JLabel mTagsLabel;
    private JTextField mTagsText;

    private JLabel mContentLabel;
    private JTextField mContentText;
    private HelpButton mHelp;

    public SearchAdvancedPanel() {
        mTagsLabel = new JLabel(I18N.BUNDLE.getString("Tags_label"));
        mTagsText = new JTextField(new MaxLengthDocument(255), "", 10);
        
        mContentLabel = new JLabel(I18N.BUNDLE.getString("Content_label"));
        mContentText = new JTextField(new MaxLengthDocument(255), "", 10);
        mHelp=new HelpButton(I18N.BUNDLE.getString("SearchAdvancedPanel_tiptooltext"));
        
        createLayout();
    }

    private void createLayout() {
        mContentLabel.setLabelFor(mContentText);
        mTagsLabel.setLabelFor(mTagsText);
        
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

        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mTagsLabel, constraints);
        add(mContentLabel, constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mTagsText, constraints);
        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mContentText, constraints);
        
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.gridx = 2;
        constraints.gridy = 1;
        add(mHelp, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.weighty = 1;
        constraints.weightx = 1;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.BOTH;
        JLabel filler = new JLabel();
        add(filler, constraints);

    }

    public String[] getTags() {
        Pattern pat = Pattern.compile("\"[^\"]*\"");
        Matcher mat=pat.matcher(mTagsText.getText());
        List<String> tags=new ArrayList<String>();
        while(mat.find()){
            String tag=mat.group(); 
            tags.add(tag.substring(1,tag.length()-1));
        }
        return tags.toArray(new String[tags.size()]);
    }
    
    public String getContent() {
        return mContentText.getText();
    }
}
