package com.docdoku.gwt.explorer.client.data;


import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.resources.icons.ExplorerImageBundle;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.common.WorkflowModelDTO;
import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.Date;

@Deprecated
public class WorkflowModelDataSource implements TableDataSource {
    
    private String[] headers; 
    private String[][] data;
    private WorkflowModelDTO[] workflows;
    
    public WorkflowModelDataSource(WorkflowModelDTO[] workflows,  String login) {
        super();
        this.workflows=workflows;
        
        ExplorerI18NConstants explorerConstants=ServiceLocator.getInstance().getExplorerI18NConstants();
        ExplorerImageBundle imageBundle = ServiceLocator.getInstance().getExplorerImageBundle();
        this.headers = new String[]{ explorerConstants.tableID(), explorerConstants.tableAuthor(), explorerConstants.tableCreationDate()};
        this.data = new String[workflows.length][3];
        for( int i=0; i < workflows.length ; i++ ){
            this.data[i][0] = HTMLUtil.imageItemHTML(imageBundle.workflowRowIcon(),workflows[i].getId());
            this.data[i][1] = workflows[i].getAuthor().toString();
            this.data[i][2] = format(workflows[i].getCreationDate());
        }
    }

    public WorkflowModelDTO getElementAt(int row){
        return workflows[row];
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

    private String format(Date date){
        return DateTimeFormat.getShortDateFormat().format(date);
    }

    public String getEmptyCaseMessage() {
        return ServiceLocator.getInstance().getExplorerI18NConstants().emptyWorkflowModelLabel();
    }

    public String[] getTooltipForRowColumn(int row, int column) {
        return null ;
    }
}
