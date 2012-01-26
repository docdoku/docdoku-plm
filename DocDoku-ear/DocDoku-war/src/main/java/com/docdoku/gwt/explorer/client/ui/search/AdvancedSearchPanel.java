/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.gwt.explorer.client.ui.search;


import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.data.ShortDateFormater;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.RoundedDateBox;
import com.docdoku.gwt.explorer.client.ui.widget.RoundedDateBox.RoundType;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DateBox;
import java.util.Date;

public class AdvancedSearchPanel extends FlexTable implements ValueChangeHandler<Date> {

    private TextBox labelsField;
    private TextBox contentField;
    private DateBox fromDate;
    private DateBox toDate;
    private Date fromBackup;
    private Date toBackup;
    //1 january 2000
    private final static long DEFAULT_FROM_DATE = 946681200000L;
    

    public AdvancedSearchPanel() {
        ExplorerI18NConstants constants = ServiceLocator.getInstance().getExplorerI18NConstants() ;
        labelsField = new TextBox();
        contentField = new TextBox();

        this.setText(0, 0, constants.labelLabel());
        this.setText(1, 0, constants.contentLabel());
        this.setWidget(0, 1, labelsField);
        this.setWidget(1, 1, contentField);

        fromDate = new RoundedDateBox(RoundType.FLOOR);
        fromDate.setFormat(new ShortDateFormater());
        setText(2, 0, constants.creationDateLabel() + " " + constants.fromLabel());
        setText(3, 0, constants.toLabel());
        setWidget(2, 1, fromDate);
        toDate = new RoundedDateBox(RoundType.CEIL) ;
        toDate.setFormat(new ShortDateFormater()) ;
        toDate.setValue(new Date());
        setWidget(3, 1, toDate);

        fromDate.setValue(new Date(DEFAULT_FROM_DATE));

        fromBackup = fromDate.getValue();
        toBackup = toDate.getValue();

        fromDate.addValueChangeHandler(this);
        toDate.addValueChangeHandler(this);


    }

    public String[] getTags() {
        if (!labelsField.getText().trim().isEmpty()) {
            String rawString = labelsField.getText().trim();
            String splitPattern ="(\"( )*\")|,"; //
            String result[] = rawString.split(splitPattern);
            if (result.length != 0){
                result[0] = result[0].replace('\"', ' ');
                result[result.length - 1] = result[result.length - 1].replace('\"', ' ');
                for (int i = 0 ; i < result.length ; i++){
                    result[i] = result[i].trim();
                }
                return result;
            }else{
                return null;
            }
        } else {
            return null;
        }
    }

    public String getContent() {
        return contentField.getText();
    }

    Date getFromDate() {
        return fromDate.getValue();
    }

    Date getToDate() {
        return toDate.getValue() ;

    }

    public void onValueChange(ValueChangeEvent<Date> event) {
        if (event.getSource() == fromDate) {
            if (fromDate.getValue().after(toDate.getValue())) {
                fromDate.setValue(fromBackup);
            } else {
                fromBackup = fromDate.getValue();
            }
        } else {
            if (toDate.getValue().before(fromDate.getValue())) {
                toDate.setValue(toBackup);
            } else {
                toBackup = toDate.getValue();
            }
        }
    }
}
