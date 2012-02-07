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

package com.docdoku.gwt.explorer.client.ui.doc;

import com.docdoku.gwt.explorer.client.data.DocOracle;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.client.ui.widget.spinbox.SpinBox;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.server.rest.dto.DocumentDTO;
import com.docdoku.server.rest.dto.DocumentMasterDTO;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 *
 * @author manu
 */
public class ChooseLinkPanel extends Composite implements SelectionHandler<SuggestOracle.Suggestion>, KeyUpHandler {

    private final int DEFAULT_SUGGEST_SIZE = 10;
    private SuggestBox docMSuggest;
    private HorizontalPanel mainPanel;
    private DocumentMasterDTO masterDoc;
    private SpinBox iterationsSpin;
    private int suggestSize;
    private String workspaceId;

    public ChooseLinkPanel(DocOracle oracle, final DocumentDTO doc) {
        this(oracle);

        AsyncCallback<DocumentMasterDTO> callback = new AsyncCallback<DocumentMasterDTO>() {

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }

            public void onSuccess(DocumentMasterDTO result) {
                masterDoc = result;
               // iterationsSpin.setMaxValue(masterDoc.getDocumentIterations().size());
                iterationsSpin.setMinValue(1);
                iterationsSpin.setValue(doc.getIteration());
                iterationsSpin.setVisible(true);
                docMSuggest.setText(result.getId() + "-" + result.getVersion());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getDocM(doc.getWorkspaceId(), doc.getDocumentMasterId(), doc.getDocumentMasterVersion(), callback);

    }

    public ChooseLinkPanel(DocOracle oracle) {
        docMSuggest = new SuggestBox(oracle);
        iterationsSpin = new SpinBox();

        // composite main panel :
        mainPanel = new HorizontalPanel();
        mainPanel.add(docMSuggest);
        mainPanel.add(iterationsSpin);
        initWidget(mainPanel);
        iterationsSpin.setVisible(false);

        docMSuggest.addKeyUpHandler(this);
        docMSuggest.addSelectionHandler(this);
        suggestSize = DEFAULT_SUGGEST_SIZE;
        docMSuggest.setLimit(suggestSize);
        workspaceId = oracle.getWorkspaceId();
    }

    public DocumentDTO getSelectedDocument() {
        if (masterDoc != null) {
            return null;//masterDoc.getDocumentIterations().get(iterationsSpin.getValue() - 1);
        } else {
            return null;
        }
    }

    public void onSelection(SelectionEvent<Suggestion> event) {
        iterationsSpin.setVisible(true);
        DocOracle.DocOracleSuggestion selection = (DocOracle.DocOracleSuggestion) event.getSelectedItem();
        masterDoc = selection.getDocM();
        //iterationsSpin.setMaxValue(masterDoc.getDocumentIterations().size());
        iterationsSpin.setMinValue(1);
        //iterationsSpin.setValue(masterDoc.getDocumentIterations().size());
    }

    public void onKeyUp(KeyUpEvent event) {
        if (event.getNativeKeyCode() != KeyCodes.KEY_ENTER) {
            iterationsSpin.setVisible(false);
            masterDoc = null;
        } else {
            if (!docMSuggest.isSuggestionListShowing()) {
                // perform a search
                String splitted[] = docMSuggest.getText().split("-");
                if (splitted.length > 1) {
                    String version = splitted[splitted.length - 1];
                    String fullName = "";
                    for (int i = 0; i < splitted.length - 1; i++) {
                        fullName += splitted[i] + "-";
                    }
                    fullName = fullName.substring(0, fullName.length()-1) ;

                    AsyncCallback<DocumentMasterDTO> callback = new AsyncCallback<DocumentMasterDTO>() {

                        public void onFailure(Throwable caught) {
                            HTMLUtil.showError(caught.getMessage());
                        }

                        public void onSuccess(DocumentMasterDTO result) {
                            if (result != null) {
                                iterationsSpin.setVisible(true);
                                masterDoc = result;
                                //iterationsSpin.setMaxValue(masterDoc.getDocumentIterations().size());
                                iterationsSpin.setMinValue(1);
                                //iterationsSpin.setValue(masterDoc.getDocumentIterations().size());
                            } else {
                                iterationsSpin.setVisible(false);
                                masterDoc = null;
                            }
                        }
                    };

                    ServiceLocator.getInstance().getExplorerService().getDocM(workspaceId, fullName, version, callback);

                }

            }
        }
    }

    public int getSuggestSize() {
        return suggestSize;
    }

    public void setSuggestSize(int suggestSize) {
        this.suggestSize = suggestSize;
        docMSuggest.setLimit(suggestSize);
    }
}
