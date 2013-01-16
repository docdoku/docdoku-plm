/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.client.ui.doc;

import com.docdoku.client.data.DocsTableModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentMaster;

import javax.swing.*;

public class ViewIterationsPanel extends JScrollPane {

    private DocsTable mDocsTable;

    public ViewIterationsPanel(DocumentMaster pDocM) {
        mDocsTable = new DocsTable(new DocsTableModel(pDocM));

        createLayout();
    }

    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("ViewIterationsPanel_border")));
        getViewport().add(mDocsTable);
    }

    public DocumentIteration getSelectedDoc() {
        return mDocsTable.getSelectedDoc();
    }
}
