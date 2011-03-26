package com.docdoku.gwt.explorer.client.ui.workflow.editor;

import com.docdoku.gwt.explorer.client.ui.folder.*;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 *
 * @author Florent GARIN
 */
public class WorkflowModelMainPanel extends DataRoundedPanel{

    private TextBox m_idTextBox;

    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public WorkflowModelMainPanel(){
        inputPanel.setText(0,0,i18n.fieldLabelName());
        m_idTextBox = new TextBox();
        inputPanel.setWidget(0,1,m_idTextBox);
    }
    
    public void setWorkflowModelID(String name){
        m_idTextBox.setText(name);
    }
   
    public String getWorkflowModelID(){
        return m_idTextBox.getText();
    }
    public TextBox getWorkflowModelIDTextBox(){
        return m_idTextBox;
    }
    public void clearInputs(){
        m_idTextBox.setText("");
    }
}
