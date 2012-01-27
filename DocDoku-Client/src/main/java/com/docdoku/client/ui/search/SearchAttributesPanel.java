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

package com.docdoku.client.ui.search;

import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import javax.swing.*;

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.core.document.InstanceAttributeTemplate;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.document.SearchQuery;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JCheckBox;
import org.jdesktop.swingx.JXDatePicker;

public class SearchAttributesPanel extends JPanel {

    private JPanel mCenterPanel;
    private JButton mAddButton;
    
    private JComboBox mTemplateList;
    private JLabel mTemplateLabel;
    
    private final static GridBagConstraints ATTRIBUTE_PANEL_CONSTRAINTS=new GridBagConstraints();
    static{
        ATTRIBUTE_PANEL_CONSTRAINTS.weighty = 0;
        ATTRIBUTE_PANEL_CONSTRAINTS.weightx = 1;
        ATTRIBUTE_PANEL_CONSTRAINTS.fill = GridBagConstraints.HORIZONTAL;
        ATTRIBUTE_PANEL_CONSTRAINTS.gridheight = 1;
        ATTRIBUTE_PANEL_CONSTRAINTS.gridwidth = 6;
        ATTRIBUTE_PANEL_CONSTRAINTS.gridx = 0;
        ATTRIBUTE_PANEL_CONSTRAINTS.gridy = GridBagConstraints.RELATIVE;
    }
    
    private final static GridBagConstraints ADD_BUTTON_CONSTRAINTS=(GridBagConstraints) ATTRIBUTE_PANEL_CONSTRAINTS.clone();
    static{
        ADD_BUTTON_CONSTRAINTS.weighty = 1;
        ADD_BUTTON_CONSTRAINTS.weightx = 0;
        ADD_BUTTON_CONSTRAINTS.gridwidth = 1;
        ADD_BUTTON_CONSTRAINTS.fill = GridBagConstraints.NONE;
        ADD_BUTTON_CONSTRAINTS.insets = GUIConstants.INSETS;
        ADD_BUTTON_CONSTRAINTS.anchor = GridBagConstraints.NORTH;
    }
    private List<AttributePanel> mAttributePanels = new LinkedList<AttributePanel>();
    
    public SearchAttributesPanel() {
        mCenterPanel =new JPanel();
        
        Image img =
                Toolkit.getDefaultToolkit().getImage(SearchAttributesPanel.class.getResource("/com/docdoku/client/resources/icons/add.png"));
        ImageIcon addIcon = new ImageIcon(img);
        mAddButton = new JButton(addIcon);
        mAddButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {             
                AttributePanel attrPanel = new AttributePanel();
                mCenterPanel.add(attrPanel, ATTRIBUTE_PANEL_CONSTRAINTS, 0);
                if(mAttributePanels.size()==1)
                        mAttributePanels.get(0).mRemoveButton.setEnabled(true);
                mAttributePanels.add(attrPanel);        
            }
        });
        
        mTemplateLabel = new JLabel(I18N.BUNDLE.getString("Template_label"));
        DocumentMasterTemplate[] templates=MainModel.getInstance().getDocMTemplates();
        Object[] comboBoxTemplateValues = new Object[templates.length + 1];
        comboBoxTemplateValues[0] = I18N.BUNDLE.getString("Not_specified");
        int i = 1;
        for (DocumentMasterTemplate template : templates)
            comboBoxTemplateValues[i++] = template;
        
        mTemplateList = new JComboBox(comboBoxTemplateValues);

        mTemplateList.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(mTemplateList.getSelectedItem() instanceof DocumentMasterTemplate){
                    DocumentMasterTemplate template = (DocumentMasterTemplate) mTemplateList.getSelectedItem();

                    mAttributePanels.clear();
                    mCenterPanel.removeAll();
                    for(InstanceAttributeTemplate attrTemplate:template.getAttributeTemplates()){
                        AttributePanel attrPanel = new AttributePanel(attrTemplate);
                        mCenterPanel.add(attrPanel, ATTRIBUTE_PANEL_CONSTRAINTS, 0);
                        mAttributePanels.add(attrPanel);
                    }
                    if(mAttributePanels.size()==0){
                        AttributePanel attrPanel = new AttributePanel();
                        mCenterPanel.add(attrPanel, ATTRIBUTE_PANEL_CONSTRAINTS);
                        mAttributePanels.add(attrPanel);
                    }
                    if(mAttributePanels.size()==1)
                            mAttributePanels.get(0).mRemoveButton.setEnabled(false);

                    mCenterPanel.add(mAddButton, ADD_BUTTON_CONSTRAINTS);
                    mCenterPanel.revalidate();
                    mCenterPanel.repaint();
                }else{
                    mAttributePanels.clear();
                    mCenterPanel.removeAll();
                    initAttributesPanel();
                }
            }
        });
        createLayout();
    }

    public SearchQuery.AbstractAttributeQuery[] getInstanceAttributes(){
        List<SearchQuery.AbstractAttributeQuery> attrList = new ArrayList<SearchQuery.AbstractAttributeQuery>();
        for(AttributePanel panel:mAttributePanels){
            SearchQuery.AbstractAttributeQuery attr=panel.getInstanceAttribute();
            if(attr!=null)
                attrList.add(attr);
             
        }
        return attrList.toArray(new SearchQuery.AbstractAttributeQuery[attrList.size()]);
    }
    
    private void createLayout() {
        setLayout(new BorderLayout());
        mCenterPanel.setLayout(new GridBagLayout());
        initAttributesPanel();
        
        JScrollPane attributesScrollPane = new JScrollPane(mCenterPanel);      
        add(attributesScrollPane,BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel();
        mTemplateLabel.setLabelFor(mTemplateList);
        
        southPanel.add(mTemplateLabel);
        southPanel.add(mTemplateList);
        add(southPanel,BorderLayout.SOUTH);       
    }

    private void initAttributesPanel(){
        AttributePanel attrPanel = new AttributePanel();
        attrPanel.mRemoveButton.setEnabled(false);
        mCenterPanel.add(attrPanel, ATTRIBUTE_PANEL_CONSTRAINTS);
        mAttributePanels.add(attrPanel);
        mCenterPanel.add(mAddButton, ADD_BUTTON_CONSTRAINTS);
    }
    private class AttributePanel extends JPanel {

        private JComboBox mTypeList;
        private JLabel mNameLabel;
        private JTextField mNameText;
        private JLabel mValueLabel;
        private JComponent mValueComponent;
        private JButton mRemoveButton;

        public AttributePanel(InstanceAttributeTemplate pTemplate) {
            init();
            mNameText.setText(pTemplate.getName());
            mTypeList.setSelectedItem(pTemplate.getAttributeType());
            mValueComponent = createAttributeComponent(pTemplate.getAttributeType());
            createLayout();
            registerListeners();
            
        }
        
        public AttributePanel() {
            init();
            InstanceAttributeTemplate.AttributeType type = (InstanceAttributeTemplate.AttributeType) mTypeList.getSelectedItem();
            mValueComponent = createAttributeComponent(type);
            createLayout();
            registerListeners();
        }

        private void init(){
            mNameLabel = new JLabel(I18N.BUNDLE.getString("Name_label"));
            mValueLabel = new JLabel(I18N.BUNDLE.getString("Value_label"));

            mNameText = new JTextField(new MaxLengthDocument(50), "", 10);
            mTypeList =
                    new JComboBox(InstanceAttributeTemplate.AttributeType.values());
            
            

            Image img = Toolkit.getDefaultToolkit().getImage(AttributePanel.class.getResource("/com/docdoku/client/resources/icons/remove.png"));
            ImageIcon removeIcon = new ImageIcon(img);

            mRemoveButton = new JButton(removeIcon);
            mRemoveButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    SearchAttributesPanel.this.mCenterPanel.remove(AttributePanel.this);
                    mAttributePanels.remove(AttributePanel.this);
                    if(mAttributePanels.size()==1)
                        mAttributePanels.get(0).mRemoveButton.setEnabled(false);
                    
                    SearchAttributesPanel.this.mCenterPanel.revalidate();
                    SearchAttributesPanel.this.mCenterPanel.repaint();
                }
            });
        }
        
        private void registerListeners(){
            mTypeList.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    InstanceAttributeTemplate.AttributeType type = (InstanceAttributeTemplate.AttributeType) mTypeList.getSelectedItem();
                    mValueComponent = createAttributeComponent(type);
                    refresh();
                }
            });
        }
        
        public SearchQuery.AbstractAttributeQuery getInstanceAttribute(){
            String name = mNameText.getText();
            if(name==null || name.equals(""))
                return null;
            
            InstanceAttributeTemplate.AttributeType attributeType = (InstanceAttributeTemplate.AttributeType)mTypeList.getSelectedItem();
            SearchQuery.AbstractAttributeQuery attr = null;
            
            switch (attributeType) {
            case TEXT:
                JTextField componentText = (JTextField) mValueComponent;
                attr = new SearchQuery.TextAttributeQuery(name,componentText.getText()+"");
                break;
            case NUMBER:
                JFormattedTextField componentNumber = (JFormattedTextField) mValueComponent;
                
                float floatValue = 0;
                try {
                    floatValue = NumberFormat.getInstance().parse(componentNumber.getText()).floatValue();
                } catch (ParseException pEx) {
                    System.err.println(pEx.getMessage());
                }
                attr = new SearchQuery.NumberAttributeQuery(name,floatValue);
                break;
            case BOOLEAN:
                JCheckBox componentBoolean = (JCheckBox) mValueComponent;
                attr = new SearchQuery.BooleanAttributeQuery(name,componentBoolean.isSelected());
                break;
            case DATE:
                DateAttributePanel componentDate = (DateAttributePanel) mValueComponent;
                Object fromValue = componentDate.getDateFrom().getDate();
                Date dateFromValue = null;
                if(fromValue instanceof Date){
                    dateFromValue=(Date)fromValue;
                }
                Object toValue = componentDate.getDateTo().getDate();
                Date dateToValue = null;
                if(toValue instanceof Date){
                    dateToValue=(Date)toValue;
                }
                attr = new SearchQuery.DateAttributeQuery(name,dateFromValue,dateToValue);


                break;
            }
            return attr;
        }
        
        public void refresh() {
            removeAll();
            createLayout();
            revalidate();
        }

        private void createLayout() {
            setLayout(new GridBagLayout());
            mNameLabel.setLabelFor(mNameText);
            mValueLabel.setLabelFor(mValueComponent);
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.weighty = 0;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = GUIConstants.INSETS;
            constraints.gridheight = 1;
            constraints.gridwidth = 1;
            constraints.gridy = 0;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;

            constraints.gridx = GridBagConstraints.RELATIVE;
            add(mRemoveButton, constraints);
            add(mTypeList, constraints);
            add(mNameLabel, constraints);
            add(mNameText, constraints);
            add(mValueLabel, constraints);
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            add(mValueComponent, constraints);

        }

        private JComponent createAttributeComponent(InstanceAttributeTemplate.AttributeType pType) {
            switch (pType) {
                case NUMBER:
                    return new JFormattedTextField(NumberFormat.getInstance());
                case BOOLEAN:
                    return new JCheckBox();
                case DATE:
                    return new DateAttributePanel();
                case TEXT:
                default:
                    return new JTextField(new MaxLengthDocument(50), "", 10);
            }
        }
    }
    private static class DateAttributePanel extends JPanel{
        private JXDatePicker mDateFrom;
        private JXDatePicker mDateTo;

        private final static Date FROM_DATE;

        static {
            Calendar cal = Calendar.getInstance();
            cal.set(2000, 0, 1, 0, 0);
            FROM_DATE = cal.getTime();
        }

        public DateAttributePanel(){
            setLayout(new FlowLayout(FlowLayout.LEFT));
            mDateFrom = new JXDatePicker(FROM_DATE);
            mDateTo = new JXDatePicker(new Date());

            add(new JLabel(I18N.BUNDLE.getString("FromDate_label")));
            add(mDateFrom);
            add(new JLabel(I18N.BUNDLE.getString("ToDate_label")));
            add(mDateTo);
        }

        public JXDatePicker getDateFrom() {
            return mDateFrom;
        }

        public JXDatePicker getDateTo() {
            return mDateTo;
        }

        
    }
}
