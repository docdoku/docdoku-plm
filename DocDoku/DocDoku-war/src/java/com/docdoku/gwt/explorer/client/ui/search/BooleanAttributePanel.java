package com.docdoku.gwt.explorer.client.ui.search;

import com.docdoku.gwt.explorer.common.InstanceAttributeDTO;
import com.docdoku.gwt.explorer.common.InstanceBooleanAttributeDTO;
import com.google.gwt.user.client.ui.CheckBox;

public class BooleanAttributePanel extends AbstractAttributePanel {

	private CheckBox valueField ;
	
	public BooleanAttributePanel() {
		valueField = new CheckBox();
		add(valueField);
	}

    @Override
    public InstanceAttributeDTO getAttribute() {
        if (getNameValue().isEmpty()){
            return null ;
        }
        InstanceBooleanAttributeDTO result = new InstanceBooleanAttributeDTO();
        result.setBooleanValue(valueField.getValue());
        result.setName(getNameValue());
        
        return result ;
    }



}
