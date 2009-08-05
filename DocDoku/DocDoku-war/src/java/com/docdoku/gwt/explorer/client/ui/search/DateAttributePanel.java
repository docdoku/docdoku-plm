package com.docdoku.gwt.explorer.client.ui.search;

import com.docdoku.gwt.explorer.client.data.ShortDateFormater;
import com.docdoku.gwt.explorer.client.ui.widget.DocdokuDateBox;
import com.docdoku.gwt.explorer.common.InstanceAttributeDTO;
import com.docdoku.gwt.explorer.common.InstanceDateAttributeSearchDTO;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.datepicker.client.DateBox;

public class DateAttributePanel extends AbstractAttributePanel {

    private static final int MAX_VISIBLE_LENGTH = 5;
//    private DateBox dateField;
    private DateBox fromDate;
    private DateBox toDate;

    public DateAttributePanel() {
        super(false);
//        dateField = new DateBox();
        fromDate = new DocdokuDateBox(DocdokuDateBox.RoundType.FLOOR);
        fromDate.setFormat(new ShortDateFormater());
        fromDate.getTextBox().setVisibleLength(MAX_VISIBLE_LENGTH);
        toDate = new DocdokuDateBox(DocdokuDateBox.RoundType.CEIL);
        toDate.setFormat(new ShortDateFormater());
        toDate.getTextBox().setVisibleLength(MAX_VISIBLE_LENGTH);
        add(new Label("≥"));
        add(fromDate);
        add(new Label("≤"));
        add(toDate);
    }

    @Override
    public InstanceAttributeDTO getAttribute() {
        if (getNameValue().isEmpty()) {
            return null;
        }
//        InstanceDateAttributeDTO result = new InstanceDateAttributeDTO();
//        result.setDateValue(dateField.getValue());
//        result.setName(getNameValue());
//        return result;

        InstanceDateAttributeSearchDTO result = new InstanceDateAttributeSearchDTO();
        result.setDateFrom(fromDate.getValue());
        result.setDateTo(toDate.getValue());
        result.setName(getNameValue());
        return result;
    }
}
