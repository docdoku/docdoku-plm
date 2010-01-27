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

package com.docdoku.client.ui.search;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Date;
import java.util.Calendar;

import javax.swing.*;

import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.VersionFormat;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.core.entities.MasterDocumentTemplate;
import com.docdoku.core.entities.User;
import com.docdoku.core.entities.keys.Version;
import java.awt.Component;
import java.util.LinkedHashSet;
import java.util.Set;

public class SearchPanel extends JPanel {

    private JLabel mTitleLabel;
    private JTextField mTitleText;

    private JLabel mTypeLabel;
    private JComboBox mTypeComboBox;
    
    private JLabel mIDLabel;
    private JTextField mIDText;

    private JLabel mVersionLabel;
    private JFormattedTextField mVersionText;

    private JLabel mAuthorLabel;
    private JComboBox mAuthorComboBox;
    
    private JLabel mCreationDateLabel;
    private JSpinner mSpinnerCreationDateFrom;
    private JSpinner mSpinnerCreationDateTo;

    private final static Date FROM_DATE;

    static {
        Calendar cal = Calendar.getInstance();
        cal.set(2000, 0, 1, 0, 0);
        FROM_DATE = cal.getTime();
    }

    public SearchPanel() {
        MainModel model = MainModel.getInstance();
        mTitleLabel = new JLabel(I18N.BUNDLE.getString("Title_label"));
        mTitleText = new JTextField(new MaxLengthDocument(50), "", 10);

        mTypeLabel = new JLabel(I18N.BUNDLE.getString("Type_label"));
        MasterDocumentTemplate[] templates = model.getMDocTemplates();
        Set<String> comboBoxTypeValues = new LinkedHashSet<String>();
        comboBoxTypeValues.add(I18N.BUNDLE.getString("Not_specified"));
        for (MasterDocumentTemplate template : templates){
            String type=template.getDocumentType();
            if(type!=null && !type.equals(""))
                comboBoxTypeValues.add(type);
        }
        mTypeComboBox = new JComboBox(comboBoxTypeValues.toArray(new String[comboBoxTypeValues.size()]));
        mTypeComboBox.setEditable(true);
        
        mIDLabel = new JLabel(I18N.BUNDLE.getString("ID_label"));
        mIDText = new JTextField(new MaxLengthDocument(50), "", 10);

        mVersionLabel = new JLabel(I18N.BUNDLE.getString("Version_label"));
        mVersionText = new JFormattedTextField(new VersionFormat());

        mAuthorLabel = new JLabel(I18N.BUNDLE.getString("Author_label"));
        User[] users = model.getUsers();
        Object[] comboBoxAuthorValues = new Object[users.length + 1];
        comboBoxAuthorValues[0] = I18N.BUNDLE.getString("Not_specified");
        int i = 1;
        for (User user : users)
            comboBoxAuthorValues[i++] = user;

        mAuthorComboBox = new JComboBox(comboBoxAuthorValues);
        mAuthorComboBox.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                String label;
                if(value instanceof User){
                    User author = (User)value;
                    label = author.getName();
                }else
                    label = value + "";
                setText(label);
                return this;
            }
        });
        
        mCreationDateLabel = new JLabel(I18N.BUNDLE.getString("CreationDate_label"));
        mSpinnerCreationDateFrom = new JSpinner(new SpinnerDateModel(FROM_DATE, null, null, Calendar.DAY_OF_MONTH));
        mSpinnerCreationDateTo = new JSpinner(new SpinnerDateModel());

        createLayout();
    }

    private void createLayout() {
        mIDLabel.setLabelFor(mIDText);
        mTitleLabel.setLabelFor(mTitleText);
        mTypeLabel.setLabelFor(mTypeComboBox);
        mVersionLabel.setLabelFor(mVersionText);
        mAuthorLabel.setLabelFor(mAuthorComboBox);
        
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
        add(mIDLabel, constraints);
        add(mTitleLabel, constraints);
        add(mTypeLabel, constraints);       
        add(mVersionLabel, constraints);
        add(mAuthorLabel, constraints);
        add(mCreationDateLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.gridwidth = 4;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mIDText, constraints);
        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mTitleText, constraints);
        add(mTypeComboBox, constraints);        
        add(mVersionText, constraints);
        add(mAuthorComboBox, constraints);
        
        constraints.gridy = 5;
        constraints.weightx = 0;
        constraints.gridwidth = 1;
        add(new JLabel(I18N.BUNDLE.getString("FromDate_label")), constraints);
        constraints.weightx = 1;
        constraints.gridx = 2;
        add(mSpinnerCreationDateFrom, constraints);
        constraints.weightx = 0;
        constraints.gridx = 3;
        add(new JLabel(I18N.BUNDLE.getString("ToDate_label")), constraints);
        constraints.weightx = 1;
        constraints.gridx = 4;
        add(mSpinnerCreationDateTo, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.weighty = 1;
        constraints.weightx = 1;
        constraints.gridwidth = 5;
        constraints.fill = GridBagConstraints.BOTH;
        JLabel filler = new JLabel();
        add(filler, constraints);
    }

    public String getId() {
        return mIDText.getText();
    }

    public String getTitle() {
        return mTitleText.getText();
    }
    
    public Version getVersion() {
        return (Version) mVersionText.getValue();
    }

    public String getType() {
        if(mTypeComboBox.getSelectedIndex()==0)
            return null;
        else{
            Object selectedItem = mTypeComboBox.getSelectedItem();
            if (selectedItem instanceof String)
                return (String) selectedItem;
            else
                return null;
        }
    }
    
    public User getAuthor() {
        Object selectedItem = mAuthorComboBox.getSelectedItem();
        if (selectedItem instanceof User)
            return (User) selectedItem;
        else
            return null;
    }

    public Date getCreationDateFrom() {
        return (Date) mSpinnerCreationDateFrom.getValue();
    }

    public Date getCreationDateTo() {
        return (Date) mSpinnerCreationDateTo.getValue();
    }
}
