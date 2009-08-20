package com.docdoku.gwt.explorer.client.data;


import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.resources.icons.ExplorerImageBundle;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.common.MasterDocumentTemplateDTO;
import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.Date;

@Deprecated
public class MDocTemplateDataSource implements TableDataSource {
    
    private String[] headers; 
    private String[][] data;
    private MasterDocumentTemplateDTO[] templates;
    
    public MDocTemplateDataSource(MasterDocumentTemplateDTO[] templates,  String login) {
        super();
        this.templates=templates;
        
        ExplorerI18NConstants explorerConstants=ServiceLocator.getInstance().getExplorerI18NConstants();
        ExplorerImageBundle imageBundle = ServiceLocator.getInstance().getExplorerImageBundle();
        this.headers = new String[]{ explorerConstants.tableID(),explorerConstants.tableDocumentType(), explorerConstants.tableAuthor(), explorerConstants.tableCreationDate()};
        this.data = new String[templates.length][4];
        for( int i=0; i < templates.length ; i++ ){
            this.data[i][0] = HTMLUtil.imageItemHTML(imageBundle.templateRowIcon(),templates[i].getId());
            this.data[i][1] = templates[i].getDocumentType();
            this.data[i][2] = templates[i].getAuthor();
            this.data[i][3] = format(templates[i].getCreationDate());
        }
    }

    public MasterDocumentTemplateDTO getElementAt(int row){
        return templates[row];
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
        return ServiceLocator.getInstance().getExplorerI18NConstants().emptyDocTemplateLabel();
    }

    public String[] getTooltipForRowColumn(int row, int column) {
        return null;
    }
}
