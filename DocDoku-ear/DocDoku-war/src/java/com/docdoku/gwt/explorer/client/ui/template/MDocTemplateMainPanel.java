package com.docdoku.gwt.explorer.client.ui.template;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 *
 * @author Florent GARIN
 */
public class MDocTemplateMainPanel extends DataRoundedPanel{

    private Label m_authorLabel;
    private TextBox m_idTextBox;
    private TextBox m_typeTextBox;
    private TextBox m_maskTextBox;
    private CheckBox m_generated;
    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public MDocTemplateMainPanel(){
        createLayout();
    }

    public void setCreationMode(boolean creationMode){
        m_idTextBox.setEnabled(creationMode);
        if(creationMode){
            inputPanel.setText(0,0,"");
            m_authorLabel.setText("");
        }else{
            inputPanel.setText(0,0,i18n.fieldLabelAuthor());
        }
    }
    private void createLayout() {

        inputPanel.setText(0,0,i18n.fieldLabelAuthor());
        m_authorLabel = new Label();
        inputPanel.setWidget(0,1,m_authorLabel);

        inputPanel.setText(1,0,i18n.fieldLabelIDMandatory());
        m_idTextBox = new TextBox();
        inputPanel.setWidget(1,1,m_idTextBox);

        inputPanel.setText(2,0,i18n.fieldLabelDocumentType());
        m_typeTextBox = new TextBox();
        inputPanel.setWidget(2,1,m_typeTextBox);

        inputPanel.setText(3,0,i18n.fieldLabelMask());
        m_maskTextBox=new TextBox();
        inputPanel.setWidget(3,1,m_maskTextBox);

        m_generated=new CheckBox(i18n.fieldLabelGeneratedID());
        inputPanel.setWidget(4,1,m_generated);

    }



    public String getMDocTemplateId(){
        return m_idTextBox.getText();
    }

    public String getMask(){
        return m_maskTextBox.getText();
    }

    public String getDocumentType(){
        return m_typeTextBox.getText();
    }

    public boolean isMDocTemplateIdGenerated(){
        return m_generated.getValue();
    }

    public void clearInputs(){
        m_authorLabel.setText("");
        m_idTextBox.setText("");
        m_maskTextBox.setText("");
        m_typeTextBox.setText("");
        m_generated.setValue(false);
    }

    public void setMDocTemplateAuthor(String author){
        m_authorLabel.setText(author);
    }
    public void setMDocTemplateID(String id){
        m_idTextBox.setText(id);
    }
    public void setMDocTemplateMask(String mask){
        m_maskTextBox.setText(mask);
    }
    public void setMDocTemplateType(String type){
        m_typeTextBox.setText(type);
    }
    public void setMDocTemplateGeneratedID(boolean generatedID){
        m_generated.setValue(generatedID);
    }
}
