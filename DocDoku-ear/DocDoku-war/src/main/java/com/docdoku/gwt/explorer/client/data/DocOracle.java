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

import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.shared.DocMResponse;
import com.docdoku.server.rest.dto.DocumentMasterDTO;
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

        AsyncCallback<DocMResponse> callbackServiceNew = new AsyncCallback<DocMResponse>() {

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }

            public void onSuccess(DocMResponse resultS) {
                DocumentMasterDTO[] result = resultS.getData();
                // generate response :
                SortedSet<DocumentMasterDTO> sorted = new TreeSet<DocumentMasterDTO>();
                for (DocumentMasterDTO docM : result) {
                    sorted.add(docM);
                }

                List<DocOracleSuggestion> responseList = new LinkedList<DocOracleSuggestion>();
                for (DocumentMasterDTO m : sorted) {
                    DocOracleSuggestion suggestion = new DocOracleSuggestionImpl(m);
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

            ServiceLocator.getInstance().getExplorerService().searchDocMs(workspaceId, realQuery, "", "", "", "", new Date(DEFAULT_FROM_DATE), new Date(), null, null, "", 0, REQUEST_SIZE, callbackServiceNew);
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

        DocumentMasterDTO getDocM();
    }

    private class DocOracleSuggestionImpl implements DocOracleSuggestion {

        private DocumentMasterDTO doc;

        public DocOracleSuggestionImpl(DocumentMasterDTO doc) {
            this.doc = doc;
        }

        public DocumentMasterDTO getDocM() {
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
