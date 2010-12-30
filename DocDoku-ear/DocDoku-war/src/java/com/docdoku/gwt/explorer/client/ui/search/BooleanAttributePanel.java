package com.docdoku.gwt.explorer.client.ui.search;

import com.docdoku.gwt.explorer.shared.SearchQueryDTO;
import com.google.gwt.user.client.ui.CheckBox;

public class BooleanAttributePanel extends AbstractAttributePanel {

	private CheckBox valueField ;
	
	public BooleanAttributePanel() {
		valueField = new CheckBox();
		add(valueField);
	}

    @Override
    public SearchQueryDTO.AbstractAttributeQueryDTO getAttribute() {
        if (getNameValue().isEmpty()){
            return null;
        }
        SearchQueryDTO.BooleanAttributeQueryDTO result = new SearchQueryDTO.BooleanAttributeQueryDTO(getNameValue(),valueField.getValue());
        return result;
    }
}
