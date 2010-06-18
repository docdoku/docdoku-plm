package fr.senioriales.stocks.gwt.client.ui.widget.input;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.ListBox;
import fr.senioriales.stocks.gwt.client.actions.Action;
import fr.senioriales.stocks.gwt.client.ui.widget.util.NotEmptyChecker;

public class EditableList extends ListBox implements MouseOverHandler, MouseOutHandler, BlurHandler, FocusHandler {

    private final static String DEFAULT_STYLE = "editableList";
    private final static String DEFAULT_FOCUS_STYLE = "editableList-selected";
    private final static String DEFAULT_OVER_STYLE = "editableList-over";
    private String normalStyle;
    private String selectedStyle;
    private String overStyle;
    private boolean hasFocus;
    private Action cmd;

    public EditableList() {
        normalStyle = DEFAULT_STYLE;
        selectedStyle = DEFAULT_FOCUS_STYLE;
        overStyle = DEFAULT_OVER_STYLE;
        setStyleName(normalStyle);
        addBlurHandler(this);
        addMouseOutHandler(this);
        addMouseOverHandler(this);
        addFocusHandler(this);
        hasFocus = false;
    }

    public void onFocus(FocusEvent event) {
        addStyleName(selectedStyle);
        removeStyleName(overStyle);
        hasFocus = true;
    }

    public void onBlur(BlurEvent event) {
        removeStyleName(selectedStyle);
        hasFocus = false;
    }

    public void onMouseOver(MouseOverEvent event) {
        if (!hasFocus) {
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

    public String getOverStyle() {
        return overStyle;
    }

    public void setOverStyle(String overStyle) {
        this.overStyle = overStyle;
    }

    public void setAction(Action cmd) {
        this.cmd = cmd;
    }

}
