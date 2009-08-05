/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.docdoku.gwt.explorer.common;

import java.util.Date;

/**
 *
 * @author manu
 */
public class InstanceDateAttributeSearchDTO extends InstanceAttributeDTO {

    private Date dateFrom;
    private Date dateTo;

    @Override
    public Date[] getValue() {
        Date res[] = new Date[2];
        res[0] = dateFrom;
        res[1] = dateTo;
        return res;
    }

    /**
     * This method shouldn't be called
     * @param pValue
     * @return
     */
    @Override
    public boolean setValue(Object pValue) {

        return false;

    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }
}
