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

import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.core.common.Version.VersionUnit;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.util.Tools;
import com.docdoku.core.workflow.WorkflowModel;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;

public class CreateDocMPanel extends JPanel {
    
    private JLabel mTemplateLabel;
    private JLabel mAuthorLabel;
    private JLabel mIDLabel;
    private JLabel mTitleLabel;
    private JLabel mVersionLabel;
    private JLabel mWorkflowModelLabel;
    
    private JLabel mAuthorValueLabel;
    private JFormattedTextField mIDText;
    private MaskFormatter mMaskFormatter;
    private JTextField mTitleText;
    private JLabel mVersionValueLabel;
    private JComboBox mWorkflowModelList;
    private JComboBox mTemplateList;
    
    public CreateDocMPanel() {
        mAuthorLabel = new JLabel(I18N.BUNDLE.getString("Author_label"));
        mTemplateLabel = new JLabel(I18N.BUNDLE.getString("Template_label"));
        mAuthorValueLabel = new JLabel(MainModel.getInstance().getUser().getName());
        mIDLabel = new JLabel(I18N.BUNDLE.getString("IDMandatory_label"));
        mTitleLabel = new JLabel(I18N.BUNDLE.getString("Title_label"));
        mVersionLabel = new JLabel(I18N.BUNDLE.getString("Version_label"));
        mVersionValueLabel = new JLabel(VersionUnit.A + "");
        mWorkflowModelLabel = new JLabel(I18N.BUNDLE.getString("Workflow_label"));
        
        WorkflowModel[] models=MainModel.getInstance().getWorkflowModels();
        Object[] comboBoxValues = new Object[models.length + 1];
        comboBoxValues[0] = I18N.BUNDLE.getString("None_label");
        int i = 1;
        for (WorkflowModel model : models)
            comboBoxValues[i++] = model;
        
        mWorkflowModelList = new JComboBox(comboBoxValues);
        
        DocumentMasterTemplate[] templates=MainModel.getInstance().getDocMTemplates();
        comboBoxValues = new Object[templates.length + 1];
        comboBoxValues[0] = I18N.BUNDLE.getString("None_label");
        i = 1;
        for (DocumentMasterTemplate template : templates)
            comboBoxValues[i++] = template;
             
        mTemplateList = new JComboBox(comboBoxValues);
        mTemplateList.setRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                String label;
                if(value instanceof DocumentMasterTemplate){
                    DocumentMasterTemplate template = (DocumentMasterTemplate)value;
                    String type = template.getDocumentType();                   
                    label = template.getId();
                    if(type!=null && type.length()>0)
                        label += " (" + type + ")";
                }else
                    label = value + "";
                setText(label);
                return this;
            }
        });
        mMaskFormatter = new MaskFormatter();
        mMaskFormatter.setValueContainsLiteralCharacters(true);
        mMaskFormatter.setPlaceholderCharacter('_');
        
        mIDText = new JFormattedTextField();
        mIDText.setDocument(new MaxLengthDocument(50));
        mIDText.setText("");
        mIDText.setColumns(10);
        
        mTitleText = new JTextField(new MaxLengthDocument(50), "", 10);
        createLayout();
        createListener();
    }
    
    public String getId() {
        return mIDText.getText();
    }
    
    public String getTitle() {
        return mTitleText.getText();
    }
    
    
    public WorkflowModel getWorkflowModel() {
        Object selectedItem = mWorkflowModelList.getSelectedItem();
        if (selectedItem instanceof WorkflowModel)
            return (WorkflowModel) selectedItem;
        else
            return null;
    }
    
    public DocumentMasterTemplate getDocMTemplate() {
        Object selectedItem = mTemplateList.getSelectedItem();
        if (selectedItem instanceof DocumentMasterTemplate)
            return (DocumentMasterTemplate) selectedItem;
        else
            return null;
    }
    
    public JTextField getIDText() {
        return mIDText;
    }
    
    public JComboBox getTemplateList(){
        return mTemplateList;
    }
    
    private void createListener(){
        mTemplateList.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                DocumentMasterTemplate template = getDocMTemplate();
                if(template!=null &&  !template.getMask().equals("")){
                    String inputMask = template.getMask();
                    String convertedMask = Tools.convertMask(inputMask);
                    try {
                        mMaskFormatter.setMask(convertedMask);
                        mMaskFormatter.install(mIDText);
                        if(template.isIdGenerated()){
                            String generatedId = MainModel.getInstance().getGeneratedId(MainModel.getInstance().getWorkspace().getId(), template.getId());
                            mIDText.setText(generatedId);
                        }
                    } catch (ParseException pEx) {
                        System.err.println(pEx.getMessage());
                    }
                }else{
                    mMaskFormatter.uninstall();
                    mIDText.setText("");
                }
                    
            }
        });
    }
    
    private void createLayout() {
        mIDLabel.setLabelFor(mIDText);
        mTitleLabel.setLabelFor(mTitleText);
        mWorkflowModelLabel.setLabelFor(mWorkflowModelList);
        mTemplateLabel.setLabelFor(mTemplateList);
        
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 0;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        add(mAuthorLabel, constraints);
        
        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mVersionLabel, constraints);
        add(mTemplateLabel, constraints);
        add(mIDLabel, constraints);
        add(mTitleLabel, constraints);
        add(mWorkflowModelLabel, constraints);
      
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mAuthorValueLabel, constraints);
        
        constraints.gridy = GridBagConstraints.RELATIVE;    
        add(mVersionValueLabel, constraints);
        add(mTemplateList, constraints);
        add(mIDText, constraints);
        add(mTitleText, constraints);
        add(mWorkflowModelList, constraints);
    }
}