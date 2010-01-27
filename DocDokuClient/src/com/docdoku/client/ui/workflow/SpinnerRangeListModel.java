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
