package fr.senioriales.stocks.gwt.client.ui.widget.input;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Window;
import fr.senioriales.stocks.gwt.client.actions.Action;
import fr.senioriales.stocks.gwt.client.ui.widget.util.NotEmptyChecker;


public class EditableLabel extends DocdokuLineEdit implements MouseOverHandler, MouseOutHandler, BlurHandler, FocusHandler, KeyDownHandler {

    private final static String DEFAULT_STYLE = "editableText";
    private final static String DEFAULT_FOCUS_STYLE = "editableText-selected";
    private final static String DEFAULT_OVER_STYLE = "editableText-over";
    private String normalStyle;
    private String selectedStyle;
    private String overStyle;
    private boolean hasFocus ;

    private Action cmd;


    public EditableLabel() {
        normalStyle = DEFAULT_STYLE;
        selectedStyle = DEFAULT_FOCUS_STYLE;
        overStyle = DEFAULT_OVER_STYLE;
        setStyleName(normalStyle);
        addBlurHandler(this);
        addMouseOutHandler(this);
        addMouseOverHandler(this);
        addFocusHandler(this);
        addKeyDownHandler(this);
        hasFocus =false ;
        setVisibleLength(15);
    }

    public void onFocus(FocusEvent event) {
        addStyleName(selectedStyle);
        this.selectAll();
        removeStyleName(overStyle);
        hasFocus = true ;
    }

    @Override
    public void onChange(ChangeEvent event) {
        if (!checker.check(super.getText())) {
            setText(backup);
        } else {
            if(cmd!=null && Window.confirm("Confirmez-vous les changements ?")){
                cmd.execute(this);
                backup = super.getText();
            }else{
                 setText(backup);
            }
            
        }
    }

    public void onBlur(BlurEvent event) {
        removeStyleName(selectedStyle);
        hasFocus = false ;
        
//        int x = event.getNativeEvent().getClientX();
//        int y = event.getNativeEvent().getClientY() ;
//
//        if (x < getOffsetWidth() +  getAbsoluteLeft() && x > getAbsoluteLeft() && y < getOffsetHeight() + getAbsoluteTop() && y > getAbsoluteTop()){
//            addStyleName(overStyle) ;
//        }
    }

    public void onMouseOver(MouseOverEvent event) {
        if (!hasFocus){
            addStyleName(overStyle);
        }
    }

    public void onMouseOut(MouseOutEvent event) {
        removeStyleName(overStyle);
    }

    public String getNormalStyle() {
        return normalStyle;
    }

    public void setNormalStyle(String normalStyle) {
        this.normalStyle = normalStyle;
        setStyleName(normalStyle);
    }

    public String getSelectedStyle() {
        return selectedStyle;
    }

    public void setSelectedStyle(String selectedStyle) {
        this.selectedStyle = selectedStyle;
    }

    public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
            setText(getBackup());
            setFocus(false);
        } else if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            setFocus(false);
        }
    }

    public String getOverStyle() {
        return overStyle;
    }

    public void setOverStyle(String overStyle) {
        this.overStyle = overStyle;
    }

    public void setAction(Action cmd){
        this.cmd=cmd;
    }

}
