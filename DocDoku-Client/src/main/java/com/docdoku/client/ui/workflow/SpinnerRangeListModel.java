/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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

package com.docdoku.client.ui.workflow;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


public class SpinnerRangeListModel extends SpinnerNumberModel implements ListDataListener {

    private ListModel mListModel;

    public SpinnerRangeListModel(ListModel pListModel, int pValue) {
        super(pValue, 1, Math.max(1, pListModel.getSize()), 1);
        mListModel = pListModel;
        mListModel.addListDataListener(this);
    }

    public SpinnerRangeListModel(ListModel pListModel) {
        this(pListModel, Math.max(1, pListModel.getSize()));
    }

    public void contentsChanged(ListDataEvent e) {
        refresh();
    }

    public void intervalAdded(ListDataEvent e) {
        refresh();
    }

    public void intervalRemoved(ListDataEvent e) {
        refresh();
    }

    private void refresh() {
        setMaximum(new Integer(Math.max(1,mListModel.getSize())));
        setValue(new Integer(Math.max(1,Math.min(mListModel.getSize(), getNumber().intValue()))));
    }

}
