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

package com.docdoku.android.plm.client.documents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * {@code Activity} for displaying either the list of checked out documents or the results of an advanced document search.
 * <br>The type of result to display is specified in the {@code Intent} extra with key {@link #LIST_MODE_EXTRA}.
 * <p>Layout file: {@link /res/layout/activity_element_list.xml activity_element_list}
 *
 * @author: Martin Devillers
 * @version 1.0
 */
public class DocumentSimpleListActivity extends DocumentListActivity implements HttpGetTask.HttpGetListener {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.documents.DocumentSimpleListActivity";

    /**
     * {@code Intent} extra key to find the type of result to display
     */
    public static final String LIST_MODE_EXTRA = "list mode";
    /**
     * {@code Intent} extra key to find the search query to execute, if relevant
     */
    public static final String SEARCH_QUERY_EXTRA = "search query";
    /**
     * Value of {@code Intent} extra with key {@link #LIST_MODE_EXTRA} indicating that the list of checked out documents should be shown
     */
    public static final int CHECKED_OUT_DOCUMENTS_LIST = 1;
    /**
     * Value of {@code Intent} extra with key {@link #LIST_MODE_EXTRA} indicating that the list of search results should be shown
     */
    public static final int SEARCH_RESULTS_LIST = 2;

    private int activityIconId;

    /**
     * Called when the {@code Activity} is created.
     * <br>Reads from the {@code Intent} the which documents should be displayed, and starts an {@link HttpGetTask} to query
     * the server for the results.
     *
     * @param savedInstanceState
     * @see android.app.Activity
     * @see DocumentListActivity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "DocumentSimpleListActivity starting");

        Intent intent = getIntent();
        int listType = intent.getIntExtra(LIST_MODE_EXTRA, 0);
        switch(listType){
            case CHECKED_OUT_DOCUMENTS_LIST:
                activityIconId = R.id.checkedOutDocuments;
                new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/checkedouts/" + getCurrentUserLogin() + "/documents/");
                break;
            case SEARCH_RESULTS_LIST:
                activityIconId = R.id.documentSearch;
                new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/search/" + intent.getStringExtra(SEARCH_QUERY_EXTRA) + "/documents/");
                break;
        }
    }

    /**
     * Called when the query result is obtained.
     * <br>Reads the array and adds the documents to the {@code Adapter}, then notifies it that its data set has changed.
     *
     * @param result a {@code JSONArray} of {@link Document Documents}
     * @see com.docdoku.android.plm.network.HttpGetTask.HttpGetListener
     */
    @Override
    public void onHttpGetResult(String result) {
        removeLoadingView();
        ArrayList<Document> docsArray = new ArrayList<Document>();
        try {
            JSONArray docsJSON = new JSONArray(result);
            for (int i=0; i<docsJSON.length(); i++){
                JSONObject docJSON = docsJSON.getJSONObject(i);
                Document doc = new Document(docJSON.getString("id"));
                doc.setStateChangeNotification(docJSON.getBoolean("stateSubscription"));
                doc.setIterationNotification(docJSON.getBoolean("iterationSubscription"));
                doc.updateFromJSON(docJSON, getResources());
                docsArray.add(doc);
            }
            documentListView.setAdapter(new DocumentAdapter(docsArray));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error handling json of workspace's documents");
            e.printStackTrace();
            Log.i(LOG_TAG, "Error message: " + e.getMessage());
        }
    }

    /**
     *
     * @return
     * @see com.docdoku.android.plm.client.SimpleActionBarActivity#getActivityButtonId()
     */
    @Override
    protected int getActivityButtonId() {
        return activityIconId;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
