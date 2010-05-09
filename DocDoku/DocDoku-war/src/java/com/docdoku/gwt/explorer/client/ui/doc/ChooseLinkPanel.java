/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.docdoku.gwt.explorer.client.ui.doc;

import com.docdoku.gwt.explorer.client.data.DocOracle;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.client.ui.widget.spinbox.SpinBox;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.shared.DocumentDTO;
import com.docdoku.gwt.explorer.shared.MasterDocumentDTO;
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
    private SuggestBox mdocSuggest;
    private HorizontalPanel mainPanel;
    private MasterDocumentDTO masterDoc;
    private SpinBox iterationsSpin;
    private int suggestSize;
    private String workspaceId;

    public ChooseLinkPanel(DocOracle oracle, final DocumentDTO doc) {
        this(oracle);

        AsyncCallback<MasterDocumentDTO> callback = new AsyncCallback<MasterDocumentDTO>() {

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }

            public void onSuccess(MasterDocumentDTO result) {
                masterDoc = result;
                iterationsSpin.setMaxValue(masterDoc.getIterations().size());
                iterationsSpin.setMinValue(1);
                iterationsSpin.setValue(doc.getIteration());
                iterationsSpin.setVisible(true);
                mdocSuggest.setText(result.getId() + "-" + result.getVersion());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getMDoc(doc.getWorkspaceId(), doc.getMasterDocumentId(), doc.getMasterDocumentVersion(), callback);

    }

    public ChooseLinkPanel(DocOracle oracle) {
        mdocSuggest = new SuggestBox(oracle);
        iterationsSpin = new SpinBox();

        // composite main panel :
        mainPanel = new HorizontalPanel();
        mainPanel.add(mdocSuggest);
        mainPanel.add(iterationsSpin);
        initWidget(mainPanel);
        iterationsSpin.setVisible(false);

        mdocSuggest.addKeyUpHandler(this);
        mdocSuggest.addSelectionHandler(this);
        suggestSize = DEFAULT_SUGGEST_SIZE;
        mdocSuggest.setLimit(suggestSize);
        workspaceId = oracle.getWorkspaceId();
    }

    public DocumentDTO getSelectedDocument() {
        if (masterDoc != null) {
            return masterDoc.getIterations().get(iterationsSpin.getValue() - 1);
        } else {
            return null;
        }
    }

    public void onSelection(SelectionEvent<Suggestion> event) {
        iterationsSpin.setVisible(true);
        DocOracle.DocOracleSuggestion selection = (DocOracle.DocOracleSuggestion) event.getSelectedItem();
        masterDoc = selection.getMDoc();
        iterationsSpin.setMaxValue(masterDoc.getIterations().size());
        iterationsSpin.setMinValue(1);
        iterationsSpin.setValue(masterDoc.getIterations().size());
    }

    public void onKeyUp(KeyUpEvent event) {
        if (event.getNativeKeyCode() != KeyCodes.KEY_ENTER) {
            iterationsSpin.setVisible(false);
            masterDoc = null;
        } else {
            if (!mdocSuggest.isSuggestionListShowing()) {
                // perform a search
                String splitted[] = mdocSuggest.getText().split("-");
                if (splitted.length > 1) {
                    String version = splitted[splitted.length - 1];
                    String fullName = "";
                    for (int i = 0; i < splitted.length - 1; i++) {
                        fullName += splitted[i] + "-";
                    }
                    fullName = fullName.substring(0, fullName.length()-1) ;

                    AsyncCallback<MasterDocumentDTO> callback = new AsyncCallback<MasterDocumentDTO>() {

                        public void onFailure(Throwable caught) {
                            HTMLUtil.showError(caught.getMessage());
                        }

                        public void onSuccess(MasterDocumentDTO result) {
                            if (result != null) {
                                iterationsSpin.setVisible(true);
                                masterDoc = result;
                                iterationsSpin.setMaxValue(masterDoc.getIterations().size());
                                iterationsSpin.setMinValue(1);
                                iterationsSpin.setValue(masterDoc.getIterations().size());
                            } else {
                                iterationsSpin.setVisible(false);
                                masterDoc = null;
                            }
                        }
                    };

                    ServiceLocator.getInstance().getExplorerService().getMDoc(workspaceId, fullName, version, callback);

                }

            }
        }
    }

    public int getSuggestSize() {
        return suggestSize;
    }

    public void setSuggestSize(int suggestSize) {
        this.suggestSize = suggestSize;
        mdocSuggest.setLimit(suggestSize);
    }
}
