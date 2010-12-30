package com.docdoku.gwt.explorer.client.ui.search;

import com.docdoku.gwt.explorer.shared.SearchQueryDTO;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public abstract class AbstractAttributePanel extends HorizontalPanel {

    private static final int VISIBLE_LENGTH = 10;
    private TextBox nameField;
    private Label afterName;

    public AbstractAttributePanel() {
        this(true);
    }

    public AbstractAttributePanel(boolean showEqual) {
        nameField = new TextBox();
        nameField.setVisibleLength(VISIBLE_LENGTH);
        add(nameField);
        if (showEqual) {
            afterName = new Label("=");
            add(afterName);
        }
    }

    public String getNameValue() {
        return nameField.getText();
    }

    public void setNameValue(String name) {
        nameField.setText(name);
    }

    abstract public SearchQueryDTO.AbstractAttributeQueryDTO getAttribute();
}
