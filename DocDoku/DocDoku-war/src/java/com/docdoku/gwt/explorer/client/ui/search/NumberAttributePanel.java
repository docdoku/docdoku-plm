package com.docdoku.gwt.explorer.client.ui.search;

import com.docdoku.gwt.explorer.common.InstanceAttributeDTO;
import com.docdoku.gwt.explorer.common.InstanceNumberAttributeDTO;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.TextBox;

public class NumberAttributePanel extends AbstractAttributePanel implements ChangeHandler {
	
	private TextBox valueField ;
	private String backup ;
	
	public NumberAttributePanel() {
		valueField = new TextBox();
		add(valueField) ;
		backup = "";
		valueField.addChangeHandler(this);
		valueField.setTextAlignment(TextBox.ALIGN_RIGHT);
		valueField.setVisibleLength(4);
	}

	@Override
	public void onChange(ChangeEvent event) {
		
		if (!valueField.getText().isEmpty()){
			if (valueField.getText().matches("^[0-9]+(\\.|,)?[0-9]*")) {
				backup = valueField.getText() ;
			}else{
				valueField.setText(backup) ;
			}
		}
		
	}

    @Override
    public InstanceAttributeDTO getAttribute() {
        if (getNameValue().isEmpty()){
            return null ;
        }
        InstanceNumberAttributeDTO result = new InstanceNumberAttributeDTO();
        Float f = new Float(valueField.getText());
        result.setNumberValue(f);
        result.setName(getNameValue());
        return result ;
    }

}
