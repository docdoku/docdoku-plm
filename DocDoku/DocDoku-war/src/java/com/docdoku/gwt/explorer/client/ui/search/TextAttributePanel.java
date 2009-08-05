package com.docdoku.gwt.explorer.client.ui.search;

import com.docdoku.gwt.explorer.common.InstanceAttributeDTO;
import com.docdoku.gwt.explorer.common.InstanceTextAttributeDTO;
import com.google.gwt.user.client.ui.TextBox;



public class TextAttributePanel extends AbstractAttributePanel {
	
	private TextBox valueField ;
	
	public TextAttributePanel() {
		super();
		valueField = new TextBox();
		add(valueField) ;
	}


    @Override
    public InstanceAttributeDTO getAttribute() {
        if (getNameValue().isEmpty()){
            return null ;
        }
        InstanceTextAttributeDTO result = new InstanceTextAttributeDTO();
        result.setTextValue(valueField.getText());
        result.setName(getNameValue());
        return result ;
    }
	
}
