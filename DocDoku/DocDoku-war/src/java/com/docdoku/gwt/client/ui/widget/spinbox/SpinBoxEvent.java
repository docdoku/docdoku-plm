package com.docdoku.gwt.client.ui.widget.spinbox;

import java.util.EventObject;

@SuppressWarnings("serial")
public class SpinBoxEvent extends EventObject {

	private int newValue;

	public SpinBoxEvent(Object source, int value) {
		super(source);
		this.newValue = value ;
		
	}

	public int getNewValue() {
		return newValue;
	}

}
