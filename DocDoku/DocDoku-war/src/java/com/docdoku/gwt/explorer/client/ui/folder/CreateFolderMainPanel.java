package com.docdoku.gwt.explorer.client.ui.folder;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 *
 * @author Florent GARIN
 */
public class CreateFolderMainPanel extends DataRoundedPanel{

    private TextBox m_parentTextBox;
    private TextBox m_folderTextBox;

    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public CreateFolderMainPanel(){
        inputPanel.setText(0,0,i18n.fieldLabelParentFolder());
        m_parentTextBox = new TextBox();
        inputPanel.setWidget(0,1,m_parentTextBox);

        inputPanel.setText(1,0,i18n.fieldLabelName());
        m_folderTextBox = new TextBox();
        inputPanel.setWidget(1,1,m_folderTextBox);
    }
    
    public void setParentFolder(String parentFolder){
        m_parentTextBox.setText(parentFolder);
    }
    public String getParentFolderText(){
        return m_parentTextBox.getText();
    }
    public String getFolderText(){
        return m_folderTextBox.getText();
    }
    
    public void clearInputs(){
        m_folderTextBox.setText("");
    }
}
