package com.docdoku.gwt.explorer.client.ui.search;

import com.docdoku.gwt.explorer.shared.SearchQueryDTO;
import com.google.gwt.user.client.ui.TextBox;



public class TextAttributePanel extends AbstractAttributePanel {
	
	private TextBox valueField ;
	
	public TextAttributePanel() {
		super();
		valueField = new TextBox();
		add(valueField) ;
	}


    @Override
    public SearchQueryDTO.AbstractAttributeQueryDTO getAttribute() {
        if (getNameValue().isEmpty()){
            return null;
        }
        SearchQueryDTO.TextAttributeQueryDTO result = new SearchQueryDTO.TextAttributeQueryDTO(getNameValue(),valueField.getText());
        return result;
    }
	
}
