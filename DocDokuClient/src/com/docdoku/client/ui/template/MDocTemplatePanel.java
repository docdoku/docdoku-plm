package com.docdoku.client.ui.template;

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.HelpButton;
import com.docdoku.client.data.MainModel;

import javax.swing.*;
import java.awt.*;
import com.docdoku.client.localization.I18N;

public abstract class MDocTemplatePanel extends JPanel {
    
    private JLabel mAuthorLabel;
    protected JLabel mIDLabel;
    protected JLabel mDocumentTypeLabel;
    protected JLabel mMaskLabel;
    protected JCheckBox mIdGenerated;
    private JLabel mAuthorValueLabel;
    private HelpButton mHelp;
    
    
    public MDocTemplatePanel() {
        mAuthorLabel = new JLabel(I18N.BUNDLE.getString("Author_label"));
        mAuthorValueLabel = new JLabel(MainModel.getInstance().getUser().getName());
        
        mIDLabel = new JLabel(I18N.BUNDLE.getString("IDMandatory_label"));
        mDocumentTypeLabel = new JLabel(I18N.BUNDLE.getString("DocumentType_label"));
        mMaskLabel = new JLabel(I18N.BUNDLE.getString("Mask_label"));
        mHelp=new HelpButton(I18N.BUNDLE.getString("EditMDocTemplatePanel_tiptooltext"));
        mIdGenerated = new JCheckBox(I18N.BUNDLE.getString("IDGenerated_label"));
        createLayout();
    }
    
     public boolean isIdGenerated(){
        return mIdGenerated.isSelected();
    }
    
    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("MDocTemplatePanel_border")));
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
        add(mIDLabel, constraints);
        add(mDocumentTypeLabel, constraints);
        add(mMaskLabel, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mAuthorValueLabel, constraints);
        
        constraints.gridy = 4;
        add(mIdGenerated, constraints);
        
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.gridx = 2;
        constraints.gridy = 3;
        add(mHelp, constraints);
    }
}