package fr.senioriales.stocks.gwt.client.ui.widget.input;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.TextBox;
import fr.senioriales.stocks.gwt.client.ui.widget.util.DocdokuChecker;
import java.util.HashSet;
import java.util.Set;

/**
 * DocdokuLineEdit is a simple TextBox providing checking features on input
 * A DocdokuLineEdit fires DocdokuLineEditEvent whenever its input state change.
 */
public class DocdokuLineEdit extends TextBox implements ChangeHandler, KeyPressHandler {

    protected DocdokuChecker checker;
    protected String backup;
    protected Set<DocdokuLineEditListener> observers;
    protected boolean hasAcceptableInput;

    public DocdokuLineEdit() {
        backup = "";
        checker = new DocdokuChecker() {

            public boolean check(String expressionToCheck) {
                return true;
            }
        };
        addChangeHandler(this);
//        addKeyUpHandler(this);
        addKeyPressHandler(this);
        observers = new HashSet<DocdokuLineEditListener>();
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

    public void addListener(DocdokuLineEditListener listener) {
        observers.add(listener);
    }

    public void removeListener(DocdokuLineEditListener listener) {
        observers.remove(listener);
    }

    private void fireInputStateChange() {
        DocdokuLineEditEvent event = new DocdokuLineEditEvent(this);
        for (DocdokuLineEditListener observer : observers) {
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
