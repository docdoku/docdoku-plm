/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.docdoku.gwt.explorer.client.data;

import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.common.MDocResponse;
import com.docdoku.gwt.explorer.common.MasterDocumentDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author manu
 */
public class DocOracle extends SuggestOracle {

    //1 january 2000
    private final static long DEFAULT_FROM_DATE = 946681200000L;
    private final static int DEFAULT_TRIGGER_SIZE = 1;
    private final static int REQUEST_SIZE = 10;
    private String workspaceId;
    private int triggerSize;

    public DocOracle() {
    }

    @Override
    public void requestSuggestions(final Request request, final Callback callback) {
        // retrive info

        AsyncCallback<MDocResponse> callbackServiceNew = new AsyncCallback<MDocResponse>() {

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }

            public void onSuccess(MDocResponse resultS) {
                MasterDocumentDTO[] result = resultS.getData();
                // generate response :
                SortedSet<MasterDocumentDTO> sorted = new TreeSet<MasterDocumentDTO>();
                for (MasterDocumentDTO mdoc : result) {
                    sorted.add(mdoc);
                }

                List<DocOracleSuggestion> responseList = new LinkedList<DocOracleSuggestion>();
                for (MasterDocumentDTO mdoc : sorted) {
                    DocOracleSuggestion suggestion = new DocOracleSuggestionImpl(mdoc);
                    responseList.add(suggestion);
                }
                callback.onSuggestionsReady(request, new Response(responseList));
            }
        };

        if (request.getQuery().length() >= triggerSize) {
            // create the real query.
            // if the query ends with a '-', remove it
            String realQuery = request.getQuery();
            if (realQuery.endsWith("-")) {
                realQuery = realQuery.substring(0, realQuery.length() - 1);
            }

            ServiceLocator.getInstance().getExplorerService().searchMDocs(workspaceId, realQuery, "", "", "", "", new Date(DEFAULT_FROM_DATE), new Date(), null, null, "", 0, REQUEST_SIZE, callbackServiceNew);
        } else {
            List<DocOracleSuggestion> responses = new LinkedList<DocOracleSuggestion>();
            callback.onSuggestionsReady(request, new Response(responses));
        }
    }

    public int getTriggerSize() {
        return triggerSize;
    }

    public void setTriggerSize(int triggerSize) {
        this.triggerSize = triggerSize;
    };

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public interface DocOracleSuggestion extends Suggestion {

        MasterDocumentDTO getMDoc();
    }

    private class DocOracleSuggestionImpl implements DocOracleSuggestion {

        private MasterDocumentDTO doc;

        public DocOracleSuggestionImpl(MasterDocumentDTO doc) {
            this.doc = doc;
        }

        public MasterDocumentDTO getMDoc() {
            return doc;
        }

        public String getDisplayString() {
            return doc.getId() + "-" + doc.getVersion();
        }

        public String getReplacementString() {
            return doc.getId() + "-" + doc.getVersion();

        }
    }
}
