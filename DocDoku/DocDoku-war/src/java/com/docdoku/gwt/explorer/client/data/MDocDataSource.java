package com.docdoku.gwt.explorer.client.data;

import com.docdoku.gwt.explorer.common.MasterDocumentDTO;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.resources.icons.ExplorerImageBundle;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.common.DocumentDTO;
import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MDocDataSource implements TableDataSource {

    private String[] headers;
    private String[][] data;
    private MasterDocumentDTO[] mdocs;
    private final ExplorerI18NConstants explorerConstants = ServiceLocator.getInstance().getExplorerI18NConstants();
    private final ExplorerImageBundle imageBundle = ServiceLocator.getInstance().getExplorerImageBundle();

    public MDocDataSource(MasterDocumentDTO[] mdocs, String login) {
        super();
        this.mdocs = mdocs;

        this.headers = new String[]{explorerConstants.tableID(), explorerConstants.tableVersion(), explorerConstants.tableIteration(), explorerConstants.tableAuthor(), explorerConstants.tableTitle(), explorerConstants.tableCreationDate()};
        this.data = new String[mdocs.length][6];
        for (int i = 0; i < mdocs.length; i++) {
            if (mdocs[i].getCheckOutUser() != null) {
                if (mdocs[i].getCheckOutUser().equals(login)) {
                    this.data[i][0] = HTMLUtil.imageItemHTML(imageBundle.documentEditRowIcon(), mdocs[i].getId());
                } else {
                    this.data[i][0] = HTMLUtil.imageItemHTML(imageBundle.documentLockRowIcon(), mdocs[i].getId());
                }


            } else {
                this.data[i][0] = HTMLUtil.imageItemHTML(imageBundle.documentRowIcon(), mdocs[i].getId());
            }


            this.data[i][1] = mdocs[i].getVersion();

            DocumentDTO iteration = mdocs[i].getLastIteration();
            int iterationNumber = 0;
            if (iteration != null) {
                iterationNumber = iteration.getIteration();
            }
            this.data[i][2] = iterationNumber + "";
            this.data[i][3] = mdocs[i].getAuthor();
            this.data[i][4] = mdocs[i].getTitle();
            this.data[i][5] = format(mdocs[i].getCreationDate());
        }
    }

    public MasterDocumentDTO getElementAt(int row) {
        return mdocs[row];
    }

    public int getRowCount() {
        return data.length;
    }

    public String[] getRow(int i) {
        return data[i];
    }

    public String[] getHeaderRow() {
        return headers;
    }

    private String format(Date date) {
        return DateTimeFormat.getShortDateFormat().format(date);
    }

    public String getEmptyCaseMessage() {
        return ServiceLocator.getInstance().getExplorerI18NConstants().emptyDocLabel();
    }

    // TODO : we can make something better than this
    // with a Critera class which will handle values and properties...
    // and a getMatchingCritera method in TableDataSource
    public List<Integer> getCheckedInDocuments() {
        List<Integer> result = new LinkedList<Integer>();
        int i = 0;
        for (MasterDocumentDTO masterDocumentDTO : mdocs) {
            if (masterDocumentDTO.getCheckOutUser() != null) {
                result.add(i);
            }
            i++;
        }
        return result;
    }

    public List<Integer> getCheckedOutDocuments() {
        List<Integer> result = new LinkedList<Integer>();
        int i = 0;
        for (MasterDocumentDTO masterDocumentDTO : mdocs) {
            if (masterDocumentDTO.getCheckOutUser() == null) {
                result.add(i);
            }
            i++;
        }
        return result;
    }

    public String[] getTooltipForRowColumn(int row, int column) {


        if (row >= 0 && row < mdocs.length && mdocs[row].getCheckOutUser() != null && column == 0) {
            ExplorerI18NConstants constants = ServiceLocator.getInstance().getExplorerI18NConstants();
            String tooltip = constants.checkedInBy() + " " + mdocs[row].getCheckOutUser() + " " + constants.onLabel() + " " + DateTimeFormat.getShortDateTimeFormat().format(mdocs[row].getCheckOutDate());
            String result[] = new String[1];
            result[0] = tooltip;
            return result;

        }

        return null;

    }
}
