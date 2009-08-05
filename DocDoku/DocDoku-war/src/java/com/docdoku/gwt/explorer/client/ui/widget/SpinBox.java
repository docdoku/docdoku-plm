package com.docdoku.gwt.explorer.client.ui.widget;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.resources.icons.ExplorerImageBundle;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SpinBox extends Composite implements ClickHandler, ChangeHandler{

        private static final String DEFAULT_STYLE ="docdoku-spinBox" ;
        
	
	private List<SpinBoxListener> observers ;
	
	private int minValue ;
	private int maxValue ;
	private PushButton buttonUp ;
	private PushButton buttonDown ;
	private int value ;
	private TextBox inputField ;
	int backupValue ;
	
	public SpinBox(){
		this(0,100,0);
	}
	
	public SpinBox(int min, int max, int initial){
		this.observers = new ArrayList<SpinBoxListener>();
		maxValue = max ;
		minValue = min ;
		value = initial ;
		backupValue = value ;
		setupUi();
		setupListeners() ;
                inputField.setStyleName(DEFAULT_STYLE);
	}
		
	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
		if (value > maxValue){
			value = maxValue ;
                        onValueChanged() ;
		}else{
                    onValueChangeWithoutNotification();
                }
		
	}

    public void setMinValue(int minValue){
        this.minValue = minValue ;
        if(value < minValue){
            value = minValue ;
            onValueChanged();
        }else{
            onValueChangeWithoutNotification();
        }
        
    }

	public int getValue() {
		return value;
	}

    public void setValue (int newValue){
        value = newValue;
        onValueChanged();
    }

	private void setupUi(){
		HorizontalPanel mainPanel = new HorizontalPanel();
		VerticalPanel buttonsPanel = new VerticalPanel();
		ExplorerImageBundle images = ServiceLocator.getInstance().getExplorerImageBundle() ;
		Image up = new Image();
		Image down = new Image() ;
		images.getSmallUpImage().applyTo(up);
		images.getSmallDownImage().applyTo(down);
		buttonUp = new PushButton(up);
        buttonUp.setStyleName("spinbox-button");
		buttonDown = new PushButton(down);
        buttonDown.setStyleName("spinbox-button");
		buttonsPanel.add(buttonUp);
		buttonsPanel.add(buttonDown);
		inputField = new TextBox() ;
		inputField.setVisibleLength(1) ;
		inputField.setText(""+value);
		inputField.setTextAlignment(TextBox.ALIGN_RIGHT) ;
		mainPanel.add(inputField);
		mainPanel.add(buttonsPanel);
		initWidget(mainPanel);
		onValueChanged() ;
	}

	private void setupListeners(){
		buttonUp.addClickHandler(this);
		buttonDown.addClickHandler(this);
		inputField.addChangeHandler(this);
	}

	@Override
	public void onClick(ClickEvent event) {
		if (event.getSource() == buttonUp) {
			if (value < maxValue){
				value ++ ;
				onValueChanged() ;
			}
			
		} else {
			if (value>minValue) {
				value-- ;
				onValueChanged() ;
			}
		}
	}

        private void onValueChangeWithoutNotification(){
            // value is the new backup now
		backupValue = value ;
		if (value == maxValue && value == minValue){
			buttonUp.setEnabled(false);
			buttonDown.setEnabled(false) ;
		}else if (value == maxValue){
			buttonUp.setEnabled(false);
			buttonDown.setEnabled(true) ;
		}else if(value == minValue){
			buttonUp.setEnabled(true);
			buttonDown.setEnabled(false) ;
		}else{
			buttonUp.setEnabled(true);
			buttonDown.setEnabled(true);
		}
		inputField.setText(""+value);
        }
	
	
	private void onValueChanged(){
		// value is the new backup now
		backupValue = value ;
		if (value == maxValue && value == minValue){
			buttonUp.setEnabled(false);
			buttonDown.setEnabled(false) ;
		}else if (value == maxValue){
			buttonUp.setEnabled(false);
			buttonDown.setEnabled(true) ;
		}else if(value == minValue){
			buttonUp.setEnabled(true);
			buttonDown.setEnabled(false) ;
		}else{
			buttonUp.setEnabled(true);
			buttonDown.setEnabled(true);
		}
		inputField.setText(""+value);
		// send event
		fireChange() ;
	}


	@Override
	public void onChange(ChangeEvent event) {
		if (inputField.getText().matches("^[0-9]+")){
			Integer tempValue = new Integer(inputField.getText()) ;
			if (tempValue > minValue && tempValue < maxValue){
				value = tempValue ;
			}else{
				// restaure backup :
				value = backupValue ;
			}
		}else{
			value = backupValue ;
		}
		onValueChanged() ;
	}
	
	public void addListener(SpinBoxListener l){
		this.observers.add(l);
	}
	
	public void removeListener(SpinBoxListener l){
		this.observers.remove(l);
	}
	
	private void fireChange(){
		SpinBoxEvent ev = new SpinBoxEvent(this, value);
		for (SpinBoxListener l : observers) {
			l.onValueChanged(ev);
		}
		
	}
}
