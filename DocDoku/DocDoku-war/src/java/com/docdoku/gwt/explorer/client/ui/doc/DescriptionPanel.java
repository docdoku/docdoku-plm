package com.docdoku.gwt.explorer.client.ui.doc;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 *
 * @author Florent GARIN
 */
public class DescriptionPanel extends DataRoundedPanel{

    private TextArea m_descriptionTextArea;


    public DescriptionPanel(){
        super(ServiceLocator.getInstance().getExplorerI18NConstants().tabDescription());
        createLayout();
    }

    private void createLayout() {
        m_descriptionTextArea = new TextArea();
        m_descriptionTextArea.setVisibleLines(8);
        m_descriptionTextArea.setCharacterWidth(25);
        
        inputPanel.setWidget(0,0,m_descriptionTextArea);
    }

    public void clearInputs(){
        m_descriptionTextArea.setText("");
    }

    public String getMDocDescription(){
        return m_descriptionTextArea.getText();
    }
}
