package com.docdoku.client.ui.doc;

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.client.data.MainModel;
import com.docdoku.core.entities.MasterDocumentTemplate;
import com.docdoku.core.entities.WorkflowModel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;

import javax.swing.*;
import java.awt.*;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.keys.Version.VersionUnit;
import com.docdoku.core.util.Tools;
import javax.swing.text.MaskFormatter;

public class CreateMDocPanel extends JPanel {
    
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
    
    public CreateMDocPanel() {
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
        
        MasterDocumentTemplate[] templates=MainModel.getInstance().getMDocTemplates();
        comboBoxValues = new Object[templates.length + 1];
        comboBoxValues[0] = I18N.BUNDLE.getString("None_label");
        i = 1;
        for (MasterDocumentTemplate template : templates)
            comboBoxValues[i++] = template;
             
        mTemplateList = new JComboBox(comboBoxValues);
        mTemplateList.setRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                String label;
                if(value instanceof MasterDocumentTemplate){
                    MasterDocumentTemplate template = (MasterDocumentTemplate)value;
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
    
    public String getID() {
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
    
    public MasterDocumentTemplate getMDocTemplate() {
        Object selectedItem = mTemplateList.getSelectedItem();
        if (selectedItem instanceof MasterDocumentTemplate)
            return (MasterDocumentTemplate) selectedItem;
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
            public void itemStateChanged(ItemEvent e) {
                MasterDocumentTemplate template = getMDocTemplate();
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