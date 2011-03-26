package com.docdoku.gwt.explorer.client.ui.doc;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.docdoku.gwt.explorer.shared.WorkflowModelDTO;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 *
 * @author Florent GARIN
 */
public class CreateVersionMainPanel extends DataRoundedPanel{


    private TextBox m_titleTextBox;
    private ListBox m_workflowListBox;

    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public CreateVersionMainPanel(){
        createLayout();
    }

    private void createLayout() {
        inputPanel.setText(0,0,i18n.fieldLabelTitle());
        m_titleTextBox = new TextBox();
        inputPanel.setWidget(0,1,m_titleTextBox);

        inputPanel.setText(1,0,i18n.fieldLabelWorkflow());
        m_workflowListBox = new ListBox();
        m_workflowListBox.setVisibleItemCount(1);
        inputPanel.setWidget(1,1,m_workflowListBox);

    }

    
    public void setWorkflowModels(WorkflowModelDTO[] wks){
        m_workflowListBox.clear();
        m_workflowListBox.addItem(ServiceLocator.getInstance().getExplorerI18NConstants().selectionNone());
        for(WorkflowModelDTO wk:wks)
            m_workflowListBox.addItem(wk.getId());
    }


    public String getWorkflowModelId(){
        int index=m_workflowListBox.getSelectedIndex();
        return index<1?null:m_workflowListBox.getValue(index);
    }

    public String getMDocTitle(){
        return m_titleTextBox.getText();
    }

    public void clearInputs(){
        m_titleTextBox.setText("");
    }
}
