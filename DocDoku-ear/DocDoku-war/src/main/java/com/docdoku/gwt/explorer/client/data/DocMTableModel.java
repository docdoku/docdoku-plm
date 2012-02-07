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

package com.docdoku.gwt.explorer.client.data;

import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.client.ui.widget.table.TableModel;
import com.docdoku.gwt.client.ui.widget.table.TableModelIndex;
import com.docdoku.server.rest.dto.DocumentDTO;
import com.docdoku.server.rest.dto.DocumentMasterDTO;
import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class DocMTableModel implements TableModel {

    private String headers[];
    private Object data[][];
    private DocumentMasterDTO docMs[];
    private CheckOutStatus status[];

    public enum CheckOutStatus {

        CHECKED_OUT_BY_USER,
        CHECKED_OUT_BY_OTHER,
        CHECKED_IN
    }

    public DocMTableModel(DocumentMasterDTO[] docMs, String login, boolean createVersionEnabled) {
        this.docMs = docMs;
        ExplorerI18NConstants explorerConstants = ServiceLocator.getInstance().getExplorerI18NConstants();

        headers = new String[]{null, null, explorerConstants.tableID(), explorerConstants.tableVersion(), explorerConstants.tableIteration(), explorerConstants.tableAuthor(), explorerConstants.tableTitle(), explorerConstants.tableCreationDate(), null};

        // data
        status = new CheckOutStatus[docMs.length];
        data = new Object[docMs.length][9];
        for (int i = 0; i < docMs.length; i++) {
            data[i][0] = docMs[i].isIterationSubscription();
            data[i][1] = docMs[i].isStateSubscription();
            this.data[i][2] = docMs[i].getId();
            /*
            if (docMs[i].getCheckOutUser() != null) {
                if (docMs[i].getCheckOutUser().getLogin().equals(login)) {
//                    this.data[i][2] = HTMLUtil.imageItemHTML(imageBundle.documentEditRowIcon(), docMs[i].getId());
                    status[i] = CheckOutStatus.CHECKED_OUT_BY_USER;
                } else {
//                    this.data[i][2] = HTMLUtil.imageItemHTML(imageBundle.documentLockRowIcon(), docMs[i].getId());
                    status[i] = CheckOutStatus.CHECKED_OUT_BY_OTHER;
                }
            } else {
//                this.data[i][2] = HTMLUtil.imageItemHTML(imageBundle.documentRowIcon(), docMs[i].getId());
                status[i] = CheckOutStatus.CHECKED_IN;
            }
             
            this.data[i][3] = docMs[i].getVersion();

            DocumentDTO iteration = docMs[i].getLastIteration();
            int iterationNumber = 0;
            if (iteration != null) {
                iterationNumber = iteration.getIteration();
            }
            this.data[i][4] = new String(iterationNumber + "");
            this.data[i][5] = docMs[i].getAuthor().getName();
            this.data[i][6] = docMs[i].getTitle();
            this.data[i][7] = docMs[i].getCreationDate();
            this.data[i][8] = createVersionEnabled;*/
        }

    }

    public int getRowCount() {
        return 0;//docMs.length;
    }

    public int getColumnCount() {
        return 9;
    }

    public String[] getHeaderRow() {
        return headers;
    }

    public String getEmptyCaseMessage() {
        return ServiceLocator.getInstance().getExplorerI18NConstants().emptyDocLabel();
    }

    public String[] getTooltip(int row, int column) {
        if (row >= 0 && row < docMs.length /*&& docMs[row].getCheckOutUser() != null && column == 2*/) {
            ExplorerI18NConstants constants = ServiceLocator.getInstance().getExplorerI18NConstants();
            String tooltip = null;//constants.checkedInBy() + " " + docMs[row].getCheckOutUser().getName() + " " + constants.onLabel() + " " + DateTimeFormat.getShortDateTimeFormat().format(docMs[row].getCheckOutDate());
            String result[] = new String[1];
            result[0] = tooltip;
            return result;

        }
        return null;
    }

    public Object getValueAt(TableModelIndex index) {
        if (index.getRow() >= 0 && index.getRow() < data.length && index.getColumn() >= 0 && index.getColumn() < headers.length) {
            return data[index.getRow()][index.getColumn()];
        } else {
            return null;
        }
    }

    public DocumentMasterDTO getValueAt(int row) {
        if (row >= 0 && row < docMs.length) {
            return docMs[row];
        } else {
            return null;
        }
    }

    public CheckOutStatus getStatusAt(int row) {
        if (row >= 0 && row < status.length) {
            return status[row];
        } else {
            return null;
        }
    }

    // TODO : we can make something better than this
    // with a Critera class which will handle values and properties...
    // and a getMatchingCritera method in TableDataSource
    public List<Integer> getCheckedInDocuments() {
        List<Integer> result = new LinkedList<Integer>();
        int i = 0;
        /*
        for (DocumentMasterDTO documentMasterDTO : docMs) {
            if (documentMasterDTO.getCheckOutUser() != null) {
                result.add(i);
            }
            i++;
        }*/
        return result;
    }

    public List<Integer> getCheckedOutDocuments() {
        List<Integer> result = new LinkedList<Integer>();
        int i = 0;
        /*
        for (DocumentMasterDTO documentMasterDTO : docMs) {
            if (documentMasterDTO.getCheckOutUser() == null) {
                result.add(i);
            }
            i++;
        }*/
        return result;
    }
}
