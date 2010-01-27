package com.docdoku.client.ui.doc;

import com.docdoku.client.data.DocsTableModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.Document;
import com.docdoku.core.entities.MasterDocument;

import javax.swing.*;
import java.awt.*;

public class ViewIterationsPanel extends JScrollPane {

    private DocsTable mDocsTable;

    public ViewIterationsPanel(MasterDocument pMDoc) {
        mDocsTable = new DocsTable(new DocsTableModel(pMDoc));

        createLayout();
    }

    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("ViewIterationsPanel_border")));
        getViewport().add(mDocsTable);
    }

    public Document getSelectedDoc() {
        return mDocsTable.getSelectedDoc();
    }
}
