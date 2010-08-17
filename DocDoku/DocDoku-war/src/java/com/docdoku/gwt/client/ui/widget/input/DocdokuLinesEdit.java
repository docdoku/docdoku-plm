package fr.senioriales.stocks.gwt.client.ui.widget.input;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import fr.senioriales.stocks.gwt.client.ui.widget.util.DocdokuChecker;
import java.util.HashSet;
import java.util.Set;

/**
 * DocdokuLineEdit is a simple TextBox providing checking features on input
 * A DocdokuLineEdit fires DocdokuLineEditEvent whenever its input state change.
 */
public class DocdokuLinesEdit extends TextArea implements ChangeHandler, KeyPressHandler {

    protected DocdokuChecker checker;
    protected String backup;
    protected Set<DocdokuLinesEditListener> observers;
    protected boolean hasAcceptableInput;

    public DocdokuLinesEdit() {
        backup = "";
        checker = new DocdokuChecker() {

            public boolean check(String expressionToCheck) {
                return true;
            }
        };
        addChangeHandler(this);
//        addKeyUpHandler(this);
        addKeyPressHandler(this);
        observers = new HashSet<DocdokuLinesEditListener>();
        hasAcceptableInput = true;
    }

    public void onChange(ChangeEvent event) {
        if (!checker.check(super.getText())) {
            setText(backup);
        } else {
            backup = super.getText();
        }
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        backup = text;
        hasAcceptableInput = containsAcceptableInput();
    }

    @Override
    public String getText() {
        if (checker.check(super.getText())) {
            return super.getText();
        } else {
            return backup;
        }
    }

    protected String getBackup() {
        return backup;
    }

    public void setChecker(DocdokuChecker checker) {
        this.checker = checker;
    }

    public boolean containsAcceptableInput() {
        return checker.check(super.getText());
    }

    public void addListener(DocdokuLinesEditListener listener) {
        observers.add(listener);
    }

    public void removeListener(DocdokuLinesEditListener listener) {
        observers.remove(listener);
    }

    private void fireInputStateChange() {
        DocdokuLinesEditEvent event = new DocdokuLinesEditEvent(this);
        for (DocdokuLinesEditListener observer : observers) {
            observer.onInputStateChange(event);
        }
    }

    public void onKeyPress(KeyPressEvent event) {
        if (checker.check(super.getText()) != hasAcceptableInput) {
            hasAcceptableInput = containsAcceptableInput();
            fireInputStateChange();
        }
    }
}
